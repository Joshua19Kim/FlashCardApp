package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.assg1.datastore.Storage
import nz.ac.canterbury.seng303.assg1.models.Card

class CreateCardViewModel(
    private val cardStorage: Storage<Card>
) : ViewModel() {
    private val _title = mutableStateOf("")
    val title: String get() = _title.value

    private val _options = mutableStateListOf("", "")
    val options: List<String> get() = _options

    private val _correctOptionIndices = mutableStateOf(setOf<Int>())
    val correctOptionIndices: Set<Int> get() = _correctOptionIndices.value

    private val _showCancelDialog = mutableStateOf(false)
    val showCancelDialog: Boolean get() = _showCancelDialog.value

    private val _showDeleteDialog = mutableStateOf(false)
    val showDeleteDialog: Boolean get() = _showDeleteDialog.value

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteCard(cardId: Int) {
        viewModelScope.launch {
            cardStorage.delete(cardId).collect { result ->
                if (result == 1) {
                    _deleteSuccess.value = true
                }
            }
        }
    }

    fun showCancelDialog() {
        _showCancelDialog.value = true
    }

    fun dismissCancelDialog() {
        _showCancelDialog.value = false
    }

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
        val nonEmptyOptions = _options.mapIndexedNotNull { index, option ->
            if (option.isNotBlank()) Card.Option(index, option) else null
        }
        val adjustedCorrectIndices = _correctOptionIndices.value
            .filter { it < nonEmptyOptions.size }
            .map { nonEmptyOptions[it].id }
            .toSet()

        return if (_title.value.isNotBlank() && nonEmptyOptions.size >= 2 && adjustedCorrectIndices.isNotEmpty()) {
            Card.create(
                id = System.currentTimeMillis().toInt(),
                title = _title.value,
                initialOptions = nonEmptyOptions.map { it.text },
                correctOptionIndices = adjustedCorrectIndices
            )
        } else null
    }
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value
    private val _showErrorDialog = mutableStateOf(false)

    fun validateAndSaveCard(): Boolean {
        val nonEmptyOptionsCount = _options.count { it.isNotBlank() }
        val validCorrectOptions = _correctOptionIndices.value.count { index ->
            index < _options.size && _options[index].isNotBlank()
        }

        when {
            _title.value.isBlank() -> {
                _errorMessage.value = "A flash card must have a question."
                _showErrorDialog.value = true
                return false
            }
            nonEmptyOptionsCount < 2 -> {
                _errorMessage.value = "A flash card must have at least 2 non-empty answers."
                _showErrorDialog.value = true
                return false
            }
            validCorrectOptions == 0 -> {
                _errorMessage.value = "A flash card must have at least 1 correct non-empty answer."
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

    fun saveCard() {
        createCard()?.let { card ->
            viewModelScope.launch {
                cardStorage.insert(card).collect { result ->
                    if (result == 1) {
                    }
                }
            }
        }
    }
    fun isOptionEmpty(index: Int): Boolean {
        return options.getOrNull(index)?.isBlank() ?: true
    }

    fun loadCard(cardId: Int) {
        viewModelScope.launch {
            cardStorage.get { it.id == cardId }.collect { card ->
                initializeWithCard(card)
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