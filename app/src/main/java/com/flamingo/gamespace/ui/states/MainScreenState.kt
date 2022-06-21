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

package com.flamingo.gamespace.ui.states

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.Settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import com.android.systemui.statusbar.phone.DEFAULT_GAMESPACE_DISABLE_FULLSCREEN_INTENT
import com.android.systemui.statusbar.phone.DEFAULT_GAMESPACE_DISABLE_HEADSUP
import com.android.systemui.statusbar.phone.DEFAULT_GAMESPACE_DYNAMIC_MODE
import com.android.systemui.statusbar.phone.DEFAULT_GAMESPACE_ENABLED

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreenState(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            coroutineScope.launch {
                when (val key = uri?.lastPathSegment) {
                    Settings.System.GAMESPACE_ENABLED -> {
                        gameSpaceEnabled = getBoolSetting(key, DEFAULT_GAMESPACE_ENABLED)
                    }
                    Settings.System.GAMESPACE_DYNAMIC_MODE -> {
                        dynamicMode = getBoolSetting(key, DEFAULT_GAMESPACE_DYNAMIC_MODE)
                    }
                    Settings.System.GAMESPACE_DISABLE_HEADSUP -> {
                        disableHeadsUp = getBoolSetting(key, DEFAULT_GAMESPACE_DISABLE_HEADSUP)
                    }
                    Settings.System.GAMESPACE_DISABLE_FULLSCREEN_INTENT -> {
                        disableFullscreenIntent =
                            getBoolSetting(key, DEFAULT_GAMESPACE_DISABLE_FULLSCREEN_INTENT)
                    }
                }
            }
        }
    }

    var gameSpaceEnabled by mutableStateOf(DEFAULT_GAMESPACE_ENABLED)
        private set

    var dynamicMode by mutableStateOf(DEFAULT_GAMESPACE_DYNAMIC_MODE)
        private set

    var disableHeadsUp by mutableStateOf(DEFAULT_GAMESPACE_DISABLE_HEADSUP)
        private set

    var disableFullscreenIntent by mutableStateOf(DEFAULT_GAMESPACE_DISABLE_FULLSCREEN_INTENT)
        private set

    init {
        coroutineScope.launch {
            updateSettings()
        }
        registerSettingsObservers(
            context.contentResolver,
            Settings.System.GAMESPACE_ENABLED,
            Settings.System.GAMESPACE_DYNAMIC_MODE,
            Settings.System.GAMESPACE_DISABLE_HEADSUP,
            Settings.System.GAMESPACE_DISABLE_FULLSCREEN_INTENT
        )
    }

    private suspend fun updateSettings() {
        gameSpaceEnabled =
            getBoolSetting(Settings.System.GAMESPACE_ENABLED, DEFAULT_GAMESPACE_ENABLED)
        dynamicMode =
            getBoolSetting(Settings.System.GAMESPACE_DYNAMIC_MODE, DEFAULT_GAMESPACE_DYNAMIC_MODE)
        disableHeadsUp = getBoolSetting(
            Settings.System.GAMESPACE_DISABLE_HEADSUP,
            DEFAULT_GAMESPACE_DISABLE_HEADSUP
        )
        disableFullscreenIntent = getBoolSetting(
            Settings.System.GAMESPACE_DISABLE_FULLSCREEN_INTENT,
            DEFAULT_GAMESPACE_DISABLE_FULLSCREEN_INTENT
        )
    }

    private fun registerSettingsObservers(contentResolver: ContentResolver, vararg keys: String) {
        keys.forEach {
            contentResolver.registerContentObserver(
                Settings.System.getUriFor(it),
                false,
                settingsObserver
            )
        }
    }

    private suspend fun getBoolSetting(key: String, def: Boolean) =
        withContext(Dispatchers.IO) {
            Settings.System.getInt(context.contentResolver, key, if (def) 1 else 0) == 1
        }

    fun setGameSpaceEnabledSetting(enabled: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_ENABLED,
            if (enabled) 1 else 0
        )
    }

    fun setDynamicModeEnabledSetting(enabled: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_DYNAMIC_MODE,
            if (enabled) 1 else 0
        )
    }

    fun setHeadsUpDisabledSetting(disabled: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_DISABLE_HEADSUP,
            if (disabled) 1 else 0
        )
    }

    fun setFullScreenIntentDisabledSetting(disabled: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_DISABLE_FULLSCREEN_INTENT,
            if (disabled) 1 else 0
        )
    }

    private fun updateSetting(key: String, value: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            Settings.System.putInt(context.contentResolver, key, value)
        }
    }

    internal fun dispose() {
        context.contentResolver.unregisterContentObserver(settingsObserver)
    }
}

@Composable
fun rememberMainScreenState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MainScreenState {
    val state = remember(context, coroutineScope) {
        MainScreenState(context = context, coroutineScope = coroutineScope)
    }
    DisposableEffect(state) {
        onDispose {
            state.dispose()
        }
    }
    return state
}