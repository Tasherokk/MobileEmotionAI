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
            vm.login(
                vb.etUsername.text?.toString().orEmpty(),
                vb.etPassword.text?.toString().orEmpty()
            )
        }

        vb.btnRegister.setOnClickListener {
            // для примера: name можно взять из username (или добавь отдельное поле)
            val u = vb.etUsername.text?.toString().orEmpty()
            val p = vb.etPassword.text?.toString().orEmpty()
            vm.register(u, name = u, password = p)
        }

        vm.loading.observe(this) { loading ->
            vb.progress.visibility = if (loading) View.VISIBLE else View.GONE
            vb.btnLogin.isEnabled = !loading
            vb.btnRegister.isEnabled = !loading
        }

        vm.error.observe(this) { err ->
            vb.tvError.text = err
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
