package nz.ac.canterbury.seng303.assg1.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayCardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayCardScreen(
    viewModel: PlayCardViewModel = koinViewModel(),
    onGameFinished: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val gameFinished by viewModel.gameFinished.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isOptionSelected by viewModel.isOptionSelected.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val answerFeedback by viewModel.answerFeedback.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(answerFeedback) {
        answerFeedback?.let { (isCorrect, isLastCard) ->
            val message = when {
                isCorrect && isLastCard -> "Correct! You've completed the game!"
                isCorrect -> "Correct! Moving to the next card."
                isLastCard -> "Incorrect. This was the last card."
                else -> "Incorrect. Moving to the next card."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearAnswerFeedback()
        }
    }


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
                text = "Progress: $progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
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
                    .padding(top = 16.dp),
                enabled = isOptionSelected
            ) {
                Text(if (currentCardIndex < cards.size - 1) "Next" else "Finish")
            }
        }
    } else {
        GameResultScreen(
            playerName = playerName,
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