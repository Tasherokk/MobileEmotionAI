package com.example.emotionsai.ui.launch

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.emotionsai.databinding.ActivityLaunchBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity
import com.example.emotionsai.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        // Initial state: hidden, small, and slightly below
        binding.ivLogo.alpha = 0f
        binding.ivLogo.scaleX = 0.5f
        binding.ivLogo.scaleY = 0.5f
        
        binding.tvAppName.alpha = 0f
        binding.tvAppName.translationY = 50f

        // Slowly "Pop up" the logo
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1500)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        // Fade in and slide up the App Name
        binding.tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(600)
            .setDuration(1200)
            .withEndAction {
                proceedToNext()
            }
            .start()
    }

    private fun proceedToNext() {
        lifecycleScope.launch {
            delay(1000)
            
            val storage = ServiceLocator.tokenStorage(this@LaunchActivity)
            val next = if (storage.isLoggedIn()) {
                Intent(this@LaunchActivity, MainActivity::class.java)
            } else {
                Intent(this@LaunchActivity, LoginActivity::class.java)
            }
            next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(next)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
