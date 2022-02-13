package com.masterplus.notex.roomdb.models.backups

import com.masterplus.notex.enums.ReminderTypes
import java.io.Serializable

data class BackupReminder(
    var createdDate:String,
    var nextDate:String,
    var reminderType: ReminderTypes,
    var isCompleted:Boolean
):Serializable
