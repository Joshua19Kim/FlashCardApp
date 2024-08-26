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