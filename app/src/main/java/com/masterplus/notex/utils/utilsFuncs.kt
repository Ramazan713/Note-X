package com.masterplus.notex.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.StrikethroughSpan
import android.widget.TextView
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.views.ContentBriefView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

public fun (TextView).showTags(tags:List<Tag>):TextView{
    this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_label_24,0,0,0)
    this.text = tags.joinToString()
    return this
}

inline fun <reified T:Any> (T?).deepCopyNullable():T?{
    return Gson().fromJson(Gson().toJson(this),object: TypeToken<T>() {}.type)
}
inline fun <reified T:Any> (T).deepCopy():T{
    return Gson().fromJson(Gson().toJson(this),object: TypeToken<T>() {}.type)
}

public fun (TextView).showTime(dateStr:String): TextView {
    val stringFormat= SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val newDate=stringFormat.parse(dateStr)
    newDate?.let {dt->
        val date= Calendar.getInstance().also { it.time=dt }
        val currentDate= Calendar.getInstance()
        when {
            currentDate.get(Calendar.DAY_OF_MONTH)==date.get(Calendar.DAY_OF_MONTH) -> {
                this.text= SimpleDateFormat("HH:mm", Locale.getDefault()).format(date.time)
            }
            currentDate.get(Calendar.YEAR)==date.get(Calendar.YEAR)-> {
                this.text= SimpleDateFormat("d MMM", Locale.getDefault()).format(date.time)
            }
            else -> {
                this.text= SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date.time)
            }
        }
    }
    return this
}

public fun getHighLightedText(realText:String,searchedText: String): SpannableStringBuilder {
    val sb= SpannableStringBuilder(realText)
    val p= Pattern.compile(searchedText, Pattern.CASE_INSENSITIVE)
    val matcher=p.matcher(realText)
    while(matcher.find()){
        sb.setSpan(BackgroundColorSpan(Color.YELLOW),matcher.start(),matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }
    return sb
}


@SuppressLint("SetTextI18n")
public fun (TextView).showShortContents(contents:List<ContentBriefView>, typeNote:NoteType, size:Int,isHalf:Boolean
                                        ,searchedText:String=""):TextView{
    if(typeNote==NoteType.Text){
        if(contents.isNotEmpty()){
            var text=contents[0].text.trimEnd()
            val maxSize:Int=if(isHalf)149 else 299
            var textSize:Int = text.length
            val newLinesArray=text.split("\n")
            if(newLinesArray.size>=10){
                text=newLinesArray.subList(0,10).joinToString("\n")
                text+="..."
            }else{
                if(textSize>=maxSize)
                    text+="..."
            }
            textSize=text.length

            text.substring(0, min(textSize,maxSize)).let {
                var subText=it
                if(subText.length>=maxSize && !subText.endsWith("..."))
                    subText+="..."
                this.text= if(searchedText!="") getHighLightedText(subText,searchedText) else subText
            }
            this.paintFlags=if(contents[0].isCheck) Paint.STRIKE_THRU_TEXT_FLAG else 0
        }

    }else{

        var spanText=SpannableStringBuilder("")
        val maxSize:Int=if(isHalf)49 else 99
        var start:Int=0
        var textSize:Int=0
        var minSize:Int=0
        for(x in contents.indices){
            if(x>=5)
                break
            val it=contents[x]
            start=spanText.length
            textSize=it.text.length
            minSize= min(textSize,maxSize)
            spanText.append("•",it.text.substring(0,minSize), if(textSize>=maxSize) "...\n" else "\n" )
            if(it.isCheck){
                spanText.setSpan(StrikethroughSpan(),start+1,spanText.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        when {
            size>5 -> {
                spanText.append("... +${size-5}")
            }
            size!=0 -> {
                spanText.replace(spanText.length-1,spanText.length," ")
            }
            else -> {
                spanText=SpannableStringBuilder(" ")
            }
        }
        spanText.let { text->
            this.text= if(searchedText!="") getHighLightedText(text.toString(),searchedText) else text
        }

    }
    return this
}