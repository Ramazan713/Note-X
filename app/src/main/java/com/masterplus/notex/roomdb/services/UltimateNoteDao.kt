package com.masterplus.notex.roomdb.services

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.roomdb.models.UltimateNote

@Dao
interface UltimateNoteDao {

    @Transaction
    @Query("""select * from noteView where kindNote in (:noteKinds) and allTypeVisible=1 order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end desc """)
    fun getUltimatePageAllNotesDESC(orderType: OrderNote, noteKinds: List<NoteKinds>)
            : PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where kindNote in (:noteKinds) and allTypeVisible=1 order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageAllNotesASC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where kindNote in (:noteKinds) order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end desc """)
    fun getUltimatePageNotesDESC(orderType: OrderNote, noteKinds: List<NoteKinds>)
            : PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where kindNote in (:noteKinds)  order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageNotesASC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView 
        where kindNote in (:noteKinds) and bookId=:bookId order by weight desc, 
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end desc""")
    fun getUltimatePageNotesWithBookIdDESC(bookId:Long, orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView 
        where kindNote in (:noteKinds) and bookId=:bookId order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageNotesWithBookIdASC(bookId:Long, orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>


    @Transaction
    @Query("""select nv.* from noteView nv inner join noteWithTags nwt on nwt.noteId=nv.noteId
        where nv.kindNote in (:noteKinds) and nwt.tagId=:tagId order by nv.weight desc,
        case when :orderType="AZ" then nv.title when :orderType="EDIT_TIME" then nv.updateDate else nv.color end desc""")
    fun getUltimatePageNotesWithTagIdDESC(tagId:Long, orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select nv.* from noteView nv inner join noteWithTags nwt on nwt.noteId=nv.noteId
        where nv.kindNote in (:noteKinds) and nwt.tagId=:tagId order by nv.weight desc,
        case when :orderType="AZ" then nv.title when :orderType="EDIT_TIME" then nv.updateDate else nv.color end asc""")
    fun getUltimatePageNotesWithTagIdASC(tagId:Long, orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>


    @Transaction
    @Query("""select * from noteView where bookId=0 and kindNote in (:noteKinds) order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end desc""")
    fun getUltimatePageNotesEmptyBookDESC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where bookId=0 and kindNote in (:noteKinds) order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageNotesEmptyBookASC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where reminderDone=0 and kindNote in (:noteKinds) order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageNotesReminderASC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView where reminderDone=0 and kindNote in (:noteKinds) order by weight desc,
        case when :orderType="AZ" then title when :orderType="EDIT_TIME" then updateDate else color end asc""")
    fun getUltimatePageNotesReminderDESC(orderType: OrderNote, noteKinds: List<NoteKinds>): PagingSource<Int, UltimateNote>

    @Transaction
    @Query("""select * from noteView nv inner join briefContents bc on bc.noteId=nv.noteId
        where lower(nv.title) like '%'||:query||'%' or lower(bc.text) like '%'||:query||'%' group by nv.noteId limit 37""")
    fun getUltimatePageNotesWithSearch(query:String): PagingSource<Int, UltimateNote>

}