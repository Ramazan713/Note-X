package com.masterplus.notex.designpatterns.factory

import com.masterplus.notex.databinding.FragmentDisplayCheckListNoteBinding
import com.masterplus.notex.databinding.FragmentDisplayTextNoteBinding
import com.masterplus.notex.designpatterns.state.*
import com.masterplus.notex.enums.NoteKinds

class NoteStateFactory {

    companion object{
        fun getTextNoteState(noteKind: NoteKinds,context: TextNoteEditorContext):NoteStateAbstract<FragmentDisplayTextNoteBinding>{
            return when(noteKind){
                NoteKinds.TRASH_KIND->TextNoteTrashState(context)
                NoteKinds.ARCHIVE_KIND->TextNoteArchiveKindState(context)
                else->TextNoteAllKindState(context)
            }
        }
        fun getCheckNoteState(noteKind: NoteKinds,context: CheckNoteEditorContext):NoteStateAbstract<FragmentDisplayCheckListNoteBinding>{
            return when(noteKind){
                NoteKinds.TRASH_KIND->CheckNoteTrashState(context)
                NoteKinds.ARCHIVE_KIND->CheckNoteArchiveKindState(context)
                else->CheckNoteAllKindState(context)
            }
        }


    }

}