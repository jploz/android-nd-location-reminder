package com.udacity.locationreminder.locationreminders

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.udacity.locationreminder.base.BaseViewModel
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

private const val TAG = "ReminderDescriptionViewModel"

class ReminderDescriptionViewModel(
    val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    /**
     * Delete reminder from the data source
     */
    fun deleteReminder(reminderData: ReminderDataItem?) {
        reminderData?.let {
            viewModelScope.launch {
                dataSource.deleteReminder(it.id)
            }
        }
    }
}
