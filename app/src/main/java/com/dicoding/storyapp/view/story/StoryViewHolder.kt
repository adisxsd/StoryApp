package com.dicoding.storyapp.view.story

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.databinding.ItemStoryBinding

class StoryViewHolder(
    private val binding: ItemStoryBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(story: ListStoryItem, onItemClick: ((ListStoryItem) -> Unit)?) {
        binding.tvItemName.text = story.name
        binding.tvItemDescription.text = story.description
        Glide.with(binding.ivItemPhoto.context)
            .load(story.photoUrl)
            .placeholder(R.drawable.baseline_image_24)
            .into(binding.ivItemPhoto)

        binding.root.setOnClickListener {
            onItemClick?.invoke(story)
        }
    }
}

