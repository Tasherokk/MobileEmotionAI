package com.example.emotionsai.ui.employee.camera

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentCameraBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.login.LoginActivity
import com.example.emotionsai.util.PhotoVerifyResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceLoginCameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var scannerAnimator: ValueAnimator? = null

    private var attemptsLeft = 3
    private var verifyJob: Job? = null

    private inline fun safeUi(block: (FragmentCameraBinding) -> Unit) {
        val b = _binding ?: return
        if (!isAdded || view == null) return
        block(b)
    }

    private inline fun safeContext(block: (android.content.Context) -> Unit) {
        val ctx = context ?: return
        if (!isAdded) return
        block(ctx)
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            goLoginHard(clearTokens = false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupUiForFaceLogin()
        setupClickListeners()
        checkCameraPermission()
        startScannerAnimation()
    }

    private fun startScannerAnimation() {
        binding.viewScanner.post {
            val parentHeight = binding.root.height.toFloat()
            scannerAnimator = ValueAnimator.ofFloat(0f, parentHeight).apply {
                duration = 3000
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    val value = animator.animatedValue as Float
                    binding.viewScanner.translationY = value
                }
                start()
            }
        }
    }

    private fun setupUiForFaceLogin() {
        binding.tvSelectedEvent.visibility = View.GONE
        binding.loadingLayout.visibility = View.GONE
        binding.btnCapture.isEnabled = true
    }

    private fun setupClickListeners() {
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnClose.setOnClickListener {
            safeContext { ctx ->
                MaterialAlertDialogBuilder(ctx)
                    .setTitle("Face ID")
                    .setMessage("Cancel Face ID and go to login page?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Go") { _, _ ->
                        goLoginHard(clearTokens = false)
                    }
                    .show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show()
                goLoginHard(clearTokens = false)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().cacheDir, 
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    verifyFace(photoFile)
                }
            }
        )
    }

    private fun verifyFace(photoFile: File) {
        val stableFile = try { copyToStableDir(photoFile) } catch (e: Exception) { return }

        safeUi {
            it.loadingLayout.visibility = View.VISIBLE
            it.btnCapture.isEnabled = false
        }

        val faceRepo = ServiceLocator.faceAuthRepository(requireContext())
        val authRepo = ServiceLocator.authRepository(requireContext())

        verifyJob?.cancel()
        verifyJob = viewLifecycleOwner.lifecycleScope.launch {
            val result = faceRepo.verifyFace(stableFile)
            runCatching { stableFile.delete() }
            if (_binding == null) return@launch

            safeUi {
                it.loadingLayout.visibility = View.GONE
                it.btnCapture.isEnabled = true
            }

            when (result) {
                PhotoVerifyResult.Approved -> navigateToHome()
                is PhotoVerifyResult.Rejected -> {
                    attemptsLeft--
                    if (attemptsLeft <= 0) {
                        authRepo.logout()
                        goLoginHard(clearTokens = false)
                    } else {
                        Toast.makeText(context, "Face doesn't match. $attemptsLeft attempts left", Toast.LENGTH_SHORT).show()
                    }
                }
                is PhotoVerifyResult.Error -> {
                    if (result.httpCode == 403) {
                        authRepo.logout()
                        goLoginHard(clearTokens = false)
                    } else {
                        Toast.makeText(context, "Verification error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_faceLogin_to_home)
    }

    private fun goLoginHard(clearTokens: Boolean) {
        if (clearTokens) ServiceLocator.authRepository(requireContext()).logout()
        val i = Intent(requireContext(), LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scannerAnimator?.cancel()
        verifyJob?.cancel()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        _binding = null
    }

    private fun copyToStableDir(src: File): File {
        val dst = File(requireContext().filesDir, "face_login_${System.currentTimeMillis()}.jpg")
        src.inputStream().use { input -> dst.outputStream().use { output -> input.copyTo(output) } }
        return dst
    }
}
