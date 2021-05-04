package com.udacity.locationreminder.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
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
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: FakeDataSource
    private lateinit var appContext: Context

    // Subject under test
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val reminders = mutableListOf(
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

        repository = FakeDataSource(reminders = reminders)
        appContext = ApplicationProvider.getApplicationContext()
        viewModel = RemindersListViewModel(appContext as Application, repository)
    }

    @After
    fun tearDownViewModel() {
        // ensure clean data for tests
        runBlocking {
            repository.deleteAllReminders()
        }

        // Koin instance must be stopped between tests
        stopKoin()
    }

    @Test
    fun showNoData_noReminders_isTrue() = runBlockingTest {
        repository.deleteAllReminders()
        viewModel.loadReminders()
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun showNoData_withData_isFalse() = runBlockingTest {
        viewModel.loadReminders()
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun showLoading_noData_isTrueWhileLoading() = runBlockingTest {
        repository.deleteAllReminders()
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun showLoading_withData_isTrueWhileLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun remindersList_noData_isEmpty() = runBlockingTest {
        repository.deleteAllReminders()
        viewModel.loadReminders()
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
    }

    @Test
    fun remindersList_withData_isNotEmpty() = runBlockingTest {
        viewModel.loadReminders()
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(false))
    }
}
