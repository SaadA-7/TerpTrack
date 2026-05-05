package com.example.terptrack.controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.terptrack.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.umd.terptrack.controller.ItemAdapter
import com.umd.terptrack.model.ItemRepository
import com.umd.terptrack.model.LostItem

class MainActivity : AppCompatActivity() {

    private val repository = ItemRepository()
    private lateinit var adapter: ItemAdapter
    private var items: List<LostItem> = emptyList()

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val name = preferences.getString(KEY_DISPLAY_NAME, "Terrappin")
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome back, $name!"

        adapter = ItemAdapter(emptyList()){ i ->
            startActivity(Intent(this, ItemDetailActivity::class.java).apply {
                putExtra(EXTRA_TITLE,i.title)
                putExtra(EXTRA_DESCRIPTION,i.description)
                putExtra(EXTRA_BUILDING,i.buildingName)
                putExtra(EXTRA_IMAGE_URL, i.imageUrl)
                putExtra(EXTRA_RATING,i.conditionRating)
                putExtra(EXTRA_TIMESTAMP,i.timestamp)
                // PART 3: Pass GPS coordinates to detail map
                putExtra(EXTRA_LATITUDE, i.latitude)
                putExtra(EXTRA_LONGITUDE, i.longitude)
            })
        }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewFeed)
        rv.adapter = adapter

        repository.getAllItems(
            onResult = { i ->
                items = i

                val fvProgress = findViewById<SeekBar>(R.id.seekBarDistance).progress
                val filtered = if (fvProgress == 0) {
                    items
                }
                else {
                    items.filter { e -> e.conditionRating <= fvProgress }
                }
                adapter.updateData(filtered)
            },
            onFailure = { e ->
                Toast.makeText(this, "Sytem Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )

        val seekBar= findViewById<SeekBar>(R.id.seekBarDistance)
        val tvSeekValue= findViewById<TextView>(R.id.tvSeekValue)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                tvSeekValue.text = if (progress == 0){
                    "All"
                } else {
                    "$progress miles"
                }

                val filtered = if (progress == 0) {
                    items
                }
                else {
                    items.filter { e -> e.conditionRating <= progress }
                }
                adapter.updateData(filtered)

            }
            override fun onStartTrackingTouch(sb: SeekBar) {

            }
            override fun onStopTrackingTouch(sb: SeekBar) {

            }
        })

        tvSeekValue.text = "${seekBar.progress} miles"


        findViewById<Button>(R.id.btnReport).setOnClickListener {
            startActivity(Intent(this, ReportItemActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val adView = AdView(this)
        adView.setAdSize(AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT))
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        findViewById<LinearLayout>(R.id.ad_view).addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        val name = preferences.getString(KEY_DISPLAY_NAME, "Terp")
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome back, $name!"

        repository.getAllItems(
            onResult = { i ->
                items = i
                val progress = findViewById<SeekBar>(R.id.seekBarDistance).progress
                val filtered = if (progress == 0) {
                    items
                }
                else {
                    items.filter { e -> e.conditionRating <= progress }
                }
                adapter.updateData(filtered)
            },
            onFailure = {}
        )
    }

    companion object {
        const val PREFS_NAME= "TerpTrackPrefs"
        const val KEY_DISPLAY_NAME= "user_display_name"
        const val KEY_MAP_TYPE= "preferred_map_type"
        const val EXTRA_TITLE= "extra_title"
        const val EXTRA_DESCRIPTION= "extra_description"
        const val EXTRA_BUILDING= "extra_building"
        const val EXTRA_IMAGE_URL= "extra_image_url"
        const val EXTRA_RATING= "extra_rating"
        const val EXTRA_TIMESTAMP= "extra_timestamp"
        // PART 3: New extras for map coordinates
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }
}