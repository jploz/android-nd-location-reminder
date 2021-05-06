package com.udacity.locationreminder

import android.app.Application
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.locationreminder.locationreminders.RemindersActivity
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.local.LocalDB
import com.udacity.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.udacity.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminder.util.DataBindingIdlingResource
import com.udacity.locationreminder.util.EspressoIdlingResource
import com.udacity.locationreminder.util.clickOnViewChild
import com.udacity.locationreminder.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            @Suppress("USELESS_CAST")
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    // End to End testing to the app
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
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
    fun addNewReminder_navigatesToSaveReminderFragmentAndItemIsDisplayedInRemindersList() =
        runBlocking {
            repository.saveReminder(reminders[0])
            repository.saveReminder(reminders[1])

            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            onView(withId(R.id.addReminderFAB)).perform(click())

            onView(withId(R.id.reminderTitle)).perform(replaceText("Title of reminder 3"))
            onView(withId(R.id.reminderDescription)).perform(replaceText("Description of reminder 3"))

            onView(withId(R.id.selectLocation)).perform(click())
            onView(withId(R.id.map)).check(matches(isDisplayed()))
            onView(withId(R.id.map)).perform(longClick())
            onView(withId(R.id.saveLocation)).perform(click())

            onView(withId(R.id.saveReminder)).perform(click())

            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(hasDescendant(withText("Title of reminder 1"))))
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(hasDescendant(withText("Title of reminder 2"))))
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(hasDescendant(withText("Title of reminder 3"))))

            activityScenario.close()
        }

    @Test
    fun addReminder_noLocationIsSelected_errorMessageIsDisplayed(): Unit = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title of reminder"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description of reminder"))
        onView(withId(R.id.saveReminder)).perform(click())

        val snackbarMessage = appContext.getString(R.string.select_location)
        onView(withText(snackbarMessage)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun clickReminderListItem_remindersDescriptionIsDisplayed(): Unit = runBlocking {
        repository.saveReminder(reminders[0])
        repository.saveReminder(reminders[1])

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("Title of reminder 1"))))
            .check(matches(hasDescendant(withText("Title of reminder 2"))))

        val position = 0
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                position,
                clickOnViewChild(R.id.reminderCardView)
            )
        )

        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Title of reminder 1")))

        onView(withId(R.id.reminderDescription))
            .check(matches(isDisplayed()))
            .check(matches(withText("Description of reminder 1")))

        onView(withId(R.id.reminderLocation))
            .check(matches(isDisplayed()))
            .check(matches(withText("User defined location")))

        onView(withId(R.id.reminderLatLng))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }
}
