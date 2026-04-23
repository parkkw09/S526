package app.peter.s526.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.peter.s526.presentation.ui.screen.DetailScreen
import app.peter.s526.presentation.ui.screen.MainPagerScreen
import app.peter.s526.presentation.ui.screen.SearchScreen
import app.peter.s526.presentation.view.main.MainViewModel

@Composable
fun S526NavHost(
    navController: NavHostController,
    startDestination: String = "main"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("main") {
            // MainViewModel은 HiltViewModel이므로 hiltViewModel() 로 가져옵니다.
            // (주의: 기존에 Activity scope 였으므로 만약 필요하다면 parent entry 뷰모델 사용 등 고려.
            // 이 마이그레이션에서는 단일 NavHost 내에 있으므로 기본 hiltViewModel() 사용)
            val viewModel: MainViewModel = hiltViewModel()
            MainPagerScreen(
                viewModel = viewModel,
                onBookClick = { book ->
                    navController.navigate("detail/${book.isbn}")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onHistoryQueryClick = { query ->
                    navController.navigate("search?query=${query}")
                }
            )
        }

        composable(
            route = "search?query={query}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                navController.getBackStackEntry("main")
            }
            val viewModel: MainViewModel = hiltViewModel(parentEntry)
            val query = backStackEntry.arguments?.getString("query") ?: ""
            
            SearchScreen(
                viewModel = viewModel,
                initialQuery = query,
                onBookClick = { book ->
                    navController.navigate("detail/${book.isbn}")
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = "detail/{isbn}",
            arguments = listOf(
                navArgument("isbn") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                navController.getBackStackEntry("main")
            }
            val viewModel: MainViewModel = hiltViewModel(parentEntry)
            val isbn = backStackEntry.arguments?.getString("isbn") ?: return@composable
            
            DetailScreen(
                viewModel = viewModel,
                isbn = isbn,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}
