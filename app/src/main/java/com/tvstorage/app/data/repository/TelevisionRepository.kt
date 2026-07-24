package com.tvstorage.app.data.repository

import com.tvstorage.app.data.dao.TelevisionDao
import com.tvstorage.app.data.entity.TelevisionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelevisionRepository @Inject constructor(
    private val dao: TelevisionDao
) {
    fun getAllActive(): Flow<List<TelevisionEntity>> = dao.getAllActive()

    fun getAllArchived(): Flow<List<TelevisionEntity>> = dao.getAllArchived()

    fun getByIdFlow(id: Long): Flow<TelevisionEntity?> = dao.getByIdFlow(id)

    suspend fun getById(id: Long): TelevisionEntity? = dao.getById(id)

    suspend fun insert(television: TelevisionEntity): Long = dao.insert(television)

    suspend fun update(television: TelevisionEntity) = dao.update(television)

    suspend fun delete(television: TelevisionEntity) = dao.delete(television)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    fun searchActive(query: String): Flow<List<TelevisionEntity>> = dao.searchActive(query)

    fun getActiveCount(): Flow<Int> = dao.getActiveCount()

    suspend fun getByOrderNumber(orderNumber: String): TelevisionEntity? = dao.getByOrderNumber(orderNumber)

    suspend fun setAllPaused(isPaused: Boolean) = dao.setAllPaused(isPaused)
}