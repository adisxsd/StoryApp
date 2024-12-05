package com.dicoding.storyapp.view.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.utils.Result
import com.dicoding.storyapp.view.login.LoginActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import com.dicoding.storyapp.view.story.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var ivAddPhoto: ImageView
    private lateinit var edAddDescription: EditText
    private lateinit var buttonAdd: Button
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var userPreference: UserPreference

    private var selectedImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            ivAddPhoto.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addstory_activity)

        ivAddPhoto = findViewById(R.id.iv_add_photo)
        edAddDescription = findViewById(R.id.ed_add_description)
        buttonAdd = findViewById(R.id.button_add)

        userPreference = UserPreference(this)
        val factory = ViewModelFactory.getInstance(this)
        addStoryViewModel = ViewModelProvider(this, factory)[AddStoryViewModel::class.java]

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ivAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        buttonAdd.setOnClickListener {
            handleAddStory()
        }

        addStoryViewModel.addStoryResponse.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Gagal upload: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
                Result.Loading -> {
                    Toast.makeText(this, "Mengunggah...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleAddStory() {
        val description = edAddDescription.text.toString()

        lifecycleScope.launch {
            val token = getToken()

            if (selectedImageUri == null || description.isBlank() || token.isNullOrEmpty()) {
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@AddStoryActivity, "You need to log in first!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddStoryActivity, LoginActivity::class.java))
                } else {
                    Toast.makeText(this@AddStoryActivity, "Mohon lengkapi foto dan deskripsi.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Move file path extraction and file creation to IO dispatcher to prevent UI thread blocking
            val filePath = withContext(Dispatchers.IO) { getRealPathFromURI(selectedImageUri!!) }

            if (filePath != null) {
                val file = File(filePath)

                // Create RequestBody for file (photo)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                // Create RequestBody for description
                val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)

                // Call ViewModel to add story
                addStoryViewModel.addStory("Bearer $token", descriptionBody.toString(), body)
            } else {
                Toast.makeText(this@AddStoryActivity, "Gagal mendapatkan path file", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getRealPathFromURI(uri: Uri): String? {
        val file = File(cacheDir, "temp_image.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }

    private suspend fun getToken(): String? {
        val tokenFlow: Flow<String?> = userPreference.getToken()
        var token: String? = null

        tokenFlow.collect { value ->
            token = value
        }

        return token
    }
}
