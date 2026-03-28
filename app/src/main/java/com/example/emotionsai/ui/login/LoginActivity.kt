package com.example.emotionsai.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emotionsai.databinding.ActivityLoginBinding
import com.example.emotionsai.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var vb: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.btnLogin.setOnClickListener {
            // Clear previous errors when attempting a new login
            vb.tvError.visibility = View.GONE
            
            vm.login(
                vb.etUsername.text?.toString().orEmpty(),
                vb.etPassword.text?.toString().orEmpty()
            )
        }

        vm.error.observe(this) { err ->
            if (!err.isNullOrBlank()) {
                vb.tvError.text = err
                vb.tvError.visibility = View.VISIBLE
            } else {
                vb.tvError.visibility = View.GONE
            }
        }

        vm.loading.observe(this) { isLoading ->
            vb.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            vb.btnLogin.isEnabled = !isLoading
        }

        vm.success.observe(this) { ok ->
            if (ok) {
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }
        }
    }
}
