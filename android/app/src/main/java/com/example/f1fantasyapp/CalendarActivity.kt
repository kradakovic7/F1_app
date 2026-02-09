package com.example.f1fantasyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalendarActivity : AppCompatActivity() {

    private lateinit var adapter: RaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val rv = findViewById<RecyclerView>(R.id.rvCalendar)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = RaceAdapter(emptyList())
        rv.adapter = adapter

        fetchCalendar()
    }

    private fun fetchCalendar() {
        RetrofitClient.instance.getCalendar().enqueue(object : Callback<List<Race>> {
            override fun onResponse(call: Call<List<Race>>, response: Response<List<Race>>) {
                if (response.isSuccessful) {
                    val races = response.body() ?: emptyList()
                    adapter.updateData(races)
                } else {
                    Toast.makeText(this@CalendarActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Race>>, t: Throwable) {
                Toast.makeText(this@CalendarActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}