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
import androidx.core.graphics.toColorInt

class StatsFragment : Fragment() {
    private val emotionColors = mapOf(
        "happy" to "#4CAF50".toColorInt(),
        "sad" to "#2196F3".toColorInt(),
        "angry" to "#F44336".toColorInt(),
        "surprised" to "#FFEB3B".toColorInt(),
        "neutral" to "#9E9E9E".toColorInt(),
        "fear" to "#3F51B5".toColorInt(),
        "disgust" to "#8BC34A".toColorInt()
    )
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StatsViewModel

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
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
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
//
//                    binding.tvHistoryEmpty.visibility =
//                        if (state.history.isEmpty()) View.VISIBLE else View.GONE
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

    private fun displayStats(stats: com.example.emotionsai.data.remote.FeedbackResponse) {
        // Total submissions

        // Average confidence

        // Top emotion
        val emoji = getEmotionEmoji(stats.emotion)
        binding.tvTopEmotionEmoji.text = emoji
        binding.tvTopEmotionName.text = stats.emotion.uppercase()

        binding.layoutEmotions.removeAllViews()
        // Emotion distribution
        val chip = Chip(requireContext()).apply {
            chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                emotionColors[stats.emotion]?.toInt() ?: Color.GRAY
            )
            setTextColor(Color.WHITE)
            isClickable = false
            isCheckable = false
        }
        binding.layoutEmotions.addView(chip)


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
