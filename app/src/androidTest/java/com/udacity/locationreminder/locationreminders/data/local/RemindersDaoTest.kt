package com.udacity.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
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
    fun getReminders_returnsCorrectData() = runBlockingTest {
        database.reminderDao().saveReminder(reminders[0])
        database.reminderDao().saveReminder(reminders[1])

        val actual = database.reminderDao().getReminders()

        assertThat(actual.size, `is`(2))
        assertThat(actual, hasItem(reminders[0]))
        assertThat(actual, hasItem(reminders[1]))
    }

    @Test
    fun getReminders_noData_returnsNotNull() = runBlockingTest {
        val actual = database.reminderDao().getReminders()
        assertThat(actual, notNullValue())
        assertThat(actual.isEmpty(), `is`(true))
    }

    @Test
    fun getReminderById_returnsCorrectData() = runBlockingTest {
        val reminder = reminders[0]
        database.reminderDao().saveReminder(reminder)

        val actual = database.reminderDao().getReminderById(reminder.id)

        assertThat(actual?.id, `is`(reminder.id))
        assertThat(actual?.title, `is`(reminder.title))
        assertThat(actual?.description, `is`(reminder.description))
        assertThat(actual?.location, `is`(reminder.location))
        assertThat(actual?.latitude, `is`(reminder.latitude))
        assertThat(actual?.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders_returnsNoData() = runBlockingTest {
        database.reminderDao().saveReminder(reminders[0])
        database.reminderDao().saveReminder(reminders[1])

        var actual = database.reminderDao().getReminders()
        assertThat(actual.size, `is`(2))
        assertThat(actual, hasItem(reminders[0]))
        assertThat(actual, hasItem(reminders[1]))

        database.reminderDao().deleteAllReminders()

        actual = database.reminderDao().getReminders()
        assertThat(actual.isEmpty(), `is`(true))
    }

    @Test
    fun deleteReminderById_returnsNoData() = runBlockingTest {
        database.reminderDao().saveReminder(reminders[0])
        database.reminderDao().saveReminder(reminders[1])

        var actual = database.reminderDao().getReminders()
        assertThat(actual.size, `is`(2))
        assertThat(actual, hasItem(reminders[0]))
        assertThat(actual, hasItem(reminders[1]))

        database.reminderDao().deleteReminderById(reminders[0].id)

        actual = database.reminderDao().getReminders()
        assertThat(actual.size, `is`(1))
        assertThat(actual, hasItem(reminders[1]))
    }
}
