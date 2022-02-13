package com.masterplus.notex.views.bottomSheetDialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentAddTextBottomSheetDiaBinding
import com.masterplus.notex.models.AddTextItem
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.viewmodels.items.SetAddTextViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddTextBottomSheetDiaFragment : BottomSheetDialogFragment() {

    private var _binding:FragmentAddTextBottomSheetDiaBinding? = null
    private val binding get() = _binding!!
    private var isTextAvailable:Boolean=false

    private var isWritingContinue:Boolean=false

    private val viewModelSetItem: SetAddTextViewModel by viewModels({requireParentFragment()})
    private var parameterAddTextItem=ParameterAddTextItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentAddTextBottomSheetDiaBinding.inflate(layoutInflater,container,false)

        requireArguments().let { args->
            parameterAddTextItem=args.get("parameterAddText") as ParameterAddTextItem
        }
        binding.textTitleFromAddText.text=parameterAddTextItem.title
        parameterAddTextItem.contentText.also { content->
            if(content.trim()!=""){
                binding.addTextFromAddText.setText(content)
                searchTextAndSetWarnings(content)
            }
        }

        var job:Job? = null
        binding.addTextFromAddText.addTextChangedListener {
            job?.cancel()
            isWritingContinue=true
            job=lifecycleScope.launch {
                delay(300)
                if(it!=null){
                    searchTextAndSetWarnings(it.toString())
                }
                isWritingContinue=false
            }
        }
        binding.btCancelFromAddText.setOnClickListener {
            this.dismiss()
        }
        binding.btApprovedFromAddText.setOnClickListener {
            val text=binding.addTextFromAddText.text.toString().trim()
            if(text!=""&&isTextAvailable&&!isWritingContinue){
                parameterAddTextItem.apply {
                    viewModelSetItem.setItem(AddTextItem(text,isEdit,tag, editedId))
                }
                this.dismiss()
            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addTextFromAddText.requestFocus()
    }

    private fun searchTextAndSetWarnings(text:String){
        isTextAvailable=searchTextForIsAvailable(text)
        if(!isTextAvailable&&text!=""){
            binding.warningFromAddText.text=getText(R.string.already_exists_text)
        }else{
            binding.warningFromAddText.text=""
        }
    }

    private fun searchTextForIsAvailable(text:String):Boolean{
        if(text.trim()==""){
            return false
        }
        parameterAddTextItem.apply {
            if(!isEdit||isEdit&&text!=contentText.lowercase()){
                textListForSearching.forEach {
                    if(it.lowercase()==text){
                        return false
                    }
                }
                return true
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}