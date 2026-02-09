package com.example.f1fantasyapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DriverAdapter(private var drivers: List<Driver>) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvName: TextView = view.findViewById(R.id.tvDriverName)
        val tvTeam: TextView = view.findViewById(R.id.tvTeam)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)

        val ivDriver: ImageView = view.findViewById(R.id.ivDriverProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = drivers[position]

        holder.tvRank.text = "${position + 1}"

        val fullName = if (driver.name.contains(driver.surname, ignoreCase = true)) {
            driver.name
        } else {
            "${driver.name} ${driver.surname}"
        }

        holder.tvName.text = fullName
        holder.tvTeam.text = driver.team
        holder.tvPrice.text = "${driver.price}M"
        holder.tvPoints.text = "${driver.points.toInt()} pts"

        val imageResId = getDriverImageResource(driver.surname)
        holder.ivDriver.setImageResource(imageResId)


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DriverDetailActivity::class.java)

            intent.putExtra("NAME", driver.name)
            intent.putExtra("SURNAME", driver.surname)
            intent.putExtra("TEAM", driver.team)
            intent.putExtra("POINTS", driver.points)
            intent.putExtra("PRICE", driver.price)

            intent.putExtra("IMAGE_RES_ID", imageResId)

            context.startActivity(intent)
        }
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

    override fun getItemCount() = drivers.size

    fun updateData(newDrivers: List<Driver>) {
        drivers = newDrivers
        notifyDataSetChanged()
    }
}