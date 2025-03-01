package com.pdf.pdfmaker.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pdf.pdfmaker.R
import com.pdf.pdfmaker.databinding.ActivityImagePreviewBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import java.io.File

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private var imageUri: Uri? = null
    private lateinit var gpuImage: GPUImage

    // ActivityResultLauncher for cropping
    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = CropImage.getActivityResult(result.data).uri
            binding.fullscreenImage.setImageURI(resultUri)
            imageUri = resultUri
        } else if (result.resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            val error = CropImage.getActivityResult(result.data).error
            Toast.makeText(this, "Crop failed: ${error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Image URI from Intent
        imageUri = Uri.parse(intent.getStringExtra("image_uri"))

        // Load Image with Glide
        Glide.with(this)
            .load(imageUri)
            .error(R.drawable.preview_background) // Fallback image in case of error
            .into(binding.fullscreenImage)

        // Initialize GPUImage for filters
        gpuImage = GPUImage(this)
        gpuImage.setImage(imageUri) // Set the initial image

        // Crop Button
        binding.cropButton.setOnClickListener {
            imageUri?.let { uri ->
                // Start cropping with Android-Image-Cropper
                CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1) // Square aspect ratio
                    .start(this)
            }
        }

        // Apply Filters
        binding.filterButton.setOnClickListener {
            // Apply Sepia filter using GPUImage
            gpuImage.setFilter(GPUImageSepiaToneFilter())
            binding.fullscreenImage.setImageBitmap(gpuImage.bitmapWithFilterApplied)
            Toast.makeText(this, "Sepia Filter Applied!", Toast.LENGTH_SHORT).show()
        }

        // Add Text to Image
        binding.textButton.setOnClickListener {
            // For adding text, you can use a custom library or Canvas-based implementation
            Toast.makeText(this, "Text feature not implemented yet", Toast.LENGTH_SHORT).show()
        }

        // Delete Image
        binding.deleteButton.setOnClickListener {
            imageUri?.let { uri ->
                val file = File(uri.path!!)
                if (file.exists()) {
                    if (file.delete()) {
                        Toast.makeText(this, "Image Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Retake Image (Restart Camera)
        binding.retakeButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}