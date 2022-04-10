package com.example.astroview.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.astroview.R
import com.example.astroview.api.data.HelpPage
import com.example.astroview.databinding.CardHelpBinding

class HelpPageMainAdapter(
    private val helpPageList: Map<String, List<HelpPage>>,
    private val keys: Collection<String>
) : RecyclerView.Adapter<HelpPageMainAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardHelpBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, helpPageList)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(keys.elementAt(position))
    }

    override fun getItemCount() = helpPageList.size

    class ViewHolder(
        private val binding: CardHelpBinding,
        private val helpPageList: Map<String, List<HelpPage>>
    ) : RecyclerView.ViewHolder(binding.root) {
        private var isHidden = false
        fun bindItems(categoryName: String) {
            binding.itemName.text = categoryName
            binding.cardExpanded.visibility = View.GONE
            isHidden = true
            binding.reviewRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

            binding.root.setOnClickListener {
                isHidden = !isHidden
                if (isHidden) {
                    TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup, AutoTransition())
                    binding.cardExpanded.visibility = View.GONE
                    binding.reviewRecyclerView.adapter = null
                    binding.itemName.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.recycler_view_closed,
                        0,
                        0,
                        0

                    )
                } else {
                    TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup, AutoTransition())
                    binding.cardExpanded.visibility = View.VISIBLE
                    binding.reviewRecyclerView.adapter = HelpPageInternalAdapter(
                        helpPageList[categoryName]!!
                    )
                    binding.itemName.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.recycler_view_open,
                        0,
                        0,
                        0

                    )
                }
            }
        }
    }
}