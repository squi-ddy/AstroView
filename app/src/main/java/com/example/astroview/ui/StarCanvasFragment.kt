package com.example.astroview.ui

import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.astroview.R
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentStarCanvasBinding
import com.example.astroview.starmap.core.CoreConstants
import com.example.astroview.starmap.math.Vec2
import com.example.astroview.starmap.stars.data.ProjectedStar
import com.otaliastudios.zoom.ZoomEngine
import kotlin.concurrent.thread


class StarCanvasFragment : Fragment() {
    private var _binding: FragmentStarCanvasBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarCanvasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        var centerVector = Vec2.fromXY(0, 0)
        binding.canvasContainer.doOnLayout {
            val offsetViewBounds = Rect()
            binding.circleBackground.getDrawingRect(offsetViewBounds)
            binding.canvasContainer.offsetDescendantRectToMyCoords(
                binding.circleBackground,
                offsetViewBounds
            )

            centerVector = Vec2.fromXY(
                offsetViewBounds.centerX(),
                offsetViewBounds.centerY()
            )
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
                centerVector
            )
        }
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
            binding.starViewport.addView(starImage)
        }
    }
}