package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerNameViewModel : ViewModel() {
    private val _playerName = MutableStateFlow("Player")
    val playerName: StateFlow<String> = _playerName

    fun setPlayerName(name: String) {
        _playerName.value = if (name.isBlank()) "Player" else name
    }
}