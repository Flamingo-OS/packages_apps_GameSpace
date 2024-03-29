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
import android.database.ContentObserver
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle

import com.flamingo.systemui.game.DEFAULT_GAMESPACE_DISABLE_CALL_RINGING
import com.flamingo.systemui.game.DEFAULT_GAMESPACE_DISABLE_FULLSCREEN_INTENT
import com.flamingo.systemui.game.DEFAULT_GAMESPACE_DISABLE_HEADSUP
import com.flamingo.systemui.game.DEFAULT_GAMESPACE_DYNAMIC_MODE
import com.flamingo.systemui.game.DEFAULT_GAMESPACE_ENABLED
import com.flamingo.systemui.game.DEFAULT_GAMESPACE_HIDE_PRIVACY_INDICATORS
import com.flamingo.gamespace.data.settings.RingerMode
import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.androidx.compose.get

class MainScreenState(
    private val contentResolver: ContentResolver,
    private val coroutineScope: CoroutineScope,
    private val settingsRepository: SettingsRepository
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
                    Settings.System.GAMESPACE_DISABLE_CALL_RINGING -> {
                        disableCallRinging =
                            getBoolSetting(key, DEFAULT_GAMESPACE_DISABLE_CALL_RINGING)
                    }
                    Settings.System.GAMESPACE_HIDE_PRIVACY_INDICATORS -> {
                        hidePrivacyIndicator =
                            getBoolSetting(key, DEFAULT_GAMESPACE_HIDE_PRIVACY_INDICATORS)
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

    var disableCallRinging by mutableStateOf(DEFAULT_GAMESPACE_DISABLE_CALL_RINGING)
        private set

    var hidePrivacyIndicator by mutableStateOf(DEFAULT_GAMESPACE_HIDE_PRIVACY_INDICATORS)
        private set

    val ringerMode: Flow<RingerMode>
        get() = settingsRepository.ringerMode

    val disableAdaptiveBrightness: Flow<Boolean>
        get() = settingsRepository.disableAdaptiveBrightness

    val showGameToolsHandle: Flow<Boolean>
        get() = settingsRepository.showGameToolsHandle

    private var registeredSettingsObserver = false

    init {
        coroutineScope.launch {
            updateSettings()
        }
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
        disableCallRinging = getBoolSetting(
            Settings.System.GAMESPACE_DISABLE_CALL_RINGING,
            DEFAULT_GAMESPACE_DISABLE_CALL_RINGING
        )
        hidePrivacyIndicator = getBoolSetting(
            Settings.System.GAMESPACE_HIDE_PRIVACY_INDICATORS,
            DEFAULT_GAMESPACE_HIDE_PRIVACY_INDICATORS
        )
    }

    private fun registerSettingsObservers(vararg keys: String) {
        keys.forEach {
            contentResolver.registerContentObserver(
                Settings.System.getUriFor(it),
                false,
                settingsObserver,
                UserHandle.USER_CURRENT
            )
        }
    }

    private suspend fun getBoolSetting(key: String, def: Boolean) =
        withContext(Dispatchers.IO) {
            Settings.System.getIntForUser(
                contentResolver,
                key,
                if (def) 1 else 0,
                UserHandle.USER_CURRENT
            ) == 1
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

    fun setCallRingingDisabled(disabled: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_DISABLE_CALL_RINGING,
            if (disabled) 1 else 0
        )
    }

    fun setPrivacyIndicatorHidden(hide: Boolean) {
        updateSetting(
            Settings.System.GAMESPACE_HIDE_PRIVACY_INDICATORS,
            if (hide) 1 else 0
        )
    }

    private fun updateSetting(key: String, value: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            Settings.System.putIntForUser(contentResolver, key, value, UserHandle.USER_CURRENT)
        }
    }

    fun setRingerMode(mode: RingerMode) {
        coroutineScope.launch {
            settingsRepository.setRingerMode(mode)
        }
    }

    fun setAdaptiveBrightnessDisabled(disabled: Boolean) {
        coroutineScope.launch {
            settingsRepository.setAdaptiveBrightnessDisabled(disabled)
        }
    }

    fun setShowGameToolsHandle(show: Boolean) {
        coroutineScope.launch {
            settingsRepository.setShowGameToolsHandle(show)
        }
    }

    internal fun registerSettingsObservers() {
        if (registeredSettingsObserver) return
        registerSettingsObservers(
            Settings.System.GAMESPACE_ENABLED,
            Settings.System.GAMESPACE_DYNAMIC_MODE,
            Settings.System.GAMESPACE_DISABLE_HEADSUP,
            Settings.System.GAMESPACE_DISABLE_FULLSCREEN_INTENT,
            Settings.System.GAMESPACE_DISABLE_CALL_RINGING,
            Settings.System.GAMESPACE_HIDE_PRIVACY_INDICATORS
        )
        registeredSettingsObserver = true
    }

    internal fun unregisterSettingsObservers() {
        if (!registeredSettingsObserver) return
        contentResolver.unregisterContentObserver(settingsObserver)
        registeredSettingsObserver = false
    }
}

@Composable
fun rememberMainScreenState(
    contentResolver: ContentResolver = LocalContext.current.contentResolver,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    settingsRepository: SettingsRepository = get(),
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): MainScreenState {
    val state = remember(contentResolver, coroutineScope, settingsRepository) {
        MainScreenState(
            contentResolver = contentResolver,
            coroutineScope = coroutineScope,
            settingsRepository = settingsRepository
        )
    }
    DisposableEffect(state) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            state.registerSettingsObservers()
        }
        onDispose {
            state.unregisterSettingsObservers()
        }
    }
    return state
}