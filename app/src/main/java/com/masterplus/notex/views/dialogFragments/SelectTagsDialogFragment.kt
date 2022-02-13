package com.masterplus.notex.views.dialogFragments

import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.adapters.SelectTagsAdapter
import com.masterplus.notex.databinding.FragmentSelectTagsDialogBinding
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.viewmodels.SelectTagsViewModel
import com.masterplus.notex.viewmodels.items.DestroyListenerViewModel
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SelectTagsDialogFragment  : CustomDialogFragment() {

    private var _binding:FragmentSelectTagsDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel:SelectTagsViewModel by viewModels()
    private val viewModelAddText: SetAddTextViewModel by viewModels()
    private val viewModelDestroyListener: DestroyListenerViewModel by viewModels({requireParentFragment()})


    private val recyclerAdapter:SelectTagsAdapter = SelectTagsAdapter()

    private var noteIds:List<Long> = listOf()
    private var isEmptyNoteId:Boolean=true

    private var lastAllTags:List<Tag> = listOf()
    private var lastSearchedText:String=""

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val recyclerState=binding.recyclerFromSTG.layoutManager?.onSaveInstanceState()
        outState.putParcelable("recyclerState",recyclerState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val recyclerState=savedInstanceState?.getParcelable<Parcelable>("recyclerState")
        binding.recyclerFromSTG.layoutManager?.onRestoreInstanceState(recyclerState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSelectTagsDialogBinding.inflate(layoutInflater,container,false)

        requireArguments().also { args->
            noteIds=args.getLongArray("noteIds")?.toList() ?: listOf()
            isEmptyNoteId=noteIds.isEmpty()
            viewModel.loadCommonAndIndeterminateTags(noteIds)
        }

        recyclerAdapter.setListener(selectTagsAdapterListener)


        binding.recyclerFromSTG.adapter=recyclerAdapter
        binding.recyclerFromSTG.layoutManager=LinearLayoutManager(requireContext())

        setObservers()
        setAddTagView()
        setSearchingView()

        return binding.root
    }
    private fun setObservers(){
        viewModel.liveTags.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.tags=it
            lastAllTags=it
        })

        viewModel.commonTags.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.setChosenTags(it)
        })

        viewModel.indeterminateTags.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.setIndeterminateTags(it)
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelAddText.liveItem.collect {
                        viewModel.insertTag(it.approvedText)
                        clearSearchView()
                    }
                }
            }
        }
    }

    private fun setSearchingView(){
        binding.searchTagsFromSTG.searchVView.onActionViewExpanded()
        binding.searchTagsFromSTG.searchVView.clearFocus()

        var job: Job?=null
        binding.searchTagsFromSTG.searchVView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                job?.cancel()
                job=lifecycleScope.launch {
                    delay(300)
                    if(newText!=null&&newText.trim()!=""){
                        lastSearchedText=newText
                        recyclerAdapter.tags=getSearchedTags()
                    }else{
                        recyclerAdapter.tags=lastAllTags
                        lastSearchedText=""
                    }
                }
                return true
            }
        })
    }

    private fun getSearchedTags():List<Tag>{
        val searchedTags = mutableListOf<Tag>()
        lastAllTags.forEach {
            if(it.name.lowercase().contains(lastSearchedText.lowercase()))
                searchedTags.add(it)
        }
        return searchedTags
    }

    private fun setAddTagView(){
        binding.addTagFromSelectTags.let { imageTextView->
            imageTextView.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_add_24))
            imageTextView.imageTextText.typeface = Typeface.DEFAULT_BOLD
            imageTextView.imageTextText.gravity = Gravity.CENTER
            imageTextView.imageTextText.text = getString(R.string.add_tag_text)

            imageTextView.root.setOnClickListener {
                ParameterAddTextItem(lastSearchedText,getString(R.string.enter_tag_name_text),
                    false,lastAllTags.map { it.name }).also { parameterAddText->
                    customGetDialogs.getAddTextDia(parameterAddText)
                        .show(childFragmentManager,"")
                }
            }
        }

    }

    private fun clearSearchView(){
        binding.searchTagsFromSTG.searchVView.clearFocus()
        binding.searchTagsFromSTG.searchVView.setQuery("",false)
        lastSearchedText=""
    }

    private val selectTagsAdapterListener=object:SelectTagsAdapter.SelectTagsAdapterListener{
        override fun clickedTag(tag: Tag, isSelected: Boolean, pos: Int) {
            if(!isEmptyNoteId){
                if(isSelected){
                    viewModel.addRelationTagWithNotes(tag, noteIds)
                }else{
                    viewModel.removeRelationTagWithNotes(tag, noteIds)
                }
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        viewModelDestroyListener.setIsClosed(true)
    }


}