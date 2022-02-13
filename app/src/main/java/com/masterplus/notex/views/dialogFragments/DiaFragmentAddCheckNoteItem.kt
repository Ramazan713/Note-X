package com.masterplus.notex.views.dialogFragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDiaAddCheckNoteItemBinding
import com.masterplus.notex.models.AddedCheckNoteItem
import com.masterplus.notex.models.EditedCheckNoteItem
import com.masterplus.notex.models.ParameterAddCheckItem
import com.masterplus.notex.viewmodels.items.AddCheckItemViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiaFragmentAddCheckNoteItem : CustomDialogFragment() {
    private var _binding:FragmentDiaAddCheckNoteItemBinding? = null
    private val binding get() = _binding!!
    private var isEdit:Boolean=false
    private var inputText:String=""
    private var itemPos:Int=0
    private val sharedIsFirstKey="isFirstAddCheckNoteItem"
    private val viewModel: AddCheckItemViewModel by viewModels({requireParentFragment()})

    @Inject
    lateinit var imm: InputMethodManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiaAddCheckNoteItemBinding.inflate(inflater,container,false)

        requireArguments().let { args->
            val parameter=args.getSerializable("parameterCheckItem") as ParameterAddCheckItem
            isEdit=parameter.isEdit
            inputText=parameter.inputText
            itemPos=parameter.pos
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editCheckNoteItem.let { editText ->
            editText.post {
                showSoftKeyboard()
                editText.requestFocus()
            }
        }
        setAppearanceForEditState()
        setRadioViews()
        setButtonViews()
    }

    private fun setAppearanceForEditState(){
        binding.radioGrCheckNoteItem.isVisible = !isEdit
        binding.titleCheckNoteItem.text = if(isEdit)getString(R.string.edit_text) else getString(R.string.add_text)
        binding.btNextCheckNoteItem.isVisible = !isEdit
        if(isEdit)
            binding.editCheckNoteItem.setText(inputText)

    }
    private fun setRadioViews(){
        val isFirst=sharedPreferences.getBoolean(sharedIsFirstKey,true)
        binding.radioGrCheckNoteItem.check(if (isFirst) R.id.radioAddFirstCheckNoteItem else R.id.radioAddLastCheckNoteItem)

        binding.radioGrCheckNoteItem.setOnCheckedChangeListener { group, checkedId ->
            sharedPreferences.edit().putBoolean(sharedIsFirstKey,checkedId == R.id.radioAddFirstCheckNoteItem).apply()
        }
    }
    private fun setButtonViews(){
        binding.btCancelCheckNoteItem.setOnClickListener {
            dismiss()
        }
        binding.btApprovedCheckNoteItem.setOnClickListener {
            val value = binding.editCheckNoteItem.text.trim().toString()
            if(value!=""){
                if(isEdit)
                    viewModel.setSentEditedCheckItem(EditedCheckNoteItem(value,itemPos))
                else
                    viewModel.setSendAddedCheckItem(AddedCheckNoteItem(value,sharedPreferences.getBoolean(sharedIsFirstKey,true)))
            }
            dismiss()
        }
        binding.btNextCheckNoteItem.setOnClickListener {
            val value = binding.editCheckNoteItem.text.trim().toString()
            if(value!=""){
                viewModel.setSendAddedCheckItem(AddedCheckNoteItem(value,sharedPreferences.getBoolean(sharedIsFirstKey,true)))
                binding.editCheckNoteItem.setText("")
            }
            else
                dismiss()
        }
    }
    private fun showSoftKeyboard(){
        imm.showSoftInput(binding.editCheckNoteItem,0)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}