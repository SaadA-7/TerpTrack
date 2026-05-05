package com.example.terptrack.controller

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.terptrack.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.umd.terptrack.model.ItemRepository
import com.umd.terptrack.model.LostItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportItemActivity : AppCompatActivity(), OnMapReadyCallback {

    // edit - add Google Map reference
    private var mMap: GoogleMap? = null
    private val repository = ItemRepository()
    private var currentRating: Float = 3f
    private var selectedImageUri: Uri? = null

    // PART 3: GPS fields
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // PART 3: Camera URI for the temp photo file
    private var cameraPhotoUri: Uri? = null

    // Launcher that handles the camera result
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraPhotoUri != null) {
                selectedImageUri = cameraPhotoUri
                findViewById<ImageView>(R.id.imgPhotoPreview).setImageURI(cameraPhotoUri)
            } else {
                Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher to request CAMERA permission at runtime
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
            }
        }

    // Launcher to request LOCATION permission at runtime
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineGranted || coarseGranted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied — enter location manually", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fvTitle = findViewById<EditText>(R.id.etTitle)
        val fvDescription = findViewById<EditText>(R.id.etDescription)
        val fvLocation = findViewById<AutoCompleteTextView>(R.id.etLocation)

        // edit - listen for when the user types an address and hits "Enter" or "Done"
        fvLocation.setOnEditorActionListener { _, actionId, _ ->
            val locationName = fvLocation.text.toString().trim()
            if (locationName.isNotEmpty()) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    // Convert the typed address into coordinates
                    val addresses = geocoder.getFromLocationName(locationName, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // Update our global variables for the Submit button
                        currentLatitude = address.latitude
                        currentLongitude = address.longitude
                        // Move the map!
                        updateMapLocation(currentLatitude, currentLongitude)
                        Toast.makeText(this, "Map updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Could not find that address on the map", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("GPS", "Geocode from text failed", e)
                }
            }
            false // Returns false to let the keyboard close normally
        }

        val fvRatingBar = findViewById<RatingBar>(R.id.ratingBarCondition)
        val btnPhoto = findViewById<Button>(R.id.btnChoosePhoto)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnCancel = findViewById<Button>(R.id.btnBack)
        // edit - Initialize Google Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // PART 3: Initialize location client and auto-fill GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationAndFill()

        btnCancel.setOnClickListener {
            finish()
        }

        fvRatingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    currentRating = rating
                }
            }

        // PART 3: Open the CAMERA instead of gallery
        btnPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnSubmit.setOnClickListener {
            val title = fvTitle.text.toString().trim()
            val description = fvDescription.text.toString().trim()
            val location = fvLocation.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                repository.uploadImage(
                    imageUri = selectedImageUri!!,
                    onSuccess = { url -> saveItem(title, description, location, url) },
                    onFailure = { e ->
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                saveItem(title, description, location, imageUrl = "")
            }
        }
    }

    // PART 3: Create a temp file and launch the camera
    private fun launchCamera() {
        val photoFile = createImageFile()
        cameraPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(cameraPhotoUri!!)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("TERPTRACK_${timestamp}_", ".jpg", storageDir)
    }

    // PART 3: GPS auto-fill logic
    private fun requestLocationAndFill() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    // edit - tell the map to move to this location
                    updateMapLocation(currentLatitude, currentLongitude)

                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val addressLine = address.getAddressLine(0) ?: "${address.locality ?: ""}"
                            findViewById<AutoCompleteTextView>(R.id.etLocation).setText(addressLine)
                        } else {
                            findViewById<AutoCompleteTextView>(R.id.etLocation)
                                .setText("%.5f, %.5f".format(location.latitude, location.longitude))
                        }
                    } catch (e: Exception) {
                        Log.e("GPS", "Geocoder failed", e)
                        findViewById<AutoCompleteTextView>(R.id.etLocation)
                            .setText("%.5f, %.5f".format(location.latitude, location.longitude))
                    }
                } else {
                    Toast.makeText(this, "Could not get location — enter manually", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("GPS", "Location fetch failed", e)
                Toast.makeText(this, "Location unavailable — enter manually", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveItem(title: String, description: String, location: String, imageUrl: String) {
        val item = LostItem(
            title = title,
            description = description,
            buildingName = location,
            imageUrl = imageUrl,
            conditionRating = currentRating,
            timestamp = System.currentTimeMillis(),
            // ===== PART 3: Attach GPS coordinates =====
            latitude = currentLatitude,
            longitude = currentLongitude
        )

        repository.addItem(
            item = item,
            onSuccess = {
                Toast.makeText(this, "Item successfully reported!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { e ->
                Toast.makeText(this, "Submit failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Edit - add Map Logic
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // If GPS already found our location before the map finished loading, update it now
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            updateMapLocation(currentLatitude, currentLongitude)
        }
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val currentLatLng = LatLng(lat, lng)
        mMap?.let { map ->
            map.clear() // Clear old markers
            map.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
        }
    }

}