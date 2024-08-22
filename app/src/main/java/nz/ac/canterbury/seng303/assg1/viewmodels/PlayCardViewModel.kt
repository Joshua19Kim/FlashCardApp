package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class PlayCardViewModel(private val cardStorage: Storage<Card>) : ViewModel() {
    private val _playerName = mutableStateOf("")
    val playerName: String get() = _playerName.value

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex

    private val _selectedOptions = MutableStateFlow<Set<Int>>(emptySet())
    val selectedOptions: StateFlow<Set<Int>> = _selectedOptions

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished

    private val _gameResults = mutableStateOf<List<Pair<Card, Boolean>>>(emptyList())
    val gameResults: List<Pair<Card, Boolean>> get() = _gameResults.value


    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            cardStorage.getAll().collect { cardList ->
                _cards.value = cardList.shuffled()
            }
        }
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    fun toggleOption(optionId: Int) {
        _selectedOptions.value = _selectedOptions.value.toMutableSet().apply {
            if (contains(optionId)) remove(optionId) else add(optionId)
        }
    }

    fun nextCard() {
        val currentCard = _cards.value[_currentCardIndex.value]
        val isCorrect = _selectedOptions.value == currentCard.correctOptionId
        _gameResults.value += currentCard to isCorrect

        if (_currentCardIndex.value < _cards.value.size - 1) {
            _currentCardIndex.value++
            _selectedOptions.value = emptySet()
        } else {
            _gameFinished.value = true
        }
    }

    fun restartGame() {
        _currentCardIndex.value = 0
        _selectedOptions.value = emptySet()
        _gameResults.value = emptyList()
        _gameFinished.value = false
        loadCards()
    }
}