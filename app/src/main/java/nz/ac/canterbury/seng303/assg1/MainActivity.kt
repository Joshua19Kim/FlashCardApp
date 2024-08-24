package nz.ac.canterbury.seng303.assg1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Flash Card 303") },
                            navigationIcon = {
                                if (currentRoute != "Home") {
                                    IconButton(onClick = { navController.popBackStack() }) {
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
                                LaunchedEffect(Unit) {
                                    playCardViewModel.resetGameState()
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
    var newPlayerName by remember { mutableStateOf("") }
    val cards by cardViewModel.cards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isShuffling by remember { mutableStateOf(false) }
    var showShuffleMessage by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isShuffling) 360f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

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

                Text("Player: $playerName", style = MaterialTheme.typography.titleMedium)

                Button(
                    onClick = {
                        newPlayerName = playerName
                        showNameDialog = true
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Change Player Name")
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
                            playCardViewModel.resetGameState()
                            navController.navigate("PlayCard/${playerName}")
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
            }

            Button(
                onClick = {
                    if (cards.isNotEmpty()) {
                        isShuffling = true
                        scope.launch {
                            cardViewModel.shuffleCards()
                            showShuffleMessage = true
                            delay(1000)
                            showShuffleMessage = false
                            isShuffling = false
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "No cards to shuffle",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "🔀",
                    modifier = Modifier
                        .rotate(rotation)
                        .padding(end = 8.dp)
                )
                Text("Shuffle")
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
                "Cards shuffled!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }


    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Change Player Name") },
            text = {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        playerNameViewModel.setPlayerName(newPlayerName)
                        showNameDialog = false
                    }
                ) {
                    Text("Save")
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