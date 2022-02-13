package com.masterplus.notex.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import com.masterplus.notex.MainActivity
import com.masterplus.notex.R
import com.masterplus.notex.roomdb.entities.Reminder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

object Utils {


    @SuppressLint("ConstantLocale")
    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())

    @SuppressLint("ConstantLocale")
    private val dateFormatFile: DateFormat = SimpleDateFormat("yyyy-MM-dd HH.mm.ss",Locale.getDefault())

    @SuppressLint("ConstantLocale")
    private val onlyDateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd",Locale.getDefault())

    @SuppressLint("ConstantLocale")
    private val RCC3339DateFormat:DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ",
        Locale.getDefault())

    public fun getFormatDate(date:Date=Calendar.getInstance().time): String = dateFormat.format(date.time)
    public fun getFormatDateFile(date:Date=Calendar.getInstance().time): String = dateFormatFile.format(date.time)
    public fun getDateWithFormat(date:String):Date?=dateFormat.parse(date)
    public fun getOnlyDateFormat(date:Date=Calendar.getInstance().time):String = onlyDateFormat.format(date.time)
    fun getRFC3339FormatDate(date: Date=Calendar.getInstance().time):String = RCC3339DateFormat.format(date.time)

    fun getInsideNoteDateFormat(date: String):String{
        try{
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(date)?.let{
                return SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(it)
            }
        }catch (ex:Exception){ }
        return ""

    }

    fun getReminderSpanDateText(reminder: Reminder):
            SpannableStringBuilder {
        val text= getInsideNoteDateFormat(reminder.nextDate)
        val spanText= SpannableStringBuilder(text)
        if(reminder.isCompleted){
            spanText.setSpan(StrikethroughSpan(),0,text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spanText
    }

    fun setToolbarTitle(activity: Activity,title:String){
        if(activity is MainActivity){
            activity.binding.toolbarText.text=title
        }
    }

    fun changeToolBarColor(activity: Activity,color:Int){
        if(activity is MainActivity){
            activity.window.statusBarColor=color
            activity.binding.toolbar.setBackgroundColor(color)
            activity.binding.bannerAdd.setBackgroundColor(color)
        }
    }

    fun changeToolBarColorByDefault(activity: Activity){
        if(activity is MainActivity){
            val color= activity.getColor(R.color.default_background_color)
            changeToolBarColor(activity, color)
        }
    }
    fun getColorIntWithAddition(colorInt:Int,context: Context):Int{
        return colorInt-context.resources.getInteger(R.integer.color_addition_number)
    }
    fun getColorIntWithAddition(color:String,context: Context):Int{
        return Color.parseColor(color)-context.resources.getInteger(R.integer.color_addition_number)
    }
    fun getColorUIMode(context: Context,color: String):Int{
        return getColorUIMode(context, Color.parseColor(color))
    }
    fun getColorUIMode(context: Context,color: Int):Int{
        when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
            Configuration.UI_MODE_NIGHT_NO -> {
                return color
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                if (color==-1)//if color is white
                    return darken(context.getColor(R.color.default_color),0.3)
                return darken(color,.25)
            }
        }
        return color
    }
    private fun darken(color: Int, fraction: Double): Int {
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)
        red = darkenColor(red, fraction)
        green = darkenColor(green, fraction)
        blue = darkenColor(blue, fraction)
        val alpha = Color.alpha(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun darkenColor(color: Int, fraction: Double): Int {
        return max(color - color * fraction, 0.0).toInt()
    }

    fun isInternetConnectionAvailable(context: Context):Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false

        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun getFontSize(sharedPreferences: SharedPreferences):Int{
        return sharedPreferences.getInt("font_size_text",18)
    }


}