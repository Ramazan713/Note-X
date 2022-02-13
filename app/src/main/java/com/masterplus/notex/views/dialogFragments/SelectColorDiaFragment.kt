package com.masterplus.notex.views.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.adapters.SelectColorAdapter
import com.masterplus.notex.databinding.FragmentSelectColorDiaBinding
import com.masterplus.notex.models.ColorItem
import com.masterplus.notex.utils.Utils
import com.masterplus.notex.viewmodels.items.DestroyListenerViewModel
import com.masterplus.notex.viewmodels.items.SetSelectColorItemViewModel

class SelectColorDiaFragment : DialogFragment() {

    private var _binding:FragmentSelectColorDiaBinding?=null
    private val binding get() = _binding!!

    private val recyclerAdapter=SelectColorAdapter()
    private var selectedColor: ColorItem?=null

    private val viewModelSetItem: SetSelectColorItemViewModel by viewModels({requireParentFragment()})
    private val viewModelDestroyListener: DestroyListenerViewModel by viewModels({requireParentFragment()})

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedColor",selectedColor?.rawColor)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSelectColorDiaBinding.inflate(inflater,container,false)
        requireArguments().let { args->
            val color = args.getString("selectedColor","#FFFFFF")
            selectedColor= ColorItem(color, Utils.getColorUIMode(requireContext(),color))
        }

        if(savedInstanceState?.getString("selectedColor") != null){
            val color=savedInstanceState.getString("selectedColor")
            selectedColor= color?.let { ColorItem(it, Utils.getColorUIMode(requireContext(),color)) }
        }

        binding.recyckerSelectColor.apply {
            adapter=recyclerAdapter
            layoutManager=GridLayoutManager(requireContext(),5,GridLayoutManager.VERTICAL,false)
        }
        binding.btApprovedSelectColor.setOnClickListener {
            dismiss()
        }

        val colors=resources.getStringArray(R.array.colors).toList()
        recyclerAdapter.items=colors.map { ColorItem(it, Utils.getColorUIMode(requireContext(),it)) }

        recyclerAdapter.setDefaultColorAndListener(adapterListener, selectedColor)

        val selectedPos=colors.indexOf(selectedColor?.rawColor)
        if(selectedPos>=0)
            binding.recyckerSelectColor.smoothScrollToPosition(selectedPos)

        return binding.root
    }

    private val adapterListener = object:SelectColorAdapter.SelectColorListener{
        override fun getSelectedColor(color: ColorItem) {
            selectedColor=color
            viewModelSetItem.setItem(color.rawColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        viewModelDestroyListener.setIsClosed(true)
    }

}