package nz.ac.canterbury.seng303.assg1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.screens.CardListScreen
import nz.ac.canterbury.seng303.assg1.screens.CreateCardScreen
import nz.ac.canterbury.seng303.assg1.screens.EditCardScreen
import nz.ac.canterbury.seng303.assg1.ui.theme.Assg1Theme
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {
    private val createCardViewModel: CreateCardViewModel by viewModel()

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
                                    onEditCardClick = { card ->
                                        navController.currentBackStackEntry?.savedStateHandle?.set("card", card)
                                        navController.navigate("EditCard")
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

                            composable("EditCard") {
                                val card = navController.previousBackStackEntry?.savedStateHandle?.get<Card>("card")
                                card?.let {
                                    EditCardScreen(
                                        card = it,
                                        onCardEdited = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun Home(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("CreateCard") }) {
            Text("Create Flash Card")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("cardList") }) {
            Text("View Flash Cards")
        }
    }
}
