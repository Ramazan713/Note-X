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
import com.masterplus.notex.adapters.SelectBookAdapter
import com.masterplus.notex.databinding.FragmentSelectBookDialogBinding
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.roomdb.views.BookCountView
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.viewmodels.SelectBookViewModel
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectBookDialogFragment : CustomDialogFragment() {

    private var _binding:FragmentSelectBookDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel:SelectBookViewModel by viewModels()
    private val viewModelAddText: SetAddTextViewModel by viewModels()

    private lateinit var recyclerAdapter:SelectBookAdapter

    private var note:UnitedNote? = null

    private var parentSelectedBook:BookCountView? = null
    private var lastSearchedText:String = ""
    private var lastAllBooks:List<BookCountView> = listOf()

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val recyclerState=savedInstanceState?.getParcelable<Parcelable>("recyclerState")
        binding.recyclerFromBookDia.layoutManager?.onRestoreInstanceState(recyclerState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.stateSelectedBookCountView.value=recyclerAdapter.selectedItem
        val recyclerState=binding.recyclerFromBookDia.layoutManager?.onSaveInstanceState()
        outState.putParcelable("recyclerState",recyclerState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSelectBookDialogBinding.inflate(layoutInflater,container,false)

        requireArguments().let { args->
            note = args.getSerializable("note") as UnitedNote
        }

        recyclerAdapter = SelectBookAdapter()

        binding.recyclerFromBookDia.adapter=recyclerAdapter
        binding.recyclerFromBookDia.layoutManager=LinearLayoutManager(requireContext())


        if(note!=null&&note!!.note.bookId!=0L){
            viewModel.getBookCountView(note!!.note.bookId)
        }

        binding.btCancelFromBookDia.setOnClickListener {
            dismiss()
        }

        binding.btApproveFromBookDia.setOnClickListener {
            val selectedBook=recyclerAdapter.selectedItem
            if(selectedBook!=null){
                if(note!=null){
                    viewModel.updateNoteBookId(note!!.note.uid,selectedBook.bookId,selectedBook.isVisibleItems)
                }
                this.dismiss()
            }
        }
        setObservers()
        setSearchingView()
        setAddBookView()
        reloadForConfigurationChange()

        return binding.root
    }

    private fun setObservers(){

        viewModel.liveBookCountViews.observe(viewLifecycleOwner, Observer {items->
            recyclerAdapter.items=items.toMutableList().apply {
                if(note!=null&&note!!.note.bookId!=0L){
                    this.add(0,BookCountView(getString(R.string.remove_from_list_text),0,0,true))
                }
                lastAllBooks=this
            }
            recyclerAdapter.selectedItem?.also { selected->
                recyclerAdapter.getItemPos(selected).also { pos->
                    if(pos!=-1)
                        binding.recyclerFromBookDia.scrollToPosition(pos)
                }
            }
        })
        viewModel.parentBookCountView.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.selectedItem=it
            parentSelectedBook=it
            reloadForConfigurationChange()
        })


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelAddText.liveItem.collect {
                        viewModel.insertBook(it.approvedText)
                        clearSearchView()
                    }
                }
            }
        }
    }

    private fun reloadForConfigurationChange(){
        viewModel.stateSelectedBookCountView.value?.also { stateItem->
            recyclerAdapter.selectedItem=stateItem
        }
    }

    private fun setSearchingView(){
        binding.searchFromBookDia.searchVView.onActionViewExpanded()
        binding.searchFromBookDia.searchVView.clearFocus()

        var job: Job?=null
        binding.searchFromBookDia.searchVView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                job?.cancel()
                job=lifecycleScope.launch {
                    delay(300)
                    if(newText!=null&&newText.trim()!=""){
                        lastSearchedText=newText
                        recyclerAdapter.items=getSearchedBooks()
                    }else{
                        recyclerAdapter.items=lastAllBooks
                        lastSearchedText=""
                    }
                }
                return true
            }
        })
    }
    private fun getSearchedBooks():List<BookCountView>{
        val searchedBooks = mutableListOf<BookCountView>()
        lastAllBooks.forEach {
            if(it.name.lowercase().contains(lastSearchedText.lowercase()))
                searchedBooks.add(it)
        }
        return searchedBooks
    }

    private fun setAddBookView(){
        binding.addBookItemFromSelectBook.let { imageTextView->
            imageTextView.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_add_24))
            imageTextView.imageTextText.typeface = Typeface.DEFAULT_BOLD
            imageTextView.imageTextText.gravity = Gravity.CENTER
            imageTextView.imageTextText.text = getString(R.string.book_add_text)

            imageTextView.root.setOnClickListener {v->
                ParameterAddTextItem(lastSearchedText,getString(R.string.enter_notebook_name_text),
                    false,lastAllBooks.map { it.name }).also { parameterAddText->
                    customGetDialogs.getAddTextDia(parameterAddText)
                        .show(childFragmentManager,"")
                }
            }
        }
    }

    private fun clearSearchView(){
        binding.searchFromBookDia.searchVView.clearFocus()
        binding.searchFromBookDia.searchVView.setQuery("",false)
        lastSearchedText=""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}