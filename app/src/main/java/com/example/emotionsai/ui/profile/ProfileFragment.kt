package com.example.emotionsai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.UserRole
import com.example.emotionsai.databinding.FragmentProfileBinding
import com.example.emotionsai.ui.login.LoginActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _vb: FragmentProfileBinding? = null
    private val vb get() = _vb!!

    private val vm: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vb = FragmentProfileBinding.bind(view)

        // Setup logout button first, before any loading state changes
        vb.btnLogout.setOnClickListener { 
            Log.d("ProfileFragment", "Logout button clicked")
            showLogoutDialog()
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            vb.progress.visibility = if (loading) View.VISIBLE else View.GONE
            // Don't hide layoutContent completely, just show loading indicator
        }

        vm.me.observe(viewLifecycleOwner) { me ->
            if (me != null) {
                // Display name
                val displayName = if (me.name.isNotBlank()) me.name else me.username
                vb.tvHello.text = displayName
                vb.tvUsername.text = "@${me.username}"

                // Display role
                val role = UserRole.from(me.role)
                vb.tvRole.text = when (role) {
                    UserRole.HR -> "HR Manager"
                    UserRole.EMPLOYEE -> "Employee"
                }
                vb.tvRoleEmoji.text = when (role) {
                    UserRole.HR -> "👔"
                    UserRole.EMPLOYEE -> "💼"
                }

                // Display company
                if (!me.company_name.isNullOrBlank()) {
                    vb.cardCompany.visibility = View.VISIBLE
                    vb.tvCompanyName.text = me.company_name
                } else {
                    vb.cardCompany.visibility = View.GONE
                }

                // Display department
                if (!me.department_name.isNullOrBlank()) {
                    vb.cardDepartment.visibility = View.VISIBLE
                    vb.tvDepartmentName.text = me.department_name
                } else {
                    vb.cardDepartment.visibility = View.GONE
                }
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
            Log.d("ProfileFragment", "forceLogout observed: $go")
            if (go == true) {
                Log.d("ProfileFragment", "Starting logout process")
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
        Log.d("ProfileFragment", "showLogoutDialog called")
        
        if (!isAdded) {
            Log.e("ProfileFragment", "Fragment not added!")
            return
        }
        
        val activity = activity
        if (activity == null || activity.isFinishing) {
            Log.e("ProfileFragment", "Activity null or finishing!")
            return
        }
        
        Log.d("ProfileFragment", "Creating dialog...")
        try {
            val dialog = AlertDialog.Builder(activity)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ -> 
                    Log.d("ProfileFragment", "Logout confirmed")
                    vm.logout() 
                }
                .setNegativeButton("Cancel") { dlg, _ ->
                    Log.d("ProfileFragment", "Logout cancelled")
                    dlg.dismiss()
                }
                .setCancelable(true)
                .create()
            
            Log.d("ProfileFragment", "Dialog created, showing...")
            dialog.show()
            Log.d("ProfileFragment", "Dialog.show() called, isShowing=${dialog.isShowing}")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error showing dialog", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
