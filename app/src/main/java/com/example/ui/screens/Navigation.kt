package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "progress",
                    onClick = { navController.navigate("progress") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Progress") },
                    label = { Text("Progress") }
                )
                NavigationBarItem(
                    selected = currentRoute == "track",
                    onClick = { navController.navigate("track") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Track") },
                    label = { Text("Track") }
                )
                NavigationBarItem(
                    selected = currentRoute == "insights",
                    onClick = { navController.navigate("insights") },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("progress") { ProgressScreen(viewModel) }
            composable("track") { TrackScreen(viewModel) }
            composable("insights") { InsightsScreen(viewModel) }
        }
    }
}
