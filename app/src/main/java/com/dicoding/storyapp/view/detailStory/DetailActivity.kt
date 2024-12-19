package com.dicoding.storyapp.view.detailStory

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
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
        ViewModelFactory.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val animationDescription = ObjectAnimator.ofFloat(binding.tvDetailDescription, "translationY", 1000f, 0f)
        animationDescription.duration = 1000
        animationDescription.start()

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        Log.d("DetailActivity", "Story ID: $storyId")

        binding.progressBar.visibility = View.VISIBLE

        storyDetailViewModel.storyDetail.observe(this, Observer { story ->
            if (story != null) {
                showStoryDetail(story)
            } else {
                Log.e("DetailActivity", "Story data is null!")
                showError()
            }
        })

        storyId?.let { storyDetailViewModel.loadStoryDetail(it) }
    }

    private fun showStoryDetail(story: Story) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        binding.tvDetailCreatedAt.text = formatDateFromIso(story.createdAt)

        Glide.with(this)
            .load(story.photoUrl)
            .placeholder(R.drawable.baseline_image_24)
            .into(binding.ivDetailPhoto)

        binding.progressBar.visibility = View.GONE
    }

    private fun showError() {
        binding.tvDetailName.text = getString(R.string.error_loading_story)
        binding.tvDetailDescription.text = ""
        binding.tvDetailCreatedAt.text = ""

        binding.progressBar.visibility = View.GONE
    }

    companion object {
        const val EXTRA_STORY_ID = "EXTRA_STORY_ID"
    }
}
