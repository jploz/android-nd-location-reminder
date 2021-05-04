package com.udacity.locationreminder.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.locationreminder.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setupDatabaseAndRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun tearDownDatabase() {
        database.close()
    }

    private val reminders = listOf(
        ReminderDTO(
            title = "Title of reminder 1",
            description = "Description of reminder 1",
            location = "User defined location",
            latitude = 10.0,
            longitude = 10.0
        ),
        ReminderDTO(
            title = "Title of reminder 2",
            description = "Description of reminder 2",
            location = "User defined location",
            latitude = 20.0,
            longitude = 20.0
        ),
    )

    @Test
    fun getReminders_returnsCorrectData() = runBlocking {
        repository.saveReminder(reminders[0])
        repository.saveReminder(reminders[1])

        val actual = repository.getReminders() as Result.Success<List<ReminderDTO>>

        assertThat(actual.data.size, `is`(2))
        assertThat(actual.data, hasItem(reminders[0]))
        assertThat(actual.data, hasItem(reminders[1]))
    }

    @Test
    fun getReminders_noData_returnsNotNull() = runBlocking {
        val actual = repository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(actual, notNullValue())
        assertThat(actual.data.isEmpty(), `is`(true))
    }

    @Test
    fun getReminder_returnsCorrectData() = runBlocking {
        val reminder = reminders[0]
        repository.saveReminder(reminder)

        val actual = repository.getReminder(reminder.id) as Result.Success<ReminderDTO>

        assertThat(actual.data.id, `is`(reminder.id))
        assertThat(actual.data.title, `is`(reminder.title))
        assertThat(actual.data.description, `is`(reminder.description))
        assertThat(actual.data.location, `is`(reminder.location))
        assertThat(actual.data.latitude, `is`(reminder.latitude))
        assertThat(actual.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminder_noData_returnsError() = runBlocking {
        val reminder = reminders[0]
        val actual = repository.getReminder(reminder.id) as Result.Error

        assertThat(actual.message, notNullValue())
        assertThat(actual.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteAllReminders_returnsNoData() = runBlocking {
        repository.saveReminder(reminders[0])
        repository.saveReminder(reminders[1])

        val actual = repository.getReminders() as Result.Success<List<ReminderDTO>>

        assertThat(actual.data, notNullValue())
        assertThat(actual.data.size, `is`(2))
        assertThat(actual.data, hasItem(reminders[0]))
        assertThat(actual.data, hasItem(reminders[1]))

        repository.deleteAllReminders()
        val reminders = repository.getReminders() as Result.Success<List<ReminderDTO>>

        assertThat(reminders.data, notNullValue())
        assertThat(reminders.data.isEmpty(), `is`(true))
    }

    @Test
    fun deleteReminder_returnsNoData() = runBlocking {
        repository.saveReminder(reminders[0])
        repository.saveReminder(reminders[1])

        var actual = repository.getReminders() as Result.Success<List<ReminderDTO>>

        assertThat(actual.data, notNullValue())
        assertThat(actual.data.size, `is`(2))
        assertThat(actual.data, hasItem(reminders[0]))
        assertThat(actual.data, hasItem(reminders[1]))

        repository.deleteReminder(reminders[0].id)
        actual = repository.getReminders() as Result.Success<List<ReminderDTO>>

        assertThat(actual.data.size, `is`(1))
        assertThat(actual.data, hasItem(reminders[1]))
    }
}
