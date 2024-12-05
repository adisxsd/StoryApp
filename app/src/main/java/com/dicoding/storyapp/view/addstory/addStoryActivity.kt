package com.dicoding.storyapp.view.addstory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.storyapp.databinding.AddstoryActivityBinding
import com.dicoding.storyapp.utils.Result
import com.dicoding.storyapp.view.story.MainActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import java.io.File
import java.io.IOException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: AddstoryActivityBinding
    private lateinit var addStoryViewModel: AddStoryViewModel
    private var photoFile: File? = null

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddstoryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this)
        addStoryViewModel = ViewModelProvider(this, viewModelFactory)[AddStoryViewModel::class.java]

        addStoryViewModel.addStoryResponse.observe(this, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, intent)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Failed to upload story: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.buttonGallery.setOnClickListener {
            openGallery()
        }

        binding.buttonAdd.setOnClickListener {
            if (photoFile != null) {
                val description = binding.edAddDescription.text.toString()
                addStoryViewModel.uploadStory(description, photoFile!!)
            } else {
                Toast.makeText(this, "Please select a photo first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open the gallery to pick an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            data?.data?.let { uri ->
                // Convert URI to File
                photoFile = uriToFile(uri)

                // Show the selected photo in ImageView using Glide
                Glide.with(this)
                    .load(uri) // You can load a File or URI here
                    .into(binding.ivAddPhoto) // Assuming ivSelectedPhoto is the ImageView's ID

                Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Helper function to convert URI to File
    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val tempFile = File(cacheDir, "temp_image.jpg")
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tempFile
    }
}
