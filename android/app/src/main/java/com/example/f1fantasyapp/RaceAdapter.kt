package com.example.f1fantasyapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RaceAdapter(private var races: List<Race>) : RecyclerView.Adapter<RaceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRound: TextView = view.findViewById(R.id.tvRound)
        val tvName: TextView = view.findViewById(R.id.tvRaceName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_race, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val race = races[position]
        holder.tvRound.text = race.round.toString()
        holder.tvName.text = race.name
        holder.tvLocation.text = race.location
        holder.tvDate.text = race.date

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RaceResultsActivity::class.java)

            intent.putExtra("ROUND_NUM", race.round)
            intent.putExtra("RACE_NAME", race.name)

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = races.size

    fun updateData(newRaces: List<Race>) {
        races = newRaces
        notifyDataSetChanged()
    }
}