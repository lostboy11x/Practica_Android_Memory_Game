package com.example.memorygame.DataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Interfície DAO (Data Access Object) per a l'entitat Partida.
 *
 * Aquesta interfície proporciona mètodes per accedir a la base de dades i realitzar operacions CRUD
 * (Create, Read, Update, Delete) relacionades amb les partides.
 */
@Dao
interface PartidaDao {
    /**
     * Recupera una partida mitjançant el seu identificador.
     *
     * @param partidaId Identificador de la partida.
     * @return La partida corresponent a l'identificador especificat, o null si no s'ha trobat cap partida.
     */
    @Query("SELECT * FROM partides WHERE id = :partidaId")
    suspend fun getPartidaById(partidaId: Int): Partida?

    /**
     * Compta el nombre de partides amb un determinat identificador.
     *
     * @param partidaId Identificador de la partida.
     * @return El nombre de partides amb l'identificador especificat.
     */
    @Query("SELECT COUNT(*) FROM partides WHERE id = :partidaId")
    suspend fun countPartidaById(partidaId: Int): Int

    /**
     * Obté una llista de totes les partides disponibles.
     *
     * @return LiveData que conté una llista de totes les partides.
     */
    @Query("SELECT * FROM partides")
    fun getPartides(): LiveData<List<Partida>>

    /**
     * Insereix una partida a la base de dades si encara no existeix.
     *
     * @param partida La partida a inserir.
     */
    @Transaction
    suspend fun insertIfNotExists(partida: Partida) {
        val count = countPartidaById(partida.id)
        if (count == 0) {
            insert(partida)
        }
    }

    /**
     * Insereix una nova partida a la base de dades.
     *
     * @param partida La partida a inserir.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(partida: Partida)

    /**
     * Elimina totes les partides de la base de dades.
     */
    @Query("DELETE FROM partides")
    suspend fun deleteAll()
}


