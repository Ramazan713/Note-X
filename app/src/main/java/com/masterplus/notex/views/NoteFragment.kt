package com.masterplus.notex.views

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.masterplus.notex.MainNavDirections
import com.masterplus.notex.R
import com.masterplus.notex.views.bottomSheetDialogFragments.SortingAndAppearanceBottomDiaFragment
import com.masterplus.notex.adapters.NotePagingAdapter
import com.masterplus.notex.databinding.FragmentNoteBinding
import com.masterplus.notex.designpatterns.strategy.RootNoteDefaultItem
import com.masterplus.notex.enums.*
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.models.copymove.NoteCopyMoveObject
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.viewmodels.RootNoteViewModel
import com.masterplus.notex.viewmodels.SortingViewModel
import com.masterplus.notex.viewmodels.items.DestroyListenerViewModel
import com.masterplus.notex.viewmodels.items.SetCopyMoveItemViewModel
import com.masterplus.notex.viewmodels.items.SetSelectColorItemViewModel
import com.masterplus.notex.views.view.CustomFragment
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class NoteFragment @Inject constructor(private val pagingAdapter: NotePagingAdapter,
                                       private val showMessage: ShowMessage,
                                       private val customAlerts: CustomAlerts,
                                       private val customGetDialogs: CustomGetDialogs,
                                       private val sharedPreferences:SharedPreferences
) : CustomFragment(){

    private var _binding:FragmentNoteBinding?=null
    private val binding get() = _binding!!
    private val viewModel:RootNoteViewModel by viewModels()
    private val viewModelSorting:SortingViewModel by viewModels()
    private val viewModelSelectColor: SetSelectColorItemViewModel by viewModels()
    private val viewModelCopyMoveListener: SetCopyMoveItemViewModel by viewModels()
    private val viewModelDestroyListener:DestroyListenerViewModel by viewModels()


    private lateinit var navController: NavController

    private lateinit var callback:ActionMode.Callback
    private var actionMode:ActionMode? = null
    private var lastEditState:Boolean? = null

    private lateinit var parameterRootNote:ParameterRootNote

    private var lastSearchedText:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("lastSearchedText",lastSearchedText)
        outState.putBoolean("isActionMode",actionMode!=null)
        if(actionMode!=null)
            viewModel.stateSelectedNotes.value=pagingAdapter.getSelectedItems().toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNoteBinding.inflate(layoutInflater,container,false)
        navController=Navigation.findNavController(requireActivity(), R.id.frames)
        binding.notesAppbar.setExpanded(false)

        NoteFragmentArgs.fromBundle(requireArguments()).let {args->
            parameterRootNote=args.rootParameterNote
                ?: ParameterRootNote(null,RootNoteDefaultItem(),RootNoteFrom.DEFAULT_NOTE)

            setAppearanceViews()
            setTitle()
            loadNoteDataWithOrder()
        }

        setUpActionModeCallback()

        binding.floatingAddNote.setOnClickListener {
            val noteParameter = ParameterNote(parameterRootNote.parentId,
                noteFlags = parameterRootNote.rooNoteItem.getNoteFlag(),
                isEmptyNote = true,searchText = null,noteKinds = parameterRootNote.rooNoteItem.getNoteKind())
            customGetDialogs.getSelectNoteTypeDia(noteParameter)
                .show(childFragmentManager,"")
        }
        pagingAdapter.setInit(notePagingListener,isNoteHalfSize = getSpanSizeForGridLayout()>1)

        binding.recyclerNote.adapter=pagingAdapter
        binding.recyclerNote.layoutManager=GridLayoutManager(requireContext(),getSpanSizeForGridLayout())

        setAppearanceNoteAdapter()
        setObservers()

        pagingAdapter.addLoadStateListener {loadState->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached) {
                _binding?.noteEmptyInfo?.root?.isGone = pagingAdapter.itemCount!=0
                _binding?.recyclerNote?.isGone = pagingAdapter.itemCount==0
            }
        }

        adjustConfigurationReload(savedInstanceState)
        return _binding?.root
    }

    private fun setObservers(){

        viewModel.getUltimateNotes(parameterRootNote).observe(viewLifecycleOwner, Observer {
            pagingAdapter.submitData(lifecycle,it)
        })
        viewModel.getSearchedUltimateNotes()?.observe(viewLifecycleOwner, Observer { it ->
            pagingAdapter.submitData(lifecycle,it)
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelSorting.isSortingChange.collect {
                        loadNoteDataWithOrder()
                    }
                }
                launch {
                    viewModelDestroyListener.isClosed.collect {
                        actionMode?.finish()
                    }
                }
                launch {
                    viewModelCopyMoveListener.liveItem.collect {
                        actionMode?.finish()
                    }
                }
                launch {
                    viewModelSelectColor.liveItem.collect {
                        viewModel.changeNoteColors(it,pagingAdapter.getSelectedItems().map { it.noteView.noteId })
                    }
                }
                launch {
                    viewModelSorting.isAppearanceChange.collect {
                        setAppearanceNoteAdapter()
                    }
                }
            }
        }
    }


    private fun adjustConfigurationReload(savedInstanceState: Bundle?){
        savedInstanceState?.let { bundle ->
            if(bundle.getString("lastSearchedText","")!=""){
                lastSearchedText=bundle.getString("lastSearchedText")?:""
                viewModel.setSearchText(lastSearchedText)
                pagingAdapter.setSearchText(lastSearchedText)
                bundle.putString("lastSearchText","")
            }
            if(bundle.getBoolean("isActionMode",false)){
                setEdit(true)
                pagingAdapter.setSelectedItems(viewModel.stateSelectedNotes.value?:listOf())
                bundle.putBoolean("isActionMode",false)
            }
        }
    }

    private fun setAppearanceViews(){
        binding.noteEmptyInfo.apply {
            imageEmptyInfo.setImageDrawable(ContextCompat.getDrawable(requireContext()
                ,parameterRootNote.rooNoteItem.getBackgroundDrawableIdForEmptyNoteList()))

            textEmptyInfo.text=parameterRootNote.rooNoteItem.getBackgroundEmptyListDescription(requireContext())
        }
        binding.imageTitleNote.setImageDrawable(ContextCompat.getDrawable(requireContext()
            ,parameterRootNote.rooNoteItem.getImageTitleDrawableId()))

        if(parameterRootNote.rootNoteFrom==RootNoteFrom.TRASH_FROM_NOTE){
            binding.floatingAddNote.isVisible=false
        }


    }
    private fun setTitle(){
        val title=parameterRootNote.rooNoteItem.getTitle(requireContext())
        binding.notesAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, p1 ->
            setToolbarTitle(if(p1.absoluteValue>binding.textNoteTitle.height) title else "")
        })
        binding.textNoteTitle.text=title
    }


    private fun setEdit(isEdit:Boolean){
        if(lastEditState==null||lastEditState!=isEdit){
            if(!isEdit){
                actionMode?.finish()
                pagingAdapter.exitEdit()
            }
            lastEditState=isEdit
        }
    }


    private val notePagingListener=object:NotePagingAdapter.NotePagingAdapterListener{
        override fun clickedNote(item: UltimateNote, pos: Int) {
            item.noteView.let { note->
                val parameterNote=ParameterNote(note.noteId,note.color,parameterRootNote.rooNoteItem.getNoteFlag(),
                    false,lastSearchedText,noteKinds = item.noteView.kindNote)
                if(note.typeContent==NoteType.Text){
                    navController.navigate(MainNavDirections.actionGlobalDisplayTextNoteFragment(parameterNote))
                }else{
                    navController.navigate(MainNavDirections.actionGlobalDisplayCheckListNoteFragment(parameterNote))
                }
            }
        }
        override fun isLongClick(selectedSize: Int, isLongActive: Boolean) {
            if(isLongActive){
                if(actionMode==null){
                    actionMode=requireActivity().startActionMode(callback)
                    setEdit(true)
                }
                actionMode?.title=selectedSize.toString()
                actionMode?.invalidate()
            }else{
                setEdit(false)
            }
        }
    }

    private fun loadNoteDataWithOrder(){
        val orderNote=OrderNote.valueOf(sharedPreferences.getString("orderNote",null)
            ?:OrderNote.EDIT_TIME.toString())
        val isDescending=sharedPreferences.getBoolean("isDescendingOrder",true)
        viewModel.setSortingChange(orderNote, isDescending)
    }
    private fun setAppearanceNoteAdapter(){
        val isContentVisible=sharedPreferences.getBoolean("isContentsVisible",true)
        val isTagsVisible=sharedPreferences.getBoolean("isTagsVisible",true)
        pagingAdapter.setAppearance(isContentVisible,isTagsVisible)
    }

    private fun setUpActionModeCallback(){
        callback=object:ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                if(parameterRootNote.rootNoteFrom==RootNoteFrom.TRASH_FROM_NOTE){
                    mode?.menuInflater?.inflate(R.menu.action_trash_menu,menu)
                }else{
                    mode?.menuInflater?.inflate(R.menu.action_root_note_menu,menu)
                    mode?.menuInflater?.inflate(R.menu.archive_menu,menu)
                    menu?.findItem(R.id.root_note_pin_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    menu?.findItem(R.id.root_note_unpin_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    menu?.findItem(R.id.root_note_color_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    menu?.findItem(R.id.root_note_tag_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    menu?.findItem(R.id.root_note_selectAll_act_menu_item)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, menu: Menu?): Boolean {
                val selectedItems=pagingAdapter.getSelectedItems()
                val selectedItemSize=selectedItems.size

                val isAddArchiveVisible:Boolean = selectedItems.any { it.noteView.kindNote!=NoteKinds.ARCHIVE_KIND }
                menu?.findItem(R.id.achieve_remove_menu_item)?.isVisible=!isAddArchiveVisible
                menu?.findItem(R.id.achieve_add_menu_item)?.isVisible=isAddArchiveVisible

                val isPinItemVisible:Boolean=selectedItems.any { it.noteView.weight==0 }
                menu?.findItem(R.id.root_note_pin_act_menu_item)?.isVisible=isPinItemVisible
                menu?.findItem(R.id.root_note_unpin_act_menu_item)?.isVisible=!isPinItemVisible

                menu?.findItem(R.id.root_note_add_reminder_act_menu_item)?.isVisible = selectedItemSize==1


                return true
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                when(p1?.itemId){
                    R.id.root_note_color_act_menu_item ->{
                        val colors= mutableSetOf<String>()
                        pagingAdapter.getSelectedItems().map { colors.add(it.noteView.color) }
                        val selectedColor:String?=if(colors.size==1)colors.last() else null
                        customGetDialogs.getSelectColorDia(selectedColor)
                            .show(childFragmentManager,"")
                    }
                    R.id.root_note_tag_act_menu_item ->{
                        val selectedNoteIds=pagingAdapter.getSelectedItems().map { it.noteView.noteId }
                        customGetDialogs.getSelectTagsDia(selectedNoteIds)
                            .show(childFragmentManager,"")
                    }
                    R.id.root_note_add_reminder_act_menu_item->{
                        val noteId=pagingAdapter.getSelectedItems()[0].noteView.noteId
                        customGetDialogs.getSelectReminderDia(noteId)
                            .show(childFragmentManager,"")
                    }
                    R.id.root_note_selectAll_act_menu_item ->{
                        pagingAdapter.selectAll()
                    }
                    R.id.root_note_delete_act_menu_item ->{
                        customAlerts.showDeleteSelectedNotesToTrashAlert(requireContext()){x,y->
                            viewModel.sendNotesToTrashWithId(pagingAdapter.getSelectedItems().map { it.noteView.noteId })
                            actionMode?.finish()
                            showSuccessMessage()
                        }
                    }
                    R.id.root_note_pin_act_menu_item ->{
                        viewModel.setPinNotes(pagingAdapter.getSelectedItems().map { it.noteView.noteId },true)
                        actionMode?.finish()
                    }
                    R.id.root_note_unpin_act_menu_item ->{
                        viewModel.setPinNotes(pagingAdapter.getSelectedItems().map { it.noteView.noteId },false)
                        actionMode?.finish()
                    }
                    R.id.root_note_move_act_menu_item ->{
                        val copyMoveObject=NoteCopyMoveObject(true,
                            pagingAdapter.getSelectedItems().map { it.noteView.noteId },requireContext())

                        customGetDialogs.getCopyMoveDia(copyMoveObject)
                            .show(childFragmentManager,"")

                    }
                    R.id.root_note_copy_act_menu_item ->{
                        val copyMoveObject=NoteCopyMoveObject(false,
                            pagingAdapter.getSelectedItems().map { it.noteView.noteId },requireContext())
                        customGetDialogs.getCopyMoveDia(copyMoveObject)
                            .show(childFragmentManager,"")
                    }
                    R.id.achieve_remove_menu_item ->{
                        viewModel.changeNotesKindsWithNoteIds(pagingAdapter.getSelectedItems().map { it.noteView.noteId },NoteKinds.ALL_KIND)
                        actionMode?.finish()
                        showSuccessMessage()
                    }
                    R.id.achieve_add_menu_item ->{
                        viewModel.changeNotesKindsWithNoteIds(pagingAdapter.getSelectedItems().map { it.noteView.noteId },NoteKinds.ARCHIVE_KIND)
                        actionMode?.finish()
                        showSuccessMessage()
                    }
                    R.id.restore_notes_act_trash_menu_item ->{
                        customAlerts.showRestoreSelectedNotesAlert(requireContext()){x,y->
                            viewModel.recoverNotes(pagingAdapter.getSelectedItems().map { it.noteView.noteId })
                            actionMode?.finish()
                            showSuccessMessage()
                        }
                    }
                    R.id.remove_notes_forever_act_trash_menu_item ->{
                        customAlerts.showDeleteSelectedNotesForEverAlert(requireContext()){x,y->
                            viewModel.deleteNotesForEver(pagingAdapter.getSelectedItems().map { it.noteView.noteId })
                            actionMode?.finish()
                            showSuccessMessage()
                        }
                    }
                }
                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                actionMode=null
                setEdit(false)

            }

        }
    }

    private fun showSuccessMessage(){
        showMessage.showShort(getString(R.string.success_text))
    }

    private fun getSpanSizeForGridLayout():Int{
        return sharedPreferences.getInt("rootNoteSpanSize",1)
    }
    private fun setSpanSizeForGridLayout(newSpanSize:Int){
        sharedPreferences.edit().putInt("rootNoteSpanSize",newSpanSize).apply()
    }
    private fun changeSpanSizeAppearance(menuItem: MenuItem){
        (binding.recyclerNote.layoutManager as GridLayoutManager?)?.let { layoutManager->
            val currentSpanSize=getSpanSizeForGridLayout()
            layoutManager.spanCount=currentSpanSize
            menuItem.setIcon(if(currentSpanSize==2) R.drawable.ic_baseline_view_agenda_24 else R.drawable.grid2x2_24)
            pagingAdapter.setIsNoteHalfSize(currentSpanSize>1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.root_note_menu,menu)

        if(parameterRootNote.rootNoteFrom==RootNoteFrom.TRASH_FROM_NOTE){
            inflater.inflate(R.menu.trash_menu,menu)
            menu.findItem(R.id.search_notes_menu_item).isVisible=false
        }

        menu.findItem(R.id.search_notes_menu_item).also { searchItem->
            (searchItem.actionView as? SearchView)?.also { searchView ->
                searchView.setOnQueryTextListener(searchViewListener)
                if(lastSearchedText!=""){
                    searchItem.expandActionView()
                    searchView.setQuery(lastSearchedText,false)
                    searchView.clearFocus()
                }
            }
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val changeSpanViewForGridLayout=menu.findItem(R.id.changeView_note_menu_item)
        changeSpanSizeAppearance(changeSpanViewForGridLayout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.search_notes_menu_item ->{}
            R.id.changeView_note_menu_item ->{
                val currentSpanSize=getSpanSizeForGridLayout()
                setSpanSizeForGridLayout(if(currentSpanSize==1)2 else 1)
                changeSpanSizeAppearance(item)
            }
            R.id.sortView_dia_note_menu_item ->{
                val sortDiaFragment = SortingAndAppearanceBottomDiaFragment()
                sortDiaFragment.show(childFragmentManager,"")
            }
            R.id.remove_notes_forever_trash_menu_item ->{
                customAlerts.showDeleteSelectedNotesForEverAlert(requireContext()){x,y->
                    viewModel.deleteNotesForEver(pagingAdapter.snapshot().items.map { it.noteView.noteId })
                    actionMode?.finish()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        actionMode?.finish()
    }

    override fun onPause() {
        super.onPause()
        if(navController.currentDestination?.id!= R.id.noteFragment){
            actionMode?.finish()
        }

    }


    private val searchViewListener = object : SearchView.OnQueryTextListener{
        private var job: Job? = null
        override fun onQueryTextSubmit(query: String?): Boolean = false

        override fun onQueryTextChange(newText: String?): Boolean {
            job?.cancel()
            job=lifecycleScope.launch {
                delay(300)
                if(newText!=null){
                    lastSearchedText=newText.lowercase().trim()
                    if(lastSearchedText==""){
                        loadNoteDataWithOrder()
                    }
                    viewModel.setSearchText(lastSearchedText)
                    pagingAdapter.setSearchText(lastSearchedText)
                }
            }
            return true
        }

    }
}