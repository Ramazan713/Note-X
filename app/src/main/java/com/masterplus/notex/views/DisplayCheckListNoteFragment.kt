package com.masterplus.notex.views

import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.*
import com.masterplus.notex.adapters.CheckNoteAdapter
import com.masterplus.notex.databinding.FragmentDisplayCheckListNoteBinding
import com.masterplus.notex.designpatterns.state.CheckNoteEditorContext
import com.masterplus.notex.designpatterns.state.CheckNoteNullState
import com.masterplus.notex.designpatterns.strategy.RootNoteArchiveItem
import com.masterplus.notex.designpatterns.strategy.RootNoteDefaultItem
import com.masterplus.notex.designpatterns.strategy.RootNoteTrashItem
import com.masterplus.notex.enums.*
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.models.copymove.CheckItemCopyMoveObject
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.utils.*
import com.masterplus.notex.viewmodels.CheckListNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterplus.notex.models.ParameterAddCheckItem
import com.masterplus.notex.roomdb.entities.*
import com.masterplus.notex.viewmodels.items.AddCheckItemViewModel
import com.masterplus.notex.viewmodels.items.SetCopyMoveItemViewModel
import com.masterplus.notex.viewmodels.items.SetSelectColorItemViewModel
import com.masterplus.notex.views.view.CustomFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DisplayCheckListNoteFragment @Inject constructor(private val imm: InputMethodManager,
                                                       private val showMessage: ShowMessage,
                                                       private val customAlerts: CustomAlerts,
                                                       private val customGetDialogs: CustomGetDialogs,
                                                       private val sharedPreferences: SharedPreferences
) : CustomFragment() {
    private var _binding: FragmentDisplayCheckListNoteBinding?=null
    private val binding get() = _binding!!

    private lateinit var noteParameter: ParameterNote
    private val viewModel: CheckListNoteViewModel by viewModels()
    private val viewModelSelectColor: SetSelectColorItemViewModel by viewModels()
    private val viewModelAddCheckItem: AddCheckItemViewModel by viewModels()
    private val viewModelCopyMoveListener: SetCopyMoveItemViewModel by viewModels()

    private lateinit var navController: NavController
    private var lastSavedNote: UnitedNote = UnitedNote(Note(typeContent = NoteType.CheckList), arrayListOf())
    private var note: UnitedNote = lastSavedNote
    private var lastEditState:Boolean? = null

    private val checkNoteAdapter=CheckNoteAdapter()

    private lateinit var checkNoteContext:CheckNoteEditorContext

    private lateinit var callback:ActionMode.Callback
    private var actionMode:ActionMode? = null
    private lateinit var itemTouchHelper:ItemTouchHelper

    private var mInterstitialAd: InterstitialAd? = null
    private var isAdCountAdded=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isActionMode",actionMode!=null)
        outState.putBoolean("isAdCountAdded",isAdCountAdded)
        outState.putLong("noteId",note.note.uid)
        noteParameter.sharedNote=null
        viewModel.stateSelectedContentNotes.value=checkNoteAdapter.getSelectedItems().toMutableList()
        viewModel.stateParameterNote=noteParameter.deepCopy()
        super.onSaveInstanceState(outState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentDisplayCheckListNoteBinding.inflate(layoutInflater,container,false)
        setUpActionCallback()
        binding.appbarCheckNote.setExpanded(false)
        navController= Navigation.findNavController(requireActivity(),R.id.frames)
        checkNoteContext= CheckNoteEditorContext(binding)

        binding.recyclerCheckNote.adapter=checkNoteAdapter
        binding.recyclerCheckNote.layoutManager=LinearLayoutManager(requireContext())

        DisplayCheckListNoteFragmentArgs.fromBundle(requireArguments()).let { args->
            var noteId:Long?=null
            if(savedInstanceState!=null){
                isAdCountAdded=savedInstanceState.getBoolean("isAdCountAdded",isAdCountAdded)
                noteId=savedInstanceState.getLong("noteId")
            }

            noteParameter=viewModel.stateParameterNote?:args.noteParameter
            adjustBackgroundColor(noteParameter.color)

            noteParameter.sharedNote?.let { shared->
                if(noteParameter.isEmptyNote){
                    binding.editTitleCheckNote.setText(shared.title?:"")
                    note.note.title=shared.title?:""
                    note.contents.addAll(shared.contentNotes)
                    noteParameter.sharedNote=null
                }
            }

            val textFontSize=Utils.getFontSize(sharedPreferences).toFloat()
            checkNoteAdapter.setInit(Utils.getColorIntWithAddition(noteParameter.color,requireContext())
                ,noteParameter.isEmptyNote,textFontSize,adapterListener)
            setAddCheckNoteItemVisibility(false)
            if(noteParameter.isEmptyNote){
                checkNoteContext.setCurrentState(CheckNoteNullState(checkNoteContext))
                note = lastSavedNote.deepCopy()
                checkNoteAdapter.items=note.contents
                binding.showReminderViewFromCheck.root.isGone=true
                setEditState(true)
            }else{
                checkNoteContext.setCurrentState(noteParameter.noteKinds)
                viewModel.signalToLoadCurrentNote(noteId?:noteParameter.parentId!!)
            }
            note.note.kindNote=noteParameter.noteKinds
            requireActivity().invalidateOptionsMenu()

        }
        checkNoteContext.setNoteKindsListener {
            note.note.kindNote=it
            noteParameter.noteKinds=it
        }

        setObservers(savedInstanceState)
        setBehaviourSelectItemBook()
        setAddCheckNoteItemViews()
        setItemTouchHelper()
        checkAndLoadInterstitialAdd()
        adjustNavigation()
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTitleCheckNote.doOnTextChanged { text, start, before, count ->
            note.note.title=text.toString().trim()
        }

        binding.showTagsCheckNote.setOnClickListener {
            showSelectTagsDia()
        }
        binding.editTitleCheckNote.setKeyBackListener(object : (Boolean)->Unit{
            override fun invoke(isActive: Boolean) {
                if(noteParameter.noteKinds==NoteKinds.TRASH_KIND){
                    showMessage.showShort(getString(R.string.can_not_be_changed_text))
                }else
                    setEditState(isActive)
            }
        })
    }

    private fun setObservers(savedInstanceState:Bundle?){
        viewModel.note.observe(viewLifecycleOwner, Observer {
            note=it
            checkNoteAdapter.items=it.contents
            loadDataWithCheckingSearchedText(it)
            initLoading(it)
            lastSavedNote=note.deepCopy()
            adjustBackgroundColor(note.note.color)
            noteParameter.parentId = noteParameter.parentId?:it.note.uid

            if(savedInstanceState!=null){
                if(savedInstanceState.getBoolean("isActionMode")){
                    setEditState(true)
                    val items=viewModel.stateSelectedContentNotes.value?: listOf()
                    checkNoteAdapter.setSelectedItems(items)
                    savedInstanceState.putBoolean("isActionMode",false)
                }
            }

            if(noteParameter.isEmptyNote){
                setTransNullToNote(it.note.uid)
                noteParameter.isEmptyNote=false
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isRecoverNote.collect {
                        checkNoteContext.recoverNote()
                        viewModel.signalToLoadCurrentNote(note.note.uid)
                        requireActivity().invalidateOptionsMenu()
                    }
                }
                launch {
                    viewModel.isNoteSaved.collect {
                        showMessage.showLong(getString(R.string.saved_text))
                        lastSavedNote=note.deepCopy()
                        setShowDate(note.note.updateDate)
                    }
                }
                launch {
                    viewModelAddCheckItem.sentAddedCheckItem.collect {
                        val weight:Int=when{
                            it.isFirst->0
                            else->checkNoteAdapter.itemCount
                        }
                        val contentNote = ContentNote(note.note.uid,it.value,weight,false)
                        checkNoteAdapter.addItem(contentNote,weight)
                    }
                }
                launch {
                    viewModelAddCheckItem.sentEditedCheckItem.collect {
                        checkNoteAdapter.updateItem(it.newValue,it.pos)
                    }
                }
                launch {
                    viewModelCopyMoveListener.liveItem.collect {
                        if(it.isMove){
                            val items=checkNoteAdapter.getSelectedItems().toList()
                            note.contents.removeAll(items)
                            checkNoteAdapter.removeItems(items)
                        }
                        actionMode?.finish()
                    }
                }
                launch {
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
        checkNoteContext.transNullToNote(note.note.kindNote)
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
                binding.editTitleCheckNote.clearFocus()
                actionMode?.finish()
                viewModel.saveNote(note,lastSavedNote)
                binding.editTitleCheckNote.setText(viewModel.checkAndSetDefaultTitleToNote(note))
            }
            binding.editTitleCheckNote.isCursorVisible=isEdit
            checkNoteAdapter.setEdit(isEdit)
            setAddCheckNoteItemVisibility(isEdit)
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
    private fun setBehaviourSelectItemBook(){
        binding.selectBookFromCheckNote.root.setOnClickListener {
            customGetDialogs.getSelectBookDia(note)
                .show(childFragmentManager,"")
        }
    }
    private fun loadDataWithCheckingSearchedText(note: UnitedNote){
        val noteTitle=note.note.title
        val searchText=noteParameter.searchText?:""
        binding.editTitleCheckNote.setText(if(searchText!="") getHighLightedText(noteTitle,searchText) else noteTitle)

        checkNoteAdapter.setSearchedText(searchText)
    }
    private fun removeSearchedAffect(){
        if(noteParameter.searchText!=""){
            binding.editTitleCheckNote.setText(note.note.title)
            checkNoteAdapter.setSearchedText("")
            noteParameter.searchText=""
        }
    }
    private fun setIsAdCountAdded(){
        if(!isAdCountAdded){
            isAdCountAdded=true
            MainActivity.SAVED_NOTE_COUNT++
            checkAndLoadInterstitialAdd()
        }
    }

    private fun checkAndLoadInterstitialAdd(){
        if(!MainActivity.isPremiumActive&&mInterstitialAd==null&&
            MainActivity.SAVED_NOTE_COUNT>=MainActivity.MAX_SAVED_NOTE_COUNT){
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(requireContext(),getString(R.string.interstitialAdd_checkNote_id),adRequest,object :
                InterstitialAdLoadCallback(){
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
    private fun adjustBackgroundColor(color:Int){
        val colorInt=Utils.getColorUIMode(requireContext(),color)
        val colorWithAddition = Utils.getColorIntWithAddition(colorInt,requireContext())

        binding.nestedScrollDisplayCheckNote.setBackgroundColor(colorInt)
        binding.appbarCheckNote.setBackgroundColor(colorInt)
        binding.selectBookFromCheckNote.root.setCardBackgroundColor(colorWithAddition)
        binding.addItemCheckList.root.setCardBackgroundColor(colorWithAddition)
        binding.addItemCheckListBottom.root.setCardBackgroundColor(colorWithAddition)
        checkNoteAdapter.setColor(colorWithAddition)
        binding.showReminderViewFromCheck.root.setCardBackgroundColor(colorWithAddition)
        Utils.changeToolBarColor(requireActivity(),colorInt)
    }
    private fun adjustBackgroundColor(color:String){
        val colorInt:Int = Color.parseColor(color)
        adjustBackgroundColor(colorInt)
    }

    private fun setFontSize(fontSize:Float?=null){
        val size:Float=fontSize?:Utils.getFontSize(sharedPreferences).toFloat()
        checkNoteAdapter.setTextSize(size)
    }

    private fun setBookText(book: Book?){
        binding.selectBookFromCheckNote.textItemSelectBook.text = book?.name ?: getString(R.string.unselected_text)
    }
    private fun setReminderViews(reminder: Reminder?){
        binding.showReminderViewFromCheck.let { reminderView->
            if(reminder!=null){
                val backgroundId=if(reminder.reminderType == ReminderTypes.NOT_REPEATED)R.drawable.ic_baseline_access_time_24
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
    private val adapterListener = object:CheckNoteAdapter.CheckNoteAdapterListener{
        override fun selectedMenuItem(menuItem: MenuItem, item: ContentNote, position: Int) {
            when(menuItem.itemId){
                R.id.edit_check_note_menu_item->{
                    customGetDialogs.getAddCheckNoteItemDia(ParameterAddCheckItem(true,item.text,position))
                        .show(childFragmentManager,"")
                }
                R.id.remove_check_note_menu_item->{
                    customAlerts.showDeleteContentItemsAlert(requireContext()){x,y->
                        viewModel.removeContentItem(item)
                        checkNoteAdapter.removeItem(position)
                    }
                }
                R.id.copyText_check_note_menu_item->{
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText(item.text, item.text)
                    clipboard.setPrimaryClip(clip)
                    showMessage.showLong(getString(R.string.successfully_copied_text))
                }
            }
        }

        override fun editRequestItem(item: ContentNote, position: Int) {
            customGetDialogs.getAddCheckNoteItemDia(ParameterAddCheckItem(true,item.text,position))
                .show(childFragmentManager,"")
        }

        override fun selectedItemCount(count: Int) {
            if(count>0&&(lastEditState==null || lastEditState==false))
                setEditState(true)
            actionMode?.invalidate()
        }

        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
            itemTouchHelper.startDrag(viewHolder)
        }

    }

    private fun setUpActionCallback(){
        callback=object:ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.action_display_checklist_note_menu,menu)
                menu?.findItem(R.id.check_list_save_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                menu?.findItem(R.id.check_list_selectAll_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                menu?.findItem(R.id.check_list_delete_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val selectedSize=checkNoteAdapter.getSelectedItems().size
                val isNotEmpty=selectedSize>0
                menu?.findItem(R.id.check_list_delete_act_menu_item)?.isEnabled =isNotEmpty
                menu?.findItem(R.id.check_list_copy_act_menu_item)?.isEnabled=isNotEmpty
                menu?.findItem(R.id.check_list_move_act_menu_item)?.isEnabled=isNotEmpty
                menu?.findItem(R.id.check_list_checkAllNone_act_menu_item)?.isEnabled=isNotEmpty
                menu?.findItem(R.id.check_list_checkAll_act_menu_item)?.isEnabled=isNotEmpty
                mode?.title=selectedSize.toString()

                return true
            }
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                when(p1?.itemId){
                    R.id.check_list_save_act_menu_item->{
                        actionMode?.finish()
                    }
                    R.id.check_list_delete_act_menu_item->{
                        customAlerts.showDeleteContentItemsAlert(requireContext()){x,y->
                            val selectedItems = checkNoteAdapter.getSelectedItems()
                            viewModel.removeContentItems(selectedItems.toList())
                            checkNoteAdapter.removeItems(selectedItems)
                            actionMode?.finish()
                        }
                    }
                    R.id.check_list_copy_act_menu_item->{
                        showCopyMoveDiaFragment(false)
                    }
                    R.id.check_list_move_act_menu_item->{
                        showCopyMoveDiaFragment(true)
                    }
                    R.id.check_list_checkAllNone_act_menu_item->{
                        checkNoteAdapter.setCheckSelectedItems(false)
                    }
                    R.id.check_list_checkAll_act_menu_item->{
                        checkNoteAdapter.setCheckSelectedItems(true)
                    }
                    R.id.check_list_selectAll_act_menu_item->{
                        checkNoteAdapter.selectAllItems()
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
    private fun showCopyMoveDiaFragment(isMove:Boolean){
        val contentNotes = checkNoteAdapter.getSelectedItems()
        val noteId=note.note.uid
        val copyMoveObject=CheckItemCopyMoveObject(isMove,contentNotes,noteId,requireContext())
        customGetDialogs.getCopyMoveDia(copyMoveObject)
            .show(childFragmentManager,"")
    }
    private fun showSelectTagsDia(){
        val noteIds=if(note.note.uid!=0L) listOf(note.note.uid)else listOf()
        customGetDialogs.getSelectTagsDia(noteIds)
            .show(childFragmentManager,"")
    }
    private fun setTagsAndVisibility(tags:List<Tag>){
        binding.showTagsCheckNote.isGone = tags.isEmpty()
        binding.showTagsCheckNote.showTags(tags)
    }
    private fun setShowDate(dateStr:String){
        binding.showDateFromCheckNote.text=Utils.getInsideNoteDateFormat(dateStr)
    }
    private fun setAddCheckNoteItemViews(){
        binding.addItemCheckList.imageTextText.let { textView ->
            textView.gravity = Gravity.CENTER
            textView.text = getText(R.string.add_text)
            textView.typeface = Typeface.DEFAULT_BOLD
        }
        binding.addItemCheckList.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_add_circle_24))
        binding.addItemCheckList.root.setOnClickListener {
            customGetDialogs.getAddCheckNoteItemDia(ParameterAddCheckItem(false,""))
                .show(childFragmentManager,"")
        }

        binding.addItemCheckListBottom.imageTextText.let { textView ->
            textView.gravity = Gravity.CENTER
            textView.text = getText(R.string.add_text)
            textView.typeface = Typeface.DEFAULT_BOLD
        }
        binding.addItemCheckListBottom.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_add_circle_24))
        binding.addItemCheckListBottom.root.setOnClickListener {
            customGetDialogs.getAddCheckNoteItemDia(ParameterAddCheckItem(false,""))
                .show(childFragmentManager,"")
        }
    }

    private fun setAddCheckNoteItemVisibility(isEdit: Boolean){
        binding.addItemCheckList.root.isVisible=isEdit
        binding.addItemCheckListBottom.root.isVisible=isEdit
    }
    private fun navigateToAllNote(){
        popBackCurrentNav()
        val rootParameterNote= ParameterRootNote(null, RootNoteDefaultItem(), RootNoteFrom.DEFAULT_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }
    private fun navigateToTrash(){
        popBackCurrentNav()
        val rootParameterNote= ParameterRootNote(null, RootNoteTrashItem(), RootNoteFrom.TRASH_FROM_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }
    private fun navigateToArchive(){
        popBackCurrentNav()
        val rootParameterNote= ParameterRootNote(null, RootNoteArchiveItem(), RootNoteFrom.ARCHIVE_FROM_NOTE)
        navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
    }
    private fun popBackCurrentNav(inclusive:Boolean=true){
        navController.popBackStack(R.id.displayCheckListNoteFragment,inclusive)
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

    private fun setItemTouchHelper(){
        itemTouchHelper=ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val destPos=viewHolder.bindingAdapterPosition
                val targetPos=target.bindingAdapterPosition
                checkNoteAdapter.swapItems(destPos,targetPos)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerCheckNote)
    }

    private fun closeKeyboard(){
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,0)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        checkNoteContext.setOnCreateMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        checkNoteContext.setOnPrepareMenu(menu)
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
                customAlerts.showDeleteNoteToTrashAlert(requireContext()){x,y->
                    viewModel.sendNoteToTrashWithNoteId(note.note.uid)
                    if(noteParameter.noteKinds==NoteKinds.ARCHIVE_KIND) navigateToArchive() else navigateToAllNote()
                }
            }
            R.id.display_note_share_menu_item->{
                val text=UtilsShareNote.transformContentNotesToText(note.contents,NoteType.CheckList)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, note.note.title)
                    putExtra(Intent.EXTRA_TEXT, text)
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
                customAlerts.showDeleteNoteForEverAlert(requireContext()){x,y->
                    viewModel.deleteNoteForEver(note.note.uid)
                    navigateToTrash()
                }
            }
            R.id.restore_notes_act_trash_menu_item->{
                customAlerts.showRestoreNoteAlert(requireContext()){x,y->
                    viewModel.recoverNote(note.note.uid)
                }
            }
            R.id.check_list_remove_checked_notes_menu_item->{
                customAlerts.showDeleteContentItemsAlert(requireContext()){x,y->
                    val items=checkNoteAdapter.items.filter { it.isCheck }
                    viewModel.removeContentItems(items.toList())
                    checkNoteAdapter.removeItems(items)
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
    override fun onPause() {
        super.onPause()
        viewModel.saveNote(note,lastSavedNote)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        if(navController.currentDestination?.id!=R.id.displayCheckListNoteFragment)
            viewModel.lastCheckNote(note)
        _binding=null

        if(!MainActivity.isPremiumActive)
            mInterstitialAd?.show(requireActivity())

    }

}