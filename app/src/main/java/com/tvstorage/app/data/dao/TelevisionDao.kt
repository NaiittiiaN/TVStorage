package com.tvstorage.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tvstorage.app.data.entity.TelevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelevisionDao {

    @Query("SELECT * FROM televisions WHERE isArchived = 0 ORDER BY receivedDate DESC")
    fun getAllActive(): Flow<List<TelevisionEntity>>

    @Query("SELECT * FROM televisions WHERE isArchived = 1 ORDER BY receivedDate DESC")
    fun getAllArchived(): Flow<List<TelevisionEntity>>

    @Query("SELECT * FROM televisions WHERE id = :id")
    suspend fun getById(id: Long): TelevisionEntity?

    @Query("SELECT * FROM televisions WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TelevisionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(television: TelevisionEntity): Long

    @Update
    suspend fun update(television: TelevisionEntity)

    @Delete
    suspend fun delete(television: TelevisionEntity)

    @Query("DELETE FROM televisions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM televisions WHERE isArchived = 0 AND (brand LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%' OR clientName LIKE '%' || :query || '%' OR orderNumber LIKE '%' || :query || '%')")
    fun searchActive(query: String): Flow<List<TelevisionEntity>>

    @Query("SELECT COUNT(*) FROM televisions WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT * FROM televisions WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getByOrderNumber(orderNumber: String): TelevisionEntity?

    @Query("UPDATE televisions SET isPaused = :isPaused WHERE isArchived = 0")
    suspend fun setAllPaused(isPaused: Boolean)
}