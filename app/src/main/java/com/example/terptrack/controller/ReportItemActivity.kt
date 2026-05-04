package com.example.terptrack.controller

import android.net.Uri
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.terptrack.R
import com.umd.terptrack.model.ItemRepository
import com.umd.terptrack.model.LostItem

class ReportItemActivity : AppCompatActivity() {

    private val repository = ItemRepository()
    private var currentRating: Float = 3f
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                findViewById<ImageView>(R.id.imgPhotoPreview).setImageURI(it)
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

        val fvTitle= findViewById<EditText>(R.id.etTitle)
        val fvDescription= findViewById<EditText>(R.id.etDescription)
        val fvLocation= findViewById<AutoCompleteTextView>(R.id.etLocation)
        val fvRatingBar= findViewById<RatingBar>(R.id.ratingBarCondition)
        val btnPhoto= findViewById<Button>(R.id.btnChoosePhoto)
        val btnSubmit= findViewById<Button>(R.id.btnSubmit)
        val btnCancel= findViewById<Button>(R.id.btnBack)

        btnCancel.setOnClickListener {
            finish()
        }

        fvRatingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    currentRating = rating
                }
            }

        btnPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener { e ->
            val title= fvTitle.text.toString().trim()
            val description= fvDescription.text.toString().trim()
            val location= fvLocation.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Fill in all required fields", Toast.LENGTH_SHORT).show()
            }

            if (selectedImageUri != null) {
                repository.uploadImage(
                    imageUri= selectedImageUri!!,
                    onSuccess= { e -> saveItem(title, description, location, e) },
                    onFailure= { e ->
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                saveItem(title, description, location, imageUrl = "")
            }
        }
    }

    private fun saveItem(title: String, description: String, location: String, imageUrl: String) {

        val item = LostItem(
            title= title,
            description= description,
            buildingName= location,
            imageUrl= imageUrl,
            conditionRating= currentRating,
            timestamp= System.currentTimeMillis()
        )

        repository.addItem(
            item= item,
            onSuccess= {
                Toast.makeText(this, "Item successfuly reported!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure= { e ->
                Toast.makeText(this, "Submit failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }
}