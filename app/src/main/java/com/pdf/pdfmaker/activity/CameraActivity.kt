package com.pdf.pdfmaker.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.pdf.pdfmaker.PdfCreateActivity
import com.pdf.pdfmaker.R
import com.pdf.pdfmaker.databinding.ActivityCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private val capturedImages = ArrayList<Uri>() // List to store captured images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default thumbnail image
        updatePreviewThumbnail(null)

        // Request necessary permissions
        requestPermissions()

        // Capture button listener
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        // Proceed button listener
        binding.finishButton.setOnClickListener {
            if (capturedImages.isNotEmpty()) {
                val intent = Intent(this, PdfCreateActivity::class.java).apply {
                    putParcelableArrayListExtra("SELECTED_IMAGES", capturedImages)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No images captured!", Toast.LENGTH_SHORT).show()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Request permissions (Camera + Storage if needed)
    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results[Manifest.permission.CAMERA] == true) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    // Start Camera
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraActivity", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Capture Image
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File(storageDir, "IMG_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageUri = photoFile.toUri()
                    capturedImages.add(imageUri) // Add image to list
                    updatePreviewThumbnail(imageUri)
                    updateBadgeCount()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(this@CameraActivity, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Update preview thumbnail
    private fun updatePreviewThumbnail(imageUri: Uri?) {
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.preview_background)
                .into(binding.previewThumbnail)

            binding.previewThumbnail.setOnClickListener {
                val intent = Intent(this, ImagePreviewActivity::class.java)
                intent.putExtra("image_uri", imageUri.toString())
                startActivity(intent)
            }
        } else {
            Glide.with(this)
                .load(R.drawable.preview_background)
                .into(binding.previewThumbnail)
        }

        updateBadgeCount()
    }

    // Update badge count
    private fun updateBadgeCount() {
        if (capturedImages.isNotEmpty()) {
            binding.badgeCount.text = capturedImages.size.toString()
            binding.badgeCount.visibility = View.VISIBLE
        } else {
            binding.badgeCount.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
