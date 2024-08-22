package nz.ac.canterbury.seng303.assg1.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class CardViewModel(private val cardStorage: Storage<Card>) : ViewModel() {
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            cardStorage.getAll().collect { cardList ->
                _cards.value = cardList
            }
        }
    }

    private suspend fun getNextId(): Int {
        val currentCards = cardStorage.getAll().first()
        return if (currentCards.isEmpty()) 1 else currentCards.maxOf { it.id } + 1
    }

    fun addCard(card: Card) {
        viewModelScope.launch {
            val newId = getNextId()
            val newCard = card.copy(id = newId)
            cardStorage.insert(newCard).collect { result ->
                if (result == 1) {
                    loadCards()
                }
            }
        }
    }

    fun shuffleCards() {
        viewModelScope.launch {
            val currentCards = _cards.value
            println("Current cards: ${currentCards.map { it.id to it.title }}")
            if (currentCards.isNotEmpty()) {
                val shuffledCards = currentCards.shuffled().mapIndexed { index, card ->
                    card.copy(id = index + 1)
                }
                println("Shuffled cards: ${shuffledCards.map { it.id to it.title }}")

                // Clear all existing cards
                cardStorage.getAll().first().forEach { card ->
                    cardStorage.delete(card.id).first()
                }

                // Insert shuffled cards
                cardStorage.insertAll(shuffledCards).first()

                // Update the local state
                _cards.value = shuffledCards
            }
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch {
            cardStorage.edit(card.id, card).collect { result ->
                if (result == 1) {
                    loadCards()
                }
            }
        }
    }

    fun deleteCard(cardId: Int) {
        viewModelScope.launch {
            cardStorage.delete(cardId).collect { result ->
                if (result == 1) {
                    loadCards()
                }
            }
        }
    }
}