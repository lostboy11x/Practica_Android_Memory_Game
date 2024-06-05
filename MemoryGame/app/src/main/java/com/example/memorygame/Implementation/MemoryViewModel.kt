package com.example.memorygame.Implementation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorygame.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Lògica del joc.
 * ViewModel per al joc.
 * Aquesta classe gestiona l'estat del joc de memory, incloent-hi la dificultat, les imatges de les cartes, el taulell de joc
 * i les lògiques relacionades amb les accions de l'usuari.
 */
class MemoryViewModel : ViewModel() {
    /** Nivell de dificultat del joc. */
    private var difficulty: String = ""

    /** Llista d'imatges de les cartes. */
    private val cardImages: MutableList<Int> by lazy { mutableListOf() }

    /** Nom del jugador. */
    var playerName: String = ""

    /** Llista d'imatges de les cartes disponibles, segons el nivell de dificultat. */
    private lateinit var llistaCartes: MutableList<Int>

    /**
     * Estableix el nivell de dificultat del joc.
     * @param gameDifficulty Nivell de dificultat del joc ("Fàcil", "Intermedi" o "Difícil").
     */
    fun uploadDifficulty(gameDifficulty: String) {
        difficulty = gameDifficulty
    }

    /** Llista de logs del joc */
    private val _gameLogs = mutableStateListOf<String>()
    val gameLogs: List<String> get() = _gameLogs

    /**
     * Obté les imatges de les cartes.
     * @return Llista d'imatges de les cartes.
     */
    fun obtainCardImages(): List<Int> {
        return cardImages
    }

    /**
     * LiveData que conté la llista de cartes en el taulell de joc.
     */
    private val cartes: MutableLiveData<MutableList<Cards>> by lazy {
        MutableLiveData<MutableList<Cards>>()
    }

    /**
     * Obté les cartes en el taulell de joc.
     * @return LiveData que conté la llista de cartes en el taulell de joc.
     */
    fun getCards(): LiveData<MutableList<Cards>> {
        return cartes
    }

    // Variables d'estat del joc
    val gameFinished = MutableLiveData<Boolean>(false)

    /**
     * Carrega les cartes del joc segons la dificultat seleccionada.
     *
     * Aquesta funció inicialitza la llista de cartes amb els recursos de les imatges corresponents segons la dificultat
     * seleccionada pel jugador.
     */
    private fun loadCards() {
        llistaCartes = mutableListOf<Int>().apply {
            when (difficulty) {
                "Fàcil" -> {
                    addAll(
                        listOf(
                            R.drawable.carta_1,
                            R.drawable.carta_2,
                            R.drawable.carta_3
                        )
                    )
                }

                "Intermedia" -> {
                    addAll(
                        listOf(
                            R.drawable.carta_1,
                            R.drawable.carta_2,
                            R.drawable.carta_3,
                            R.drawable.carta_4,
                            R.drawable.carta_5,
                            R.drawable.carta_6,
                            R.drawable.carta_7,
                            R.drawable.carta_8
                        )
                    )
                }

                "Difícil" -> {
                    addAll(
                        listOf(
                            R.drawable.carta_1,
                            R.drawable.carta_2,
                            R.drawable.carta_3,
                            R.drawable.carta_4,
                            R.drawable.carta_5,
                            R.drawable.carta_6,
                            R.drawable.carta_7,
                            R.drawable.carta_8,
                            R.drawable.carta_9,
                            R.drawable.carta_10,
                            R.drawable.carta_11,
                            R.drawable.carta_12
                        )
                    )
                }
            }
        }
    }

    /**
     * Carrega les cartes del joc segons la dificultat seleccionada i les barreja.
     *
     * Aquesta funció carrega les cartes del joc segons la dificultat seleccionada pel jugador i les barreja aleatòriament
     * per a la partida. Després de barrejar-les, inicialitza la llista de cartes amb les imatges barrejades i les
     * actualitza en l'estat de les cartes del joc.
     */
    fun loadCardsAndShuffle() {
        loadCards()
        repeat(2) {
            cardImages.addAll(llistaCartes)
        }
        cardImages.shuffle()

        val cards = cardImages.map { Cards(it) }
        cartes.value = cards.toMutableList()
    }

    /**
     * Actualitza l'estat de les cartes visibles. Aquesta funció és cridada quan es selecciona una carta en el taulell de joc.
     * S'encarrega de gestionar l'actualització de l'estat de les cartes seleccionades, incloent-hi la comprovació de si les
     * cartes seleccionades formen una parella, la deselecció automàtica si es seleccionen més de dues cartes, i la ocultació
     * de les cartes que formin una parella.
     * @param id Identificador de la carta seleccionada.
     */


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateVisibleCardStates(card: Cards) {
        val list = cartes.value?.map { it.copy() }?.toMutableList() ?: return
        val clickedCard = list.find { it.id == card.id } ?: return

        addLog(clickedCard) // Afegir el log aquí

        clickedCard.isSelected = true
        val selectedCards = list.filter { it.isSelected }

        if (selectedCards.size == 2) {
            val areMatching = selectedCards[0].imageResourceId == selectedCards[1].imageResourceId
            if (areMatching) {
                selectedCards.forEach { it.isMatched = true }
            }
            selectedCards.forEach { it.isSelected = false }
        }

        cartes.value = list
        gameFinished.value = allCardsMatched()
    }


    /**
     * Comprova si totes les cartes del joc estan emparellades.
     * @return True si totes les cartes estan emparellades, fals altrament.
     */
    fun allCardsMatched(): Boolean {
        val cards = cartes.value ?: return false
        return cards.all { it.isMatched }
    }

    /**
     * Obté el nombre de cartes emparellades actualment en el joc.
     * @return Nombre de cartes emparellades.
     */
    fun getCardsMatchedCount(): Int {
        val cards = cartes.value ?: return 0
        return cards.count { it.isMatched }
    }

    /**
     * Obté el nombre de cartes no emparellades actualment visibles en el joc.
     * @return Nombre de cartes no emparellades i visibles.
     */
    fun getCardsNotMatchedCount(): Int {
        val cards = cartes.value ?: return 0
        return cards.count { !it.isMatched && it.isVisible }
    }

    /**
     * Reinicia el joc, netejant les llistes de cartes i el nom del jugador.
     */
    fun restartGame() {
        gameFinished.value = false
        llistaCartes.clear()
        cartes.value?.clear()
        cardImages.clear()
        _gameLogs.clear()
        playerName = ""
    }

    /**
     * Afegeix un log a la llista de logs del joc.
     * @param card La carta clicada.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addLog(card: Cards, remainingTime: Long? = null) {
        val currentTime = LocalDateTime.now()
        val logMessage = if (remainingTime != null) {
            "Carta clicada ${card.id} a les ${currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} amb $remainingTime segons restants."
        } else {
            " Carta clicada ${card.id} a les ${currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}."
        }
        _gameLogs.add(logMessage)
    }

}


/*
   fun updateVisibleCardStates(id: String) {
       val list: MutableList<Cards>? = cartes.value?.map { card ->
           if (card.id == id) {
               card.isSelected = true
           }

           card
       } as MutableList<Cards>?
       val selectedCards: List<Cards>? = list?.filter { it.isSelected }

       if (selectedCards != null && selectedCards.size == 2) {
           val areMatching =
               selectedCards[0].imageResourceId == selectedCards[1].imageResourceId
           if (areMatching) {
               selectedCards.forEach { it.isMatched = true }
           }
           selectedCards.forEach { it.isSelected = false }
       }
       cartes.value?.removeAll { true }
       cartes.value = list
       gameFinished.value = allCardsMatched()
   }*/