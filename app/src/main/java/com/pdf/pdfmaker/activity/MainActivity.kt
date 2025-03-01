package com.pdf.pdfmaker.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pdf.pdfmaker.PdfCreateActivity
import com.pdf.pdfmaker.R
import com.pdf.pdfmaker.adapter.PdfAdapter
import com.pdf.pdfmaker.databinding.ActivityMainBinding
import com.pdf.pdfmaker.roomdb.PdfDatabase
import com.pdf.pdfmaker.roomdb.PdfEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PICK_IMAGES_REQUEST = 101
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private lateinit var pdfAdapter: PdfAdapter
    private var allPdfs: List<PdfEntity> = emptyList() // Stores all PDFs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check storage permission for older Android versions
        checkStoragePermission()

        // Set up RecyclerView and SearchView
        setupRecyclerView()
        setupSearchView()

        // Load PDFs from the database
        loadPdfs()

        // Set click listener for the plus button to open the bottom sheet
        binding.plusBtn.setOnClickListener { openBottomSheet() }
    }

    private fun setupRecyclerView() {
        pdfAdapter = PdfAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pdfAdapter
        }
    }


    private fun setupSearchView() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPdfList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun loadPdfs() {
        val pdfDao = PdfDatabase.getDatabase(this).pdfDao()
        pdfDao.getAllPdfs().observe(this, Observer { pdfList ->
            allPdfs = pdfList
            pdfAdapter.submitList(pdfList)
        })
    }

    private fun filterPdfList(query: String?) {
        val filteredList = if (!query.isNullOrEmpty()) {
            allPdfs.filter { File(it.filePath).name.contains(query, ignoreCase = true) }
        } else {
            allPdfs
        }
        pdfAdapter.submitList(filteredList)
    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet, null)

        val cameraOption = view.findViewById<TextView>(R.id.cameraOption)
        val galleryOption = view.findViewById<TextView>(R.id.galleryOption)

        cameraOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            checkCameraPermission()
        }

        galleryOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            openGallery()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple selection
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            val selectedImages = ArrayList<Uri>()

            // Handle multiple image selection
            if (data?.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImages.add(imageUri)
                }
            } else if (data?.data != null) {
                selectedImages.add(data.data!!) // Single image selected
            }

            if (selectedImages.isNotEmpty()) {
                // Send images to PdfCreateActivity
                val intent = Intent(this, PdfCreateActivity::class.java)
                intent.putParcelableArrayListExtra("SELECTED_IMAGES", selectedImages)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No images selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Storage permission granted
                } else {
                    Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show()
                }
            }

            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}