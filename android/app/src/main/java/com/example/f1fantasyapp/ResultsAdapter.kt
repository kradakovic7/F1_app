package com.example.f1fantasyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(private var results: List<RaceResult>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPos: TextView = view.findViewById(R.id.tvPos)
        val tvDriver: TextView = view.findViewById(R.id.tvDriver)
        val tvTeam: TextView = view.findViewById(R.id.tvTeam)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val res = results[position]
        holder.tvPos.text = res.position.toString()
        holder.tvDriver.text = res.driver
        holder.tvTeam.text = res.team
        holder.tvTime.text = res.time
        holder.tvPoints.text = "+${res.points}"
    }

    override fun getItemCount() = results.size

    fun updateData(newResults: List<RaceResult>) {
        results = newResults
        notifyDataSetChanged()
    }
}