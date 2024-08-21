package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class CreateCardViewModel(private val cardStorage: Storage<Card>) : ViewModel() {
    private val _title = mutableStateOf("")
    val title: String get() = _title.value

    private val _options = mutableStateListOf("", "")
    val options: List<String> get() = _options

    private val _correctOptionIndices = mutableStateOf(setOf<Int>())
    val correctOptionIndices: Set<Int> get() = _correctOptionIndices.value

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateOption(index: Int, text: String) {
        if (index in _options.indices) {
            _options[index] = text
        }
    }

    fun addOption() {
        _options.add("")
    }

    fun deleteOption(index: Int) {
        if (_options.size > 2 && index in _options.indices) {
            _options.removeAt(index)
            _correctOptionIndices.value = _correctOptionIndices.value.map {
                if (it > index) it - 1 else it
            }.filter { it < _options.size }.toSet()
        }
    }

    fun toggleCorrectOption(index: Int) {
        _correctOptionIndices.value = _correctOptionIndices.value.toMutableSet().apply {
            if (index in this) remove(index) else add(index)
        }
    }

    fun canDeleteOption(): Boolean = _options.size > 2

    fun createCard(): Card? {
        val nonEmptyOptions = _options.filter { it.isNotBlank() }
        return if (_title.value.isNotBlank() && nonEmptyOptions.size >= 2 && _correctOptionIndices.value.isNotEmpty()) {
            Card.create(
                id = System.currentTimeMillis().toInt(), // Simple ID generation
                title = _title.value,
                initialOptions = nonEmptyOptions,
                correctOptionIndices = _correctOptionIndices.value
            )
        } else null
    }
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value
    private val _showErrorDialog = mutableStateOf(false)
    val showErrorDialog: Boolean get() = _showErrorDialog.value

    fun validateAndSaveCard(): Boolean {
        when {
            _title.value.isBlank() -> {
                _errorMessage.value = "A flash card must have a question."
                _showErrorDialog.value = true
                return false
            }
            _options.filter { it.isNotBlank() }.size < 2 -> {
                _errorMessage.value = "A flash card must have at least 2 answers."
                _showErrorDialog.value = true
                return false
            }
            _correctOptionIndices.value.isEmpty() -> {
                _errorMessage.value = "A flash card must have 1 correct answer."
                _showErrorDialog.value = true
                return false
            }
            else -> {
                _errorMessage.value = null
                _showErrorDialog.value = false
                saveCard()
                return true
            }
        }
    }
    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }


    fun saveCard() {
        createCard()?.let { card ->
            viewModelScope.launch {
                cardStorage.insert(card).collect { result ->
                    if (result == 1) {
                        // Card saved successfully
                        // You might want to clear the form or navigate away
                    }
                }
            }
        }
    }

    fun initializeWithCard(card: Card) {
        _title.value = card.title
        _options.clear()
        _options.addAll(card.options.map { it.text })
        _correctOptionIndices.value = card.correctOptionId
    }

    fun updateCard(id: Int) {
        val updatedCard = createCard()?.copy(id = id)
        updatedCard?.let { card ->
            viewModelScope.launch {
                cardStorage.edit(id, card).collect { result ->
                    if (result == 1) {
                        // Card updated successfully
                        // You might want to clear the form or navigate away
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            cardStorage: Storage<Card>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateCardViewModel(cardStorage) as T
            }
        }
    }


}