package nz.ac.canterbury.seng303.assg1.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    viewModel: CreateCardViewModel = koinViewModel(),
    onCreateCardClick: () -> Unit,
    onCancelCard: () -> Unit
) {
    val navController = rememberNavController()
    var showCreateExitConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        showCreateExitConfirmation = true
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Create a new flash card",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.showCancelDialog() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel card creation",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Question") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        viewModel.options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = index in viewModel.correctOptionIndices,
                    onCheckedChange = {
                        if (!viewModel.isOptionEmpty(index)) {
                            viewModel.toggleCorrectOption(index)
                        }
                    },
                    enabled = !viewModel.isOptionEmpty(index)
                )
                OutlinedTextField(
                    value = option,
                    onValueChange = {
                        viewModel.updateOption(index, it)
                        if (it.isBlank() && index in viewModel.correctOptionIndices) {
                            viewModel.toggleCorrectOption(index)
                        }
                    },
                    label = { Text("Option ${index + 1}") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                IconButton(
                    onClick = { viewModel.deleteOption(index) },
                    enabled = viewModel.canDeleteOption()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete option",
                        tint = if (viewModel.canDeleteOption()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.addOption() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Another Option")
        }

        Button(
            onClick = {
                if (viewModel.validateAndSaveCard()) {
                    onCreateCardClick()
                } else {
                    Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Card and Return")
        }
        if (viewModel.showCancelDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissCancelDialog() },
                title = { Text("Cancel Card Creation") },
                text = { Text("Do you want to cancel this card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissCancelDialog()
                            onCancelCard()
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissCancelDialog() }) {
                        Text("No")
                    }
                }
            )
        }

        if (showCreateExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showCreateExitConfirmation = false },
                title = { Text("Confirm Exit") },
                text = { Text("Do you really want to exit? Your progress will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        showCreateExitConfirmation = false
                        navController.navigate("Home")
                    }) {
                        Text("Yes, Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateExitConfirmation = false }) {
                        Text("No, Continue")
                    }
                }
            )
        }
    }
}