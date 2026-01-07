package com.example.emotionsai.ui.employee.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.databinding.FragmentStatsBinding
import com.example.emotionsai.di.ServiceLocator
import com.google.android.material.chip.Chip

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StatsViewModel
    private lateinit var adapter: FeedbackHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val feedbackRepo = ServiceLocator.feedbackRepository(requireContext())
        viewModel = StatsViewModel(feedbackRepo)

        setupRecyclerView()
        setupPeriodSelector()
        setupObservers()
        setupRefresh()
    }

    private fun setupRecyclerView() {
        adapter = FeedbackHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupPeriodSelector() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            val period = when (checkedIds[0]) {
                binding.chipWeek.id -> StatsViewModel.Period.WEEK
                binding.chipMonth.id -> StatsViewModel.Period.MONTH
                binding.chipAll.id -> StatsViewModel.Period.ALL
                else -> StatsViewModel.Period.WEEK
            }
            viewModel.selectPeriod(period)
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is StatsUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutContent.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }
                is StatsUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutContent.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE
                    
                    displayStats(state.stats)
                    adapter.submitList(state.history)
                    
                    binding.tvHistoryEmpty.visibility = 
                        if (state.history.isEmpty()) View.VISIBLE else View.GONE
                }
                is StatsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutContent.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    private fun displayStats(stats: com.example.emotionsai.data.remote.MyStatsResponse) {
        // Total submissions
        binding.tvTotalCount.text = stats.total.toString()

        // Average confidence
        binding.tvAvgConfidence.text = "${(stats.avg_confidence * 100).toInt()}%"
        binding.progressAvgConfidence.progress = (stats.avg_confidence * 100).toInt()

        // Top emotion
        val emoji = getEmotionEmoji(stats.top_emotion ?: "neutral")
        binding.tvTopEmotionEmoji.text = emoji
        binding.tvTopEmotionName.text = stats.top_emotion?.uppercase() ?: "N/A"

        // Emotion distribution
        if (stats.emotions.isNotEmpty()) {
            val colors = listOf(
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#9C27B0")  // Purple
            )

            binding.layoutEmotions.removeAllViews()
            stats.emotions.take(5).forEachIndexed { index, emotionCount ->
                val chip = Chip(requireContext()).apply {
                    text = "${emotionCount.emotion} ${emotionCount.percent.toInt()}%"
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        colors[index % colors.size]
                    )
                    setTextColor(Color.WHITE)
                    isClickable = false
                    isCheckable = false
                }
                binding.layoutEmotions.addView(chip)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
