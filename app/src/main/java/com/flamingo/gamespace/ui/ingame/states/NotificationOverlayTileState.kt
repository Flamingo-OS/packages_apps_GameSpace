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

import com.flamingo.systemui.game.DEFAULT_GAMESPACE_DISABLE_HEADSUP
import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.androidx.compose.get

class NotificationOverlayTileState(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val contentResolver: ContentResolver
) {

    val isEnabled: Flow<Boolean>
        get() = settingsRepository.enableNotificationOverlay

    var shouldShowTile by mutableStateOf(DEFAULT_GAMESPACE_DISABLE_HEADSUP)
        private set

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            updateHeadsUpSetting()
        }
    }

    init {
        updateHeadsUpSetting()
    }

    private fun updateHeadsUpSetting() {
        coroutineScope.launch {
            val headsUpDisabled = isHeadsUpDisabled()
            shouldShowTile = headsUpDisabled
        }
    }

    private suspend fun isHeadsUpDisabled() = withContext(Dispatchers.IO) {
        Settings.System.getIntForUser(
            contentResolver,
            Settings.System.GAMESPACE_DISABLE_HEADSUP,
            if (DEFAULT_GAMESPACE_DISABLE_HEADSUP) 1 else 0,
            UserHandle.USER_CURRENT
        ) == 1
    }

    fun setNotificationOverlayEnabled(enabled: Boolean) {
        coroutineScope.launch {
            settingsRepository.setNotificationOverlayEnabled(enabled)
        }
    }

    internal fun registerObserver() {
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.GAMESPACE_DISABLE_HEADSUP),
            false,
            settingsObserver,
            UserHandle.USER_CURRENT
        )
    }

    internal fun unregisterObserver() {
        contentResolver.unregisterContentObserver(settingsObserver)
    }
}

@Composable
fun rememberNotificationOverlayTileState(
    settingsRepository: SettingsRepository = get(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    contentResolver: ContentResolver = LocalContext.current.contentResolver
): NotificationOverlayTileState {
    val state = remember(settingsRepository, coroutineScope, contentResolver) {
        NotificationOverlayTileState(
            settingsRepository = settingsRepository,
            coroutineScope = coroutineScope,
            contentResolver = contentResolver
        )
    }
    DisposableEffect(state) {
        state.registerObserver()
        onDispose {
            state.unregisterObserver()
        }
    }
    return state
}
