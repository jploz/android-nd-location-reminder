package com.udacity.locationreminder.locationreminders.geofence

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminder.BuildConfig
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseFragment

private const val TAG = "GeofenceBaseFragment"

abstract class GeofenceBaseFragment : BaseFragment() {

    protected lateinit var geofencingClient: GeofencingClient

    protected lateinit var rootView: View

    protected val geofencePendingIntent: PendingIntent by lazy {
        buildGeofencePendingIntent(requireContext())
    }

    protected fun initGeofencingClient() {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    @TargetApi(29)
    protected fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        return foregroundAndBackgroundLocationPermissionApproved(
            requireContext()
        )
    }

    /*
 *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
 */
    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        Log.i(
            TAG,
            "requestForegroundAndBackgroundLocationPermissions"
        )
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext()))
            return
        if (!foregroundLocationPermissionApproved(requireContext())) {
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            @Suppress("DEPRECATION")
            requestPermissions(permissionsArray, resultCode)
        } else {
            if (runningQOrLater) {
                val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                val resultCode = REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                @Suppress("DEPRECATION")
                requestPermissions(permissionsArray, resultCode)
            }
        }
    }

    fun checkPermissionsAndStart() {
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
            checkDeviceLocationSettingsAndStart()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    /*
     *  Uses the Location Client to check the current state of location settings,
     *  and gives the user the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndStart(resolve: Boolean = true) {
        Log.i(TAG, "checkDeviceLocationSettingsAndStartGeofence")
        // check that the device's location is on
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            Log.i(TAG, "locationSettingsResponseTask.addOnFailureListener: exception: $exception")
            if (exception is ResolvableApiException && resolve) {
                try {
                    @Suppress("DEPRECATION")
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    rootView,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStart()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            Log.i(
                TAG,
                "locationSettingsResponseTask.addOnCompleteListener: successful: ${it.isSuccessful}"
            )
            if (it.isSuccessful) {
                Log.i(TAG, "locationSettingsResponseTask.addOnCompleteListener: was successful")
                onSuccessfullCheck()
            }
        }
    }

    /*
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // handle the result of the user's permission
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                rootView,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
            checkDeviceLocationSettingsAndStart()
        }
    }

    /*
     *  When we get the result from asking the user to turn on device location, we call
     *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
     *  we don't resolve the check to keep the user from seeing an endless loop.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // check that the user turned on their device location and ask again if they did not
        Log.i(TAG, "onActivityResult")
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            Log.i(TAG, "onActivityResult: REQUEST_TURN_DEVICE_LOCATION_ON")
            checkDeviceLocationSettingsAndStart(false)
        }
    }

    abstract fun onSuccessfullCheck()
}
