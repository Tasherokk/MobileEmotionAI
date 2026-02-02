package com.example.emotionsai.ui.employee.requests

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentEmployeeRequestDetailsBinding
import com.example.emotionsai.di.ServiceLocator
import java.io.File
import java.io.FileOutputStream

class EmployeeRequestDetailsFragment : Fragment(R.layout.fragment_employee_request_details) {

    private var _vb: FragmentEmployeeRequestDetailsBinding? = null
    private val vb get() = _vb!!

    private val args: EmployeeRequestDetailsFragmentArgs by navArgs()

    private val vm: EmployeeRequestDetailsViewModel by viewModels {
        ServiceLocator.employeeRequestDetailsVMFactory(requireContext())
    }

    private lateinit var adapter: RequestMessagesAdapter
    private var pendingFile: File? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pendingFile = uriToTempFile(uri)
            toast("Файл прикреплён")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentEmployeeRequestDetailsBinding.bind(view)

        adapter = RequestMessagesAdapter { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        vb.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        vb.rvMessages.adapter = adapter

        vb.btnAttach.setOnClickListener { pickFile.launch("*/*") }

        vb.btnSend.setOnClickListener {
            val details = vm.details.value
            if (details?.status == "CLOSED") return@setOnClickListener toast("Запрос закрыт")

            val text = vb.inputText.text?.toString()?.trim()
            val file = pendingFile

            if ((text.isNullOrBlank()) && file == null) return@setOnClickListener toast("Введите текст или выберите файл")

            vm.send(args.requestId, text, file)
            vb.inputText.setText("")
            pendingFile = null
        }

        vm.details.observe(viewLifecycleOwner) { d ->
            if (d == null) return@observe
            vb.tvHeader.text = "Request #${d.id} • ${d.type_name} • ${d.status}"
            adapter.submitList(d.messages)
            if (adapter.itemCount > 0) vb.rvMessages.scrollToPosition(adapter.itemCount - 1)
        }

        vm.error.observe(viewLifecycleOwner) { if (!it.isNullOrBlank()) toast(it) }

        vm.load(args.requestId)
    }

    private fun uriToTempFile(uri: Uri): File {
        val f = File(requireContext().cacheDir, "req_${System.currentTimeMillis()}")
        requireContext().contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(f).use { out -> input?.copyTo(out) }
        }
        return f
    }

    private fun toast(s: String) =
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
