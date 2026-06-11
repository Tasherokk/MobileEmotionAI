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
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch

class AuthGateFragment : Fragment(R.layout.fragment_auth_gate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenStorage = ServiceLocator.tokenStorage(requireContext())
        val settings = ServiceLocator.settingsStorage(requireContext())
        val userRepo = ServiceLocator.userRepository(requireContext())
        val authRepo = ServiceLocator.authRepository(requireContext())

        if (!tokenStorage.isLoggedIn()) {
            goLogin()
            return
        }

        lifecycleScope.launch {
            when (val r = userRepo.me()) {
                is Result.Ok -> {
                    if (settings.isFaceIdEnabled()) {
                        findNavController().navigate(R.id.action_authGate_to_faceLogin)
                    } else {
                        findNavController().navigate(R.id.action_authGate_to_home)
                    }
                }
                is Result.Err -> {
                    val isAuthDead = r.message.contains("HTTP_401") || r.message.contains("HTTP_403")
                    if (isAuthDead) {
                        authRepo.logout()
                        goLogin()
                    } else {
                        // Network/server error — token may still be valid, proceed to app
                        if (settings.isFaceIdEnabled()) {
                            findNavController().navigate(R.id.action_authGate_to_faceLogin)
                        } else {
                            findNavController().navigate(R.id.action_authGate_to_home)
                        }
                    }
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
