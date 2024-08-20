package nz.ac.canterbury.seng303.assg1.screens

import android.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel

@Composable
fun ViewCards(navController: NavController, cardViewModel: CardViewModel) {
    cardViewModel.getCards()
    val cards: List<Card> by cardViewModel.cards.collectAsState(emptyList())
    if (cards.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn {
            items(cards) { card ->
                CardItem(navController = navController, card = card, deleteFn = { id: Int -> cardViewModel.deleteCardById(id) })
                Divider() // Add a divider between items
            }
        }
    }

}
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "There are no cards created.\nPlease create some cards.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CardItem(navController: NavController, card: Card, deleteFn: (id: Int) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("CardCard/${card.id}") },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Display title and timestamp
        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxWidth()
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = convertTimestampToReadableTime(card.timestamp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            IconButton(onClick = {
                navController.navigate("EditCard/${card.id}")
            }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    tint = Color.Blue
                )
            }
            IconButton(onClick = {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Delete Card \"${card.title}\"?")
                    .setCancelable(false)
                    .setPositiveButton("Delete") { dialog, id ->
                        deleteFn(card.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}