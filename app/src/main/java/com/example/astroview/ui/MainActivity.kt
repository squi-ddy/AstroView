package com.example.astroview.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.astroview.astro.Coordinates
import com.example.astroview.astro.OrientationData
import com.example.astroview.core.AppViewModel
import com.example.astroview.databinding.ActivityMainBinding
import kotlin.math.PI
import kotlin.text.Typography.degree

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: AppViewModel by viewModels()

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            createUi()
        }
    }

    private fun createUi() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ???
            throw IllegalStateException("idk what how")
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val bestProvider = locationManager.getBestProvider(
            Criteria(), true
        ) ?: throw IllegalStateException("No provider????")

        val lastKnownLocation = locationManager.getLastKnownLocation(bestProvider)
        Coordinates.lat = lastKnownLocation?.latitude ?: 0.0
        Coordinates.long = lastKnownLocation?.longitude ?: 0.0
        Coordinates.updateMatrices()


        viewModel.orientation.observe(this) {
            binding.sensorData.text =
                "Azimuth: ${it.azimuth / PI * 180}$degree\nPitch: ${it.pitch / PI * 180}$degree\nRoll: ${it.roll / PI * 180}$degree" +
                        "\nVector: ${it.getVector()}\nStars in view: ${
                            viewModel.core.getStarsInViewport(
                                it.getVector(),
                                0.9
                            ).size
                        }"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createUi()
                } else {
                    // crash the app lmao
                    throw IllegalStateException("pls give permission :(")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        updateOrientationAngles()
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        viewModel.orientation.value =
            OrientationData(orientationAngles[0], orientationAngles[1], orientationAngles[2])
    }

}