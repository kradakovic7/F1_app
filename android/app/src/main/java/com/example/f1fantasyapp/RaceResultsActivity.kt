package com.example.f1fantasyapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RaceResultsActivity : AppCompatActivity() {
    private lateinit var adapter: ResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_results)

        val roundNum = intent.getIntExtra("ROUND_NUM", 1)
        val raceName = intent.getStringExtra("RACE_NAME") ?: "Race Results"

        findViewById<TextView>(R.id.tvRaceTitle).text = raceName

        val rv = findViewById<RecyclerView>(R.id.rvResults)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ResultsAdapter(emptyList())
        rv.adapter = adapter

        fetchResults(roundNum)
    }

    private fun fetchResults(round: Int) {
        RetrofitClient.instance.getRaceResults(round).enqueue(object : Callback<List<RaceResult>> {
            override fun onResponse(call: Call<List<RaceResult>>, response: Response<List<RaceResult>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    adapter.updateData(data)
                } else {
                    Toast.makeText(this@RaceResultsActivity, "No results yet!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RaceResult>>, t: Throwable) {
                Toast.makeText(this@RaceResultsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}