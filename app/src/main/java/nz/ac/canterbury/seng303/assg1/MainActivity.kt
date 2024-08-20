package nz.ac.canterbury.seng303.assg1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nz.ac.canterbury.seng303.assg1.screens.CreateCard
import nz.ac.canterbury.seng303.assg1.screens.EditCard
import nz.ac.canterbury.seng303.assg1.screens.CardCard
import nz.ac.canterbury.seng303.assg1.screens.ViewCards
import nz.ac.canterbury.seng303.assg1.ui.theme.Lab1Theme
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.EditCardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel

class MainActivity : ComponentActivity() {

    private val cardViewModel: CardViewModel by koinViewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardViewModel.loadDefaultCardsIfNoneExist()

        setContent {
            Lab1Theme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        // Add your AppBar content here
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
                        val createCardViewModel: CreateCardViewModel = viewModel()
                        val editCardViewModel: EditCardViewModel = viewModel()
                        NavHost(navController = navController, startDestination = "Home") {
                            composable("Home") {
                                Home(navController = navController)
                            }
                            composable(
                                "CardCard/{cardId}",
                                arguments = listOf(navArgument("cardId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val cardId = backStackEntry.arguments?.getString("cardId")
                                cardId?.let { CardIdParam: String -> CardCard(CardIdParam, cardViewModel) }
                            }
                            composable("EditCard/{CardId}", arguments = listOf(navArgument("cardId") {
                                type = NavType.StringType
                            })
                            ) { backStackEntry ->
                                val CardId = backStackEntry.arguments?.getString("cardId")
                                CardId?.let { CardIdParam: String -> EditCard(CardIdParam, editCardViewModel, cardViewModel, navController = navController) }
                            }
                            composable("ViewCards") {
                                ViewCards(navController, cardViewModel)
                            }

                            composable("CreateCard") {
                                CreateCard(navController = navController, title = createCardViewModel.title,
                                    onTitleChange = {newtitle ->
                                            val title = newtitle.replace("badword", "*******")
                                            createCardViewModel.updateTitle(title)
                                    },
                                    content = createCardViewModel.content, onContentChange = { newContent -> createCardViewModel.updateContent(newContent)},
                                    createCardFn = {question, content -> cardViewModel.createCard(question, content)}
                                    )
//                                CreateCardStandAlone(navController = navController)
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
        Button(onClick = { navController.navigate("ViewCards") }) {
            Text("View Flash Cards")
        }
        Button(onClick = { navController.navigate("CreateCard") }) {
            Text("Create Flash Card")
        }
        Button(onClick = { navController.navigate("CardGrid") }) {
            Text("Play Flash Cards")
        }


//        Button(onClick = { navController.navigate("CardCard/1") }) {
//            Text("Go to Card Card")
//        }
    }
}
