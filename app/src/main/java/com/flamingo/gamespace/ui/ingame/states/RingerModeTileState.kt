/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flamingo.gamespace.ui.ingame.states

import android.media.AudioManager
import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import com.flamingo.systemui.game.CONFIG_RINGER_MODE
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback

class RingerModeTileState(
    private val onRingerModeChangeRequest: (Int) -> Unit,
    val ringerMode: Int
) {
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
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?
): RingerModeTileState {
    val ringerMode = remember(config) { config.getInt(CONFIG_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL) }
    return remember(ringerMode) {
        RingerModeTileState(
            ringerMode = ringerMode,
            onRingerModeChangeRequest = {
                serviceCallback?.setRingerMode(it)
            }
        )
    }
}