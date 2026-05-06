package com.example.terptrack.controller

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.terptrack.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.umd.terptrack.model.ItemRepository

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val repository = ItemRepository()
    private lateinit var mapView: MapView
    private var itemLat: Double = 0.0
    private var itemLng: Double = 0.0
    private var itemTitle: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE) ?: ""
        val description = intent.getStringExtra(MainActivity.EXTRA_DESCRIPTION) ?: ""
        val building = intent.getStringExtra(MainActivity.EXTRA_BUILDING) ?: ""
        val imageUrl = intent.getStringExtra(MainActivity.EXTRA_IMAGE_URL) ?: ""
        val rating = intent.getFloatExtra(MainActivity.EXTRA_RATING, 0f)
        val itemTimestamp = intent.getLongExtra(MainActivity.EXTRA_TIMESTAMP, 0L)
        val documentId  = itemTimestamp.toString()
        itemLat = intent.getDoubleExtra(MainActivity.EXTRA_LATITUDE, 0.0)
        itemLng = intent.getDoubleExtra(MainActivity.EXTRA_LONGITUDE, 0.0)

        findViewById<TextView>(R.id.tvDetailTitle).text = itemTitle
        findViewById<TextView>(R.id.tvDetailLocation).text = building
        findViewById<TextView>(R.id.tvDetailDescription).text = description
        findViewById<RatingBar>(R.id.ratingDetailCondition).rating = rating

        // PART 3: Load image from Firebase using Glide
        val imageView = findViewById<ImageView>(R.id.imgDetailPhoto)
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(imageView)
        }

        // PART 3: Initialize the MapView
        mapView = findViewById(R.id.mapViewDetail)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnFound).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Mark as Found?")
                .setMessage("Are you sure you want to remove \"$title\" from the feed? This means the item has been returned to its owner.")
                .setPositiveButton("Yes, it's found") { _, _ ->
                    repository.deleteItem(
                        documentId = documentId,
                        onSuccess  = {
                            Toast.makeText(this, "\"$title\" has been marked as found", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onFailure  = { e ->
                            Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // PART 3: Google Map callback
    override fun onMapReady(googleMap: GoogleMap) {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        when (prefs.getString(MainActivity.KEY_MAP_TYPE, "Normal")) {
            "Satellite" -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            "Terrain" -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            else -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        if (itemLat != 0.0 || itemLng != 0.0) {
            val position = LatLng(itemLat, itemLng)
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(itemTitle)
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        }
    }

    // PART 3: MapView lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}