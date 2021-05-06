package com.udacity.locationreminder.locationreminders.data

import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        shouldReturnError = shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("No reminders found")
        } else {
            Result.Success(ArrayList(reminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) {
            Result.Error("Reminder with id = $id not found")
        } else {
            val reminder = reminders.find { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder with id = $id not found")
            }
        }
    }

    override suspend fun deleteReminder(id: String) {
        val reminder = reminders.find { it.id == id }
        if (reminder != null) {
            reminders.remove(reminder)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
