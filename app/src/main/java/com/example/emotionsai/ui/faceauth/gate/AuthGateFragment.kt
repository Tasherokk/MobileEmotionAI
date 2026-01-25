package com.example.emotionsai.ui.faceauth.gate

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.emotionsai.R
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity
import kotlinx.coroutines.launch
import com.example.emotionsai.util.Result

class AuthGateFragment : Fragment(R.layout.fragment_auth_gate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenStorage = ServiceLocator.tokenStorage(requireContext())
        val settings = ServiceLocator.settingsStorage(requireContext())
        val userRepo = ServiceLocator.userRepository(requireContext())

        if (!tokenStorage.isLoggedIn()) {
            goLogin()
            return
        }

        // Проверяем сессию одним защищённым запросом.
        lifecycleScope.launch {
            when (val r = userRepo.me()) {
                is Result.Ok -> {
                    if (settings.isFaceIdEnabled()) {
                        // TODO: заменить action id на твой
                        findNavController().navigate(R.id.action_authGate_to_faceLogin)
                    } else {
                        // TODO: заменить action id на твой home (в HR и Employee разные)
                        findNavController().navigate(R.id.action_authGate_to_home)
                    }
                }
                is Result.Err -> {
                    // refresh не сработал -> принудительный логин
                    ServiceLocator.authRepository(requireContext()).logout()
                    goLogin()
                }
            }
        }
    }

    private fun goLogin() {
        val i = Intent(requireContext(), LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        requireActivity().finish()
    }
}
