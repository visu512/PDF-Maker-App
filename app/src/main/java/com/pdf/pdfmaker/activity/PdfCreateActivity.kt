package com.pdf.pdfmaker

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.pdf.pdfmaker.activity.MainActivity
import com.pdf.pdfmaker.adapter.ImageAdapter
import com.pdf.pdfmaker.databinding.ActivityPdfCreateBinding
import com.pdf.pdfmaker.roomdb.PdfDatabase
import com.pdf.pdfmaker.roomdb.PdfEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PdfCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfCreateBinding
    private var selectedImages: ArrayList<Uri> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive selected images
        selectedImages = intent.getParcelableArrayListExtra("SELECTED_IMAGES") ?: ArrayList()

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "No images received!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = ImageAdapter(selectedImages)

        // Convert to PDF Button
        binding.btnConvertPdf.setOnClickListener {
            showPdfNameDialog()
        }
    }

    // show dailouge
    private fun showPdfNameDialog() {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pdf_name, null)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        val pdfNameInput = dialogView.findViewById<EditText>(R.id.pdfNameInput)
        val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)

        btnOk.setOnClickListener {
            val pdfName = pdfNameInput.text.toString().trim()
            if (pdfName.isNotEmpty()) {
                dialog.dismiss()
                convertImagesToPdf(pdfName)
            } else {
                Toast.makeText(this, "Enter a PDF name", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()

        // Set dialog width to 80% of the screen
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    private fun convertImagesToPdf(pdfName: String) {
        val pdfDirectory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (pdfDirectory == null) {
            Toast.makeText(this, "Unable to access storage", Toast.LENGTH_SHORT).show()
            return
        }

        val outputFile = File(pdfDirectory, "$pdfName.pdf")

        try {
            val pdfDocument = PdfDocument()
            val outputStream = FileOutputStream(outputFile)

            selectedImages.forEachIndexed { index, uri ->
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                bitmap?.let {
                    val scaledBitmap = Bitmap.createScaledBitmap(it, 595, 842, true) // A4 size
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                }
            }

            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            // Insert into Room Database
            savePdfToDatabase(outputFile.absolutePath)

            Toast.makeText(this, "PDF Saved!", Toast.LENGTH_LONG).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "PDF creation failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePdfToDatabase(pdfPath: String) {
        val pdfDao = PdfDatabase.getDatabase(this).pdfDao()
        val newPdf = PdfEntity(filePath = pdfPath)

        lifecycleScope.launch(Dispatchers.IO) {
            pdfDao.insert(newPdf) // Insert new PDF into Room Database
        }
    }
}
