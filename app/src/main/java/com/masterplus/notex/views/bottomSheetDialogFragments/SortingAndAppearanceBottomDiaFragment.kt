package com.masterplus.notex.views.bottomSheetDialogFragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.adapters.MultiCheckAdapter
import com.masterplus.notex.adapters.SelectImageTextAdapter
import com.masterplus.notex.databinding.FragmentSortingAndAppearanceDiaBinding
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.models.CheckTextObject
import com.masterplus.notex.models.ImageTextObject
import com.masterplus.notex.viewmodels.SortingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SortingAndAppearanceBottomDiaFragment() : BottomSheetDialogFragment() {

    private var _binding:FragmentSortingAndAppearanceDiaBinding? = null
    private val binding get() = _binding!!

    private lateinit var sortingAdapter:SelectImageTextAdapter
    private lateinit var appearanceAdapter:MultiCheckAdapter

    private val viewModel: SortingViewModel by viewModels({requireParentFragment()})

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val orderNotes=OrderNote.values()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSortingAndAppearanceDiaBinding.inflate(layoutInflater,container,false)
        sortingAdapter = SelectImageTextAdapter()
        appearanceAdapter = MultiCheckAdapter()


        setSortingViews()
        setAppearanceViews()
        setSpinViews()

        binding.imageExitSorting.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun setSpinViews(){
        val spinItems = arrayListOf<String>(getString(R.string.descending_text),getString(R.string.ascending_text))

        val spinAdapter=ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,spinItems)
        binding.spinnerSorting.adapter=spinAdapter
        binding.spinnerSorting.setSelection(if(sharedPreferences.getBoolean("isDescendingOrder",true)) 0 else 1)

        binding.spinnerSorting.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putBoolean("isDescendingOrder",position==0).apply()
                viewModel.setIsSortingChange(true)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setAppearanceViews(){
        binding.recyclerAppearance.adapter=appearanceAdapter
        binding.recyclerAppearance.layoutManager=LinearLayoutManager(requireContext())

        val items= mutableListOf<CheckTextObject>()
        items.add(CheckTextObject(getString(R.string.show_content_text),1,sharedPreferences.getBoolean("isContentsVisible",true)))
        items.add(CheckTextObject(getString(R.string.show_tags_text),2,sharedPreferences.getBoolean("isTagsVisible",true)))

        appearanceAdapter.setListener(appearanceListener)
        appearanceAdapter.items=items
    }

    private fun setSortingViews(){
        binding.recyclerSorting.adapter=sortingAdapter
        binding.recyclerSorting.layoutManager=LinearLayoutManager(requireContext())

        val sortingItems= mutableListOf<ImageTextObject>()
        sortingItems.add(
            ImageTextObject(getString(R.string.by_edit_time_text),
                R.drawable.ic_baseline_access_time_24,
                OrderNote.EDIT_TIME.ordinal)
        )
        sortingItems.add(
            ImageTextObject(getString(R.string.by_alphabetically_text),
                R.drawable.ic_baseline_sort_by_alpha_24,
                OrderNote.AZ.ordinal)
        )
        sortingItems.add(
            ImageTextObject(getString(R.string.by_color_text), R.drawable.ic_baseline_color_lens_24,
                OrderNote.COLOR.ordinal)
        )
        sortingAdapter.items=sortingItems
        sortingAdapter.setListener(sortingListener)

        val selectedOrderNote=OrderNote.valueOf(sharedPreferences.getString("orderNote",null)?:OrderNote.EDIT_TIME.toString())
        sortingAdapter.setSelection(true,selectedOrderNote.ordinal)
    }


    private val sortingListener=object:SelectImageTextAdapter.SelectImageTextListener{
        override fun getSelectedItem(item: ImageTextObject) {
            sharedPreferences.edit().putString("orderNote",orderNotes[item.id].toString()).apply()
            viewModel.setIsSortingChange(true)
        }

    }

    private val appearanceListener=object:MultiCheckAdapter.MultiCheckAdapterListener{
        override fun clickItem(checkTextObject: CheckTextObject) {
            if(checkTextObject.id==1){
                sharedPreferences.edit().putBoolean("isContentsVisible",checkTextObject.isCheck).apply()
            }else if(checkTextObject.id==2){
                sharedPreferences.edit().putBoolean("isTagsVisible",checkTextObject.isCheck).apply()
            }
            viewModel.setIsAppearanceChange(true)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}