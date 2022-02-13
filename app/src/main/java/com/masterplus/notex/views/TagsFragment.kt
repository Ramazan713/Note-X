package com.masterplus.notex.views

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.MainNavDirections
import com.masterplus.notex.R
import com.masterplus.notex.adapters.TagAdapter
import com.masterplus.notex.databinding.FragmentTagsBinding
import com.masterplus.notex.designpatterns.strategy.RootNoteTagItem
import com.masterplus.notex.enums.NoteFlags
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.RootNoteFrom
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.views.TagCountView
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.viewmodels.TagViewModel
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.masterplus.notex.views.view.CustomFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TagsFragment @Inject constructor(private val customAlerts: CustomAlerts,
                                       private val customGetDialogs: CustomGetDialogs
) : CustomFragment() {
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tagAdapter: TagAdapter
    private val viewModel: TagViewModel by viewModels()
    private val viewModelAddText: SetAddTextViewModel by viewModels()


    private var lastSearchedText:String=""
    private var lastAllTags:List<TagCountView> = listOf()
    private lateinit var navController: NavController

    private val RENAME_TAG=1
    private val INSERT_TAG=2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentTagsBinding.inflate(layoutInflater,container,false)
        navController= Navigation.findNavController(requireActivity(), R.id.frames)

        tagAdapter = TagAdapter()

        tagAdapter.setListener(tagAdapterListener)

        binding.recyclerTag.adapter=tagAdapter
        binding.recyclerTag.layoutManager= LinearLayoutManager(requireContext())

        setAddTagView()
        setSearchingView()
        setObservers()

        return binding.root
    }

    private fun setObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelAddText.liveItem.collect {
                        when(it.tag){
                            RENAME_TAG->{
                                viewModel.renameTag(it.approvedText,it.editedId?:0)
                            }
                            INSERT_TAG->{
                                viewModel.insertTag(it.approvedText)
                            }
                        }
                        clearSearchView()
                    }
                }
            }
        }

        viewModel.liveTagCountViews.observe(viewLifecycleOwner, Observer {
            tagAdapter.items=it
            lastAllTags=it
            setEmptyInfoView(it.isEmpty())
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addNoteFromTag.setOnClickListener {
            val parameter = ParameterNote(null,"#FFFFFF", NoteFlags.DEFAULT_NOTE,true,null, NoteKinds.ALL_KIND)
            customGetDialogs.getSelectNoteTypeDia(parameter)
                .show(childFragmentManager,"")
        }
    }

    private val tagAdapterListener=object:TagAdapter.TagListener{
        override fun clickedItem(item: TagCountView) {
            val parameterRootNote=ParameterRootNote(item.tagId,RootNoteTagItem(item.name),RootNoteFrom.TAG_FROM_NOTE)
            navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
        }

        override fun menuListener(menuItem: MenuItem, item: TagCountView, pos: Int) {
            if(menuItem.itemId== R.id.remove_tag_menu_item){
                customAlerts.showDeleteTagWithoutNotesAlert(requireContext()){x,y->
                    viewModel.deleteTagWithId(item.tagId)
                }
            }else if(menuItem.itemId== R.id.rename_tag_menu_item){
                ParameterAddTextItem(item.name,getString(R.string.rename_tag_text),
                    true,lastAllTags.map { it.name },RENAME_TAG,item.tagId).also {
                    customGetDialogs.getAddTextDia(it)
                        .show(childFragmentManager,"")
                }
            }
        }

    }

    private fun setAddTagView(){
        binding.addTagItem.let { imageTextView->

            imageTextView.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_new_label_24))
            imageTextView.imageTextText.typeface = Typeface.DEFAULT_BOLD
            imageTextView.imageTextText.gravity = Gravity.CENTER
            imageTextView.imageTextText.text = getString(R.string.add_tag_text)

            imageTextView.root.setOnClickListener {
                ParameterAddTextItem(lastSearchedText,getString(R.string.enter_tag_name_text),
                false,lastAllTags.map { it.name },INSERT_TAG).also {
                    customGetDialogs.getAddTextDia(it)
                        .show(childFragmentManager,"")
                }
            }
        }
    }
    private fun setEmptyInfoView(isItemsEmpty:Boolean){
        binding.noteEmptyInfo.apply {
            root.isVisible=isItemsEmpty
            if(isItemsEmpty){
                imageEmptyInfo.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_new_label_24))
                textEmptyInfo.text=getString(R.string.added_tags_appear_text)
            }
        }
    }

    private fun setSearchingView(){
        binding.searchViewFromTag.searchVView.onActionViewExpanded()
        binding.searchViewFromTag.searchVView.clearFocus()

        var job: Job?=null
        binding.searchViewFromTag.searchVView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                job?.cancel()
                job=lifecycleScope.launch {
                    delay(300)
                    if(newText!=null&&newText.trim()!=""){
                        lastSearchedText=newText
                        tagAdapter.items=getSearchedBooks()
                    }else{
                        tagAdapter.items=lastAllTags
                        lastSearchedText=""
                    }
                }
                return true
            }
        })
    }
    private fun getSearchedBooks():List<TagCountView>{
        val searchedBooks = mutableListOf<TagCountView>()
        lastAllTags.forEach {
            if(it.name.lowercase().contains(lastSearchedText.lowercase()))
                searchedBooks.add(it)
        }
        return searchedBooks
    }
    private fun clearSearchView(){
        binding.searchViewFromTag.searchVView.clearFocus()
        binding.searchViewFromTag.searchVView.setQuery("",false)
        lastSearchedText=""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}