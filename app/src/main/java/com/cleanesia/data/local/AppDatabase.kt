package com.cleanesia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ReportEntity::class, CellEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun cellDao(): CellDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "cleanesia.db")
                // Belum rilis: perubahan skema cukup menghapus data lama.
                // Ganti dengan migrasi sebelum rilis pertama.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
