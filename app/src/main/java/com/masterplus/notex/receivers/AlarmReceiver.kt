package com.masterplus.notex.receivers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.masterplus.notex.MainActivity
import com.masterplus.notex.R
import com.masterplus.notex.roomdb.repos.abstraction.IReminderRepo
import com.masterplus.notex.roomdb.repos.concrete.ReminderAlarmRepo
import com.masterplus.notex.roomdb.services.ReminderDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AlarmReceiver:HiltBroadcastReceiver() {

    @Inject
    lateinit var externalScope:CoroutineScope

    @Inject
    lateinit var reminderRepo: IReminderRepo

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var reminderAlarmRepo: ReminderAlarmRepo


    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context, receiveIntent: Intent) {
        super.onReceive(context, receiveIntent)

        externalScope.launch {
            val noteId:Long=receiveIntent.getLongExtra("noteId",0L)
            reminderRepo.getNoteReminderWithNoteId(noteId)?.let { noteReminder ->

                reminderAlarmRepo.updateNextAlarm(noteReminder.reminder)

                val intent = Intent(context, MainActivity::class.java)
                intent.action=context.packageName+"/alarm"
                intent.putExtra("noteType",noteReminder.note.typeContent)
                intent.putExtra("noteId",noteId)
                intent.putExtra("extra_name","extra_value")


                val pendingIntent: PendingIntent = PendingIntent.getActivity(context, noteId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder= NotificationCompat.Builder(context,"19")
                    .setSmallIcon(R.drawable.ic_baseline_add_circle_24)
                    .setContentTitle(noteReminder.note.title)
                    .setContentText(noteReminder.reminder.nextDate)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("19", "note", NotificationManager.IMPORTANCE_DEFAULT)
                    // Register the channel with the system
                    notificationManager.createNotificationChannel(channel)
                }
                notificationManager.notify(noteId.toInt(),builder.build())
            }
        }
    }
}