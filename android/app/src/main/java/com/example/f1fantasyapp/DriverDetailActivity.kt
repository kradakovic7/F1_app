package com.example.f1fantasyapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DriverDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_detail)

        val ivProfile: ImageView = findViewById(R.id.ivDetailProfile)
        val tvName: TextView = findViewById(R.id.tvDetailName)
        val tvTeam: TextView = findViewById(R.id.tvDetailTeam)
        val tvPoints: TextView = findViewById(R.id.tvDetailPoints)
        val tvPrice: TextView = findViewById(R.id.tvDetailPrice)
        val btnBack: ImageView = findViewById(R.id.btnBack)

        val name = intent.getStringExtra("NAME") ?: "Unknown"
        val surname = intent.getStringExtra("SURNAME") ?: ""
        val team = intent.getStringExtra("TEAM") ?: "No Team"
        val points = intent.getDoubleExtra("POINTS", 0.0)
        val price = intent.getDoubleExtra("PRICE", 0.0)

        val imageResId = intent.getIntExtra("IMAGE_RES_ID", R.drawable.default_driver)

        tvName.text = "$name"
        tvTeam.text = team
        tvPoints.text = "Points: ${points.toInt()}"
        tvPrice.text = "Price: ${price}M"

        ivProfile.setImageResource(imageResId)

        btnBack.setOnClickListener {
            finish()
        }
    }
}