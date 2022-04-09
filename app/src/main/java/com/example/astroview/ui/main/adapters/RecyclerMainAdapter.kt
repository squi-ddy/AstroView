package com.example.astroview.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.astroview.api.data.StarReview
import com.example.astroview.databinding.CardReviewBinding

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
        fun bindItems(review: StarReview) {
            binding.reviewDatetime.text = review.time.getDateTimeString()
            binding.reviewDescription.text = review.content
            binding.reviewUsername.text = review.user
        }
    }
}