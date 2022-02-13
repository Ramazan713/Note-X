package com.masterplus.notex.roomdb.repos.abstraction

import androidx.paging.PagingSource
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.models.UltimateNote

interface IUltimateNoteRepo {

    fun getUltimateNoteASCWithParameter(orderNote: OrderNote,parameterRootNote: ParameterRootNote):
            PagingSource<Int, UltimateNote>

    fun getUltimateNoteDESCWithParameter(orderNote: OrderNote,parameterRootNote: ParameterRootNote):
            PagingSource<Int, UltimateNote>

    fun getUltimatePageNotesWithSearch(query:String): PagingSource<Int, UltimateNote>
}