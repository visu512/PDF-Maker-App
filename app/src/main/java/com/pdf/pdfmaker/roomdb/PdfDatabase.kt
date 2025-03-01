package com.pdf.pdfmaker.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PdfEntity::class], version = 1, exportSchema = false)
abstract class PdfDatabase : RoomDatabase() {

    abstract fun pdfDao(): PDFDao

    companion object {
        @Volatile
        private var INSTANCE: PdfDatabase? = null

        fun getDatabase(context: Context): PdfDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PdfDatabase::class.java,
                    "pdf_database"
                )
                    .fallbackToDestructiveMigration() // Handles schema changes without crashing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
