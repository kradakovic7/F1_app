package com.example.f1fantasyapp

import android.os.Bundle
import android.view.MenuItem // <--- TOLE JE MANJKALO
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DriversActivity : AppCompatActivity() {

    private lateinit var adapter: DriverAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Driver Standings" // Lep naslov

        recyclerView = findViewById(R.id.rvDrivers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DriverAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchDrivers()
    }

    private fun fetchDrivers() {
        RetrofitClient.instance.getDrivers().enqueue(object : Callback<List<Driver>> {
            override fun onResponse(call: Call<List<Driver>>, response: Response<List<Driver>>) {
                if (response.isSuccessful) {
                    val driverList = response.body() ?: emptyList()

                    adapter = DriverAdapter(driverList)
                    recyclerView.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                Toast.makeText(this@DriversActivity, "Error loading drivers: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}