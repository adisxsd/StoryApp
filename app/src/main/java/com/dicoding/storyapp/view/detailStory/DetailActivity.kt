package com.dicoding.storyapp.view.detailStory

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.databinding.StoryDetailBinding
import com.dicoding.storyapp.utils.formatDateFromIso
import com.dicoding.storyapp.viewmodel.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: StoryDetailBinding
    private val storyDetailViewModel: StoryDetailViewModel by viewModels {
        ViewModelFactory.getInstance(applicationContext)  // Menggunakan ViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storyDescriptionTextView = findViewById<TextView>(R.id.tv_detail_description)

        val animationDescription = ObjectAnimator.ofFloat(storyDescriptionTextView, "translationY", 1000f, 0f)
        animationDescription.duration = 1000
        animationDescription.start()

        binding = StoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        Log.d("DetailActivity", "Story ID: $storyId")

        // Show loading progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Observasi data story detail
        storyDetailViewModel.storyDetail.observe(this, Observer { story ->
            if (story != null) {
                showStoryDetail(story)
            } else {
                Log.e("DetailActivity", "Story data is null!")
                showError()
            }
        })

        // Memuat detail story berdasarkan storyId
        storyId?.let { storyDetailViewModel.loadStoryDetail(it) }
    }

    private fun showStoryDetail(story: Story) {
        // Set the Story name, description, and created date
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        binding.tvDetailCreatedAt.text = formatDateFromIso(story.createdAt)  // Use extension function

        // Load the image using Glide
        Glide.with(this)
            .load(story.photoUrl)
            .placeholder(R.drawable.baseline_image_24)
            .into(binding.ivDetailPhoto)

        // Hide progress bar once the data is loaded
        binding.progressBar.visibility = View.GONE
    }

    private fun showError() {
        // Tampilkan pesan error jika data gagal dimuat
        binding.tvDetailName.text = getString(R.string.error_loading_story)
        binding.tvDetailDescription.text = ""
        binding.tvDetailCreatedAt.text = ""

        // Hide progress bar on error
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        const val EXTRA_STORY_ID = "EXTRA_STORY_ID"
    }
}
