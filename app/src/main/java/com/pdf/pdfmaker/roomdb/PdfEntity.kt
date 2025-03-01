package com.pdf.pdfmaker.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_table")
data class PdfEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String
)
