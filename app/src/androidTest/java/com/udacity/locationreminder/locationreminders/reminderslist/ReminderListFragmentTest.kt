package com.udacity.locationreminder.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.locationreminder.MainCoroutineRule
import com.udacity.locationreminder.R
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.local.LocalDB
import com.udacity.locationreminder.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    test the navigation of the fragments.
//    test the displayed data on the UI.
//    add testing for the error messages.

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var appContext: Application
    private lateinit var repository: ReminderDataSource

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

    @Before
    fun setupRepository() = runBlocking {
        appContext = ApplicationProvider.getApplicationContext()

        // setup DI using Koin
        // only dependencies which are required for tests are defined
        // current Koin instance needs to be stopped first
        stopKoin()

        val myModule = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            @Suppress("USELESS_CAST")
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

        // now get repository and prepare a clean data set
        repository = get()
        runBlocking {
            repository.deleteAllReminders()
            repository.saveReminder(reminders[0])
            repository.saveReminder(reminders[1])
        }
    }

    @Test
    fun reminderListFragment_dataAvailable_itemsAreDisplayedInList(): Unit = runBlocking {

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("Title of reminder 1"))))
            .check(matches(hasDescendant(withText("Title of reminder 2"))))
            .check(matches(hasDescendant(withText("Description of reminder 1"))))
            .check(matches(hasDescendant(withText("Description of reminder 2"))))
            .check(matches(hasDescendant(withText("User defined location"))))
            .check(matches(hasDescendant(withText("User defined location"))))
    }

    @Test
    fun reminderListFragment_addReminderFabClicked_navigatesToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun reminderListFragment_noData_noDataIsDisplayed(): Unit = runBlocking {

        repository.deleteAllReminders()

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView))
            .check(matches(withText(appContext.getString(R.string.no_data))))
    }
}
