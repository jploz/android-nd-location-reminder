package com.udacity.locationreminder.locationreminders.savereminder

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.R
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeDataSource
import com.udacity.locationreminder.locationreminders.data.dto.Result
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: FakeDataSource
    private lateinit var appContext: Context

    // Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        repository = FakeDataSource()
        appContext = ApplicationProvider.getApplicationContext()
        viewModel = SaveReminderViewModel(appContext as Application, repository)
        // ensure clean data for tests
        runBlocking {
            repository.deleteAllReminders()
        }
        viewModel.onClear()
    }

    @After
    fun tearDownViewModel() {
        // ensure clean data for tests
        runBlocking {
            repository.deleteAllReminders()
        }
        viewModel.onClear()

        // Koin instance must be stopped between tests
        stopKoin()
    }

    private fun setCompleteReminder() {
        viewModel.reminderTitle.value = "Title of reminder"
        viewModel.reminderDescription.value = "Description of reminder"
        viewModel.reminderSelectedLocationStr.value = "User defined location"
        viewModel.selectedPOI.value = null
        viewModel.latitude.value = 10.0
        viewModel.longitude.value = 10.0
    }

    @Test
    fun validateAndSaveReminder_missingTitle_returnsNull() = runBlockingTest {
        setCompleteReminder()
        viewModel.reminderTitle.value = null
        assertThat(viewModel.validateAndSaveReminder(), `is`(nullValue()))
    }

    @Test
    fun validateAndSaveReminder_missingDescription_returnsDataItem() = runBlockingTest {
        setCompleteReminder()
        viewModel.reminderDescription.value = null
        val expected = ReminderDataItem(
            title = "Title of reminder",
            description = null,
            location = "User defined location",
            latitude = 10.0,
            longitude = 10.0
        )
        val result = viewModel.validateAndSaveReminder()
        assertThat(result?.title, `is`(expected.title))
        assertThat(result?.description, `is`(expected.description))
        assertThat(result?.location, `is`(expected.location))
        assertThat(result?.latitude, `is`(expected.latitude))
        assertThat(result?.longitude, `is`(expected.longitude))
    }

    @Test
    fun validateAndSaveReminder_complete_returnsDataItem() = runBlockingTest {
        setCompleteReminder()
        val expected = ReminderDataItem(
            title = "Title of reminder",
            description = "Description of reminder",
            location = "User defined location",
            latitude = 10.0,
            longitude = 10.0
        )
        val result = viewModel.validateAndSaveReminder()
        assertThat(result?.title, `is`(expected.title))
        assertThat(result?.description, `is`(expected.description))
        assertThat(result?.location, `is`(expected.location))
        assertThat(result?.latitude, `is`(expected.latitude))
        assertThat(result?.longitude, `is`(expected.longitude))
    }

    @Test
    fun validateAndSaveReminder_missingLocation_returnsNull() = runBlockingTest {
        setCompleteReminder()
        viewModel.reminderSelectedLocationStr.value = null
        assertThat(viewModel.validateAndSaveReminder(), `is`(nullValue()))
    }

    @Test
    fun validateAndSaveReminder_missingTitle_triggersSnackBarErrorMessage() = runBlockingTest {
        setCompleteReminder()
        viewModel.reminderTitle.value = null
        viewModel.validateAndSaveReminder()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateAndSaveReminder_missingLocation_triggersSnackBarErrorMessage() = runBlockingTest {
        setCompleteReminder()
        viewModel.reminderSelectedLocationStr.value = null
        viewModel.validateAndSaveReminder()
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun validateAndSaveReminder_complete_triggersToastSuccessMessage() = runBlockingTest {
        setCompleteReminder()
        viewModel.validateAndSaveReminder()
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(appContext.getString(R.string.reminder_saved))
        )
    }

    @Test
    fun validateAndSaveReminder_forceError_returnsError() = runBlockingTest {
        repository.setShouldReturnError(true)
        setCompleteReminder()
        val reminder = viewModel.validateAndSaveReminder()
        assertThat(reminder, notNullValue())
        val id = reminder!!.id
        assertThat(repository.getReminder(id), instanceOf(Result.Error::class.java))
        assertThat(
            repository.getReminder(id),
            `is`(Result.Error("Reminder with id = $id not found"))
        )
    }
}
