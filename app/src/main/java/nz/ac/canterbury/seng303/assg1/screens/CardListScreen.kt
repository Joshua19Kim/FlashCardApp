package nz.ac.canterbury.seng303.assg1.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardListScreen(
    viewModel: CardViewModel = koinViewModel(),
    onCreateCardClick: () -> Unit,
    onEditCardClick: (Card) -> Unit
) {
    val cards by viewModel.cards.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Button(onClick = onCreateCardClick) {
            Text("Create New Card")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (cards.isEmpty()) {
            Text("No cards available. Create a new card to get started!")
        } else {
            LazyColumn {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onDelete = { viewModel.deleteCard(card.id) },
                        onEdit = { onEditCardClick(card) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(card: Card, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = card.title, style = MaterialTheme.typography.headlineSmall)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit card",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete card",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}