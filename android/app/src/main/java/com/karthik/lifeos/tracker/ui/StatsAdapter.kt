package com.karthik.lifeos.tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karthik.lifeos.tracker.R
import com.karthik.lifeos.tracker.data.local.ActivityStats
import com.karthik.lifeos.tracker.utils.TimeUtils

class StatsAdapter : ListAdapter<ActivityStats, StatsAdapter.StatsViewHolder>(StatsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStatsGameName: TextView = itemView.findViewById(R.id.tvStatsGameName)
        private val tvTotalTime: TextView = itemView.findViewById(R.id.tvTotalTime)
        private val tvSessionCount: TextView = itemView.findViewById(R.id.tvSessionCount)

        fun bind(stats: ActivityStats) {
            tvStatsGameName.text = stats.gameName
            tvTotalTime.text = TimeUtils.formatDuration(stats.totalPlayTimeMillis)
            tvSessionCount.text = itemView.context.getString(
                R.string.session_count_label, stats.sessionCount
            )
        }
    }

    private class StatsDiffCallback : DiffUtil.ItemCallback<ActivityStats>() {
        override fun areItemsTheSame(oldItem: ActivityStats, newItem: ActivityStats): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        override fun areContentsTheSame(oldItem: ActivityStats, newItem: ActivityStats): Boolean {
            return oldItem == newItem
        }
    }
}
