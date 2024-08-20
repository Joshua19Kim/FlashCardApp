package nz.ac.canterbury.seng303.assg1.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card
import kotlin.random.Random

class CardViewModel(
    private val cardStorage: Storage<Card>
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> get() = _cards

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard

    fun getCards() = viewModelScope.launch {
        cardStorage.getAll().catch { Log.e("Card_VIEW_MODEL", it.toString()) }
            .collect { _cards.emit(it) }
    }

    fun loadDefaultCardsIfNoneExist() = viewModelScope.launch {
        val currentCards = cardStorage.getAll().first()
        if (currentCards.isEmpty()) {
            Log.d("CARD_VIEW_MODEL", "Inserting default cards...")
            cardStorage.insertAll(Card.getCards())
                .catch { Log.w("CARD_VIEW_MODEL", "Could not insert default cards") }.collect {
                Log.d("CARD_VIEW_MODEL", "Default cards inserted successfully")
                _cards.emit(Card.getCards())
            }
        }
    }

    fun createCard(title: String, content: String) = viewModelScope.launch {
        val card = Card(
            id = Random.nextInt(0, Int.MAX_VALUE),
            title = title,
            content = content,
            timestamp = System.currentTimeMillis(),
            false
        )
        cardStorage.insert(card).catch { Log.e("CARD_VIEW_MODEL", "Could not insert card") }
            .collect()
        cardStorage.getAll().catch { Log.e("CARD_VIEW_MODEL", it.toString()) }
            .collect { _cards.emit(it) }
    }

    fun getCardById(cardId: Int?) = viewModelScope.launch {
        if (cardId != null) {
            _selectedCard.value = cardStorage.get { it.getIdentifier() == cardId }.first()
        } else {
            _selectedCard.value = null
        }
    }

    fun deleteCardById(cardId: Int?) = viewModelScope.launch {
        Log.d("CARD_VIEW_MODEL", "Deleting card: $cardId")
        if (cardId != null) {
            cardStorage.delete(cardId).collect()
            cardStorage.getAll().catch { Log.e("CARD_VIEW_MODEL", it.toString()) }
                .collect { _cards.emit(it) }
        }
    }

    fun editCardById(cardId: Int?, card: Card) = viewModelScope.launch {
        Log.d("Card_VIEW_MODEL", "Editing card: $cardId")
        if (cardId != null) {
            cardStorage.edit(cardId, card).collect()
            cardStorage.getAll().catch { Log.e("CARD_VIEW_MODEL", it.toString()) }
                .collect { _cards.emit(it) }
        }
    }
}