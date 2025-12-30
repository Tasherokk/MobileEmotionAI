package com.example.emotionsai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentProfileBinding
import com.example.emotionsai.ui.login.LoginActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _vb: FragmentProfileBinding? = null
    private val vb get() = _vb!!

    private val vm: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentProfileBinding.bind(view)

        vb.btnLogout.setOnClickListener { vm.logout() }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            vb.progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.me.observe(viewLifecycleOwner) { me ->
            if (me != null) {
                val who = if (me.name.isNotBlank()) me.name else me.username
                vb.tvHello.text = "Привет, $who!"
            }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            vb.tvError.text = err
        }

        vm.forceLogout.observe(viewLifecycleOwner) { go ->
            if (go) {
                val i = Intent(requireContext(), LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                requireActivity().finish()
            }
        }

        vm.loadMe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
