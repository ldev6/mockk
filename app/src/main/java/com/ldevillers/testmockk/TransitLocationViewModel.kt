package com.ldevillers.testmockk

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class TransitLocationViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = TransitLocationViewModel::class.simpleName
    private val context = application.applicationContext
    private var contextString: String = "";
    private var coroutineContext: CoroutineContext = Dispatchers.Default
    lateinit var locationAvailableManager: LocationAvailableManager
    lateinit var locationAvailableFlow: StateFlow<Boolean>


    constructor(application: Application, dispatcher: CoroutineDispatcher) : this(application) {
        coroutineContext = dispatcher
        locationAvailableManager = LocationAvailableManager(context)
        locationAvailableFlow = locationAvailableManager.locationAvailableFlow
        locationAvailableLiveData= locationAvailableFlow.asLiveData(Dispatchers.IO)
    }

    constructor(application: Application, contextString: String) : this(application) {
        this.contextString = contextString
    }

    private val locationRequestManager = LocationRequestManager(null)

    private var lastGeocodedAddressLocation: Location? = null
    private val realLocation = MutableStateFlow<Boolean>(false)

    val realLocationFlow: StateFlow<Boolean> = realLocation

    @Deprecated("Use realLocationFlow on Kotlin class")
    val realLocationLiveData = realLocationFlow.asLiveData(Dispatchers.IO)


    @Deprecated("Use locationAvailableFlow on Kotlin class")
    lateinit var locationAvailableLiveData :LiveData<Boolean>

    val simulatedLocationFlow = MutableStateFlow(null)

    @Deprecated("Use simulatedLocationFlow on Kotlin class")
    val simulatedLocationLiveData = simulatedLocationFlow.asLiveData(Dispatchers.IO)

    init {
        locationAvailableManager = LocationAvailableManager(context)
        locationAvailableFlow = locationAvailableManager.locationAvailableFlow
        locationAvailableLiveData= locationAvailableFlow.asLiveData(Dispatchers.IO)
        locationAvailableManager.onActive()
        viewModelScope.launch(coroutineContext) {
            locationAvailableManager.locationAvailableFlow.collect { locationActive ->
                withContext(Dispatchers.Main) {
//                    handleLocationManagerConnection(locationActive)

                    realLocation.value = locationActive

                }
            }
        }

        viewModelScope.launch(coroutineContext) {

            locationRequestManager.locationRequestFlow.collect { location ->
                // Always git the new value to transitLib for accuracy

//                if (isLocationNeedAnUpdate(realLocation.value, location)) {
////                    updateCurrentPlace(location)
//                    realLocation.value = location
//                }
            }
        }


        viewModelScope.launch(coroutineContext) {
            simulatedLocationFlow.distinctUntilChanged { old, new ->
                if (isLocationNeedAnUpdate(old, new)) {
                    Log.v(TAG, "in distinc until change =" + old + " " + new);
                    return@distinctUntilChanged true
                }
                return@distinctUntilChanged false

            }.collect()
        }
    }

    fun clear() {
        onCleared()
    }

    override fun onCleared() {
        super.onCleared()
        locationAvailableManager.onInactive()
    }

//    val lastKnownLocationFlow: Flow<Location?> = realLocationFlow.combine(
//        simulatedLocationFlow) { realLocation, simulated ->
//        if (simulated != null) {
//            return@combine simulated
//        } else {
//            return@combine realLocation
//        }
//    }

//    @Deprecated("Use Flow on Kotlin class")
//    val lastKnownLocationLiveData = lastKnownLocationFlow.asLiveData(Dispatchers.IO)

    private fun isLocationNeedAnUpdate(oldLocation: Location?, newLocation: Location?): Boolean {
        return if (oldLocation == null && newLocation != null) {
            true
        } else if (oldLocation != null && newLocation == null) {
            true
        } else {
            (oldLocation != null && newLocation != null
                    && oldLocation != newLocation
                    && oldLocation.distanceTo(
                newLocation
            ) >= 10)
        }
    }


    fun isGpsOrNetworkActive(): Boolean {
        return locationAvailableManager.isGpsOrNetworkActive()
    }

    fun updateAvailability() {
        locationAvailableManager.updatePermissions()
    }


    private fun getLocationFromString(loc: String?): Location? {
        if (loc.isNullOrEmpty()) {
            return null
        }

        var lastBroadcastedLocation: Location? = null
        val latLng = loc.split(",")

        if (latLng.size >= 2) {
            try {
                val lat = latLng[0].toDouble()
                val lng = latLng[1].toDouble()
                var time = 0L

                if (latLng.size == 3) {
                    time = latLng[2].toLong()
                }

                lastBroadcastedLocation = Location("Transit")
                lastBroadcastedLocation.latitude = lat
                lastBroadcastedLocation.longitude = lng
                lastBroadcastedLocation.time = time
            } catch (ex: NumberFormatException) {
            }
        }

        return lastBroadcastedLocation
    }

}
