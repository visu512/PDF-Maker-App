package com.pdf.pdfmaker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.pdf.pdfmaker.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity(), OnPageChangeListener {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("PDF_PATH")
        val pdfName = intent.getStringExtra("PDF_NAME")

        binding.pdfNameShow.text = pdfName ?: "Unknown PDF"

        // Back button
        binding.backBtnPdf.setOnClickListener {
            onBackPressed()
        }

        // Share PDF file
        binding.sharePdf.setOnClickListener {
            if (!filePath.isNullOrEmpty()) {
                val file = File(filePath)
                if (file.exists()) {
                    sharePdf(file)
                } else {
                    Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show()
                    Log.e("SHARE_ERROR", "File not found at: $filePath")
                }
            } else {
                Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show()
                Log.e("SHARE_ERROR", "File path is null or empty")
            }
        }

        // Load PDF into viewer
        if (!filePath.isNullOrEmpty()) {
            val file = File(filePath)
            if (file.exists()) {
                binding.pdfView.fromFile(file)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .swipeHorizontal(false)
                    .onPageChange(this)
                    .load()
            } else {
                Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show()
                Log.e("PDF_ERROR", "PDF file not found at: $filePath")
            }
        } else {
            binding.pdfView.fromAsset("sample.pdf")
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .load()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onPageChanged(page: Int, pageCount: Int) {
        binding.pdfPageNumber.text = "${page + 1}/$pageCount"
    }

    private fun sharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share PDF using"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("SHARE_ERROR", "Error sharing file: ${e.message}")
        }
    }
}
