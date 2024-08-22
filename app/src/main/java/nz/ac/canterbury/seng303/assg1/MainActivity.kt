package nz.ac.canterbury.seng303.assg1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nz.ac.canterbury.seng303.assg1.screens.CardListScreen
import nz.ac.canterbury.seng303.assg1.screens.CreateCardScreen
import nz.ac.canterbury.seng303.assg1.screens.EditCardScreen
import nz.ac.canterbury.seng303.assg1.ui.theme.Assg1Theme
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nz.ac.canterbury.seng303.assg1.screens.PlayCardScreen
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayCardViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assg1Theme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Flash Card 303") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) {
                    Box(modifier = Modifier.padding(it)) {
                        NavHost(navController = navController, startDestination = "Home") {
                            composable("Home") {
                                Home(navController = navController)
                            }

                            composable("cardList") {
                                CardListScreen(
                                    onCreateCardClick = { navController.navigate("CreateCard") },
                                    onEditCardClick = { cardId ->
                                        navController.navigate("EditCard/$cardId")
                                    }
                                )
                            }

                            composable("CreateCard") {
                                CreateCardScreen(
                                    onCreateCardClick = {
                                        navController.navigate("Home") {
                                            popUpTo("Home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(
                                route = "EditCard/{cardId}",
                                arguments = listOf(navArgument("cardId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val cardId = backStackEntry.arguments?.getInt("cardId")
                                if (cardId != null) {
                                    EditCardScreen(
                                        cardId = cardId,
                                        onCardEdited = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Error: Card not found")
                                }
                            }

                            composable(
                                route = "PlayCard/{playerName}",
                                arguments = listOf(navArgument("playerName") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                                val playCardViewModel: PlayCardViewModel by viewModel()
                                playCardViewModel.setPlayerName(playerName)
                                PlayCardScreen(
                                    viewModel = playCardViewModel,
                                    onGameFinished = {
                                        navController.navigate("Home") {
                                            popUpTo("Home") { inclusive = true }
                                        }
                                    }
                                )
                            }


                        }
                    }
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navController: NavController,
    cardViewModel: CardViewModel = koinViewModel()
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }
    val cards by cardViewModel.cards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StyledButton(
                text = "Create Flash Card",
                onClick = { navController.navigate("CreateCard") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledButton(
                text = "View Flash Cards (${cards.size})",
                onClick = { navController.navigate("cardList") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (cards.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "There is no card. Please make a card",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        showNameDialog = true
                    }
                },
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ){
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PLAY",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Enter Your Name") },
            text = {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNameDialog = false
                        navController.navigate("PlayCard/$playerName")
                    },
                    enabled = playerName.isNotBlank()
                ) {
                    Text("Start Game")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun StyledButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )
    }

}