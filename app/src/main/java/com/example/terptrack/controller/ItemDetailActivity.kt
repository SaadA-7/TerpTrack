package com.example.terptrack.controller

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.terptrack.R

class ItemDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val title= intent.getStringExtra(MainActivity.EXTRA_TITLE)
        val description= intent.getStringExtra(MainActivity.EXTRA_DESCRIPTION)
        val building= intent.getStringExtra(MainActivity.EXTRA_BUILDING)
        val imageUrl= intent.getStringExtra(MainActivity.EXTRA_IMAGE_URL)
        val rating= intent.getFloatExtra(MainActivity.EXTRA_RATING, 0f)

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailLocation).text= "$building"
        findViewById<TextView>(R.id.tvDetailDescription).text= description

        var ratingBar = findViewById<RatingBar>(R.id.ratingDetailCondition)
        ratingBar.rating= rating

        // image would go here once you guys create it
        val imageView = findViewById<ImageView>(R.id.imgDetailPhoto)


        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}