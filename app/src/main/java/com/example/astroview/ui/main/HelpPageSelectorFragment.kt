package com.example.astroview.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.astroview.R
import com.example.astroview.api.API
import com.example.astroview.api.CallbackAction
import com.example.astroview.api.data.HelpPage
import com.example.astroview.api.handleReturn
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentHelpPageSelectorBinding
import com.example.astroview.ui.main.adapters.HelpPageMainAdapter
import com.google.android.material.snackbar.Snackbar

class HelpPageSelectorFragment : Fragment() {
    private var _binding: FragmentHelpPageSelectorBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpPageSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).fab?.hide()
        setHasOptionsMenu(true)

        binding.helpRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        API.client.getPages().handleReturn(object : CallbackAction<Map<String, List<HelpPage>>> {
            override fun onSuccess(result: Map<String, List<HelpPage>>, code: Int) {
                viewModel.pages = result
                requireActivity().runOnUiThread {
                    binding.helpRecyclerView.adapter =
                        HelpPageMainAdapter(result, result.keys.sorted())
                }
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val lockGyro = menu.findItem(R.id.menu_gyro_lock)
        lockGyro.isVisible = false

        val help = menu.findItem(R.id.menu_help)
        help.isVisible = false

        val share = menu.findItem(R.id.menu_share)
        share.isVisible = false

        val download = menu.findItem(R.id.menu_download)
        download.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}