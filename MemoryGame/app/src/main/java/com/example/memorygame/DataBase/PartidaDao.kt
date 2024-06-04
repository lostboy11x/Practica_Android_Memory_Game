package com.example.memorygame.DataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PartidaDao {
    @Query("SELECT * FROM partides WHERE id = :partidaId")
    suspend fun getPartidaById(partidaId: Int): Partida?

    @Query("SELECT COUNT(*) FROM partides WHERE id = :partidaId")
    suspend fun countPartidaById(partidaId: Int): Int

    @Query("SELECT * FROM partides")
    fun getPartides(): LiveData<List<Partida>>

    @Transaction
    suspend fun insertIfNotExists(partida: Partida) {
        val count = countPartidaById(partida.id)
        if (count == 0) {
            insert(partida)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(partida: Partida)

    @Query("DELETE FROM partides")
    suspend fun deleteAll()
}

