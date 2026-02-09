package com.example.f1fantasyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(emptyList())
        rv.adapter = adapter

        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        RetrofitClient.instance.getLeaderboard().enqueue(object : Callback<List<UserRanking>> {
            override fun onResponse(call: Call<List<UserRanking>>, response: Response<List<UserRanking>>) {
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    adapter.updateData(list)
                } else {
                    Toast.makeText(this@LeaderboardActivity, "Error loading ranking", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<UserRanking>>, t: Throwable) {
                Toast.makeText(this@LeaderboardActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}