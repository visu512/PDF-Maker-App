package com.pdf.pdfmaker.roomdb


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PDFDao {
    @Insert
    suspend fun insert(pdf: PdfEntity)

    @Query("SELECT * FROM pdf_table")
    fun getAllPdfs(): LiveData<List<PdfEntity>>
}

