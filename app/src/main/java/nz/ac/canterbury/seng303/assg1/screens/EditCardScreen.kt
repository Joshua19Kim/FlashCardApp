package nz.ac.canterbury.seng303.assg1.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Int,
    onCardEdited: () -> Unit,
    onCardDeleted: () -> Unit,
    viewModel: CreateCardViewModel = koinViewModel(),

) {
    val navController = rememberNavController()
    var showEditExitConfirmation by remember { mutableStateOf(false) }
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    BackHandler {
        showEditExitConfirmation = true
    }
    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onCardDeleted()
        }
    }
    val context = LocalContext.current
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
                text = "Edit flash card",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.showDeleteDialog() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete card",
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
                    viewModel.updateCard(cardId)
                    onCardEdited()
                } else {
                    Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
        if (viewModel.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteDialog() },
                title = { Text("Confirm Deletion") },
                text = { Text("Do you really want to delete this card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissDeleteDialog()
                            viewModel.deleteCard(cardId)
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                        Text("No")
                    }
                }
            )
        }

        if (showEditExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showEditExitConfirmation = false },
                title = { Text("Confirm Exit") },
                text = { Text("Do you really want to exit? Your progress will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        showEditExitConfirmation = false
                        navController.navigate("Home")
                    }) {
                        Text("Yes, Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditExitConfirmation = false }) {
                        Text("No, Continue")
                    }
                }
            )
        }
    }
}