package com.tvstorage.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tvstorage.app.ui.screens.addtv.AddTVScreen
import com.tvstorage.app.ui.screens.archive.ArchiveScreen
import com.tvstorage.app.ui.screens.details.DetailsScreen
import com.tvstorage.app.ui.screens.home.HomeScreen
import com.tvstorage.app.ui.screens.settings.SettingsScreen

@Composable
fun TVStorageNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToAdd = { navController.navigate(NavRoutes.ADD_TV) },
                onNavigateToDetails = { tvId -> navController.navigate(NavRoutes.details(tvId)) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToArchive = { navController.navigate(NavRoutes.ARCHIVE) }
            )
        }

        composable(NavRoutes.ADD_TV) {
            AddTVScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EDIT_TV,
            arguments = listOf(navArgument("tvId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tvId = backStackEntry.arguments?.getLong("tvId") ?: return@composable
            AddTVScreen(
                tvId = tvId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DETAILS,
            arguments = listOf(navArgument("tvId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "tvstorage://details/{tvId}" })
        ) { backStackEntry ->
            val tvId = backStackEntry.arguments?.getLong("tvId") ?: return@composable
            DetailsScreen(
                tvId = tvId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(NavRoutes.editTv(tvId)) }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ARCHIVE) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { tvId -> navController.navigate(NavRoutes.details(tvId)) }
            )
        }
    }
}