package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.gameengine.CatalogProvider
import com.example.core.model.GameDefinition
import com.example.ui.screens.GameCatalogScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MindForgeTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.GamePlayViewModel
import com.example.ui.viewmodel.MainViewModel

enum class MainTab {
  HOME,
  GAMES,
  STATS,
  SETTINGS
}

data class ActiveGameSession(
  val gameId: String,
  val levelNumber: Int
)

class MainActivity : ComponentActivity() {

  private val mainViewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val userProfile by mainViewModel.userProfile.collectAsState()
      val themeMode = try {
        ThemeMode.valueOf(userProfile.themeMode)
      } catch (_: Exception) {
        ThemeMode.SYSTEM
      }

      MindForgeTheme(themeMode = themeMode) {
        MindForgeApp(mainViewModel = mainViewModel)
      }
    }
  }
}

@Composable
fun MindForgeApp(mainViewModel: MainViewModel) {
  val userProfile by mainViewModel.userProfile.collectAsState()
  var currentTab by remember { mutableStateOf(MainTab.HOME) }
  var selectedGameForLevels by remember { mutableStateOf<GameDefinition?>(null) }
  var activeGameSession by remember { mutableStateOf<ActiveGameSession?>(null) }

  val gameLevels by mainViewModel.gameLevels.collectAsState()

  // 1. Onboarding Screen
  if (!userProfile.onboardingCompleted) {
    OnboardingScreen(
      onFinishOnboarding = { mainViewModel.completeOnboarding() }
    )
    return
  }

  // 2. Active Game Screen
  if (activeGameSession != null) {
    val session = activeGameSession!!
    BackHandler {
      activeGameSession = null
      mainViewModel.loadStats()
    }

    val gamePlayVm: GamePlayViewModel = viewModel(
      key = "${session.gameId}_${session.levelNumber}_${System.currentTimeMillis() / 60000}",
      factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
          return GamePlayViewModel(
            application = mainViewModel.getApplication(),
            gameId = session.gameId,
            levelNumber = session.levelNumber,
            soundEnabled = userProfile.soundEnabled,
            hapticsEnabled = userProfile.hapticsEnabled
          ) as T
        }
      }
    )

    GamePlayScreen(
      viewModel = gamePlayVm,
      onExit = {
        activeGameSession = null
        mainViewModel.loadStats()
        // Refresh levels for current game if viewing levels
        selectedGameForLevels?.let { mainViewModel.selectGameForLevels(it) }
      },
      onNextLevel = { nextLvl ->
        activeGameSession = ActiveGameSession(session.gameId, nextLvl)
      },
      onRetry = {
        activeGameSession = ActiveGameSession(session.gameId, session.levelNumber)
      }
    )
    return
  }

  // 3. Level Selection Screen
  if (selectedGameForLevels != null) {
    val game = selectedGameForLevels!!
    BackHandler {
      selectedGameForLevels = null
      mainViewModel.clearSelectedGame()
    }

    LevelSelectionScreen(
      game = game,
      levels = gameLevels,
      onBackClick = {
        selectedGameForLevels = null
        mainViewModel.clearSelectedGame()
      },
      onSelectLevel = { lvlNum ->
        activeGameSession = ActiveGameSession(game.id, lvlNum)
      }
    )
    return
  }

  // 4. Main Tab Navigation
  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = currentTab == MainTab.HOME,
          onClick = { currentTab = MainTab.HOME },
          icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
          label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.GAMES,
          onClick = { currentTab = MainTab.GAMES },
          icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_games)) },
          label = { Text(stringResource(R.string.nav_games)) }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.STATS,
          onClick = {
            mainViewModel.loadStats()
            currentTab = MainTab.STATS
          },
          icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_stats)) },
          label = { Text(stringResource(R.string.nav_stats)) }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.SETTINGS,
          onClick = { currentTab = MainTab.SETTINGS },
          icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
          label = { Text(stringResource(R.string.nav_settings)) }
        )
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (currentTab) {
        MainTab.HOME -> {
          HomeScreen(
            viewModel = mainViewModel,
            onNavigateToGame = { game ->
              selectedGameForLevels = game
              mainViewModel.selectGameForLevels(game)
            },
            onStartDailyWorkout = {
              // Start level 1 of featured game or first unlocked game
              val targetGame = CatalogProvider.games.first()
              selectedGameForLevels = targetGame
              mainViewModel.selectGameForLevels(targetGame)
            },
            onNavigateToCatalog = { currentTab = MainTab.GAMES }
          )
        }

        MainTab.GAMES -> {
          GameCatalogScreen(
            onSelectGame = { game ->
              selectedGameForLevels = game
              mainViewModel.selectGameForLevels(game)
            }
          )
        }

        MainTab.STATS -> {
          StatisticsScreen(viewModel = mainViewModel)
        }

        MainTab.SETTINGS -> {
          SettingsScreen(viewModel = mainViewModel)
        }
      }
    }
  }
}
