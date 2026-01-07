package com.example.emotionsai.ui.employee.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.emotionsai.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayResult()
        setupClickListeners()
    }

    private fun displayResult() {
        val emotion = args.emotion
        val confidence = args.confidence
        val top3 = args.top3

        // Display main emotion with emoji
        val emoji = getEmotionEmoji(emotion)
        binding.tvEmotionEmoji.text = emoji
        binding.tvEmotionName.text = emotion.uppercase()

        // Display confidence
        binding.tvConfidence.text = "${(confidence * 100).toInt()}%"
        binding.progressConfidence.progress = (confidence * 100).toInt()

        // Display top 3 emotions
        val top3List = top3.map { 
            val parts = it.split(":")
            Pair(parts[0], parts[1].toFloatOrNull() ?: 0f)
        }

        if (top3List.size >= 2) {
            binding.tvEmotion2.text = "• ${top3List[1].first} ${(top3List[1].second * 100).toInt()}%"
        }
        if (top3List.size >= 3) {
            binding.tvEmotion3.text = "• ${top3List[2].first} ${(top3List[2].second * 100).toInt()}%"
        }

        // Display motivational message
        binding.tvMessage.text = getMotivationalMessage(emotion, confidence)
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

    private fun getMotivationalMessage(emotion: String, confidence: Float): String {
        return when (emotion.lowercase()) {
            "happy" -> "Great mood! Keep up the positive vibes! 🌟"
            "sad" -> "Everyone has tough days. Remember, tomorrow is a new opportunity! 💪"
            "angry" -> "Take a deep breath. Everything will be okay. 🌈"
            "surprised" -> "Expect the unexpected! Life is full of surprises! ✨"
            "neutral" -> "Steady and calm. A balanced state of mind. 🧘"
            "fear" -> "You've got this! Face your challenges with courage! 🦁"
            "disgust" -> "Some things bother us, but we can move past them. 🌸"
            else -> "Your emotions are valid. Take care of yourself! ❤️"
        }
    }

    private fun setupClickListeners() {
        binding.btnDone.setOnClickListener {
            findNavController().navigate(ResultFragmentDirections.actionResultToEmployeeHome())
        }

        binding.btnViewStats.setOnClickListener {
            findNavController().navigate(ResultFragmentDirections.actionResultToStats())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
