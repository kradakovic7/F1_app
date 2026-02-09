package com.example.f1fantasyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = UserManager.username ?: "Driver"
        findViewById<TextView>(R.id.tvWelcome).text = "WELCOME, ${username.uppercase()}"

        findViewById<Button>(R.id.btnFantasy).setOnClickListener {
            val intent = Intent(this, MyTeamActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnDrivers).setOnClickListener {
            startActivity(Intent(this, DriversActivity::class.java))
        }

        findViewById<Button>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        findViewById<Button>(R.id.btnLeaderboard).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure? This will permanently delete your team and progress. This action cannot be undone.")
            .setPositiveButton("DELETE") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val userId = UserManager.userId
        if (userId == -1) return

        RetrofitClient.instance.deleteUser(userId).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Account deleted. Goodbye!", Toast.LENGTH_LONG).show()

                    UserManager.userId = -1
                    UserManager.username = null
                    UserManager.isAdmin = false

                    val intent = Intent(this@HomeActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@HomeActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}