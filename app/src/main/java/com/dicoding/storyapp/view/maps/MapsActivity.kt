package com.dicoding.storyapp.view.maps

import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.dicoding.storyapp.utils.UiState
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeLocation()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapSettings()
        getMyLocation()
        setMapStyle()
    }

    private fun setupMapSettings() {
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
    }

    private fun observeLocation() {
        viewModel.stories.observe(this) { uiState ->
            when (uiState) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    val data = uiState.data // List<ListStoryItem>
                    addStoryMarkers(data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE

                }
            }
            Log.d("MapsActivity", "Data Maps story: $uiState")

        }
    }

    private fun addStoryMarkers(stories: List<ListStoryItem>) {
        var firstLocation: LatLng? = null

        stories.forEach { story ->
            val lat = (story.lat as? String)?.toDoubleOrNull() ?: (story.lat as? Double)
            val lon = (story.lon as? String)?.toDoubleOrNull() ?: (story.lon as? Double)

            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.name)
                        .snippet(story.description)
                )

                // Capture the first valid location for the camera move
                if (firstLocation == null) {
                    firstLocation = latLng
                }
            }
        }

        // Move the camera to the first valid location after all markers have been added
        firstLocation?.let {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
        }
    }




    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                showErrorSnackbar("Location permission is required to display your current location.")
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ${exception.message}")
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
