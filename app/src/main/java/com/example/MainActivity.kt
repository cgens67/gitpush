package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.GithubSyncRepository
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GitSyncViewModel
import com.example.viewmodel.GitSyncViewModelFactory
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local SQLite Database
        val database = AppDatabase.getDatabase(this)
        val repository = GithubSyncRepository(database)
        val factory = GitSyncViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[GitSyncViewModel::class.java]

        setContent {
            val isDark = viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDark.value) {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Animated Crossfade between individual high-energy views
                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenNavigation"
                    ) { screen ->
                        when (screen) {
                            is Screen.Setup -> {
                                SetupScreen(viewModel = viewModel)
                            }
                            is Screen.Main -> {
                                MainScreen(viewModel = viewModel)
                            }
                            is Screen.Progress -> {
                                ProgressScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

