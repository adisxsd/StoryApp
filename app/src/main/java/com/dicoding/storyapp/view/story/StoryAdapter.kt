package com.dicoding.storyapp.view.story

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.databinding.ItemStoryBinding

class StoryPagingAdapter(
    private val onItemClickListener: (ListStoryItem) -> Unit
) : PagingDataAdapter<ListStoryItem, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            Log.d("StoryPagingAdapter", "Binding story: ${story.name}")
            holder.bind(story)
            holder.itemView.setOnClickListener {
                onItemClickListener(story)
            }
        }
    }


    class StoryViewHolder(
        private val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: ListStoryItem) {
            binding.apply {
                tvItemName.text = story.name
                tvItemDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.image_error)
                    .into(ivItemPhoto)
            }
        }
    }
}
