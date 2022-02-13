package com.masterplus.notex.views.dialogFragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.adapters.PremiumAdapter
import com.masterplus.notex.adapters.ShowImageTextResourceAdapter
import com.masterplus.notex.databinding.FragmentPremiumDiaBinding
import com.masterplus.notex.models.ImageTextResourceObject
import com.masterplus.notex.models.PremiumObject
import com.masterplus.notex.viewmodels.BillingViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class PremiumDiaFr : CustomDialogFragment() {
    private var _binding:FragmentPremiumDiaBinding?=null
    private val binding get() = _binding!!
    private val viewModelBilling: BillingViewModel by viewModels(ownerProducer = {requireActivity()})
    private val adapterFeatures=ShowImageTextResourceAdapter()
    private val adapterPremiumItems=PremiumAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding= FragmentPremiumDiaBinding.inflate(inflater,container,false)
        setPremiumItemsRecycler()
        setFeaturesRecycler()
        buttonClicks()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


    private fun buttonClicks(){
        adapterPremiumItems.setListener {
            viewModelBilling.setPurchase(it.skuDetails,requireActivity())
        }
        binding.imageClosePre.setOnClickListener {
            dismiss()
        }

        viewModelBilling.isLoading.observe(viewLifecycleOwner, Observer {
            binding.recyclerSubsItemsPre.isGone = it
            binding.premiumProgressBar.isGone = !it
        })

    }


    private fun setPremiumItemsRecycler(){
        val daysPattern="\\d+D".toRegex()

        viewModelBilling.skuDetailsLive.observe(viewLifecycleOwner, Observer {
            val premiumItems= mutableListOf<PremiumObject>()
            it?.forEachIndexed { index, skuDetails ->
                val title=skuDetails.title.split("(${getString(R.string.app_name)})")[0]
                val days = daysPattern.find(skuDetails.freeTrialPeriod)?.value?.split("D")?.get(0)
                val description = if(days!=null)"$days ${getString(R.string.days_free_trial_text)}" else ""
                premiumItems.add(PremiumObject(index+1,title,skuDetails.price
                    , description,skuDetails))
            }
            adapterPremiumItems.items=premiumItems
        })
        binding.recyclerSubsItemsPre.apply {
            adapter=adapterPremiumItems
            layoutManager=LinearLayoutManager(requireContext())
        }
    }

    private fun setFeaturesRecycler(){
        val features= mutableListOf<ImageTextResourceObject>()
        features.add(ImageTextResourceObject(1,getString(R.string.ad_free_text),
            R.drawable.ic_baseline_done_24, R.drawable.circle_green
        ))
        adapterFeatures.items=features

        binding.recyclerFeaturesPre.apply {
            adapter=adapterFeatures
            layoutManager=LinearLayoutManager(requireContext())
        }


    }

}