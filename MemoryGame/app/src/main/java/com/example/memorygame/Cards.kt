package com.example.memorygame

import java.util.UUID


/**
 * Calsse que fa referecia a la carta en el joc.
 * Aquesta classe emmagatzema la informació rellevant d'una carta, incloent-hi la seva imatge, l'estat de visibilitat,
 * si està seleccionada, si està emparellada amb una altra carta i un identificador únic.
 * @property imageResourceId Identificador de recurs de la imatge de la carta.
 * @property isVisible Indica si la carta és visible o no en el taulell de joc.
 * @property isSelected Indica si la carta està seleccionada per l'usuari.
 * @property isMatched Indica si la carta està emparellada amb una altra carta.
 * @property id Identificador únic de la carta.
 * @constructor Crea una nova instància de la classe Cards.
 * @param imageResourceId Identificador de recurs de la imatge de la carta (per defecte és null).
 * @param isVisible Indica si la carta és visible (per defecte és true).
 * @param isSelected Indica si la carta està seleccionada (per defecte és false).
 * @param isMatched Indica si la carta està emparellada (per defecte és false).
 * @param id Identificador únic de la carta (per defecte és un UUID generat aleatòriament).
 */
data class Cards(
    var imageResourceId: Int? = null,
    var isVisible: Boolean = true,
    var isSelected: Boolean = false,
    var isMatched: Boolean = false,
    var id: String = UUID.randomUUID().toString()
)
