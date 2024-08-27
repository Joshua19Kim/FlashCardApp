package nz.ac.canterbury.seng303.assg1.viewmodels

import android.os.Parcelable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class PlayCardViewModel(
    private val cardStorage: Storage<Card>,
    private val playerNameViewModel: PlayerNameViewModel
) : ViewModel() {
    val playerName = playerNameViewModel.playerName

    private val _shuffleCardsEnabled = MutableStateFlow(false)
    val shuffleCardsEnabled: StateFlow<Boolean> = _shuffleCardsEnabled

    private val _shuffleOptionsEnabled = MutableStateFlow(false)
    val shuffleOptionsEnabled: StateFlow<Boolean> = _shuffleOptionsEnabled

    private val _gameState = MutableStateFlow<GameState>(GameState.NotStarted)
    val gameState: StateFlow<GameState> = _gameState

    fun saveGameState() {
        val currentState = GameState.InProgress(
            cards = _cards.value,
            currentCardIndex = _currentCardIndex.value,
            selectedOptions = _selectedOptions.value,
            gameResults = _gameResults.value,
            shuffleCardsEnabled = _shuffleCardsEnabled.value,
            shuffleOptionsEnabled = _shuffleOptionsEnabled.value
        )
        _gameState.value = currentState
    }

    fun startNewGame(shuffleCardsEnabled: Boolean, shuffleOptionsEnabled: Boolean) {
        viewModelScope.launch {
            _shuffleCardsEnabled.value = shuffleCardsEnabled
            _shuffleOptionsEnabled.value = shuffleOptionsEnabled
            resetState()
            loadCards()
            _gameState.value = GameState.InProgress(
                cards = _cards.value,
                currentCardIndex = _currentCardIndex.value,
                selectedOptions = _selectedOptions.value,
                gameResults = _gameResults.value,
                shuffleCardsEnabled = _shuffleCardsEnabled.value,
                shuffleOptionsEnabled = _shuffleOptionsEnabled.value
            )
        }
    }


    private val _answerFeedback = MutableStateFlow<Pair<Boolean, Boolean>?>(null)
    val answerFeedback: StateFlow<Pair<Boolean, Boolean>?> = _answerFeedback



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

    init {
        loadCards()
    }

    fun toggleShuffleCards() {
        _shuffleCardsEnabled.value = !_shuffleCardsEnabled.value
        loadCards()
    }

    fun toggleShuffleOptions() {
        _shuffleOptionsEnabled.value = !_shuffleOptionsEnabled.value
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            var cardList = cardStorage.getAll().first()
            if (_shuffleCardsEnabled.value) {
                cardList = cardList.shuffled()
            }
            if (_shuffleOptionsEnabled.value) {
                cardList = cardList.map { card ->
                    card.copy(options = card.options.shuffled())
                }
            }
            _cards.value = cardList
            _currentCardIndex.value = 0
            updateProgress()
        }
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

        _answerFeedback.value = isCorrect to (_currentCardIndex.value == _cards.value.size - 1)

        if (_currentCardIndex.value < _cards.value.size - 1) {
            _currentCardIndex.value++
            _selectedOptions.value = emptySet()
            _isOptionSelected.value = false
        } else {
            _gameFinished.value = true
        }
        updateProgress()
    }

    fun clearAnswerFeedback() {
        _answerFeedback.value = null
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
    fun resetGame() {
        _gameState.value = GameState.NotStarted
        _currentCardIndex.value = 0
        _selectedOptions.value = emptySet()
        _gameResults.value = emptyList()
        _gameFinished.value = false
        _isOptionSelected.value = false
        _showExitConfirmation.value = false
        loadCards()
    }
    fun restartGame() {
        resetState()
        loadCards()
    }

}

sealed class GameState : Parcelable {
    @Parcelize
    object NotStarted : GameState()

    @Parcelize
    data class InProgress(
        val cards: List<Card>,
        val currentCardIndex: Int,
        val selectedOptions: Set<Int>,
        val gameResults: List<Pair<Card, Boolean>>,
        val shuffleCardsEnabled: Boolean,
        val shuffleOptionsEnabled: Boolean
    ) : GameState()

    @Parcelize
    object Finished : GameState()
}