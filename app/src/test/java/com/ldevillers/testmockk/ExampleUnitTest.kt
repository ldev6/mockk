package com.ldevillers.testmockk

import android.app.Application
import android.content.Context
import android.location.LocationManager
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import kotlin.time.ExperimentalTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @MockK
    lateinit var mockedLocationManager: LocationManager

    @MockK
    lateinit var mockedApplication: Application

    @MockK
    lateinit var mockedContext: Context

    lateinit var spyLocationAvailableManager: LocationAvailableManager

    lateinit var transitLocationViewModel: TransitLocationViewModel


    @Before
    fun setup() {

        MockKAnnotations.init(this)

//        mockedApplication = mockk<Application>()
//        mockedContext = mockk<Context>()
//        mockedLocationManager = mockk<LocationManager>()
        every { mockedApplication.applicationContext } returns mockedContext
        every { mockedContext.applicationContext } returns mockedContext
        every { mockedContext.getSystemService(Context.LOCATION_SERVICE) } returns mockedLocationManager

        spyLocationAvailableManager = spyk(LocationAvailableManager(mockedContext))

        transitLocationViewModel = spyk(TransitLocationViewModel(mockedApplication, TestCoroutineDispatcher())) {
            every {locationAvailableManager } returns spyLocationAvailableManager
        }
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun addition_isCorrect()  = runBlockingTest {

        every { spyLocationAvailableManager.isGpsOrNetworkActive() } answers { true }

        assertEquals(transitLocationViewModel.isGpsOrNetworkActive(), true)


        assertEquals(transitLocationViewModel.locationAvailableFlow.value, false)


        transitLocationViewModel.locationAvailableFlow.test {
            assertEquals(expectItem(), false)
//            assertEquals(expectItem(), true)

            expectNoEvents()
        }

        transitLocationViewModel.realLocationFlow.test {
           assertEquals(expectItem(), false)
            expectNoEvents()
        }


        transitLocationViewModel.updateAvailability()
        transitLocationViewModel.locationAvailableFlow.test {
            assertEquals(expectItem(), true)
            expectNoEvents()
        }
        assertEquals(transitLocationViewModel.locationAvailableFlow.value, true)
    }
}