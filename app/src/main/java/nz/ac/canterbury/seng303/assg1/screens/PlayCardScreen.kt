package nz.ac.canterbury.seng303.assg1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayCardViewModel

@Composable
fun PlayCardScreen(
    viewModel: PlayCardViewModel = viewModel(),
    onGameFinished: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val gameFinished by viewModel.gameFinished.collectAsState()

    if (cards.isEmpty()) {
        AlertDialog(
            onDismissRequest = onGameFinished,
            title = { Text("No Cards Available") },
            text = { Text("You need to add cards before playing.") },
            confirmButton = {
                TextButton(onClick = onGameFinished) {
                    Text("OK")
                }
            }
        )
    } else if (!gameFinished) {
        val currentCard = cards[currentCardIndex]
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = currentCard.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            currentCard.options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = option.id in selectedOptions,
                        onCheckedChange = { viewModel.toggleOption(option.id) }
                    )
                    Text(text = option.text, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Button(
                onClick = { viewModel.nextCard() },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 16.dp)
            ) {
                Text(if (currentCardIndex < cards.size - 1) "Next" else "Finish")
            }
        }
    } else {
        GameResultScreen(
            playerName = viewModel.playerName,
            results = viewModel.gameResults,
            onRestartGame = {
                viewModel.restartGame()
            },
            onFinish = onGameFinished
        )
    }
}

@Composable
fun GameResultScreen(
    playerName: String,
    results: List<Pair<Card, Boolean>>,
    onRestartGame: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Game Results for $playerName",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Score: ${results.count { it.second }}/${results.size}",
            style = MaterialTheme.typography.headlineSmall,
        )
        results.forEachIndexed { index, (card, isCorrect) ->
            Text(
                text = "${index + 1}. ${card.title}: ${if (isCorrect) "Correct" else "Incorrect"}",
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            if (!isCorrect) {
                Text(
                    text = "   Correct answer(s): ${card.options.filter { it.id in card.correctOptionId }.joinToString { it.text }}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onRestartGame) {
                Text("Play Again")
            }
            Button(onClick = onFinish) {
                Text("Finish")
            }
        }
    }
}