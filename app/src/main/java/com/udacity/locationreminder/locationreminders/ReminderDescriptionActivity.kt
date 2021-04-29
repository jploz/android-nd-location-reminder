package com.udacity.locationreminder.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.locationreminder.R
import com.udacity.locationreminder.databinding.ActivityReminderDescriptionBinding
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "ReminderDescription..."

/**
 * Activity that displays the reminder details after the user clicks on the notification
 * or the corresponding list item in the overview screen.
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    val viewModel: ReminderDescriptionViewModel by viewModel()

    private lateinit var binding: ActivityReminderDescriptionBinding

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        binding.reminderDataItem =
            intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.reminder_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                binding.reminderDataItem?.let {
                    viewModel.deleteReminder(it.id)
                    removeGeofence(it.id)
                    navigateToReminderList()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToReminderList() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
    }

    private fun removeGeofence(reminderId: String) {
//        if (!foregroundAndBackgroundLocationPermissionApproved()) {
//            return
//        }
        val requestIds = listOf(reminderId)
        geofencingClient.removeGeofences(requestIds).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence removed (id = $reminderId)")
            }
            addOnFailureListener {
                Log.e(TAG, "Unable to remove geofence with id = $reminderId")
            }
        }
    }
}
