package com.example.emotionsai.ui.employee.camera

import android.Manifest
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentCameraBinding
import com.example.emotionsai.di.ServiceLocator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CameraViewModel
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private val args: CameraFragmentArgs by navArgs()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val feedbackRepo = ServiceLocator.feedbackRepository(requireContext())
        val eventRepo = ServiceLocator.eventRepository(requireContext())
        viewModel = CameraViewModel(feedbackRepo, eventRepo)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupObservers()
        setupClickListeners()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupClickListeners() {
        binding.btnCapture.setOnClickListener {
            takePhoto()
        }


        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CameraUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                }
                is CameraUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCapture.isEnabled = false
                }
                is CameraUiState.Success -> {
                    binding.progressBar.visibility = View.GONE

                    // 1) сообщаем назад, что фидбэк по ивенту отправлен (см. пункт 3)
                    findNavController().previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("feedback_submitted_event_id", args.eventId)

                    // 2) навигация в Result
                    val action = CameraFragmentDirections.actionCameraToResult(
                        emotion = state.result.emotion
                    )
                    findNavController().navigate(action)
                }

                is CameraUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
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
                    viewModel.submitPhoto(photoFile, args.eventId)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
