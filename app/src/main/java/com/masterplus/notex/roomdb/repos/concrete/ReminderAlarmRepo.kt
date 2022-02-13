package com.masterplus.notex.roomdb.repos.concrete

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.masterplus.notex.enums.ReminderTypes
import com.masterplus.notex.receivers.AlarmReceiver
import com.masterplus.notex.receivers.BootReceiver
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.repos.abstraction.IReminderAlarmRepo
import com.masterplus.notex.roomdb.services.ReminderDao
import com.masterplus.notex.utils.Utils
import java.util.*
import javax.inject.Inject

class ReminderAlarmRepo @Inject constructor(private val context: Context,
                                            private val alarmManager: AlarmManager,
                                            private val reminderDao: ReminderDao)
    :IReminderAlarmRepo {


    override suspend fun setReminderAlarm(reminder: Reminder, calendar: Calendar) {
        val alarmIntent: PendingIntent? = getPendingIntent(reminder.noteId,0)
        setAlarm(alarmIntent, calendar)
        checkEnabledForBootReceiver()
    }

    override suspend fun deleteReminderAlarm(reminder: Reminder) {
        getPendingIntent(reminder.noteId,PendingIntent.FLAG_NO_CREATE)?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        checkEnabledForBootReceiver()
    }

    override suspend fun deleteReminderAlarm(noteId: Long) {
        getPendingIntent(noteId,PendingIntent.FLAG_NO_CREATE)?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        checkEnabledForBootReceiver()
    }

    override suspend fun setReminderAlarmWithSafe(reminder: Reminder, calendar: Calendar) {
        deleteReminderAlarm(reminder)
        setReminderAlarm(reminder, calendar)
    }

    override suspend fun reloadAlarmIfReminderExists(reminder: Reminder,calendar: Calendar) {
        val pendingIntent=getPendingIntent(reminder.noteId,PendingIntent.FLAG_NO_CREATE)
        if(pendingIntent==null&&reminderDao.checkReminderExists(reminder.noteId)){
            setAlarm(pendingIntent,calendar)
        }
    }



    override suspend fun updateNextAlarm(reminder: Reminder) {
        deleteReminderAlarm(reminder)
        val alarmIntent = getPendingIntent(reminder.noteId,0)
        val calendar=Calendar.getInstance()
        Utils.getDateWithFormat(reminder.nextDate)?.let { date->
            val cal = Calendar.getInstance().apply { time=date }
            calendar.set(Calendar.HOUR,cal.get(Calendar.HOUR))
            calendar.set(Calendar.MINUTE,cal.get(Calendar.MINUTE))
        }
        when(reminder.reminderType){
            ReminderTypes.NOT_REPEATED->{
                reminderDao.updateCompletedReminder(true,reminder.noteId)
                deleteReminderAlarm(reminder)
                return
            }
            ReminderTypes.DAILY->{calendar.add(Calendar.DAY_OF_MONTH,1)}
            ReminderTypes.WEEKLY->{calendar.add(Calendar.DAY_OF_WEEK,7)}
            ReminderTypes.MONTHLY->{calendar.add(Calendar.MONTH,1)}
        }
        setAlarm(alarmIntent,calendar)
        reminderDao.updateNextDateReminder(Utils.getFormatDate(calendar.time),reminder.noteId)
    }

    override suspend fun insertOrUpdateNextAlarm(reminder: Reminder){
        if(reminder.isCompleted)
            return
        val nextDateCalender=Calendar.getInstance()
        val currentDate=Calendar.getInstance().time
        Utils.getDateWithFormat(reminder.nextDate)?.let { date->
            if(!currentDate.before(date)){
                updateNextAlarm(reminder)
            }else{
                val cal = Calendar.getInstance().apply { time=date }
                nextDateCalender.set(Calendar.HOUR,cal.get(Calendar.HOUR))
                nextDateCalender.set(Calendar.MINUTE,cal.get(Calendar.MINUTE))
                setReminderAlarmWithSafe(reminder,nextDateCalender)
            }
            checkEnabledForBootReceiver()
        }
    }

    override suspend fun reloadReminderAlarms() {
        reminderDao.getUnCompletedReminders().forEach { reminder->
            val pendingIntent = getPendingIntent(reminder.noteId,0)
            Utils.getDateWithFormat(reminder.nextDate)?.let { date->
                setAlarm(pendingIntent, Calendar.getInstance().apply { time=date })
            }
        }
        checkEnabledForBootReceiver()
    }


    private fun getPendingIntent(noteId:Long,flags:Int):PendingIntent?{
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("noteId",noteId)
            PendingIntent.getBroadcast(context, noteId.toInt(), intent, flags)
        }
    }
    private fun setAlarm(alarmIntent: PendingIntent?,calendar: Calendar){
        alarmManager.setInexactRepeating(AlarmManager.RTC,calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,alarmIntent)
    }

    private suspend fun checkEnabledForBootReceiver(){
        val activeAlarmCount = reminderDao.getActiveReminderCount()
        if(activeAlarmCount==0){
            setDisabledBootReceiver()
        }else{
            setEnabledBootReceiver()
        }
    }

    private fun setEnabledBootReceiver(){
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    private fun setDisabledBootReceiver(){
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }


}