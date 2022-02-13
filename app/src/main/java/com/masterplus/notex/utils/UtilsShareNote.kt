package com.masterplus.notex.utils

import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.roomdb.entities.ContentNote

object UtilsShareNote {

    fun transformTextToContentNotes(extraText:String):Pair<NoteType,List<ContentNote>>{
        val pattern="\n?\\[.]".toRegex()
        val patternForType="^\\[.]".toRegex()
        val noteType=if(patternForType.find(extraText)!=null)NoteType.CheckList else NoteType.Text
        if(noteType==NoteType.Text)
            return Pair(NoteType.Text, listOf(ContentNote(_text = extraText.trimEnd())))

        val delimiters=pattern.findAll(extraText).map { it.value }.toList()
        val contentNotes= mutableListOf<ContentNote>()

        extraText.split(pattern).let {
            it.subList(1,it.size).forEachIndexed { index, text->
                val isCheck=delimiters[index].contains("x")
                contentNotes.add(ContentNote(_text = text.trim(),isCheck = isCheck))
            }
        }
        return Pair(NoteType.CheckList, contentNotes)
    }

    fun transformContentNotesToText(contentNotes:List<ContentNote>,noteType: NoteType):String{
        return when(noteType){
            NoteType.Text->{contentNotes[0].text}
            NoteType.CheckList->{
                var text=""
                contentNotes.forEach {
                    val prefix=if(it.isCheck)"[x] " else "[ ] "
                    text+="${prefix}${it.text}\n"
                }
                text
            }
        }
    }


}