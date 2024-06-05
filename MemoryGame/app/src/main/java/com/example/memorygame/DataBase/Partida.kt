package com.example.memorygame.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Classe que representa una partida en el joc.
 *
 * Aquesta classe defineix una partida en el joc, que pot contenir diverses propietats com ara
 * el nom del jugador, la data i hora de la partida, l'estat del temporitzador, la dificultat,
 * el nombre de cartes, el nombre de cartes encertades i el nombre de cartes no encertades.
 *
 * @property id Identificador únic de la partida (auto-generat si no es proporciona).
 * @property nomJugador El nom del jugador que ha jugat la partida.
 * @property dataHora La data i hora en què s'ha jugat la partida.
 * @property temporitzador Indica si el temporitzador estava activat durant la partida.
 * @property dificultat La dificultat de la partida (p. ex. "Fàcil", "Mitjà", "Difícil").
 * @property numeroCartes El nombre de cartes utilitzades a la partida, pot ser null si no està definit.
 * @property cartesEncertades El nombre de cartes que s'han encertat durant la partida.
 * @property cartesNoEncertades El nombre de cartes que no s'han encertat durant la partida.
 */
@Entity(tableName = "partides")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nomJugador: String,
    val dataHora: String,
    val temporitzador: Boolean,
    val dificultat: String,
    val numeroCartes: Int?,
    val cartesEncertades: Int,
    val cartesNoEncertades: Int
)
