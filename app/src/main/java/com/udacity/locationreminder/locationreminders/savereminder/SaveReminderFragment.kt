package com.udacity.locationreminder.locationreminders.savereminder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentSaveReminderBinding
import com.udacity.locationreminder.locationreminders.geofence.GEOFENCE_RADIUS_METERS
import com.udacity.locationreminder.locationreminders.geofence.GeofenceBaseFragment
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : GeofenceBaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_save_reminder,
            container,
            false
        )

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        rootView = binding.saveReminderLayout

        setDisplayHomeAsUpEnabled(true)

        initGeofencingClient()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            // use the user entered reminder details to:
            //  1) add a geofencing request
            //  2) save the reminder to the local db
            val reminderData = _viewModel.validateAndSaveReminder()
            if (reminderData != null) {
                val lat = reminderData.latitude
                val lng = reminderData.longitude
                if (lat != null && lng != null)
                    addGeofenceForReminder(reminderData.id, lat, lng)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onSuccessfullCheck() {
        Log.i(TAG, "Permissions check: success")
    }

    @Suppress("MissingPermission")
    private fun addGeofenceForReminder(reminderId: String, lat: Double, lng: Double) {
        Log.d(TAG, "addGeofenceForReminder: $reminderId")
        val geofence = Geofence.Builder()
            .setRequestId(reminderId)
            .setCircularRegion(lat, lng, GEOFENCE_RADIUS_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    Log.i(TAG, "Geofence was successfully added")
                }
                addOnFailureListener {
                    Log.w(TAG, "Unable to add Geofence: $it")
                    _viewModel.showSnackBar.postValue("There was a problem to add geofence - please check device location settings (high accuracy).")
                }
            }
        } else {
            _viewModel.showSnackBar.postValue("Unable to add geofence - please check permissions.")
        }
    }
}
