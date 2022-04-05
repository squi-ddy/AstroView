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

        binding.circleBackground.minimumWidth = 2 * CoreConstants.RADIUS
        binding.circleBackground.minimumHeight = 2 * CoreConstants.RADIUS

        binding.starCanvas.apply {
            zoomTo(10f, false)
            setMinZoom(5f)
            setMaxZoom(15f)
        }

        viewModel.projectedStars.observe(viewLifecycleOwner) {
            binding.starView.removeAllViews()
            for (star in it) {
                binding.starView.addView(
                    viewModel.core.renderStar(
                        requireContext(),
                        star,
                        CoreConstants.RADIUS.toDouble()
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}