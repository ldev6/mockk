package com.ldevillers.testmockk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationAvailableManager(private val context: Context) : LifecycleObserver {

    private val locationAvailable = MutableStateFlow<Boolean>(false)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.let {
                if (it == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    notifyObservers()
                }
            }
        }
    }

    val locationAvailableFlow: StateFlow<Boolean> = locationAvailable
    val locationAvailableLiveData = locationAvailableFlow.asLiveData()

    fun onActive() {
        notifyObservers()
//        context.registerReceiver(locationStateReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    fun onInactive() {
        try {
//            context.unregisterReceiver(locationStateReceiver)
        } catch (e: IllegalArgumentException) {
        }

    }

    fun isLocationUsable(): Boolean = isGpsOrNetworkActive()

    fun isGpsOrNetworkActive(): Boolean {
        var enableGPS = false
        var enableNetwork = false

        locationManager.let {
            try {
                enableGPS = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
                enableNetwork = it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
            }
            return enableGPS || enableNetwork
        }
    }

    fun updatePermissions() {
        notifyObservers()
    }

    private fun notifyObservers() {
        locationAvailable.value = isLocationUsable()
    }
}
