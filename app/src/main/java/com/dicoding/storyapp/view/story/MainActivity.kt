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
import com.dicoding.storyapp.utils.Result
import com.dicoding.storyapp.utils.showToast
import com.dicoding.storyapp.view.addstory.AddStoryActivity
import com.dicoding.storyapp.view.detailStory.DetailActivity
import com.dicoding.storyapp.view.login.LoginActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreference: UserPreference
    private lateinit var storyAdapter: StoryAdapter
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(applicationContext)
        storyAdapter = StoryAdapter(listOf())


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }

        checkSession()


        storyAdapter.setOnItemClickListener { story ->
            Log.d("MainActivity", "Story clicked: ${story.id}")
            if (story.id != null) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
                startActivity(intent)
            } else {
                Log.e("MainActivity", "Story ID is null!")
            }
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }

        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch {
            val token = userPreference.getToken().first()
            refreshStoriesFromAPI()
            token?.let {
                storyViewModel.getStories(it).observe(this@MainActivity) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            val stories = result.data
                            if (stories.isNotEmpty()) {
                                storyAdapter.submitList(stories)
                            } else {
                                showToast(this@MainActivity, "No stories available.")
                            }
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showToast(this@MainActivity, "Error: ${result.exception.localizedMessage}")
                        }
                    }
                }
            } ?: run {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val token = userPreference.getToken().first()
            token?.let {
                storyViewModel.getStories(it).observe(this@MainActivity) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            val stories = result.data
                            if (stories.isNotEmpty()) {
                                val currentStories = storyAdapter.currentList.toMutableList()
                                val newStory = stories.first() // Mengambil cerita terbaru
                                currentStories.add(0, newStory) // Menambahkan cerita baru ke posisi pertama

                                // Memperbarui adapter dengan daftar cerita baru
                                storyAdapter.submitList(currentStories)

                                // Scroll langsung ke cerita terbaru
                                binding.recyclerView.smoothScrollToPosition(0)
                            } else {
                                showToast(this@MainActivity, "No stories available.")
                            }
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showToast(this@MainActivity, "Error: ${result.exception.localizedMessage}")
                        }
                    }
                }
            } ?: run {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }




    private suspend fun refreshStoriesFromAPI() {
        val token = userPreference.getToken().first()
        token?.let {
            storyViewModel.getStories(it).observe(this@MainActivity) { result ->
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Success -> {
                        showLoading(false)
                        val stories = result.data
                        if (stories.isNotEmpty()) {
                            val currentList = storyAdapter.currentList.toMutableList()
                            currentList.addAll(stories)
                            storyAdapter.submitList(currentList)
                        } else {
                            showToast(this@MainActivity, "No stories available.")
                        }
                    }
                    is Result.Error -> {
                        showLoading(false)
                        showToast(this@MainActivity, "Error: ${result.exception.localizedMessage}")
                    }
                }
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun checkSession() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)

        Log.d("MainActivity", "isLoggedIn: $isLoggedIn")
        if (!isLoggedIn) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            userPreference.clearToken()

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}

