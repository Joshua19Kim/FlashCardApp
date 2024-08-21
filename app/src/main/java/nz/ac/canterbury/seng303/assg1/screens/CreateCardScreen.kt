package nz.ac.canterbury.seng303.assg1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    viewModel: CreateCardViewModel = koinViewModel(),
    onCreateCardClick: () -> Unit,

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create a new flash card",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                    onCheckedChange = { viewModel.toggleCorrectOption(index) }
                )
                OutlinedTextField(
                    value = option,
                    onValueChange = { viewModel.updateOption(index, it) },
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
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Card and Return")
        }
        if (viewModel.showErrorDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissErrorDialog() },
                title = { Text("Error") },
                text = { Text(viewModel.errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}