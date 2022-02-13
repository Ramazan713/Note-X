package com.masterplus.notex.views.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.MainNavDirections
import com.masterplus.notex.R
import com.masterplus.notex.adapters.SelectImageTextAdapter
import com.masterplus.notex.databinding.FragmentSelectTypeForNoteDiaBinding
import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.models.ImageTextObject
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.views.view.CustomDialogFragment


class SelectTypeForNoteDiaFragment : CustomDialogFragment() {

    private var _binding:FragmentSelectTypeForNoteDiaBinding?=null
    private val binding get() = _binding!!
    private lateinit var recyclerAdapter:SelectImageTextAdapter
    private lateinit var navController:NavController
    private lateinit var noteParameter: ParameterNote

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSelectTypeForNoteDiaBinding.inflate(layoutInflater,container,false)

        requireArguments().let { args->
            noteParameter = args.getSerializable("noteParameter") as ParameterNote
        }


        navController=Navigation.findNavController(requireActivity(), R.id.frames)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerAdapter= SelectImageTextAdapter()
        recyclerAdapter.setListener(adapterListener)

        binding.recyclerSelectTypeNote.apply {
            adapter=recyclerAdapter
            layoutManager=LinearLayoutManager(requireContext())
        }

        val item1= ImageTextObject(
            getString(R.string.checkList_text),
            R.drawable.ic_baseline_check_box_24,
            NoteType.CheckList.ordinal
        )
        val item2= ImageTextObject(
            getString(R.string.text_text),
            R.drawable.ic_baseline_note_add_24,
            NoteType.Text.ordinal
        )
        recyclerAdapter.items= arrayListOf(item1,item2)
    }

    private val adapterListener=object:SelectImageTextAdapter.SelectImageTextListener{
        override fun getSelectedItem(item: ImageTextObject) {
            when(item.id){

                NoteType.Text.ordinal->navController.navigate(MainNavDirections.actionGlobalDisplayTextNoteFragment(noteParameter))
                NoteType.CheckList.ordinal->navController.navigate(MainNavDirections.actionGlobalDisplayCheckListNoteFragment(noteParameter))
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}