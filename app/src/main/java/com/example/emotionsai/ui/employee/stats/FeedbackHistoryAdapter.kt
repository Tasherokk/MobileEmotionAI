package com.example.emotionsai.ui.employee.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.Feedback
import com.example.emotionsai.databinding.ItemFeedbackHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackHistoryAdapter : ListAdapter<Feedback, FeedbackHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeedbackHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemFeedbackHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(feedback: Feedback) {
            val emoji = getEmotionEmoji(feedback.emotion)
            binding.tvEmotionEmoji.text = emoji
            binding.tvEmotionName.text = feedback.emotion.uppercase()
            binding.tvConfidence.text = "${(feedback.confidence * 100).toInt()}%"
            
            // Parse and format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
                val date = inputFormat.parse(feedback.created_at.substringBefore("Z").substringBefore("."))
                binding.tvDate.text = date?.let { outputFormat.format(it) } ?: feedback.created_at
            } catch (e: Exception) {
                binding.tvDate.text = feedback.created_at
            }

            // Event info
            if (feedback.event_title != null) {
                binding.tvEvent.text = "📌 ${feedback.event_title}"
                binding.tvEvent.visibility = android.view.View.VISIBLE
            } else {
                binding.tvEvent.visibility = android.view.View.GONE
            }
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

    private class DiffCallback : DiffUtil.ItemCallback<Feedback>() {
        override fun areItemsTheSame(oldItem: Feedback, newItem: Feedback) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Feedback, newItem: Feedback) = 
            oldItem == newItem
    }
}
