package com.karthik.lifeos.tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karthik.lifeos.tracker.R
import com.karthik.lifeos.tracker.data.local.ActivitySession
import com.karthik.lifeos.tracker.utils.TimeUtils

/**
 * RecyclerView adapter for displaying activity sessions.
 * Uses ListAdapter with DiffUtil for efficient list updates.
 */
class SessionAdapter : ListAdapter<ActivitySession, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGameName: TextView = itemView.findViewById(R.id.tvGameName)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)

        fun bind(session: ActivitySession) {
            tvGameName.text = session.gameName
            tvDuration.text = TimeUtils.formatDuration(session.durationMillis)
            tvPackageName.text = session.packageName
            tvStartTime.text = itemView.context.getString(
                R.string.start_label, TimeUtils.formatTimestamp(session.startTime)
            )
            tvEndTime.text = itemView.context.getString(
                R.string.end_label, TimeUtils.formatTimestamp(session.endTime)
            )
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<ActivitySession>() {
        override fun areItemsTheSame(oldItem: ActivitySession, newItem: ActivitySession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivitySession, newItem: ActivitySession): Boolean {
            return oldItem == newItem
        }
    }
}
