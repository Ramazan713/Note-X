package com.masterplus.notex.views

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import com.masterplus.notex.adapters.BookAdapter
import com.masterplus.notex.databinding.FragmentBookBinding
import com.masterplus.notex.designpatterns.strategy.RootNoteBookItem
import com.masterplus.notex.designpatterns.strategy.RootNoteEmptyBookItem
import com.masterplus.notex.enums.NoteFlags
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.RootNoteFrom
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.views.BookCountView
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.viewmodels.BookViewModel
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.masterplus.notex.views.view.CustomFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookFragment @Inject constructor(private val customAlerts: CustomAlerts,
                                       private val customGetDialogs: CustomGetDialogs
) : CustomFragment() {

    private var _binding:FragmentBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookAdapter:BookAdapter
    private val viewModel:BookViewModel by viewModels()
    private val viewModelAddText: SetAddTextViewModel by viewModels()

    private var lastSearchedText:String=""
    private var lastAllBooks:List<BookCountView> = listOf()
    private lateinit var navController:NavController

    private val RENAME_BOOK=1
    private val INSERT_BOOK=2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentBookBinding.inflate(layoutInflater,container,false)
        navController=Navigation.findNavController(requireActivity(), R.id.frames)
        bookAdapter = BookAdapter()

        bookAdapter.setListener(bookAdapterListener)


        binding.recyclerBook.adapter=bookAdapter
        binding.recyclerBook.layoutManager=LinearLayoutManager(requireContext())

        viewModel.getUnSelectedNoteForBook()




        setAddBookView()
        setSearchingView()
        setObservers()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addNoteFromBook.setOnClickListener {
            val parameter=ParameterNote(null,"#FFFFFF",NoteFlags.DEFAULT_NOTE,true,null,NoteKinds.ALL_KIND)
            customGetDialogs.getSelectNoteTypeDia(parameter)
                .show(childFragmentManager,"")
        }

    }

    private fun setObservers(){
        viewModel.liveBookCountView.observe(viewLifecycleOwner, Observer {
            bookAdapter.items=it
            lastAllBooks=it
        })

        viewModel.unSelectedNotesCount.observe(viewLifecycleOwner, Observer {
            setUnselectedNotesItemView(it)
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelAddText.liveItem.collect {
                        when(it.tag){
                            RENAME_BOOK->{
                                viewModel.renameBook(it.approvedText,it.editedId?:0)
                            }
                            INSERT_BOOK->{
                                viewModel.insertBook(it.approvedText)
                            }
                        }
                        clearSearchView()
                    }
                }
            }
        }
    }


    private val bookAdapterListener=object:BookAdapter.BookListener{
        override fun clickedItem(item: BookCountView) {
            val parameterRootNote=
                ParameterRootNote(item.bookId, RootNoteBookItem(item.name), RootNoteFrom.BOOK_FROM_NOTE)
            navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
        }

        override fun menuListener(menuItem: MenuItem, item: BookCountView, pos: Int) {
            when (menuItem.itemId) {

                R.id.remove_book_menu_item -> {
                    customAlerts.showDeleteBookWithNotesAlert(requireContext()) { x, y ->
                        viewModel.deleteBookWithId(item.bookId)
                    }
                }
                R.id.rename_book_menu_item -> {
                    ParameterAddTextItem(item.name,getString(R.string.notebook_rename_text),
                        true,lastAllBooks.map { it.name },RENAME_BOOK,item.bookId).also {
                        customGetDialogs.getAddTextDia(it)
                            .show(childFragmentManager,"")
                    }
                }
                R.id.setVisible_book_menu_item -> {
                    customAlerts.showSetVisibilityNotesInsideBook(requireContext(),true){x,y->
                        viewModel.changeBookAttrVisibility(item.bookId,true)
                    }
                }
                R.id.setInVisible_book_menu_item -> {
                    customAlerts.showSetVisibilityNotesInsideBook(requireContext(),false){x,y->
                        viewModel.changeBookAttrVisibility(item.bookId,false)
                    }
                }
            }
        }

    }

    private fun setAddBookView(){
        binding.addBookItem.let { imageTextView->
            imageTextView.apply {
                imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_library_add_24))
                imageTextText.typeface = Typeface.DEFAULT_BOLD
                imageTextText.gravity = Gravity.CENTER
                imageTextText.text = getString(R.string.book_add_text)

                root.setOnClickListener {
                    ParameterAddTextItem(lastSearchedText,getString(R.string.enter_notebook_name_text),
                        false,lastAllBooks.map { it.name },INSERT_BOOK).also {
                        customGetDialogs.getAddTextDia(it)
                            .show(childFragmentManager,"")
                    }
                }
            }

        }
    }

    private fun setSearchingView(){
        binding.searchViewFromBook.searchVView.onActionViewExpanded()
        binding.searchViewFromBook.searchVView.clearFocus()

        var job: Job?=null
        binding.searchViewFromBook.searchVView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                job?.cancel()
                job=lifecycleScope.launch {
                    delay(300)
                    if(newText!=null&&newText.trim()!=""){
                        lastSearchedText=newText
                        bookAdapter.items=getSearchedBooks()
                    }else{
                        bookAdapter.items=lastAllBooks
                        lastSearchedText=""
                    }
                }
                return true
            }
        })
    }
    private fun setUnselectedNotesItemView(noteCount:Int){
        binding.unSelectedNotesItem.let { itemView->
            itemView.textUpperDTItem.text=getString(R.string.unselected_text)
            itemView.textDownDTItem.text=noteCount.toString()
            itemView.imageDTItem.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_library_books_24))
            itemView.root.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.default_color))

            itemView.root.setOnClickListener {
                val rootParameterNote=ParameterRootNote(null,RootNoteEmptyBookItem(),RootNoteFrom.EMPTY_BOOK_FROM_NOTE)
                navController.navigate(MainNavDirections.actionGlobalNoteFragment(rootParameterNote))
            }
        }

    }

    private fun getSearchedBooks():List<BookCountView>{
        val searchedBooks = mutableListOf<BookCountView>()
        lastAllBooks.forEach {
            if(it.name.lowercase().contains(lastSearchedText.lowercase()))
                searchedBooks.add(it)
        }
        return searchedBooks
    }
    private fun clearSearchView(){
        binding.searchViewFromBook.searchVView.clearFocus()
        binding.searchViewFromBook.searchVView.setQuery("",false)
        lastSearchedText=""
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(getString(R.string.notebook_text))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}