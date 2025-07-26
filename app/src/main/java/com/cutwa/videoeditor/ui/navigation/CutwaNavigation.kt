package com.cutwa.videoeditor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cutwa.videoeditor.ui.screens.editor.EditorScreen
import com.cutwa.videoeditor.ui.screens.home.HomeScreen
import com.cutwa.videoeditor.ui.screens.projects.ProjectsScreen

@Composable
fun CutwaNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProjects = {
                    navController.navigate(Screen.Projects.route)
                },
                onNavigateToEditor = { projectId ->
                    navController.navigate(Screen.Editor.createRoute(projectId))
                }
            )
        }
        
        composable(Screen.Projects.route) {
            ProjectsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenProject = { projectId ->
                    navController.navigate(Screen.Editor.createRoute(projectId))
                }
            )
        }
        
        composable(
            route = Screen.Editor.route,
            arguments = Screen.Editor.arguments
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            EditorScreen(
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}