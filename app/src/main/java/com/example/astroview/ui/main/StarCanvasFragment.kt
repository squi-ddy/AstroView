package com.example.astroview.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.astroview.R
import com.example.astroview.api.API
import com.example.astroview.api.CallbackAction
import com.example.astroview.api.data.StarReview
import com.example.astroview.api.handleReturn
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentStarCanvasBinding
import com.example.astroview.starmap.astro.Coordinates
import com.example.astroview.starmap.astro.OrientationData
import com.example.astroview.starmap.core.CoreConstants
import com.example.astroview.starmap.math.Vec2
import com.example.astroview.starmap.stars.StarUtils
import com.example.astroview.starmap.stars.data.ProjectedStar
import com.example.astroview.ui.main.adapters.StarCommentAdapter
import com.google.android.material.snackbar.Snackbar
import com.otaliastudios.zoom.ZoomEngine
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread


class StarCanvasFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentStarCanvasBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var ct = 0
    private var showGyroscope = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarCanvasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).fab?.show()

        setHasOptionsMenu(true)

        binding.starCanvas.apply {
            setMaxZoom(20f)
            setMinZoom(1f)
        }

        binding.northPointer.textSize = CoreConstants.NORTH_TEXT_SIZE.toFloat()

        viewModel.projectedStars.observe(viewLifecycleOwner) {
            updateStars(it)
        }

        binding.starCanvas.engine.addListener(object : ZoomEngine.Listener {
            override fun onIdle(engine: ZoomEngine) {
                // ignore
            }

            override fun onUpdate(engine: ZoomEngine, matrix: Matrix) {
                updateStars(viewModel.projectedStars.value!!)
            }

        })

        binding.canvasContainer.addOnLayoutChangeListener {_, _, _, _, _, _, _, _, _ ->
            val offsetViewBounds = Rect()
            binding.circleBackground.getDrawingRect(offsetViewBounds)
            binding.canvasContainer.offsetDescendantRectToMyCoords(
                binding.circleBackground,
                offsetViewBounds
            )

            viewModel.centreVector = Vec2.fromXY(
                offsetViewBounds.centerX(),
                offsetViewBounds.centerY()
            )

            viewModel.core.renderNorth(
                binding.northPointer,
                viewModel.orientation.value!!.getAxes(),
                binding.circleBackground.width / 2 + CoreConstants.NORTH_DISTANCE.toDouble(),
                viewModel.centreVector
            )
        }

        binding.canvasContainer.doOnLayout {
            updateStars(viewModel.projectedStars.value!!)
        }

        viewModel.orientation.observe(viewLifecycleOwner) {
            thread {
                val stars = viewModel.core.getStarsInViewport(
                    it.getVector(),
                    CoreConstants.COS_LIMIT_FOV
                )
                stars.sortedBy { s -> viewModel.core.getVMagnitude(s.star, s.level) }
                viewModel.projectedStars.postValue(
                    viewModel.core.projectStars(
                        stars,
                        it.getAxes(),
                        resources.getDimensionPixelSize(R.dimen.star_map_radius).toDouble(),
                        CoreConstants.COS_LIMIT_FOV
                    )
                )
            }

            viewModel.core.renderNorth(
                binding.northPointer,
                it.getAxes(),
                binding.circleBackground.width / 2 + CoreConstants.NORTH_DISTANCE.toDouble(),
                viewModel.centreVector
            )
        }

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        viewModel.updateOrientation.observe(viewLifecycleOwner) { update ->
            if (update) registerSensors() else (sensorManager.unregisterListener(this))
        }

        if (!permissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) useLocationPerms()
                    else {
                        AlertDialog.Builder(
                            requireContext(),
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

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            useLocationPerms()
        }

        viewModel.selectedStar.observe(viewLifecycleOwner) {
            displayStarInfo(it)
        }

        binding.closeInfoButton.setOnClickListener {
            Log.e("sus", "pressed")
            viewModel.selectedStar.value = null
        }
    }

    private fun permissionGranted(permission: String): Boolean {
        return requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
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

    private fun displayStarInfo(star: ProjectedStar?) {
        updateStars(viewModel.projectedStars.value!!)

        if (star == null) {
            binding.starInfoContainer.visibility = View.GONE
            showGyroscope = true
            requireActivity().invalidateOptionsMenu()
            viewModel.updateOrientation.value = viewModel.savedUpdateOrientation
        } else {
            binding.starInfoContainer.visibility = View.VISIBLE

            viewModel.savedUpdateOrientation = viewModel.updateOrientation.value!!
            viewModel.updateOrientation.value = false
            showGyroscope = false
            requireActivity().invalidateOptionsMenu()

            val infoBinding = binding.starInfoContent
            val hipIndex = StarUtils.getHipparcosIndex(star.star.star)
            val hipparcosStar =
                viewModel.core.starManager!!.getHipparcosStar(hipIndex)
            infoBinding.starName.text = hipparcosStar?.name ?: "Unnamed Star"
            infoBinding.magnitude.text = getString(
                R.string.star_info_magnitude,
                viewModel.core.starManager!!.getVMagnitude(
                    star.star.star,
                    star.star.level
                )

            )
            infoBinding.bV.text = getString(
                R.string.star_info_bV, StarUtils.getBV(star.star.star)
            )
            infoBinding.hipparcos.text = getString(R.string.star_info_hipparcos, hipIndex)

            infoBinding.comments.layoutManager = LinearLayoutManager(requireContext())

            API.client.getStarReviews(star.star.star.hashCode()).handleReturn(object :
                CallbackAction<List<StarReview>> {
                override fun onSuccess(result: List<StarReview>, code: Int) {
                    infoBinding.comments.adapter = StarCommentAdapter(result)
                }

                override fun onFailure(error: Throwable) {
                    Snackbar.make(
                        binding.root,
                        "Network error. Try again later.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })

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

        if (ct >= CoreConstants.UPDATE_CYCLES) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateStars(it: List<ProjectedStar>) {
        binding.starViewport.removeAllViews()
        val r = resources.getDimensionPixelSize(R.dimen.star_map_radius).toDouble()
        val panV = Vec2.fromXY(
            r / binding.starCanvas.zoom - binding.starCanvas.panX,
            r / binding.starCanvas.zoom - binding.starCanvas.panY
        )
        for (star in it) {
            val starImage = viewModel.core.renderStar(
                requireContext(),
                star,
                r,
                r / binding.starCanvas.zoom - resources.getDimensionPixelSize(R.dimen.star_size),
                panV
            ) ?: continue
            starImage.setOnClickListener {
                Log.e("sus", "touched")
                viewModel.selectedStar.value = star
            }
            binding.starViewport.addView(starImage)
        }
        renderRing()
    }

    private fun renderRing() {
        val r = resources.getDimensionPixelSize(R.dimen.star_map_radius).toDouble()
        val panV = Vec2.fromXY(
            r / binding.starCanvas.zoom - binding.starCanvas.panX,
            r / binding.starCanvas.zoom - binding.starCanvas.panY
        )
        val selectedStar = viewModel.selectedStar.value ?: return
        viewModel.core.renderStar(
            requireContext(),
            selectedStar,
            r,
            r / binding.starCanvas.zoom - resources.getDimensionPixelSize(R.dimen.star_size),
            panV
        ) ?: return
        binding.starViewport.addView(
            viewModel.core.renderStarSelected(
                requireContext(),
                selectedStar,
                r
            )
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val lockGyro = menu.findItem(R.id.menu_gyro_lock)
        lockGyro.isVisible = showGyroscope
        lockGyro.icon = if (viewModel.updateOrientation.value!!) {
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_screen_lock_rotation_24
            )
        } else {
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_screen_rotation_24
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_gyro_lock -> {
                viewModel.updateOrientation.value = !viewModel.updateOrientation.value!!
                if (viewModel.updateOrientation.value!!) {
                    item.icon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_screen_lock_rotation_24
                    )
                } else {
                    item.icon =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_screen_rotation_24
                        )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }
}