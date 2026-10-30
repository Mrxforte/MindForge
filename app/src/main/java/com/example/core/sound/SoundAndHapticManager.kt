package com.example.core.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundAndHapticManager(private val context: Context) {

  private var toneGenerator: ToneGenerator? = try {
    ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
  } catch (e: Exception) {
    null
  }

  private val vibrator: Vibrator? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  } catch (e: Exception) {
    null
  }

  fun playCorrect(soundEnabled: Boolean, hapticsEnabled: Boolean) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
      } catch (_: Exception) {}
    }
    if (hapticsEnabled) {
      vibrate(50)
    }
  }

  fun playIncorrect(soundEnabled: Boolean, hapticsEnabled: Boolean) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
      } catch (_: Exception) {}
    }
    if (hapticsEnabled) {
      vibrate(120)
    }
  }

  fun playLevelComplete(soundEnabled: Boolean, hapticsEnabled: Boolean) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
      } catch (_: Exception) {}
    }
    if (hapticsEnabled) {
      vibrate(150)
    }
  }

  fun playClick(soundEnabled: Boolean, hapticsEnabled: Boolean) {
    if (hapticsEnabled) {
      vibrate(20)
    }
  }

  private fun vibrate(durationMs: Long) {
    try {
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(durationMs)
        }
      }
    } catch (_: Exception) {}
  }
}
