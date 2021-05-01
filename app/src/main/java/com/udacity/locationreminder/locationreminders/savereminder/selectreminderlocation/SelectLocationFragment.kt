package com.udacity.locationreminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminder.BuildConfig
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseFragment
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentSelectLocationBinding
import com.udacity.locationreminder.locationreminders.geofence.LOCATION_PERMISSION_INDEX
import com.udacity.locationreminder.locationreminders.geofence.REQUEST_LOCATION_PERMISSION
import com.udacity.locationreminder.locationreminders.geofence.foregroundLocationPermissionApproved
import com.udacity.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "SelectLocationFragment"

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: SaveReminderViewModel by inject()

    private val selectLocationViewModel: SelectLocationViewModel by viewModels()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container,
            false
        )

        selectLocationViewModel.initLocation(
            _viewModel.selectedPOI.value,
            _viewModel.latitude.value,
            _viewModel.longitude.value
        )

        binding.viewModel = selectLocationViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        Log.d(TAG, "onCreateView: setup map")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        // binding cannot be used to obtain the fragment, see https://stackoverflow.com/a/63751842
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.saveLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        // When the user confirms on the selected location,
        // send back the selected location details to the view model
        // and navigate back to the previous fragment to save the reminder and add the geofence
        Log.d(TAG, "onLocationSelected called")

        _viewModel.setSelectedLocation(
            selectLocationViewModel.getSelectedPOI(),
            selectLocationViewModel.getSelectedLocation()
        )

        _viewModel.navigationCommand.value = NavigationCommand.BackTo(R.id.saveReminderFragment)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStyle()
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = false

        setMapClick()
        setMapLongClick()
        setPoiClick()
        initMarker()

        enableMyLocation()
    }

    private fun initMarker() {
        val poi = selectLocationViewModel.getSelectedPOI()
        if (poi != null) {
            showPoiMarker(poi)
        } else {
            val latLng = selectLocationViewModel.getSelectedLocation()
            if (latLng != null)
                showLocationMarker(latLng)
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomToMyLocation() {
        Log.d(TAG, "zoomToMyLocation: location requested")
        val zoom = 15f
        fusedLocationClient.lastLocation.addOnSuccessListener { myLocation: Location? ->
            Log.d(TAG, "zoomToMyLocation: laddOnSuccessListener called")
            if (myLocation != null) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            myLocation.latitude,
                            myLocation.longitude
                        ), zoom
                    )
                )
            } else {
                Log.w(TAG, "zoomToMyLocation: unable to get location")
            }
        }
    }

    private fun setMapStyle() {
        try {
            // Customize the styling of the base map using a JSON object defined in a raw resource file
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Map style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Cannot find map style. Error: $e")
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        Log.d(TAG, "enableMyLocation called")
        // TODO: check also if location service is enabled
        if (!foregroundLocationPermissionApproved(requireContext())) {
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            @Suppress("DEPRECATION")
            requestPermissions(permissionsArray, REQUEST_LOCATION_PERMISSION)
            return
        }
        Log.d(TAG, "enableMyLocation: enable myLocation")
        map.isMyLocationEnabled = true
        zoomToMyLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (grantResults.isEmpty()
            || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar.make(
                binding.selectLocationLayout,
                R.string.permission_access_location_denied_explanation,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            enableMyLocation()
        }
    }

    private fun showPoiMarker(poi: PointOfInterest) {
        map.clear()
        val poiMarker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        poiMarker.showInfoWindow()
    }

    private fun setPoiClick() {
        map.setOnPoiClickListener { poi ->
            showPoiMarker(poi)
            selectLocationViewModel.selectPoi(poi)
        }
    }

    private fun showLocationMarker(latLng: LatLng) {
        map.clear()
        val snippet = String.format(
            Locale.getDefault(),
            getString(R.string.lat_long_snippet),
            latLng.latitude,
            latLng.longitude
        )
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun setMapLongClick() {
        map.setOnMapLongClickListener { latLng ->
            showLocationMarker(latLng)
            selectLocationViewModel.selectLocation(latLng)
        }
    }

    private fun setMapClick() {
        map.setOnMapClickListener {
            map.clear()
            selectLocationViewModel.clearLocations()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
