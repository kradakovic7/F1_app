package com.example.f1fantasyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color

class LeaderboardAdapter(private var users: List<UserRanking>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRankPos)
        val tvName: TextView = view.findViewById(R.id.tvUsername)
        val tvPoints: TextView = view.findViewById(R.id.tvTotalPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.tvRank.text = "${position + 1}"
        holder.tvName.text = user.username
        holder.tvPoints.text = "${user.points.toInt()} pts"

        if (user.id == UserManager.userId) {
            holder.tvName.setTextColor(Color.CYAN)
        } else {
            holder.tvName.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserRanking>) {
        users = newUsers
        notifyDataSetChanged()
    }
}