package com.example.emotionsai.ui.employee.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var attemptsLeft = 3

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            goLoginHard()
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
    }

    private fun setupUiForFaceLogin() {
        // Прячем "event" части — в FaceLogin они не нужны
        binding.btnSelectEvent.visibility = View.GONE
        binding.tvSelectedEvent.visibility = View.GONE

        // Состояние по умолчанию
        binding.progressBar.visibility = View.GONE
        binding.btnCapture.isEnabled = true
    }

    private fun setupClickListeners() {
        binding.btnCapture.setOnClickListener { takePhoto() }

        binding.btnClose.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Face ID")
                .setMessage("Выйти из проверки и перейти к логину?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Выйти") { _, _ ->
                    // FaceAuth — критичный шаг, поэтому выход = логин
                    goLoginHard()
                }
                .show()
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
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show()
                goLoginHard()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().cacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    verifyFace(photoFile)
                }
            }
        )
    }

    private fun verifyFace(photoFile: File) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = false

        val faceRepo = ServiceLocator.faceAuthRepository(requireContext())
        val authRepo = ServiceLocator.authRepository(requireContext())

        lifecycleScope.launch {
            val result = faceRepo.verifyFace(photoFile)

            when (result) {
                PhotoVerifyResult.Approved -> {
                    // ✅ Face OK -> перейти на home в текущем графе
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    navigateToHome()
                }

                is PhotoVerifyResult.Rejected -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true

                    attemptsLeft--

                    if (attemptsLeft <= 0) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Face ID")
                            .setMessage("Лимит попыток исчерпан. Выполните вход по логину и паролю.")
                            .setPositiveButton("OK") { _, _ ->
                                authRepo.logout()
                                goLoginHard()
                            }
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Face ID")
                            .setMessage("${result.reason}\nОсталось попыток: $attemptsLeft")
                            .setPositiveButton("Повторить", null)
                            .show()
                    }
                }

                is PhotoVerifyResult.Error -> {
                    // Тут может быть: нет сети, 401 (refresh failed), и т.д.
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true

                    // Безопаснее: если это auth-ошибка — чистим токены и на логин
                    authRepo.logout()
                    Toast.makeText(requireContext(), "Authorization error. Please login again.", Toast.LENGTH_LONG).show()
                    goLoginHard()
                }
            }
        }
    }

    /**
     * ВАЖНО: в employee и hr графах home разные.
     * Поэтому делаем два action id (по одному в каждом графе) с одинаковым именем:
     * action_faceLogin_to_home
     */
    private fun navigateToHome() {
        findNavController().navigate(R.id.action_faceLogin_to_home)
    }

    private fun goLoginHard() {
        val i = Intent(requireContext(), LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
