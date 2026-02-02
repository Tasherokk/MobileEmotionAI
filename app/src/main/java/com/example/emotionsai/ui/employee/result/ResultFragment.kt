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

        // Display main emotion with emoji
        val emoji = getEmotionEmoji(emotion)
        binding.tvEmotionEmoji.text = emoji
        binding.tvEmotionName.text = emotion.uppercase()

        // Display motivational message
        binding.tvMessage.text = getMotivationalMessage(emotion)
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

    private fun getMotivationalMessage(emotion: String): String {
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
            findNavController().navigate(ResultFragmentDirections.actionResultFragmentToEmployeeEventsFragment())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
