package com.cutwa.videoeditor.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Projects : Screen("projects")
    object Editor : Screen("editor/{projectId}") {
        fun createRoute(projectId: String) = "editor/$projectId"
        val arguments: List<NamedNavArgument> = listOf(
            navArgument("projectId") {
                type = NavType.StringType
                nullable = true
            }
        )
    }
}