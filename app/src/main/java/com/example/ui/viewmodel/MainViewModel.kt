package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.database.AppDatabase
import com.example.core.data.database.entity.GameSessionEntity
import com.example.core.data.database.entity.UserProfileEntity
import com.example.core.data.repository.CognitiveStats
import com.example.core.data.repository.GameRepository
import com.example.core.data.repository.LevelDisplayItem
import com.example.core.model.GameCategory
import com.example.core.model.GameDefinition
import com.example.core.sound.SoundAndHapticManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getInstance(application)
  val repository = GameRepository(database)
  val soundManager = SoundAndHapticManager(application)

  val userProfile: StateFlow<UserProfileEntity> = repository.getUserProfile()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

  private val _stats = MutableStateFlow<CognitiveStats?>(null)
  val stats: StateFlow<CognitiveStats?> = _stats.asStateFlow()

  private val _selectedGameForLevels = MutableStateFlow<GameDefinition?>(null)
  val selectedGameForLevels: StateFlow<GameDefinition?> = _selectedGameForLevels.asStateFlow()

  private val _gameLevels = MutableStateFlow<List<LevelDisplayItem>>(emptyList())
  val gameLevels: StateFlow<List<LevelDisplayItem>> = _gameLevels.asStateFlow()

  val recentSessions: StateFlow<List<GameSessionEntity>> = repository.getRecentSessions(20)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    loadStats()
  }

  fun loadStats() {
    viewModelScope.launch {
      _stats.value = repository.calculateStats()
    }
  }

  fun selectGameForLevels(game: GameDefinition) {
    _selectedGameForLevels.value = game
    viewModelScope.launch {
      repository.getLevelsForGame(game.id).collect { levels ->
        _gameLevels.value = levels
      }
    }
  }

  fun clearSelectedGame() {
    _selectedGameForLevels.value = null
    _gameLevels.value = emptyList()
  }

  fun updateTheme(themeMode: ThemeMode) {
    viewModelScope.launch {
      val current = userProfile.value
      repository.updateUserProfile(current.copy(themeMode = themeMode.name))
    }
  }

  fun updateLanguage(lang: String) {
    viewModelScope.launch {
      val current = userProfile.value
      repository.updateUserProfile(current.copy(language = lang))
    }
  }

  fun toggleSound(enabled: Boolean) {
    viewModelScope.launch {
      val current = userProfile.value
      repository.updateUserProfile(current.copy(soundEnabled = enabled))
    }
  }

  fun toggleHaptics(enabled: Boolean) {
    viewModelScope.launch {
      val current = userProfile.value
      repository.updateUserProfile(current.copy(hapticsEnabled = enabled))
    }
  }

  fun completeOnboarding() {
    viewModelScope.launch {
      val current = userProfile.value
      repository.updateUserProfile(current.copy(onboardingCompleted = true))
    }
  }

  fun resetAllProgress() {
    viewModelScope.launch {
      repository.resetAllData()
      loadStats()
    }
  }
}
