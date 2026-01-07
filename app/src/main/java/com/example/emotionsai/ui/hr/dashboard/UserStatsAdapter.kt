package com.example.emotionsai.ui.hr.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.UserStats
import com.example.emotionsai.databinding.ItemUserStatsBinding

class UserStatsAdapter : ListAdapter<UserStats, UserStatsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserStatsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ViewHolder(private val binding: ItemUserStatsBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(userStats: UserStats, position: Int) {
            binding.tvPosition.text = "$position"
            binding.tvUserName.text = userStats.name
            binding.tvUsername.text = "@${userStats.username}"
            binding.tvTotalFeedbacks.text = "${userStats.total} feedbacks"
            binding.tvAvgConfidence.text = "${(userStats.avg_confidence * 100).toInt()}%"
            
            val emoji = getEmotionEmoji(userStats.top_emotion ?: "neutral")
            binding.tvTopEmotion.text = "$emoji ${userStats.top_emotion?.uppercase() ?: "N/A"}"
        }

        private fun getEmotionEmoji(emotion: String): String {
            return when (emotion.lowercase()) {
                "happy" -> "😊"
                "sad" -> "😢"
                "angry" -> "😠"
                "surprised" -> "😮"
                "neutral" -> "😐"
                "fear" -> "😨"
                "disgust" -> "😖"
                else -> "🙂"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UserStats>() {
        override fun areItemsTheSame(oldItem: UserStats, newItem: UserStats) = 
            oldItem.user_id == newItem.user_id

        override fun areContentsTheSame(oldItem: UserStats, newItem: UserStats) = 
            oldItem == newItem
    }
}
