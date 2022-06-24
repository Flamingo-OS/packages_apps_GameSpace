package com.flamingo.gamespace.ui.ingame.states

import android.content.res.Resources
import android.media.AudioManager
import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

import com.android.systemui.statusbar.phone.CONFIG_RINGER_MODE

class RingerModeTileState(
    resources: Resources,
    config: Bundle,
    private val onRingerModeChangeRequest: (Int) -> Unit,
) {
    val shouldShowTile = !resources.getBoolean(com.android.internal.R.bool.config_hasAlertSlider)

    val ringerMode = config.getInt(CONFIG_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)

    fun cycleToNextMode() {
        val nextMode = when (ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        onRingerModeChangeRequest(nextMode)
    }
}

@Composable
fun rememberRingerModeTileState(
    resources: Resources = LocalContext.current.resources,
    config: Bundle,
    onRingerModeChangeRequest: (Int) -> Unit
): RingerModeTileState {
    val ringerModeChangeCallback by rememberUpdatedState(newValue = onRingerModeChangeRequest)
    return remember(resources, config) {
        RingerModeTileState(
            resources = resources,
            config = config,
            onRingerModeChangeRequest = ringerModeChangeCallback
        )
    }
}