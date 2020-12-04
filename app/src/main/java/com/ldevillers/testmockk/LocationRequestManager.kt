package com.ldevillers.testmockk

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocationRequestManager(private val defaultValue: Location?) {
    private val _locationRequestFlow = MutableStateFlow<Location?>(defaultValue)
    val locationRequestFlow: Flow<Location?> = _locationRequestFlow
}
