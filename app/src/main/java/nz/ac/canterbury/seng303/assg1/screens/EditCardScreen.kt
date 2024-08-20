package nz.ac.canterbury.seng303.assg1.screens

import android.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.assg1.viewmodels.EditCardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCard(
    CardId: String,
    editCardViewModel: EditCardViewModel,
    cardViewModel: CardViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val selectedCardState by cardViewModel.selectedCard.collectAsState(null)
    val card: Card? = selectedCardState // we explicitly assign to Card to help the compilers smart cast out

    LaunchedEffect(card) {  // Get the default values for the Card properties
        if (card == null) {
            cardViewModel.getCardById(CardId.toIntOrNull())
        } else {
            editCardViewModel.setDefaultValues(card)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = editCardViewModel.title,
            onValueChange = { editCardViewModel.updateTitle(it) },
            label = { Text("title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = editCardViewModel.content,
            onValueChange = { editCardViewModel.updateContent(it) },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .fillMaxHeight()
                .weight(1f)
        )
        OutlinedTextField(
            value = convertTimestampToReadableTime(editCardViewModel.timestamp),
            onValueChange = { },
            label = { Text("Timestamp") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Archived: ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Checkbox(
                checked = editCardViewModel.isArchived,
                onCheckedChange = {editCardViewModel.updateIsArchived(it)}
            )
        }
        Button(
            onClick = {
                cardViewModel.editCardById(CardId.toIntOrNull(), card = Card(CardId.toInt(), editCardViewModel.title, editCardViewModel.content, editCardViewModel.timestamp, editCardViewModel.isArchived))
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Edited Card!")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id ->
                        navController.navigate("CardList")
                    }
                val alert = builder.create()
                alert.show()

            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Save")
        }
    }
}
