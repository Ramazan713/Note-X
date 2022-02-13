package com.masterplus.notex.views.bottomSheetDialogFragments

import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.adapters.CopyMoveNoteAdapter
import com.masterplus.notex.databinding.FragmentCopyMoveNoteBottomSheetDiaBinding
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.models.ReturnCopyMoveItem
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.models.copymove.CopyMoveObject
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.viewmodels.CopyMoveViewModel
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.masterplus.notex.viewmodels.items.SetCopyMoveItemViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CopyMoveBottomSheetDia: BottomSheetDialogFragment() {

    private var _binding:FragmentCopyMoveNoteBottomSheetDiaBinding? = null
    private val binding get() = _binding!!
    private var lastItems:List<CopyMoveItem> = listOf()
    private var selectedItem:CopyMoveItem? = null

    private val viewModel:CopyMoveViewModel by viewModels()
    private val viewModelAddText: SetAddTextViewModel by viewModels()
    private val viewModelListener: SetCopyMoveItemViewModel by viewModels({parentFragment?:requireActivity()})

    private val recyclerAdapter = CopyMoveNoteAdapter()
    private lateinit var copyMoveObject: CopyMoveObject

    @Inject
    lateinit var showMessage: ShowMessage

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val recyclerState=savedInstanceState?.getParcelable<Parcelable>("recyclerState")
        binding.recyclerCMNote.layoutManager?.onRestoreInstanceState(recyclerState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.stateSelectedCopMoveItem.value=recyclerAdapter.selectedItem
        val recyclerState=binding.recyclerCMNote.layoutManager?.onSaveInstanceState()
        outState.putParcelable("recyclerState",recyclerState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCopyMoveNoteBottomSheetDiaBinding.inflate(layoutInflater,container,false)

        requireArguments().let { args->
            copyMoveObject = args.getSerializable("copyMoveObject") as CopyMoveObject
            copyMoveObject.loadItems(viewModel,viewLifecycleOwner)
        }


        recyclerAdapter.setInit(adapterListener,copyMoveObject.getFirstImageId(),copyMoveObject.getLastImageId())

        binding.recyclerCMNote.adapter=recyclerAdapter
        binding.recyclerCMNote.layoutManager=LinearLayoutManager(requireContext())

        setObservers()
        setButtons()
        setAddItemViews()
        return binding.root
    }


    private fun setObservers(){
        viewModel.liveItems.observe(viewLifecycleOwner, Observer {
            lastItems=it
            recyclerAdapter.items=it

            reloadForConfiguration()
            binding.warningCMNote.let { warningView->
                warningView.isVisible=it.isEmpty()
                warningView.text=copyMoveObject.getEmptyListMessage()
            }
        })
        viewModel.isCompleted.observe(viewLifecycleOwner, Observer {
            viewModelListener.setItem(ReturnCopyMoveItem(copyMoveObject.isMove))
            showMessage.showLong(getString(R.string.success_text))
            dismiss()
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModelAddText.liveItem.collect {
                        copyMoveObject.insertItem(viewModel,it.approvedText)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleCMNote.text = copyMoveObject.getTitle()

    }

    private fun reloadForConfiguration(){
        selectedItem=viewModel.stateSelectedCopMoveItem.value
        recyclerAdapter.selectedItem=selectedItem
    }


    private fun setButtons(){
        binding.btCancelCMNote.setOnClickListener {
            dismiss()
        }
        binding.btApproveCMNote.setOnClickListener {
            if(selectedItem!=null){
                copyMoveObject.update(viewModel, selectedItem!!)
            }
        }
    }
    private fun setAddItemViews(){
        binding.addItemFromMoveCopy.root.isVisible=copyMoveObject.isAddItemsAllowed

        if(copyMoveObject.isAddItemsAllowed){
            binding.addItemFromMoveCopy.imageTextText.let { textView ->
                textView.text=getString(R.string.add_text)
                textView.gravity=Gravity.CENTER
                val params=LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
                textView.layoutParams=params
            }
            binding.addItemFromMoveCopy.imageTextImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_add_circle_24))
            binding.addItemFromMoveCopy.root.setOnClickListener {
                ParameterAddTextItem("",copyMoveObject.getAddTextTitle(),
                    false,lastItems.map { it.name }).also {parameterAddText->
                    customGetDialogs.getAddTextDia(parameterAddText)
                        .show(childFragmentManager,"")
                }
            }
        }
    }

    private val adapterListener=object:CopyMoveNoteAdapter.CopyMoveNoteListener{
        override fun selectedItem(item: CopyMoveItem) {
            selectedItem=item
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}