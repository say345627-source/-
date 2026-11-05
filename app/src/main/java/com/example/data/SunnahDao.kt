package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SunnahDao {
    @Query("SELECT * FROM sunan ORDER BY id ASC")
    fun getAllSunan(): Flow<List<SunnahEntity>>

    @Query("SELECT * FROM sunan WHERE id = :id LIMIT 1")
    suspend fun getSunnahById(id: Int): SunnahEntity?

    @Query("SELECT COUNT(*) FROM sunan")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sunan: List<SunnahEntity>)

    @Update
    suspend fun update(sunnah: SunnahEntity)

    @Query("UPDATE sunan SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE sunan SET lastAppliedDate = :date, timesAppliedTotal = timesAppliedTotal + 1 WHERE id = :id")
    suspend fun markAppliedToday(id: Int, date: String)

    @Query("UPDATE sunan SET lastAppliedDate = '' WHERE id = :id")
    suspend fun unmarkAppliedToday(id: Int)

    @Query("SELECT * FROM sunan WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<SunnahEntity>>

    @Query("SELECT * FROM sunan WHERE lastAppliedDate = :todayDate")
    fun getAppliedToday(todayDate: String): Flow<List<SunnahEntity>>
}
