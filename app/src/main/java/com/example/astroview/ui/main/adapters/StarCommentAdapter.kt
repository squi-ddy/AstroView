package com.example.astroview.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.astroview.api.data.StarReview
import com.example.astroview.databinding.CardReviewBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin

class StarCommentAdapter(
    private val commentList: Collection<StarReview>
) : RecyclerView.Adapter<StarCommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(commentList.elementAt(position))
    }

    override fun getItemCount() = commentList.size

    class ViewHolder(private val binding: CardReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context
        private val markWon = Markwon.builder(context).apply {
            usePlugin(TablePlugin.create(context))
            usePlugin(StrikethroughPlugin.create())
        }.build()

        fun bindItems(review: StarReview) {
            binding.reviewDatetime.text = review.time.getDateTimeString()
            markWon.setMarkdown(binding.reviewDescription, review.content)
            binding.reviewUsername.text = review.user
        }
    }
}