package com.example.emotionsai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentProfileBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _vb: FragmentProfileBinding? = null
    private val vb get() = _vb!!

    private val vm: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vb = FragmentProfileBinding.bind(view)
        vb.swFaceLogin.isChecked = ServiceLocator.settingsStorage(requireContext()).isFaceIdEnabled()
        
        vb.btnLogout.setOnClickListener { showLogoutDialog() }
        
        vb.swFaceLogin.setOnClickListener {
            ServiceLocator.settingsStorage(requireContext()).setFaceIdEnabled(vb.swFaceLogin.isChecked)
        }
        
        vm.loading.observe(viewLifecycleOwner) { loading ->
            vb.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.me.observe(viewLifecycleOwner) { me ->
            if (me != null) {
                vb.tvHello.text = if (me.name.isNotBlank()) me.name else me.username
                vb.tvUsername.text = "@${me.username}"
                vb.tvRole.text = me.role
                vb.tvCompanyName.text = me.company_name ?: "—"
                vb.tvDepartmentName.text = me.department_name ?: "—"
            }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            if (err.isNotBlank()) {
                vb.tvError.text = err
                vb.tvError.visibility = View.VISIBLE
            } else {
                vb.tvError.visibility = View.GONE
            }
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
