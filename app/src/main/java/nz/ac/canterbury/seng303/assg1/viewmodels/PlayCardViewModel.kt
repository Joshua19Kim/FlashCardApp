package nz.ac.canterbury.seng303.assg1.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class PlayCardViewModel(
    private val cardStorage: Storage<Card>,
    private val playerNameViewModel: PlayerNameViewModel
) : ViewModel() {
    val playerName = playerNameViewModel.playerName

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled

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

    private val _progress = MutableStateFlow("0/${_cards.value.size}")
    val progress: StateFlow<String> = _progress

    private val _isOptionSelected = MutableStateFlow(false)
    val isOptionSelected: StateFlow<Boolean> = _isOptionSelected

    private val _showExitConfirmation = MutableStateFlow(false)
    val showExitConfirmation: StateFlow<Boolean> = _showExitConfirmation

    init {
        loadCards()
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        Log.d("PlayCardViewModel", "Shuffle toggled: ${if (_shuffleEnabled.value) "ON" else "OFF"}")
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            val cardList = cardStorage.getAll().first()
            _cards.value = if (_shuffleEnabled.value) cardList.shuffled() else cardList
            _currentCardIndex.value = 0
            updateProgress()
            Log.d("PlayCardViewModel", "Cards loaded. Shuffle is ${if (_shuffleEnabled.value) "ON" else "OFF"}")
        }
    }

    private fun shuffleCards() {
        _cards.value = _cards.value.shuffled()
        _currentCardIndex.value = 0
        updateProgress()
    }

    fun toggleOption(optionId: Int) {
        _selectedOptions.value = _selectedOptions.value.toMutableSet().apply {
            if (contains(optionId)) remove(optionId) else add(optionId)
        }
        _isOptionSelected.value = _selectedOptions.value.isNotEmpty()
    }

    fun nextCard() {
        val currentCard = _cards.value[_currentCardIndex.value]
        val isCorrect = _selectedOptions.value == currentCard.correctOptionId
        _gameResults.value += currentCard to isCorrect

        if (_currentCardIndex.value < _cards.value.size - 1) {
            _currentCardIndex.value++
            _selectedOptions.value = emptySet()
            _isOptionSelected.value = false
        } else {
            _gameFinished.value = true
        }
        updateProgress()
    }

    private fun updateProgress() {
        _progress.value = "${_currentCardIndex.value + 1}/${_cards.value.size}"
    }

    private fun resetState() {
        _currentCardIndex.value = 0
        _selectedOptions.value = emptySet()
        _gameResults.value = emptyList()
        _gameFinished.value = false
        _isOptionSelected.value = false
        _showExitConfirmation.value = false
    }

    fun restartGame() {
        resetState()
        loadCards()
        Log.d("PlayCardViewModel", "Game restarted. Shuffle is ${if (_shuffleEnabled.value) "ON" else "OFF"}")
    }

    fun resetGameState(shuffleEnabled: Boolean) {
        _shuffleEnabled.value = shuffleEnabled
        resetState()
        loadCards()
        Log.d(
            "PlayCardViewModel",
            "Game state reset. Shuffle is ${if (_shuffleEnabled.value) "ON" else "OFF"}"
        )

    }
    fun onTryExit() {
        _showExitConfirmation.value = true
    }

    fun onExitConfirmed() {
        _showExitConfirmation.value = false
    }

    fun onExitCancelled() {
        _showExitConfirmation.value = false
    }
}