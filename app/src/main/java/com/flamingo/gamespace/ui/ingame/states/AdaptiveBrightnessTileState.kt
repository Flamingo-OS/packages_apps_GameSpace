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

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.Settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdaptiveBrightnessTileState(
    private val contentResolver: ContentResolver,
    private val coroutineScope: CoroutineScope,
    private val onToggleState: (Boolean) -> Unit,
) {

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            updateSettings()
        }
    }

    var isEnabled by mutableStateOf(false)
        private set

    init {
        updateSettings()
    }

    private fun updateSettings() {
        coroutineScope.launch {
            isEnabled = withContext(Dispatchers.IO) {
                Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }
        }
    }

    fun toggleAdaptiveBrightness() {
        onToggleState(!isEnabled)
    }

    internal fun registerSettingsObserver() {
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
            false,
            settingsObserver
        )
    }

    internal fun unregisterSettingsObserver() {
        contentResolver.unregisterContentObserver(settingsObserver)
    }
}

@Composable
fun rememberAdaptiveBrightnessTileState(
    contentResolver: ContentResolver = LocalContext.current.contentResolver,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    serviceCallback: GameSpaceServiceCallback?
): AdaptiveBrightnessTileState {
    val state = remember(contentResolver, coroutineScope) {
        AdaptiveBrightnessTileState(
            contentResolver = contentResolver,
            coroutineScope = coroutineScope,
            onToggleState = {
                serviceCallback?.setAdaptiveBrightnessDisabled(!it)
            }
        )
    }
    DisposableEffect(state) {
        state.registerSettingsObserver()
        onDispose {
            state.unregisterSettingsObserver()
        }
    }
    return state
}