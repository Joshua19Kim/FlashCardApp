package nz.ac.canterbury.seng303.assg1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.delay
import nz.ac.canterbury.seng303.assg1.screens.PlayCardScreen
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayCardViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayerNameViewModel


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assg1Theme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: "Home"
                var showPlayExitConfirmation by remember { mutableStateOf(false) }
                var showCreateExitConfirmation by remember { mutableStateOf(false) }
                var showEditExitConfirmation by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Flash Card 303") },
                            navigationIcon = {
                                if (currentRoute != "Home") {
                                    IconButton(onClick = {
                                        if (currentRoute.startsWith("PlayCard")) {
                                            showPlayExitConfirmation = true
                                        } else if (currentRoute.startsWith("CreateCard")) {
                                            showCreateExitConfirmation = true
                                        } else if (currentRoute.startsWith("EditCard")) {
                                            showEditExitConfirmation = true
                                        } else {
                                            navController.popBackStack()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
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
                                    },
                                    onCancelCard = {
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
                                        onCardEdited = { navController.popBackStack() },
                                        onCardDeleted = {
                                            navController.navigate("cardList") {
                                                popUpTo("cardList") { inclusive = true }
                                            }
                                        }
                                    )
                                } else {
                                    Text("Error: Card not found")
                                }
                            }

                            composable(
                                route = "PlayCard/{playerName}/{shuffleEnabled}",
                                arguments = listOf(
                                    navArgument("playerName") { type = NavType.StringType },
                                    navArgument("shuffleEnabled") { type = NavType.BoolType }
                                )
                            ) { backStackEntry ->
                                val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                                val shuffleEnabled = backStackEntry.arguments?.getBoolean("shuffleEnabled") ?: false
                                val playCardViewModel: PlayCardViewModel by viewModel()
                                LaunchedEffect(Unit) {
                                    playCardViewModel.resetGameState(shuffleEnabled)
                                }
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

                        if (showPlayExitConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showPlayExitConfirmation = false },
                                title = { Text("Confirm Exit") },
                                text = { Text("Do you really want to exit? Your progress will be lost.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showPlayExitConfirmation = false
                                        navController.popBackStack()
                                    }) {
                                        Text("Yes, Exit")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showPlayExitConfirmation = false }) {
                                        Text("No, Continue")
                                    }
                                }
                            )
                        }
                        if (showCreateExitConfirmation || showEditExitConfirmation) {
                            AlertDialog(
                                onDismissRequest = {
                                    showCreateExitConfirmation = false
                                    showEditExitConfirmation = false
                                                   },
                                title = { Text("Confirm Exit") },
                                text = { Text("Do you really want to exit?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showCreateExitConfirmation = false
                                        showEditExitConfirmation = false
                                        navController.popBackStack()
                                    }) {
                                        Text("Yes, Exit")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showCreateExitConfirmation = false
                                        showEditExitConfirmation = false
                                    }) {
                                        Text("No, Continue")
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navController: NavController,
    cardViewModel: CardViewModel = koinViewModel(),
    playCardViewModel: PlayCardViewModel = koinViewModel(),
    playerNameViewModel: PlayerNameViewModel = koinViewModel()

) {
    val playerName by playerNameViewModel.playerName.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var dialogPlayerName by remember { mutableStateOf("") }
    val cards by cardViewModel.cards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showShuffleMessage by remember { mutableStateOf(false) }
    val shuffleEnabled by playCardViewModel.shuffleEnabled.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$playerName",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Button(
                    onClick = {
                        dialogPlayerName = ""
                        showNameDialog = true
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Change Name")
                }

                Spacer(modifier = Modifier.height(50.dp))

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
                            navController.navigate("PlayCard/${playerName}/${shuffleEnabled}")
                        }
                    },
                    modifier = Modifier.size(200.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
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

                Spacer(modifier = Modifier.height(80.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        "Shuffle Cards",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = shuffleEnabled,
                        onCheckedChange = {
                            playCardViewModel.toggleShuffle()
                            if (it) {
                                showShuffleMessage = true
                            }
                        }
                    )
                }

            }


        }
    }
    AnimatedVisibility(
        visible = showShuffleMessage,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Cards shuffled!  \n\n\n" +
                        " Let's Play Flash Card!",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LaunchedEffect(showShuffleMessage) {
            delay(1500)
            showShuffleMessage = false
        }
    }


    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Change Player Name") },
            text = {
                OutlinedTextField(
                    value = dialogPlayerName,
                    onValueChange = { dialogPlayerName = it },
                    label = { Text("Enter New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogPlayerName.isNotBlank()) {
                            playerNameViewModel.setPlayerName(dialogPlayerName)
                            showNameDialog = false
                        }
                    },
                    enabled = dialogPlayerName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNameDialog = false
                    }
                ) {
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