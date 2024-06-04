package com.example.memorygame.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partides")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nomJugador: String,
    val dataHora: String,
    val temporitzador: Boolean,
    val dificultat: String,
    val numeroCartes: Int?,
    val cartesEncertades: Int,
    val cartesNoEncertades: Int
)
