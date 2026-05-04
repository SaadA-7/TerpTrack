package com.example.terptrack.controller

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.terptrack.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val preferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val username = findViewById<EditText>(R.id.etDisplayName)
        val spinner = findViewById<Spinner>(R.id.spinnerMapType)

        val mapOptions = listOf("Normal", "Satellite", "Terrain")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapOptions
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        username.setText(preferences.getString(MainActivity.KEY_DISPLAY_NAME, "Terrapin"))
        spinner.setSelection(mapOptions.indexOf(preferences.getString(MainActivity.KEY_MAP_TYPE, "Normal")).coerceAtLeast(0))

        val btnSave = findViewById<Button>(R.id.btnSaveSettings)
        btnSave.setOnClickListener {

            var name = username.text.toString().trim()
            if (name.isEmpty()) {
                name = "Terrapin"
            }

            preferences.edit().putString(MainActivity.KEY_DISPLAY_NAME, name)
                .putString(MainActivity.KEY_MAP_TYPE, spinner.selectedItem.toString()).apply()

            Toast.makeText(this, "Settings have been successfully saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        /* this is when cancel is clicked we go back to old screen */
        val btnCancel = findViewById<Button>(R.id.btnCancelSettings)
        btnCancel.setOnClickListener { finish() }
    }
}