package com.dicoding.storyapp.view.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.view.addstory.AddStoryActivity
import com.dicoding.storyapp.view.detailStory.DetailActivity
import com.dicoding.storyapp.view.login.LoginActivity
import com.dicoding.storyapp.view.maps.MapsActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreference: UserPreference
    private lateinit var storyAdapter: StoryPagingAdapter
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(applicationContext)

        storyAdapter = StoryPagingAdapter { story ->
            Log.d("MainActivity", "Story clicked: ${story.id}")
            if (story.id != null) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
                startActivity(intent)
            } else {
                Log.e("MainActivity", "Story ID is null!")
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }

        setSupportActionBar(binding.toolbar)
        checkSession()

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            val token = userPreference.getToken().first()
            Log.d("MainActivity", "Token: $token")
            if (!token.isNullOrEmpty()) {
                storyViewModel.getStories(token)
                showLoading(true)
                observeStories()
            } else {
                redirectToLogin()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val token = userPreference.getToken().first()
            Log.d("MainActivity", "Token: $token")
            if (!token.isNullOrEmpty()) {
                storyViewModel.getStories(token)
                showLoading(true)
                observeStories()
            } else {
                redirectToLogin()
            }
        }
    }

    private fun observeStories() {
        storyViewModel.pagingData.observe(this) { pagingData ->
            Log.d("MainActivity", "Paging data received: $pagingData")
            storyAdapter.submitData(lifecycle, pagingData)
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun checkSession() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (!isLoggedIn) {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToMaps() {
        val intent = Intent(this@MainActivity, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun performLogout() {
        lifecycleScope.launch {
            userPreference.clearToken()
            redirectToLogin()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            R.id.action_maps -> {
                redirectToMaps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}




