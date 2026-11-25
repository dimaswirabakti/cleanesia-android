package com.cleanesia.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CellDao {
    @Query("SELECT * FROM cells WHERE cellId = :cellId")
    suspend fun get(cellId: String): CellEntity?

    @Upsert
    suspend fun upsert(cell: CellEntity)

    @Query("SELECT * FROM cells")
    fun observeAll(): Flow<List<CellEntity>>

    @Query("SELECT COUNT(*) FROM cells")
    fun observeCount(): Flow<Int>
}
