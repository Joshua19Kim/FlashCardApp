package nz.ac.canterbury.seng303.assg1.screens

import android.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCard(
    navController: NavController,
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    createCardFn: (String, String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Add a new flash card",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { onTitleChange(it) },
            label = { Text("Input title here") },
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = content,
            onValueChange = { onContentChange(it) },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .fillMaxHeight()
                .weight(1f)
        )
        Button(
            onClick = {
                createCardFn(title, content)
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Created Card!")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id ->
                        onTitleChange("")
                        onContentChange("")
                        navController.navigate("cardList")
                    }
                    .setNegativeButton("Cancel") { dialog, id -> dialog.dismiss() }
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
