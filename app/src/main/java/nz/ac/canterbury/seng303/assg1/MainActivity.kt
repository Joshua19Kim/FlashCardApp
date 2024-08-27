package nz.ac.canterbury.seng303.assg1

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.compose.ui.Alignment
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import nz.ac.canterbury.seng303.assg1.viewmodels.GameState
import org.koin.androidx.compose.koinViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayerNameViewModel
import nz.ac.canterbury.seng303.lab2.R


class MainActivity : ComponentActivity() {
    private lateinit var playCardViewModel: PlayCardViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleConfigurationChanges()

        val playCardViewModel: PlayCardViewModel by viewModel()
        this.playCardViewModel = playCardViewModel


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

                            composable("LogoSplash/{playerName}/{shuffleCardsEnabled}/{shuffleOptionsEnabled}",
                                arguments = listOf(
                                    navArgument("playerName") { type = NavType.StringType },
                                    navArgument("shuffleCardsEnabled") { type = NavType.BoolType },
                                    navArgument("shuffleOptionsEnabled") { type = NavType.BoolType }
                                )
                            ) { backStackEntry ->
                                val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                                val shuffleCardsEnabled = backStackEntry.arguments?.getBoolean("shuffleCardsEnabled") ?: false
                                val shuffleOptionsEnabled = backStackEntry.arguments?.getBoolean("shuffleOptionsEnabled") ?: false
                                LogoSplashScreen(
                                    onSplashComplete = {
                                        navController.navigate("PlayCard/$playerName/$shuffleCardsEnabled/$shuffleOptionsEnabled") {
                                            popUpTo("LogoSplash/{playerName}/{shuffleCardsEnabled}/{shuffleOptionsEnabled}") { inclusive = true }
                                        }
                                    }
                                )
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
                                route = "PlayCard/{playerName}/{shuffleCardsEnabled}/{shuffleOptionsEnabled}",
                                arguments = listOf(
                                    navArgument("playerName") { type = NavType.StringType },
                                    navArgument("shuffleCardsEnabled") { type = NavType.BoolType },
                                    navArgument("shuffleOptionsEnabled") { type = NavType.BoolType }
                                )
                            ) { backStackEntry ->
                                val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                                val shuffleCardsEnabled = backStackEntry.arguments?.getBoolean("shuffleCardsEnabled") ?: false
                                val shuffleOptionsEnabled = backStackEntry.arguments?.getBoolean("shuffleOptionsEnabled") ?: false

                                val viewModel: PlayCardViewModel = koinViewModel()

                                LaunchedEffect(Unit) {
                                    viewModel.startNewGame(shuffleCardsEnabled, shuffleOptionsEnabled)
                                }

                                PlayCardScreen(
                                    viewModel = viewModel,
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
                                        playCardViewModel.resetGame()
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
    private fun handleConfigurationChanges() {
        val configChanges = ActivityInfo.CONFIG_ORIENTATION or
                ActivityInfo.CONFIG_SCREEN_SIZE or
                ActivityInfo.CONFIG_KEYBOARD_HIDDEN
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current game state
        val playCardViewModel: PlayCardViewModel by viewModel()
        val currentState = playCardViewModel.gameState.value
        if (currentState is GameState.InProgress) {
            outState.putParcelable("gameState", currentState)
        }
    }



}

@Composable
fun LogoSplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 2f else 1f,
        animationSpec = tween(durationMillis = 1300)
    )
    val sizeAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 0.5f else 1f,
        animationSpec = tween(durationMillis = 1300)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1300)
        onSplashComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.fcapplogo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(1000.dp)
                .alpha(alphaAnimation.value)
                .graphicsLayer(
                    scaleX = sizeAnimation.value,
                    scaleY = sizeAnimation.value
                )
        )
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
    var showShuffleCardsMessage by remember { mutableStateOf(false) }
    var showShuffleOptionsMessage by remember { mutableStateOf(false) }
    val shuffleCardsEnabled by playCardViewModel.shuffleCardsEnabled.collectAsState()
    val shuffleOptionsEnabled by playCardViewModel.shuffleOptionsEnabled.collectAsState()
    var topToastMessage by remember { mutableStateOf<String?>(null) }


    BackHandler {

    }


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
                            navController.navigate("LogoSplash/$playerName/$shuffleCardsEnabled/$shuffleOptionsEnabled")
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
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "PLAY",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Shuffle Cards",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = shuffleCardsEnabled,
                            onCheckedChange = {
                                playCardViewModel.toggleShuffleCards()
                                if (it) {
                                    showShuffleCardsMessage = true
                                } else {
                                    topToastMessage = "Shuffle Cards disabled"
                                }
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Shuffle Options",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = shuffleOptionsEnabled,
                            onCheckedChange = {
                                playCardViewModel.toggleShuffleOptions()
                                if (it) {
                                    showShuffleOptionsMessage = true
                                } else {
                                    topToastMessage = "Shuffle Options disabled"
                                }
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = topToastMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = topToastMessage ?: "",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

        }
    }


    AnimatedVisibility(
        visible = showShuffleCardsMessage,
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
        LaunchedEffect(showShuffleCardsMessage) {
            delay(1500)
            showShuffleCardsMessage = false
        }
    }
    AnimatedVisibility(
        visible = showShuffleOptionsMessage,
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
                "Options shuffled!  \n\n\n Let's Play Flash Card!",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LaunchedEffect(showShuffleOptionsMessage) {
            delay(1500)
            showShuffleOptionsMessage = false
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
    LaunchedEffect(topToastMessage) {
        if (topToastMessage != null) {
            delay(2000) // Show the message for 2 seconds
            topToastMessage = null
        }
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