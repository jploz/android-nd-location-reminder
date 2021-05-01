package com.udacity.locationreminder.locationreminders.geofence

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.locationreminder.R

private const val TAG = "GeofenceUtils"

val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_LOCATION_PERMISSION = 35

const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1


const val ACTION_GEOFENCE_EVENT = "com.udacity.locationreminder.MyApp.action.ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_METERS = 100.0f

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

fun buildGeofencePendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    intent.action = ACTION_GEOFENCE_EVENT
    return PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}

@TargetApi(29)
fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
    val backgroundPermissionApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    return foregroundLocationApproved && backgroundPermissionApproved
}

fun foregroundLocationPermissionApproved(context: Context): Boolean {
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}
