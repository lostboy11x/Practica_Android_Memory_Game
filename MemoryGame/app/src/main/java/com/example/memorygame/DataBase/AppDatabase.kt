package com.example.memorygame.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Classe abstracta que representa la base de dades principal de l'aplicació.
 *
 * Aquesta classe utilitza la llibreria Room per a gestionar la base de dades.
 * Defineix una instància de la base de dades que conté les partides i proporciona
 * un mètode per obtenir l'objecte DAO associat a les partides.
 *
 * @property INSTANCE Instància singleton de la base de dades.
 */
@Database(entities = [Partida::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Retorna l'objecte DAO per a les operacions relacionades amb les partides.
     *
     * @return L'objecte DAO de les partides.
     */
    abstract fun partidaDao(): PartidaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna una instància de la base de dades.
         *
         * Aquest mètode utilitza una implementació del patró Singleton per garantir que només hi hagi una
         * única instància de la base de dades a l'aplicació.
         *
         * @param context El context de l'aplicació.
         * @return Instància de la base de dades.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Això permet migracions simples; per mantenir les dades, cal definir una migració
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}