package com.example.astroview.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.astroview.R
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.ActivityMainBinding
import com.example.astroview.starmap.astro.Coordinates
import com.example.astroview.starmap.astro.OrientationData

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<AppViewModel>()

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var ct = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.fab.show()

        binding.fab.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_starCanvasFragment_to_loginFragment)
            binding.fab.hide()
        }

        viewModel.updateOrientation.observe(this) { update ->
            if (update) registerSensors() else (sensorManager.unregisterListener(this))
        }

        if (!permissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            useLocationPerms()
        }
    }

    private fun permissionGranted(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun useLocationPerms() {
        if (!permissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // ???
            throw IllegalStateException("idk what how")
        }

        val bestProvider = locationManager.getBestProvider(
            Criteria(), true
        ) ?: throw IllegalStateException("No provider????")

        // Android Studio is stupid lmao
        @SuppressLint("MissingPermission")
        val lastKnownLocation = locationManager.getLastKnownLocation(bestProvider)

        Coordinates.lat = lastKnownLocation?.latitude ?: 0.0
        Coordinates.long = lastKnownLocation?.longitude ?: 0.0
        Coordinates.updateMatrices()
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
                    useLocationPerms()
                } else {
                    AlertDialog.Builder(
                        this,
                        android.R.style.Theme_Material_Dialog_NoActionBar
                    ).apply {
                        setMessage(R.string.permission_refused_message)
                        setPositiveButton(R.string.permission_refused_positive) { _, _ ->
                            // at this point, requesting does nothing.
                        }
                        show()
                    }
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

        if (viewModel.updateOrientation.value!!) registerSensors()
    }

    private fun registerSensors() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        if (viewModel.updateOrientation.value!!) sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        if (ct >= 2) {
            ct = 0
            updateOrientationAngles()
        }
        ct++
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

        viewModel.addOrientation(
            OrientationData(orientationAngles[0], orientationAngles[1], orientationAngles[2])
        )

        Coordinates.updateMatrices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val lockGyro = menu.findItem(R.id.menu_gyro_lock)
        lockGyro.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_gyro_lock -> {
                viewModel.updateOrientation.value = !viewModel.updateOrientation.value!!
                if (viewModel.updateOrientation.value!!) {
                    item.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_screen_lock_rotation_24
                    )
                } else {
                    item.icon =
                        ContextCompat.getDrawable(this, R.drawable.ic_baseline_screen_rotation_24)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}