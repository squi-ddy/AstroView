package com.example.astroview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.astroview.core.AppViewModel
import com.example.astroview.core.CoreConstants
import com.example.astroview.databinding.FragmentStarCanvasBinding
import com.example.astroview.math.Vec2
import com.example.astroview.ui.util.setMargin
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

        binding.circleBackground.minimumWidth = 2 * CoreConstants.VIEWPORT_RADIUS
        binding.circleBackground.minimumHeight = 2 * CoreConstants.VIEWPORT_RADIUS

        binding.canvasContainer.setMargin(binding.circleBackground, CoreConstants.VIEWPORT_MARGINS)
        binding.starViewportNorth.setMargin(binding.starViewport, CoreConstants.VIEWPORT_MARGINS)

        binding.starCanvas.apply {
            setMaxZoom(20f)
            setMinZoom(1f)
        }

        binding.northPointer.textSize = CoreConstants.NORTH_TEXT_SIZE.toFloat()

        viewModel.projectedStars.observe(viewLifecycleOwner) {
            binding.starViewport.removeAllViews()
            for (star in it) {
                val starImage = viewModel.core.renderStar(
                    requireContext(),
                    star,
                    CoreConstants.VIEWPORT_RADIUS.toDouble(),
                    CoreConstants.VIEWPORT_RENDER_MARGIN.toDouble()
                ) ?: continue
                binding.starViewport.addView(starImage)
            }
        }

        val centerVector = Vec2.fromXY(
            CoreConstants.VIEWPORT_RADIUS + CoreConstants.VIEWPORT_MARGINS,
            CoreConstants.VIEWPORT_RADIUS + CoreConstants.VIEWPORT_MARGINS
        )

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
                        CoreConstants.VIEWPORT_RADIUS.toDouble(),
                        CoreConstants.COS_LIMIT_FOV
                    )
                )
            }

            viewModel.core.renderNorth(
                binding.northPointer,
                it.getAxes(),
                CoreConstants.NORTH_RADIUS.toDouble(),
                centerVector
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}