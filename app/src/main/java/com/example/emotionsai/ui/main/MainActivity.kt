package com.example.emotionsai.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.emotionsai.databinding.ActivityMainBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Guard: if tokens missing -> back to login
        if (!ServiceLocator.tokenStorage(this).isLoggedIn()) {
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        val navHost = supportFragmentManager.findFragmentById(vb.navHost.id) as NavHostFragment
        vb.bottomNav.setupWithNavController(navHost.navController)
    }
}
