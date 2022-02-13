package com.masterplus.notex.views

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.masterplus.notex.MainNavDirections
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayTextNoteBinding
import com.masterplus.notex.designpatterns.state.TextNoteEditorContext
import com.masterplus.notex.designpatterns.state.TextNoteNullState
import com.masterplus.notex.designpatterns.strategy.RootNoteArchiveItem
import com.masterplus.notex.designpatterns.strategy.RootNoteDefaultItem
import com.masterplus.notex.designpatterns.strategy.RootNoteTrashItem
import com.masterplus.notex.enums.*
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.utils.*
import com.masterplus.notex.viewmodels.TextNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterplus.notex.MainActivity
import com.masterplus.notex.roomdb.entities.*
import com.masterplus.notex.viewmodels.items.SetSelectColorItemViewModel
import com.masterplus.notex.views.view.CustomFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DisplayTextNoteFragment @Inject constructor(private val imm:InputMethodManager,
                                                  private val showMessage: ShowMessage,
                                                  private val customAlerts: CustomAlerts,
                                                  private val customGetDialogs: CustomGetDialogs,
                                                  private val sharedPreferences: SharedPreferences
) : CustomFragment() {

    private var _binding: FragmentDisplayTextNoteBinding?=null
    private val binding get() = _binding!!

    private lateinit var noteParameter:ParameterNote
    private val viewModel:TextNoteViewModel by viewModels()
    private val viewModelSelectColor: SetSelectColorItemViewModel by viewModels()
    private lateinit var navController:NavController
    private var lastSavedNote:UnitedNote = UnitedNote(Note(typeContent = NoteType.Text), arrayListOf(
        ContentNote()
    ))
    private var note:UnitedNote = lastSavedNote
    private var lastEditState:Boolean? = null

    private lateinit var callback:ActionMode.Callback
    private var actionMode:ActionMode? = null

    private lateinit var textNoteContext:TextNoteEditorContext

    private var mInterstitialAd: InterstitialAd? = null
    private var isAdCountAdded:Boolean=false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        noteParameter.sharedNote=null
        viewModel.stateParameterNote=noteParameter.deepCopy()
        outState.putBoolean("isActionMode",actionMode!=null)
        outState.putBoolean("isAdCountAdded",isAdCountAdded)
        outState.putLong("noteId",note.note.uid)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentDisplayTextNoteBinding.inflate(layoutInflater,container,false)
        setUpActionCallback()
        binding.appbarTextNote.setExpanded(false)
        textNoteContext = TextNoteEditorContext(binding)
        navController=Navigation.findNavController(requireActivity(),R.id.frames)

        DisplayTextNoteFragmentArgs.fromBundle(requireArguments()).let { args->
            var noteId:Long?=null
            if(savedInstanceState!=null){
                isAdCountAdded=savedInstanceState.getBoolean("isAdCountAdded",isAdCountAdded)
                noteId=savedInstanceState.getLong("noteId")
            }

            noteParameter=viewModel.stateParameterNote?:args.noteParameter
            adjustBackgroundColor(noteParameter.color)
            noteParameter.sharedNote?.let { shared->
                if(noteParameter.isEmptyNote){
                    binding.editTitleTextNote.setText(shared.title?:"")
                    binding.editContentTextNote.setText(shared.contentNotes[0].text)
                    note.note.title=shared.title?:""
                    note.contents[0]=shared.contentNotes[0]
                }
            }

            if(noteParameter.isEmptyNote){
                textNoteContext.setCurrentState(TextNoteNullState(textNoteContext))
                binding.showReminderFromTextNote.root.isGone=true
                setEditState(true)
            }else{
                textNoteContext.setCurrentState(noteParameter.noteKinds)
                viewModel.signalToLoadCurrentNote(noteId?:noteParameter.parentId!!)
            }
            note.note.kindNote=noteParameter.noteKinds
            requireActivity().invalidateOptionsMenu()
        }

        textNoteContext.setNoteKindsListener {
            note.note.kindNote=it
            noteParameter.noteKinds=it
        }

        setObservers(savedInstanceState)
        setBehaviourSelectItemBook()
        setFontSize()
        adjustNavigation()

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTitleTextNote.let { editTitle->
            editTitle.setKeyBackListener(editTextBackKeyListener)
            editTitle.doOnTextChanged { text, start, before, count ->
                note.note.title=text.toString().trim()
            }
        }
        binding.editContentTextNote.let { editContent->
            editContent.setKeyBackListener(editTextBackKeyListener)
            editContent.doOnTextChanged { text, start, before, count ->
                note.contents[0].text = text.toString().trimEnd()
            }
        }
        binding.showTagsTextNote.setOnClickListener {
            showSelectTagsDia()
        }
    }

    private fun setObservers(savedInstanceState:Bundle?){
        viewModel.note.observe(viewLifecycleOwner, Observer {
            note=it
            setEditTexts(it)
            initLoading(it)
            lastSavedNote=note.deepCopy()
            adjustBackgroundColor(note.note.color)

            if(savedInstanceState!=null){
                if(savedInstanceState.getBoolean("isActionMode")){
                    setEditState(true)
                    savedInstanceState.putBoolean("isActionMode",false)
                }
            }
            checkAndLoadInterstitialAdd()

            if(noteParameter.isEmptyNote){
                setTransNullToNote(it.note.uid)
                noteParameter.isEmptyNote=false
            }

        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isRecoverNote.collect {
                        textNoteContext.recoverNote()
                        viewModel.signalToLoadCurrentNote(note.note.uid)
                        requireActivity().invalidateOptionsMenu()
                    }
                }

                launch{
                    viewModel.isNoteSaved.collect {
                        showMessage.showLong(getString(R.string.saved_text))
                        setShowDate(note.note.updateDate)
                    }
                }
                launch{
                    viewModelSelectColor.liveItem.collect {color->
                        adjustBackgroundColor(color)
                        note.note.color=color
                        viewModel.changeColor(color,note.note.uid)
                    }
                }
            }
        }
    }

    private fun setTransNullToNote(noteId:Long){
        textNoteContext.transNullToNote(note.note.kindNote)
        requireActivity().invalidateOptionsMenu()
        when(noteParameter.noteFlags){
            NoteFlags.DEFAULT_NOTE->{
                noteParameter.parentId=noteId
            }
            NoteFlags.BOOK_FROM_NOTE->{
                viewModel.setBookId(noteId,noteParameter.parentId?:0)
            }
            NoteFlags.TAG_FROM_NOTE->{
                viewModel.setTagWithIds(noteParameter.parentId?:0,noteId)
            }
        }
    }

    private fun setEditState(isEdit:Boolean){
            if(lastEditState==null || lastEditState!=isEdit){
                lastEditState=isEdit
                if(isEdit){
                    removeSearchedAffect()
                    actionMode=requireActivity().startActionMode(callback)
                }else{
                    closeKeyboard()
                    actionMode?.finish()
                    binding.editTitleTextNote.clearFocus()
                    binding.editContentTextNote.clearFocus()
                    binding.editTitleTextNote.setText(viewModel.checkAndSetDefaultTitleToNote(note))
                    viewModel.saveNote(note,lastSavedNote)
                }
                binding.editTitleTextNote.isCursorVisible=isEdit
                binding.editContentTextNote.isCursorVisible=isEdit
                setIsAdCountAdded()
            }
    }
    private fun initLoading(note:UnitedNote){
        viewModel.getLiveReminder(note.note.uid).observe(viewLifecycleOwner, Observer { reminder->
            setReminderViews(reminder)
        })
        viewModel.getLiveTags(note.note.uid).observe(viewLifecycleOwner, Observer {
            setTagsAndVisibility(it.tags)
        })
        viewModel.getLiveBook(note.note.uid).observe(viewLifecycleOwner, Observer {
            setBookText(it)
            note.note.bookId=it?.bookId?:0L
        })
        setShowDate(note.note.updateDate)
    }
    private fun showSelectTagsDia(){
        val noteIds=if(note.note.uid!=0L) listOf(note.note.uid)else listOf()
        customGetDialogs.getSelectTagsDia(noteIds)
            .show(childFragmentManager,"")
    }

    private val editTextBackKeyListener= object :(Boolean)->Unit{
        override fun invoke(isActive:Boolean): Unit {
            if(noteParameter.noteKinds==NoteKinds.TRASH_KIND){
                showMessage.showShort(getString(R.string.can_not_be_changed_text))
            }else
                setEditState(isActive)
        }

    }
    private fun setEditTexts(note: UnitedNote){
        val searchText=noteParameter.searchText?:""
        binding.editTitleTextNote.setText(if(searchText!="")
            getHighLightedText(note.note.title,searchText) else note.note.title)
        binding.editContentTextNote.setText(if(searchText!="")
            getHighLightedText(note.contents[0].text,searchText) else note.contents[0].text)
    }
    private fun setIsAdCountAdded(){
        if(!isAdCountAdded){
            isAdCountAdded=true
            MainActivity.SAVED_NOTE_COUNT++
            checkAndLoadInterstitialAdd()
        }
    }
    private fun setFontSize(fontSize:Float?=null){
        val size:Float=fontSize?:Utils.getFontSize(sharedPreferences).toFloat()
        binding.editContentTextNote.textSize=size
    }

    private fun checkAndLoadInterstitialAdd(){
        if(!MainActivity.isPremiumActive&&mInterstitialAd==null&&
            MainActivity.SAVED_NOTE_COUNT>=MainActivity.MAX_SAVED_NOTE_COUNT){
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(requireContext(),getString(R.string.interstitialAdd_textNote_id),adRequest,object :InterstitialAdLoadCallback(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    mInterstitialAd=null
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    mInterstitialAd=p0
                    MainActivity.SAVED_NOTE_COUNT-=MainActivity.SAVED_NOTE_COUNT
                }
            })
        }
    }

    private fun removeSearchedAffect(){
        if(noteParameter.searchText!=""){
            binding.editTitleTextNote.setText(note.note.title)
            binding.editContentTextNote.setText(note.contents[0].text)
            noteParameter.searchText=""
        }
    }
    private fun setBehaviourSelectItemBook(){
        binding.selectBookFromTextNote.root.setOnClickListener {
            customGetDialogs.getSelectBookDia(note)
                .show(childFragmentManager,"")
        }
    }
    private fun adjustBackgroundColor(color:Int){
        val colorInt=Utils.getColorUIMode(requireContext(),color)
        val colorWithAddition = Utils.getColorIntWithAddition(colorInt,requireContext())

        binding.nestedScrollDisplayTextNote.setBackgroundColor(colorInt)
        binding.appbarTextNote.setBackgroundColor(colorInt)
        binding.selectBookFromTextNote.root.setCardBackgroundColor(colorWithAddition)
        binding.showReminderFromTextNote.root.setCardBackgroundColor(colorWithAddition)
        Utils.changeToolBarColor(requireActivity(),colorInt)
    }
    private fun adjustBackgroundColor(color:String){
        val colorInt:Int= Color.parseColor(color)
        adjustBackgroundColor(colorInt)
    }

    private fun setBookText(book: Book?){
        binding.selectBookFromTextNote.textItemSelectBook.text = book?.name ?: getString(R.string.unselected_text)
    }

    private fun setReminderViews(reminder: Reminder?){
        binding.showReminderFromTextNote.let { reminderView->
            if(reminder!=null){
                val backgroundId=if(reminder.reminderType== ReminderTypes.NOT_REPEATED)R.drawable.ic_baseline_access_time_24
                else R.drawable.ic_baseline_repeat_24
                reminderView.viewReminderText.setCompoundDrawablesWithIntrinsicBounds(backgroundId,0,0,0)
                reminderView.viewReminderText.text = Utils.getReminderSpanDateText(reminder)
            }

            reminderView.root.isVisible = reminder!=null
            reminderView.root.setOnClickListener {
                customGetDialogs.getSelectReminderDia(note.note.uid)
                    .show(childFragmentManager,"")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        textNoteContext.setOnCreateMenu(menu, inflater)

    }



    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        textNoteContext.setOnPrepareMenu(menu)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.display_note_color_menu_item->{
                customGetDialogs.getSelectColorDia(note.note.color)
                    .show(childFragmentManager,"")
            }
            R.id.display_note_edit_menu_item->{
                setEditState(true)
            }
            R.id.display_note_remove_menu_item->{
                customAlerts.showDeleteNoteToTrashAlert(requireContext()) { p0, p1 ->
                    viewModel.sendNoteToTrashWithNoteId(note.note.uid)
                    if(noteParameter.noteKinds==NoteKinds.ARCHIVE_KIND) navigateToArchive() else navigateToAllNote()
                }
            }
            R.id.display_note_share_menu_item->{
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, note.note.title)
                    putExtra(Intent.EXTRA_TEXT, note.contents[0].text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            R.id.display_note_tag_menu_item->{
                showSelectTagsDia()
            }
            R.id.display_note_set_reminder_menu_item->{
                customGetDialogs.getSelectReminderDia(note.note.uid)
                    .show(childFragmentManager,"")
            }
            R.id.achieve_add_menu_item->{
                note.note.kindNote=NoteKinds.ARCHIVE_KIND
                viewModel.changeNoteKindsWithNoteId(note.note.uid,NoteKinds.ARCHIVE_KIND)
                navigateToAllNote()
            }
            R.id.achieve_remove_menu_item->{
                note.note.kindNote=NoteKinds.ALL_KIND
                viewModel.changeNoteKindsWithNoteId(note.note.uid,NoteKinds.ALL_KIND)
                navigateToArchive()
            }
            R.id.remove_notes_forever_act_trash_menu_item->{
                customAlerts.showDeleteNoteForEverAlert(requireContext()) { p0, p1 ->
                    viewModel.deleteNoteForEver(note.note.uid)
                    navigateToTrash()
                }
            }
            R.id.restore_notes_act_trash_menu_item->{
                customAlerts.showRestoreNoteAlert(requireContext()){p0,p1->
                    viewModel.recoverNote(note.note.uid)
                }
            }
            R.id.select_font_note_menu_item->{
                customAlerts.showSelectFontSizeAlert(requireContext(),sharedPreferences) { pos: Int, textSize: Int ->
                    setFontSize(textSize.toFloat())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setUpActionCallback(){
        callback=object:ActionMode.Callback{
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                p0?.menuInflater?.inflate(R.menu.action_display_text_note_menu,p1)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                when(p1?.itemId){
                    R.id.text_note_save_act_menu_item->{
                        p0?.finish()
                    }
                }
                return true
            }
            override fun onDestroyActionMode(p0: ActionMode?) {
                actionMode=null
                setEditState(false)
            }
        }
    }

    private fun setTagsAndVisibility(tags:List<Tag>){
        binding.showTagsTextNote.isGone = tags.isEmpty()
        binding.showTagsTextNote.showTags(tags)
    }
    private fun setShowDate(dateStr:String){
        binding.showDateFromTextNote.text=Utils.getInsideNoteDateFormat(dateStr)
    }

    private fun navigateToAllNote(){
        popBackCurrentNav()
        val rootParameterNote=ParameterRootNote(null,RootNoteDefaultItem(),RootNoteFrom.DEFAULT_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }
    private fun navigateToTrash(){
        popBackCurrentNav()
        val rootParameterNote=ParameterRootNote(null,RootNoteTrashItem(),RootNoteFrom.TRASH_FROM_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }
    private fun navigateToArchive(){
        popBackCurrentNav()
        val rootParameterNote=ParameterRootNote(null,RootNoteArchiveItem(),RootNoteFrom.ARCHIVE_FROM_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }

    private fun closeKeyboard(){
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,0)
    }
    private fun popBackCurrentNav(inclusive:Boolean=true){
        navController.popBackStack(R.id.displayTextNoteFragment,inclusive)
    }
    private fun adjustNavigation(){
        val navController= Navigation.findNavController(requireActivity(),R.id.frames)
        val onBackPressedCallback=object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                popBackCurrentNav(false)
                navController.navigateUp()
                isEnabled=false
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }


    override fun onPause() {
        super.onPause()
        viewModel.saveNote(note,lastSavedNote)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        if(navController.currentDestination?.id!=R.id.displayTextNoteFragment)
            viewModel.lastCheckNote(note)
        _binding=null

        if(!MainActivity.isPremiumActive)
            mInterstitialAd?.show(requireActivity())
    }
}