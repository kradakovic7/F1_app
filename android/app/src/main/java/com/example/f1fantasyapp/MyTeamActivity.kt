package com.example.f1fantasyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_team)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Fantasy Team"

        fetchMyTeam()
    }

    private fun fetchMyTeam() {
        val userId = UserManager.userId
        if (userId == -1) return

        RetrofitClient.instance.getMyTeam(userId).enqueue(object : Callback<MyTeamResponse> {
            override fun onResponse(call: Call<MyTeamResponse>, response: Response<MyTeamResponse>) {

                if (response.isSuccessful && response.body() != null) {
                    val teamData = response.body()!!

                    if (teamData.drivers.isNullOrEmpty()) {
                        redirectToTeamSelection()
                    } else {
                        displayData(teamData)
                    }
                }
                else {
                    redirectToTeamSelection()
                }
            }

            override fun onFailure(call: Call<MyTeamResponse>, t: Throwable) {
                Toast.makeText(this@MyTeamActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToTeamSelection() {
        Toast.makeText(this, "Let's build your team first!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, TeamSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayData(data: MyTeamResponse) {
        try {
            findViewById<TextView>(R.id.tvBudget)?.text = "${String.format("%.1f", data.budget)} M"
            findViewById<TextView>(R.id.tvTotalPoints)?.text = "${data.totalPoints.toInt()} pts"

            // --- 1. VOZNIKI (DRIVERS) ---
            val driversContainer = findViewById<LinearLayout>(R.id.layoutDriversContainer)
            driversContainer?.removeAllViews()

            data.drivers.forEachIndexed { index, driver ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_driver, driversContainer, false)

                val fullName = if (driver.name.contains(driver.surname, ignoreCase = true)) driver.name else "${driver.name} ${driver.surname}"

                view.findViewById<TextView>(R.id.tvDriverName)?.text = fullName
                view.findViewById<TextView>(R.id.tvTeam)?.text = driver.team
                view.findViewById<TextView>(R.id.tvPoints)?.text = "${driver.points.toInt()} pts"
                view.findViewById<TextView>(R.id.tvRank)?.text = "${index + 1}"
                view.findViewById<TextView>(R.id.tvPrice)?.text = "${driver.price}M"

                val ivDriver = view.findViewById<ImageView>(R.id.ivDriverProfile)
                ivDriver?.setImageResource(getDriverImageResource(driver.surname))

                driversContainer?.addView(view)
            }

            // --- 2. EKIPE (CONSTRUCTORS) ---
            val teamsContainer = findViewById<LinearLayout>(R.id.layoutTeamsContainer)
            teamsContainer?.removeAllViews()

            data.constructors.forEach { team ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_driver, teamsContainer, false)

                view.findViewById<TextView>(R.id.tvDriverName)?.text = team.name
                view.findViewById<TextView>(R.id.tvTeam)?.text = "Constructor"
                view.findViewById<TextView>(R.id.tvPoints)?.text = "${team.points.toInt()} pts"
                view.findViewById<TextView>(R.id.tvPrice)?.text = "${team.price}M"
                view.findViewById<TextView>(R.id.tvRank)?.text = ""

                val ivDriver = view.findViewById<ImageView>(R.id.ivDriverProfile)
                ivDriver?.setImageResource(getConstructorImageResource(team.name))

                teamsContainer?.addView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun getDriverImageResource(surname: String): Int {
        return when (surname.lowercase()) {
            "verstappen" -> R.drawable.verstappen
            "leclerc" -> R.drawable.leclerc
            "hamilton" -> R.drawable.hamilton
            "norris" -> R.drawable.norris
            "piastri" -> R.drawable.piastri
            "sainz" -> R.drawable.sainz
            "russell" -> R.drawable.russell
            "alonso" -> R.drawable.alonso
            "antonelli" -> R.drawable.antonelli
            "albon" -> R.drawable.albon
            "tsunoda" -> R.drawable.tsunoda
            "hadjar" -> R.drawable.hadjar
            "lawson" -> R.drawable.lawson
            "stroll" -> R.drawable.stroll
            "ocon" -> R.drawable.ocon
            "bearman" -> R.drawable.bearman
            "hulkenberg" -> R.drawable.hulkenberg
            "bortoleto" -> R.drawable.bortoleto
            "gasly" -> R.drawable.gasly
            "colapinto" -> R.drawable.colapinto
            else -> R.drawable.default_driver
        }
    }

    fun getConstructorImageResource(teamName: String): Int {

        return when {
            teamName.contains("Red Bull", ignoreCase = true) -> R.drawable.redbull
            teamName.contains("Ferrari", ignoreCase = true) -> R.drawable.ferrari
            teamName.contains("Mercedes", ignoreCase = true) -> R.drawable.mercedes
            teamName.contains("McLaren", ignoreCase = true) -> R.drawable.mclaren
            teamName.contains("Aston", ignoreCase = true) -> R.drawable.astonmartin
            teamName.contains("Alpine", ignoreCase = true) -> R.drawable.alpine
            teamName.contains("Williams", ignoreCase = true) -> R.drawable.williams
            teamName.contains("Visa", ignoreCase = true)  -> R.drawable.rb
            teamName.contains("Sauber", ignoreCase = true) -> R.drawable.sauber
            teamName.contains("Haas", ignoreCase = true) -> R.drawable.haas
            else -> R.drawable.default_driver
        }
    }
}