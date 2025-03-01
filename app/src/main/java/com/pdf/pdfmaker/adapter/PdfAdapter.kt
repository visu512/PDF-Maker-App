//package com.pdf.pdfmaker.adapter
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.pdf.PdfRenderer
//import android.os.ParcelFileDescriptor
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.pdf.pdfmaker.R
//import com.pdf.pdfmaker.activity.PdfViewerActivity
//import com.pdf.pdfmaker.databinding.PdfItemBinding
//import com.pdf.pdfmaker.roomdb.PdfEntity
//import java.io.File
//
//class PdfAdapter : ListAdapter<PdfEntity, PdfAdapter.PdfViewHolder>(PdfDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
//        val binding = PdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PdfViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    class PdfViewHolder(private val binding: PdfItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(pdf: PdfEntity) {
//            val pdfFile = File(pdf.filePath)
//            binding.pdfName.text = pdfFile.name
//
//            val bitmap = generatePdfThumbnail(pdfFile)
//            if (bitmap != null) {
//                Glide.with(binding.pdfImage)
//                    .load(bitmap)
//                    .into(binding.pdfImage)
//            } else {
//                binding.pdfImage.setImageResource(R.drawable.preview_background)
//            }
//
//            binding.root.setOnClickListener {
//                val context = binding.root.context
//
//                if (!pdfFile.exists()) {
//                    Log.e("PDF_ERROR", "File does not exist: ${pdfFile.absolutePath}")
//                    return@setOnClickListener
//                }
//
//                val intent = Intent(context, PdfViewerActivity::class.java).apply {
//                    putExtra("PDF_PATH", pdfFile.absolutePath) // Corrected
//                    putExtra("PDF_NAME", pdfFile.name) // Corrected
//                }
//                context.startActivity(intent) // Corrected
//            }
//        }
//    }
//}
//
//class PdfDiffCallback : DiffUtil.ItemCallback<PdfEntity>() {
//    override fun areItemsTheSame(oldItem: PdfEntity, newItem: PdfEntity) = oldItem.id == newItem.id
//    override fun areContentsTheSame(oldItem: PdfEntity, newItem: PdfEntity) = oldItem == newItem
//}
//
//fun generatePdfThumbnail(pdfFile: File): Bitmap? {
//    return try {
//        val parcelFileDescriptor =
//            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
//        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
//        val page = pdfRenderer.openPage(0)
//
//        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//        page.close()
//        pdfRenderer.close()
//        parcelFileDescriptor.close()
//
//        bitmap
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}



package com.pdf.pdfmaker.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pdf.pdfmaker.R
import com.pdf.pdfmaker.activity.PdfViewerActivity
import com.pdf.pdfmaker.databinding.PdfItemBinding
import com.pdf.pdfmaker.roomdb.PdfEntity
import java.io.File

class PdfAdapter : ListAdapter<PdfEntity, PdfAdapter.PdfViewHolder>(PdfDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = PdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PdfViewHolder(private val binding: PdfItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pdf: PdfEntity) {
            val pdfFile = File(pdf.filePath)
            binding.pdfName.text = pdfFile.name

            val bitmap = generatePdfThumbnail(pdfFile)
            if (bitmap != null) {
                Glide.with(binding.pdfImage)
                    .load(bitmap)
                    .into(binding.pdfImage)
            } else {
                binding.pdfImage.setImageResource(R.drawable.preview_background)
            }

            binding.root.setOnClickListener {
                val context = binding.root.context

                if (!pdfFile.exists()) {
                    Log.e("PDF_ERROR", "File does not exist: ${pdfFile.absolutePath}")
                    return@setOnClickListener
                }

                val intent = Intent(context, PdfViewerActivity::class.java).apply {
                    putExtra("PDF_PATH", pdfFile.absolutePath)
                    putExtra("PDF_NAME", pdfFile.name)
                }
                context.startActivity(intent)
            }
        }
    }
}

// search functionality
class PdfDiffCallback : DiffUtil.ItemCallback<PdfEntity>() {
    override fun areItemsTheSame(oldItem: PdfEntity, newItem: PdfEntity) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: PdfEntity, newItem: PdfEntity) = oldItem == newItem
}

fun generatePdfThumbnail(pdfFile: File): Bitmap? {
    return try {
        val parcelFileDescriptor =
            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        val page = pdfRenderer.openPage(0)

        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()

        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
