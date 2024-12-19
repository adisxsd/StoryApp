package com.dicoding.storyapp.view.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.storyapp.databinding.AddstoryActivityBinding
import com.dicoding.storyapp.utils.Result
import com.dicoding.storyapp.view.story.MainActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: AddstoryActivityBinding
    private lateinit var addStoryViewModel: AddStoryViewModel
    private var photoFile: File? = null
    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddstoryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this)
        addStoryViewModel = ViewModelProvider(this, viewModelFactory)[AddStoryViewModel::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        addStoryViewModel.addStoryResponse.observe(this, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
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

        binding.buttonCamera.setOnClickListener {
            openCamera()
        }

        binding.buttonAdd.setOnClickListener {
            if (photoFile != null) {
                val description = binding.edAddDescription.text.toString()
                val latitude = currentLocation?.latitude
                val longitude = currentLocation?.longitude

                val compressedFile = compressAndResizeImage(photoFile!!)

                if (compressedFile.length() > 1000000) {  // 1 MB
                    Toast.makeText(this, "File terlalu besar, coba kurangi ukuran gambar", Toast.LENGTH_SHORT).show()
                } else {
                    addStoryViewModel.uploadStory(description, compressedFile, latitude, longitude)
                }
            } else {
                Toast.makeText(this, "Please select a photo first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLocation()
            } else {
                currentLocation = null
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile = createImageFile()
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.dicoding.storyapp.fileprovider",
                    it
                )
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File = cacheDir
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                photoFile = this
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        photoFile = uriToFile(uri)
                        photoFile = compressAndResizeImage(photoFile!!)
                        Glide.with(this)
                            .load(uri)
                            .into(binding.ivAddPhoto)
                        Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    photoFile?.let {
                        photoFile = compressAndResizeImage(it)
                        Glide.with(this)
                            .load(it)
                            .into(binding.ivAddPhoto)
                        Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    photoFile = null
                    Toast.makeText(this, "Camera action canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


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

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    Toast.makeText(
                        this,
                        "Location obtained: ${location.latitude}, ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun compressAndResizeImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizedBitmap = resizeImage(bitmap)
        val rotatedBitmap = correctImageRotation(resizedBitmap, file)

        val outputFile = File(cacheDir, "compressed_resized_${file.name}")
        try {
            val outputStream = FileOutputStream(outputFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Compress image with lower quality
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return outputFile
    }

    private fun resizeImage(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
    }

    private fun correctImageRotation(bitmap: Bitmap, file: File): Bitmap {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        var angle = 0
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            angle = 90
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            angle = 180
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            angle = 270
        }

        return if (angle != 0) {
            rotateImage(bitmap, angle)
        } else {
            bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
