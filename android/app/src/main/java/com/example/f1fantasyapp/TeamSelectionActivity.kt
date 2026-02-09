package com.example.f1fantasyapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeamSelectionActivity : AppCompatActivity() {

    private var allDrivers: List<Driver> = emptyList()
    private var allConstructors: List<Constructor> = emptyList()

    private val selectedDriverIds = mutableSetOf<Int>()
    private val selectedConstructorIds = mutableSetOf<Int>()

    private var currentBudget = 120.0

    private lateinit var tvBudget: TextView
    private lateinit var tvDriverCount: TextView
    private lateinit var tvConstructorCount: TextView
    private lateinit var btnSave: Button
    private lateinit var rvSelection: RecyclerView
    private lateinit var btnShowDrivers: Button
    private lateinit var btnShowConstructors: Button

    private lateinit var adapter: SelectionAdapter
    private var isViewingDrivers = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_selection)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Build Your Team"

        // Povezava UI
        tvBudget = findViewById(R.id.tvBudget)
        tvDriverCount = findViewById(R.id.tvDriverCount)
        tvConstructorCount = findViewById(R.id.tvConstructorCount)
        btnSave = findViewById(R.id.btnSaveTeam)
        rvSelection = findViewById(R.id.rvSelection)
        btnShowDrivers = findViewById(R.id.btnShowDrivers)
        btnShowConstructors = findViewById(R.id.btnShowConstructors)

        // Setup RecyclerView
        rvSelection.layoutManager = LinearLayoutManager(this)
        adapter = SelectionAdapter()
        rvSelection.adapter = adapter

        // Gumbi za preklop
        btnShowDrivers.setOnClickListener {
            isViewingDrivers = true
            updateButtonStyles()
            adapter.notifyDataSetChanged()
        }

        btnShowConstructors.setOnClickListener {
            isViewingDrivers = false
            updateButtonStyles()
            adapter.notifyDataSetChanged()
        }

        btnSave.setOnClickListener {
            saveTeamToBackend()
        }

        fetchData()
        updateUI()
    }

    // 2. LOGIKA ZA GUMB NAZAJ
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchData() {
        RetrofitClient.instance.getDrivers().enqueue(object : Callback<List<Driver>> {
            override fun onResponse(call: Call<List<Driver>>, response: Response<List<Driver>>) {
                if (response.isSuccessful) {
                    allDrivers = response.body() ?: emptyList()
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {}
        })

        RetrofitClient.instance.getConstructors().enqueue(object : Callback<List<Constructor>> {
            override fun onResponse(call: Call<List<Constructor>>, response: Response<List<Constructor>>) {
                if (response.isSuccessful) {
                    allConstructors = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Constructor>>, t: Throwable) {}
        })
    }

    private fun updateUI() {
        tvBudget.text = "${String.format("%.1f", currentBudget)}M"
        tvDriverCount.text = "${selectedDriverIds.size}/5"
        tvConstructorCount.text = "${selectedConstructorIds.size}/2"

        val isValid = selectedDriverIds.size == 5 && selectedConstructorIds.size == 2 && currentBudget >= 0
        btnSave.isEnabled = isValid
        btnSave.alpha = if (isValid) 1.0f else 0.5f

        if (currentBudget < 0) {
            tvBudget.setTextColor(Color.RED)
        } else {
            tvBudget.setTextColor(Color.parseColor("#4CAF50"))
        }
    }

    private fun updateButtonStyles() {
        if (isViewingDrivers) {
            btnShowDrivers.setBackgroundColor(Color.parseColor("#E10600"))
            btnShowConstructors.setBackgroundColor(Color.parseColor("#333333"))
        } else {
            btnShowDrivers.setBackgroundColor(Color.parseColor("#333333"))
            btnShowConstructors.setBackgroundColor(Color.parseColor("#E10600"))
        }
    }

    private fun saveTeamToBackend() {
        val userId = UserManager.userId
        if (userId == -1) return

        android.util.Log.d("FANTASY_DEBUG", "Saving team for user: $userId")
        android.util.Log.d("FANTASY_DEBUG", "Driver IDs: ${selectedDriverIds.toList()}")
        android.util.Log.d("FANTASY_DEBUG", "Constructor IDs: ${selectedConstructorIds.toList()}")

        val request = SaveTeamRequest(
            user_id = userId,
            driver_ids = selectedDriverIds.toList(),
            constructor_ids = selectedConstructorIds.toList()
        )

        RetrofitClient.instance.saveTeam(request).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TeamSelectionActivity, "Team Saved! Good Luck.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@TeamSelectionActivity, MyTeamActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@TeamSelectionActivity, "Error saving team", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                Toast.makeText(this@TeamSelectionActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
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

    inner class SelectionAdapter : RecyclerView.Adapter<SelectionAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvDriverName)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
            val tvPoints: TextView = view.findViewById(R.id.tvPoints)
            val tvRank: TextView = view.findViewById(R.id.tvRank)
            val ivProfile: ImageView = view.findViewById(R.id.ivDriverProfile) // Slika
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (isViewingDrivers) {
                val driver = allDrivers[position]

                val fullName = if (driver.name.contains(driver.surname, ignoreCase = true)) {
                    driver.name
                } else {
                    "${driver.name} ${driver.surname}"
                }
                holder.tvName.text = fullName

                holder.tvPrice.text = "${driver.price}M"
                holder.tvPoints.text = "${driver.points.toInt()} pts"
                holder.tvRank.text = "${position + 1}"

                holder.ivProfile.setImageResource(getDriverImageResource(driver.surname))

                val isSelected = selectedDriverIds.contains(driver.id)
                highlightItem(holder.itemView, isSelected)

                holder.itemView.setOnClickListener { toggleDriver(driver) }

            } else {
                val team = allConstructors[position]
                holder.tvName.text = team.name
                holder.tvPrice.text = "${team.price}M"
                holder.tvPoints.text = "${team.points.toInt()} pts"
                holder.tvRank.text = ""

                holder.ivProfile.setImageResource(R.drawable.default_driver)

                val isSelected = selectedConstructorIds.contains(team.id)
                highlightItem(holder.itemView, isSelected)

                holder.itemView.setOnClickListener { toggleConstructor(team) }
            }
        }

        override fun getItemCount(): Int {
            return if (isViewingDrivers) allDrivers.size else allConstructors.size
        }

        private fun highlightItem(view: View, isSelected: Boolean) {
            if (isSelected) {
                view.setBackgroundColor(Color.parseColor("#2A442B"))
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun toggleDriver(driver: Driver) {
            if (selectedDriverIds.contains(driver.id)) {
                selectedDriverIds.remove(driver.id)
                currentBudget += driver.price
            } else {
                if (selectedDriverIds.size >= 5) {
                    Toast.makeText(this@TeamSelectionActivity, "Max 5 Drivers!", Toast.LENGTH_SHORT).show()
                    return
                }
                if (currentBudget - driver.price < 0) {
                    Toast.makeText(this@TeamSelectionActivity, "Not enough budget!", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedDriverIds.add(driver.id)
                currentBudget -= driver.price
            }
            updateUI()
            notifyDataSetChanged()
        }

        private fun toggleConstructor(team: Constructor) {
            if (selectedConstructorIds.contains(team.id)) {
                selectedConstructorIds.remove(team.id)
                currentBudget += team.price
            } else {
                if (selectedConstructorIds.size >= 2) {
                    Toast.makeText(this@TeamSelectionActivity, "Max 2 Teams!", Toast.LENGTH_SHORT).show()
                    return
                }
                if (currentBudget - team.price < 0) {
                    Toast.makeText(this@TeamSelectionActivity, "Not enough budget!", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedConstructorIds.add(team.id)
                currentBudget -= team.price
            }
            updateUI()
            notifyDataSetChanged()
        }
    }
}