package com.masterplus.notex.views.view

import androidx.fragment.app.Fragment
import com.masterplus.notex.utils.Utils

open class CustomFragment:Fragment {
    constructor(contentLayoutId:Int):super(contentLayoutId)
    constructor():super()



    protected fun setToolbarTitle(title:String){
        Utils.setToolbarTitle(requireActivity(),title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setToolbarTitle("")
        Utils.changeToolBarColorByDefault(requireActivity())
    }
}