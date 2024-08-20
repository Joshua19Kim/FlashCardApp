package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import nz.ac.canterbury.seng303.assg1.models.Card

class EditCardViewModel: ViewModel() {
    var title by mutableStateOf("")
        private set

    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    var content by mutableStateOf("")
        private set

    fun updateContent(newContent: String) {
        content = newContent
    }

    var isArchived by mutableStateOf(false)
        private set

    fun updateIsArchived(newIsArchived: Boolean) {
        isArchived = newIsArchived
    }

    var timestamp by mutableStateOf(0L)
        private set

    // Function to set the default values based on the selected Card
    fun setDefaultValues(selectedCard: Card?) {
        selectedCard?.let {
            title = it.title
            content = it.content
            isArchived = it.isArchived
            timestamp = it.timestamp
        }
    }
}