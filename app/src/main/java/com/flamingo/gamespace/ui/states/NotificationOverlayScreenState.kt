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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

import org.koin.androidx.compose.get

class NotificationOverlayScreenState(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) {

    val notificationOverlayEnabled: Flow<Boolean>
        get() = settingsRepository.enableNotificationOverlay

    val notificationOverlaySizePortrait: Flow<Int>
        get() = settingsRepository.notificationOverlaySizePortrait

    val notificationOverlaySizeLandscape: Flow<Int>
        get() = settingsRepository.notificationOverlaySizeLandscape

    val notificationOverlayDuration: Flow<Long>
        get() = settingsRepository.notificationOverlayDuration

    fun setNotificationOverlayEnabled(enabled: Boolean) {
        coroutineScope.launch {
            settingsRepository.setNotificationOverlayEnabled(enabled)
        }
    }

    fun setPortraitNotificationOverlaySize(size: Int) {
        coroutineScope.launch {
            settingsRepository.setPortraitNotificationOverlaySize(size)
        }
    }

    fun setLandscapeNotificationOverlaySize(size: Int) {
        coroutineScope.launch {
            settingsRepository.setLandscapeNotificationOverlaySize(size)
        }
    }

    fun setNotificationOverlayDuration(duration: Int) {
        coroutineScope.launch {
            settingsRepository.setNotificationOverlayDuration(duration)
        }
    }
}

@Composable
fun rememberNotificationOverlayScreenState(
    settingsRepository: SettingsRepository = get(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(settingsRepository, coroutineScope) {
    NotificationOverlayScreenState(
        settingsRepository = settingsRepository,
        coroutineScope = coroutineScope
    )
}