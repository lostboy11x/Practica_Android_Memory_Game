package com.example.memorygame.Implementation


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memorygame.R


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
    private val llistaCartes: MutableList<Int> by lazy {
        mutableListOf<Int>().apply {
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
     * Estableix el nivell de dificultat del joc.
     * @param gameDifficulty Nivell de dificultat del joc ("Fàcil", "Intermedi" o "Difícil").
     */
    fun uploadDifficulty(gameDifficulty: String) {
        difficulty = gameDifficulty
    }

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

    /**
     * Carregar les cartes disponibles i barrejar-les per a la partida.
     */
    fun loadCardsAndShuffle() {
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
    fun updateVisibleCardStates(id: String) {
        val selectedCards: List<Cards>? = cartes.value?.filter { it.isSelected }
        val selectCount: Int = selectedCards?.size ?: 0
        var imageFind: Int? = null

        if (selectedCards != null && selectedCards.size == 2) {
            val areMatching = selectedCards[0].imageResourceId == selectedCards[1].imageResourceId
            if (areMatching) {
                selectedCards.forEach { it.isMatched = true }
            }
        }

        if (selectCount >= 2) {
            val hasSameImage: Boolean =
                selectedCards!![0].imageResourceId == selectedCards[1].imageResourceId
            if (hasSameImage) {
                imageFind = selectedCards[0].imageResourceId
            }

            selectedCards.forEach { it.isSelected = false }
        }

        val list: MutableList<Cards>? = cartes.value?.map { card ->

            if (selectCount >= 2) {
                card.isSelected = false
            }

            if (card.imageResourceId == imageFind) {
                card.isVisible = false
                card.isMatched = true
            }

            if (card.id == id) {
                card.isSelected = true
            }

            card
        } as MutableList<Cards>?

        val visibleCount: Int = list?.count { it.isVisible } ?: 0

        if (visibleCount <= 0) {
            return
        }

        cartes.value?.removeAll { true }
        cartes.value = list
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
        llistaCartes.clear()
        cardImages.clear()
        playerName = ""
    }
}
