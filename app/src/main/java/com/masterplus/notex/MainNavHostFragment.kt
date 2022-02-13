package com.masterplus.notex

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import com.masterplus.notex.views.NoteFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavHostFragment:NavHostFragment() {
    @Inject
    lateinit var fragmentFactory: NoteFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}