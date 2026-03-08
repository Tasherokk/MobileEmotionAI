package com.example.emotionsai.ui.hr.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentHrProfileBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity

class HrProfileFragment : Fragment(R.layout.fragment_hr_profile) {

    private var _vb: FragmentHrProfileBinding? = null
    private val vb get() = _vb!!

    private val vm: HrProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vb = FragmentHrProfileBinding.bind(view)

        val settings = ServiceLocator.settingsStorage(requireContext())

        vb.swFaceLogin.isChecked = settings.isFaceIdEnabled()

        vb.swFaceLogin.setOnCheckedChangeListener { _, isChecked ->
            settings.setFaceIdEnabled(isChecked)
        }

        vb.btnLogout.setOnClickListener { showLogoutDialog() }

        vm.me.observe(viewLifecycleOwner) { me ->
            me ?: return@observe
            vb.tvUsername.text = "@${me.username}"
            vb.tvHello.text = me.name.ifBlank { me.username }
        }

        vm.error.observe(viewLifecycleOwner) {
            // Error handling if needed, tvError removed in new UI
        }

        vm.loading.observe(viewLifecycleOwner) {
            vb.loadingOverlay.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.forceLogout.observe(viewLifecycleOwner) { go ->
            if (go == true) {
                vm.logoutHandled()
                val i = Intent(requireContext(), LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                requireActivity().finish()
            }
        }

        vm.loadMe()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> vm.logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
