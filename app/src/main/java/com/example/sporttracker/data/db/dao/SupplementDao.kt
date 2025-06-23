package com.example.sporttracker.data.db.dao

import androidx.room.*
import com.example.sporttracker.data.model.Supplement
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {

    @Query("SELECT * FROM supplements")
    fun getAllSupplements(): Flow<List<Supplement>>

    @Query("SELECT * FROM supplements WHERE id = :id")
    fun getSupplementById(id: Int): Flow<Supplement?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: Supplement)

    @Update
    suspend fun updateSupplement(supplement: Supplement)

    @Delete
    suspend fun deleteSupplement(supplement: Supplement)

    @Query("SELECT * FROM supplements")
    suspend fun getAllSupplementsOnce(): List<Supplement>
}
