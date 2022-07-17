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

import android.content.res.Configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration

import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.services.NotificationListener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import org.koin.androidx.compose.get

class NotificationOverlayState(
    private val notificationListener: NotificationListener,
    private val settingsRepository: SettingsRepository,
    val isPortrait: Boolean,
    coroutineScope: CoroutineScope
) {

    private var currentNotification by mutableStateOf<Pair<Int, String>?>(null)

    val showNotificationOverlay by derivedStateOf { currentNotification != null }

    val notification by derivedStateOf {
        currentNotification?.second
    }

    val visibleDuration: Flow<Long>
        get() = settingsRepository.notificationOverlayDuration

    val size: Flow<Float>
        get() = (if (isPortrait)
            settingsRepository.notificationOverlaySizePortrait
        else
            settingsRepository.notificationOverlaySizeLandscape).map { it.toFloat() }

    val defaultSize: Float
        get() = (if (isPortrait)
            DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
        else
            DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE).toFloat()

    init {
        coroutineScope.launch {
            settingsRepository.enableNotificationOverlay.collect {
                if (!it) {
                    currentNotification = null
                }
            }
        }
    }

    internal fun registerCallbacks() {
        notificationListener.registerCallbacks(
            onNotificationPosted = { id, notification ->
                currentNotification = Pair(id, notification)
            },
            onNotificationRemoved = {
                if (currentNotification?.first == it) {
                    currentNotification = null
                }
            },
        )
    }

    internal fun unregisterCallbacks() {
        notificationListener.unregisterCallbacks()
    }

    fun removeNotification() {
        currentNotification = null
    }
}

@Composable
fun rememberNotificationOverlayState(
    notificationListener: NotificationListener,
    settingsRepository: SettingsRepository = get(),
    configuration: Configuration = LocalConfiguration.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): NotificationOverlayState {
    val state = remember(
        notificationListener,
        settingsRepository,
        configuration.orientation,
        coroutineScope
    ) {
        NotificationOverlayState(
            notificationListener = notificationListener,
            settingsRepository = settingsRepository,
            coroutineScope = coroutineScope,
            isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        )
    }
    DisposableEffect(state) {
        state.registerCallbacks()
        onDispose {
            state.unregisterCallbacks()
        }
    }
    return state
}