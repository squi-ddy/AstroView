package com.example.astroview.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.astroview.api.data.HelpPage
import com.example.astroview.databinding.CardHelpInnerBinding
import com.example.astroview.ui.main.HelpPageSelectorFragmentDirections

class HelpPageInternalAdapter(
    private val helpPages: Collection<HelpPage>
) : RecyclerView.Adapter<HelpPageInternalAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardHelpInnerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(helpPages.elementAt(position))
    }

    override fun getItemCount() = helpPages.size

    class ViewHolder(
        private val binding: CardHelpInnerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var isHidden = false
        fun bindItems(page: HelpPage) {
            binding.itemName.text = page.name
            binding.root.setOnClickListener {
                val action = HelpPageSelectorFragmentDirections.actionShowHelpPage(
                    page.id, page.name
                )
                binding.root.findNavController().navigate(action)
            }
        }
    }
}