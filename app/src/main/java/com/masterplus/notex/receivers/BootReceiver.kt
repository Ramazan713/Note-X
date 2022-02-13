package com.masterplus.notex.receivers

import android.content.Context
import android.content.Intent
import com.masterplus.notex.roomdb.repos.concrete.ReminderAlarmRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver:HiltBroadcastReceiver() {

    @Inject
    lateinit var externalScope:CoroutineScope

    @Inject
    lateinit var reminderAlarmRepo: ReminderAlarmRepo


    override fun onReceive(context: Context, receiveIntent: Intent) {
        super.onReceive(context, receiveIntent)

        if(receiveIntent.action==Intent.ACTION_BOOT_COMPLETED){
            externalScope.launch {
                reminderAlarmRepo.reloadReminderAlarms()
            }
        }

    }

}