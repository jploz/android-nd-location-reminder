package com.udacity.locationreminder.locationreminders.savereminder.selectreminderlocation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest

class SelectLocationViewModel : ViewModel() {

    private val selectedPOI = MutableLiveData<PointOfInterest?>()
    private val latLng = MutableLiveData<LatLng?>()

    val isLocationSelected = Transformations.map(latLng) {
        latLng.value != null
    }

    fun initLocation(poi: PointOfInterest?, lat: Double?, lng: Double?) {
        if (poi != null) {
            selectPoi(poi)
        } else {
            if (lat != null && lng != null)
                selectLocation(LatLng(lat, lng))
        }
    }

    fun clearLocations() {
        selectedPOI.value = null
        latLng.value = null
    }

    fun selectPoi(poi: PointOfInterest) {
        selectedPOI.value = poi
        latLng.value = poi.latLng
    }

    fun selectLocation(locationLatLng: LatLng) {
        selectedPOI.value = null
        latLng.value = locationLatLng
    }

    fun getSelectedPOI(): PointOfInterest? {
        return selectedPOI.value
    }

    fun getSelectedLocation(): LatLng? {
        return latLng.value
    }
}
