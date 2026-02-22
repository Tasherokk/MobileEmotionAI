package com.example.emotionsai.ui.hr.requests

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
import com.example.emotionsai.databinding.FragmentHrRequestDetailsBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.employee.requests.RequestMessagesAdapter
import java.io.File
import java.io.FileOutputStream
import android.content.ContentResolver
import android.provider.OpenableColumns
class HrRequestDetailsFragment : Fragment(R.layout.fragment_hr_request_details) {

    private var _vb: FragmentHrRequestDetailsBinding? = null
    private val vb get() = _vb!!

    private val args: HrRequestDetailsFragmentArgs by navArgs()

    private val vm: HrRequestDetailsViewModel by viewModels {
        ServiceLocator.hrRequestDetailsVMFactory(requireContext())
    }

    private lateinit var adapter: RequestMessagesAdapter
    private var pendingFile: File? = null
    private var pendingFileName: String? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val name = requireContext().contentResolver.getDisplayName(uri) ?: "attachment"
            pendingFile = uriToTempFile(uri, name)
            pendingFileName = name
            showAttachment(name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentHrRequestDetailsBinding.bind(view)

        adapter = RequestMessagesAdapter { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        vb.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
        vb.rvMessages.adapter = adapter
        val path = savedInstanceState?.getString("pending_path")
        val name = savedInstanceState?.getString("pending_name")

        if (!path.isNullOrBlank() && !name.isNullOrBlank()) {
            val f = File(path)
            if (f.exists()) {
                pendingFile = f
                pendingFileName = name
                showAttachment(name)
            }
        }
        vb.btnAttach.setOnClickListener { pickFile.launch("*/*") }
        vb.btnRemoveAttachment.setOnClickListener { clearAttachment() }
        vb.btnSend.setOnClickListener {
            val details = vm.details.value
            if (details?.status == "CLOSED") return@setOnClickListener toast("Request is closed")

            val text = vb.inputText.text?.toString()?.trim()
            val file = pendingFile
            if ((text.isNullOrBlank()) && file == null) return@setOnClickListener toast("Enter a message or attach a file")

            vm.send(args.requestId, text, file)
            vb.inputText.setText("")
            clearAttachment() // ✅ важно
        }

        vb.btnInProgress.setOnClickListener {
            val d = vm.details.value ?: return@setOnClickListener
            if (d.status == "CLOSED") return@setOnClickListener toast("Can't change the closed request")
            vm.setInProgress(args.requestId)
        }

        vb.btnClose.setOnClickListener {
            val d = vm.details.value ?: return@setOnClickListener
            if (d.status == "CLOSED") return@setOnClickListener toast("Request is already closed")
            vm.close(args.requestId)
        }

        vm.details.observe(viewLifecycleOwner) { d ->
            if (d == null) return@observe
            vb.tvHeader.text = "Request #${d.id} • ${d.type_name} • ${d.status}"
            adapter.submitList(d.messages)
            if (adapter.itemCount > 0) vb.rvMessages.scrollToPosition(adapter.itemCount - 1)
        }

        vm.error.observe(viewLifecycleOwner) { if (!it.isNullOrBlank()) toast(it) }
        vb.btnRemoveAttachment.setOnClickListener {
            clearAttachment()
        }
        vm.load(args.requestId)
    }

    private fun uriToTempFile(uri: Uri, displayName: String): File {
        val safeName = displayName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val f = File(requireContext().cacheDir, "req_${System.currentTimeMillis()}_$safeName")
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
    private fun showAttachment(name: String) {
        vb.tvAttachmentName.text = name
        vb.attachmentCard.visibility = View.VISIBLE
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pending_path", pendingFile?.absolutePath)
        outState.putString("pending_name", pendingFileName)
    }

    private fun clearAttachment() {
        pendingFile = null
        pendingFileName = null
        vb.attachmentCard.visibility = View.GONE
    }
}
private fun ContentResolver.getDisplayName(uri: Uri): String? {
    return try {
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx != -1 && c.moveToFirst()) c.getString(idx) else null
        }
    } catch (_: Exception) {
        null
    }
}
