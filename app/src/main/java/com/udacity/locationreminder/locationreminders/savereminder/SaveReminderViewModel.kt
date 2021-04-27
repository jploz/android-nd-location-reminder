package com.udacity.locationreminder.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseViewModel
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

private const val TAG = "SaveReminderViewModel"

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    fun setSelectedLocation(poi: PointOfInterest?, latLng: LatLng?) {
        selectedPOI.value = poi
        latitude.value = latLng?.latitude
        longitude.value = latLng?.longitude
        if (poi != null) {
            reminderSelectedLocationStr.value = poi.name
        } else {
            reminderSelectedLocationStr.value = "User defined location"
        }
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(): ReminderDataItem? {
        val reminderData = ReminderDataItem(
            reminderTitle.value,
            reminderDescription.value,
            reminderSelectedLocationStr.value,
            latitude.value,
            longitude.value
        )
        Log.d(
            TAG,
            "Title: $reminderTitle.value, descr: $reminderDescription.value, " +
                    "location: $reminderSelectedLocationStr.value, " +
                    "lat/long: ${latitude.value}/${longitude.value}"
        )
        if (validateEnteredData(reminderData)) {
            // in order to start fresh after a reminder was saved, clear the view model
            saveReminder(reminderData)
            onClear()
            return reminderData
        }
        return null
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
        }
        showLoading.value = false
        showToast.value = app.getString(R.string.reminder_saved)
        navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}
