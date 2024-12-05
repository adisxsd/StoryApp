package com.dicoding.storyapp.view.story

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.ListStoryItem

class StoryAdapter(listOf: List<Any>) : ListAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    private var onItemClickListener: ((ListStoryItem) -> Unit)? = null

    // Mengatur listener untuk item klik
    fun setOnItemClickListener(listener: (ListStoryItem) -> Unit) {
        onItemClickListener = listener
    }

    // Membuat ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    // Mengikat data ke ViewHolder
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position) // Menggunakan getItem untuk mengambil item dari currentList
        story?.let {
            holder.bind(it)
            holder.itemView.setOnClickListener {
                Log.d("StoryAdapter", "Item clicked at position $position")
                onItemClickListener?.invoke(story)
            }
        }
    }

    // Menggunakan currentList untuk menghitung jumlah item
    override fun getItemCount(): Int = currentList.size

    // ViewHolder untuk item story
    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_item_photo)
        private val textName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val textDescription: TextView = itemView.findViewById(R.id.tv_item_description)

        fun bind(story: ListStoryItem) {
            textName.text = story.name
            textDescription.text = story.description

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.baseline_image_24)
                .into(imageView)
        }
    }

    // DiffUtil callback untuk mendeteksi perubahan data
    class StoryDiffCallback : DiffUtil.ItemCallback<ListStoryItem>() {
        override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
            return oldItem.id == newItem.id // Membandingkan berdasarkan ID
        }

        override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
            return oldItem == newItem // Membandingkan konten
        }
    }
}
