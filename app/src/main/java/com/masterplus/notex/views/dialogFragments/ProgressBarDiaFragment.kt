package com.masterplus.notex.views.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterplus.notex.databinding.FragmentProgressBarDiaBinding
import com.masterplus.notex.views.view.CustomDialogFragment


class ProgressBarDiaFragment : CustomDialogFragment() {


    private var _binding:FragmentProgressBarDiaBinding?=null
    private val binding get() = _binding!!
    private lateinit var title:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState!=null)
            dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentProgressBarDiaBinding.inflate(layoutInflater,container,false)
        this.isCancelable=false
        requireArguments().let { args->
            title=args.getString("title","MyTitle")
        }

        binding.titleProgressFromDia.text=title

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


}