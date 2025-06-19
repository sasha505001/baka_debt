package com.example.sporttracker.data.repository

import com.example.sporttracker.data.db.dao.SupplementDao
import com.example.sporttracker.data.model.Supplement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplementRepository @Inject constructor(
    private val supplementDao: SupplementDao
) {

    fun getAllSupplements(): Flow<List<Supplement>> {
        return supplementDao.getAllSupplements()
    }

    fun getSupplementById(id: Int): Flow<Supplement?> {
        return supplementDao.getSupplementById(id)
    }

    suspend fun insert(supplement: Supplement) {
        supplementDao.insertSupplement(supplement)
    }

    suspend fun update(supplement: Supplement) {
        supplementDao.updateSupplement(supplement)
    }

    suspend fun delete(supplement: Supplement) {
        supplementDao.deleteSupplement(supplement)
    }
}
