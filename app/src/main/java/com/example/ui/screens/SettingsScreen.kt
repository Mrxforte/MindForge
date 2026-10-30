package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val userProfile by viewModel.userProfile.collectAsState()
  var showResetDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = stringResource(R.string.nav_settings),
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Preferences, audio, theme and privacy",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Theme Mode Section
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.settings_theme), fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(12.dp))

          ThemeMode.entries.forEach { mode ->
            val isSelected = userProfile.themeMode == mode.name
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.updateTheme(mode) }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { viewModel.updateTheme(mode) }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = when (mode) {
                  ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                  ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                  ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                },
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    // Audio & Haptics Section
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Feedback & Audio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = stringResource(R.string.settings_sound), fontSize = 15.sp)
            }
            Switch(
              checked = userProfile.soundEnabled,
              onCheckedChange = { viewModel.toggleSound(it) }
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = stringResource(R.string.settings_haptics), fontSize = 15.sp)
            }
            Switch(
              checked = userProfile.hapticsEnabled,
              onCheckedChange = { viewModel.toggleHaptics(it) }
            )
          }
        }
      }
    }

    // Language Section
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.settings_language), fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          val languages = listOf(
            Pair("en", "English"),
            Pair("ru", "Русский (Russian)"),
            Pair("uz", "Oʻzbekcha (Uzbek)")
          )

          languages.forEach { (code, name) ->
            val isSelected = userProfile.language == code
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.updateLanguage(code) }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { viewModel.updateLanguage(code) }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    // Privacy & Data Management
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = stringResource(R.string.settings_data_management), fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "MindForge is 100% offline-first. No tracking, no personal information collected, and no advertisement SDKs.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.settings_reset_progress), color = ErrorRed, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // About Section
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.settings_about), fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "MindForge — Brain Training",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = stringResource(R.string.settings_version),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text(stringResource(R.string.settings_reset_progress)) },
      text = { Text(stringResource(R.string.settings_reset_confirm)) },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetAllProgress()
            showResetDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text(stringResource(R.string.action_reset), color = androidx.compose.ui.graphics.Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showResetDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
