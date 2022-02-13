package com.masterplus.notex.roomdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.masterplus.notex.enums.ReminderTypes

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = false)
    var noteId:Long,
    var createdDate:String,
    var nextDate:String,
    var reminderType:ReminderTypes,
    var isCompleted:Boolean
)