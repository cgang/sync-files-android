package com.github.cgang.syncfiles.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.cgang.syncfiles.presentation.files.FileBrowserScreen
import com.github.cgang.syncfiles.presentation.files.FileBrowserViewModel
import com.github.cgang.syncfiles.presentation.login.LoginScreen
import com.github.cgang.syncfiles.presentation.login.LoginViewModel
import com.github.cgang.syncfiles.presentation.setup.SetupScreen
import com.github.cgang.syncfiles.presentation.setup.SetupViewModel

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Login : Screen("login")
    object FileBrowser : Screen("file_browser/{repoName}") {
        fun createRoute(repoName: String) = "file_browser/$repoName"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Setup.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Setup.route) {
            val viewModel: SetupViewModel = hiltViewModel()
            SetupScreen(
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                onSuccess = { username ->
                    navController.navigate(Screen.FileBrowser.createRoute(username)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onServerChange = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.FileBrowser.route,
            arguments = listOf(
                navArgument("repoName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val repoName = backStackEntry.arguments?.getString("repoName") ?: ""
            val viewModel: FileBrowserViewModel = hiltViewModel()
            FileBrowserScreen(
                repoName = repoName,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}
