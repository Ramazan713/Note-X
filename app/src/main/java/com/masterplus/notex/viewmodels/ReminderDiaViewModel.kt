package com.masterplus.notex.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.enums.ReminderTypes
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.repos.abstraction.IReminderAlarmRepo
import com.masterplus.notex.roomdb.repos.abstraction.IReminderRepo
import com.masterplus.notex.utils.Utils.getFormatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReminderDiaViewModel @Inject constructor(private val reminderRepo: IReminderRepo,
                                               private val reminderAlarmRepo: IReminderAlarmRepo,
                                               private val externalScope:CoroutineScope):BaseViewModel() {

    private val _reminder=MutableLiveData<Reminder?>()
    val reminder:LiveData<Reminder?> get() = _reminder



    fun loadReminderWithNoteId(noteId:Long){
        viewModelScope.launch {
            _reminder.value=reminderRepo.getReminderWithNoteId(noteId)
        }
    }

    fun removeReminderWithAlarmManager(noteId: Long){
        externalScope.launch {
            reminderRepo.deleteReminderWithNoteId(noteId)
            reminderAlarmRepo.deleteReminderAlarm(noteId)
        }
    }
    fun insertOrUpdateReminderWithAlarmManager(noteId: Long,calendar: Calendar,reminderTypes: ReminderTypes){
        externalScope.launch {
            val reminder = Reminder(noteId,getFormatDate(),getFormatDate(calendar.time),reminderTypes,false)
            reminderRepo.deleteReminder(reminder)
            reminderRepo.insertReminder(reminder)
            reminderAlarmRepo.setReminderAlarmWithSafe(reminder, calendar)

        }
    }

}