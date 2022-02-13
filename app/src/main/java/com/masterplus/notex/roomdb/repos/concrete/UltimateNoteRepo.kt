package com.masterplus.notex.roomdb.repos.concrete

import androidx.paging.PagingSource
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.enums.RootNoteFrom
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.repos.abstraction.IUltimateNoteRepo
import com.masterplus.notex.roomdb.services.UltimateNoteDao
import javax.inject.Inject

class UltimateNoteRepo @Inject constructor(private val ultimateNoteDao: UltimateNoteDao):IUltimateNoteRepo {
    override fun getUltimateNoteASCWithParameter(
        orderNote: OrderNote,
        parameterRootNote: ParameterRootNote
    ): PagingSource<Int, UltimateNote> {
        val noteKinds=parameterRootNote.rooNoteItem.getNoteKinds()
        return when(parameterRootNote.rootNoteFrom){

            RootNoteFrom.DEFAULT_NOTE->ultimateNoteDao.getUltimatePageAllNotesASC(orderNote,noteKinds)

            RootNoteFrom.BOOK_FROM_NOTE->ultimateNoteDao
                .getUltimatePageNotesWithBookIdASC(parameterRootNote.parentId?:0,orderNote,noteKinds)

            RootNoteFrom.EMPTY_BOOK_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesEmptyBookASC(orderNote,noteKinds)

            RootNoteFrom.TAG_FROM_NOTE->ultimateNoteDao
                .getUltimatePageNotesWithTagIdASC(parameterRootNote.parentId?:0,orderNote,noteKinds)

            RootNoteFrom.TRASH_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesASC(orderNote,noteKinds)

            RootNoteFrom.ARCHIVE_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesASC(orderNote,noteKinds)

            RootNoteFrom.REMINDER_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesReminderASC(orderNote,noteKinds)
        }
    }

    override fun getUltimateNoteDESCWithParameter(
        orderNote: OrderNote,
        parameterRootNote: ParameterRootNote
    ): PagingSource<Int, UltimateNote> {
        val noteKinds=parameterRootNote.rooNoteItem.getNoteKinds()
        return when(parameterRootNote.rootNoteFrom){
            RootNoteFrom.DEFAULT_NOTE->ultimateNoteDao.getUltimatePageAllNotesDESC(orderNote,noteKinds)

            RootNoteFrom.BOOK_FROM_NOTE->ultimateNoteDao
                .getUltimatePageNotesWithBookIdDESC(parameterRootNote.parentId?:0,orderNote,noteKinds)

            RootNoteFrom.EMPTY_BOOK_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesEmptyBookDESC(orderNote,noteKinds)

            RootNoteFrom.TAG_FROM_NOTE->ultimateNoteDao
                .getUltimatePageNotesWithTagIdDESC(parameterRootNote.parentId?:0,orderNote,noteKinds)

            RootNoteFrom.TRASH_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesDESC(orderNote,noteKinds)

            RootNoteFrom.ARCHIVE_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesDESC(orderNote,noteKinds)

            RootNoteFrom.REMINDER_FROM_NOTE->ultimateNoteDao.getUltimatePageNotesReminderDESC(orderNote,noteKinds)
        }
    }

    override fun getUltimatePageNotesWithSearch(query: String): PagingSource<Int, UltimateNote> {
        return ultimateNoteDao.getUltimatePageNotesWithSearch(query)
    }

}