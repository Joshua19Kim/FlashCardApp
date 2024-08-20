package nz.ac.canterbury.seng303.assg1.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateCardViewModel: ViewModel() {
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
}