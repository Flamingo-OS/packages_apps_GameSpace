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

import android.content.Context
import android.database.ContentObserver
import android.os.Bundle
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

import com.android.internal.R
import com.flamingo.systemui.game.CONFIG_BACK_GESTURE_LOCKED
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockGestureTileState(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onToggleState: (Boolean) -> Unit,
    val isLocked: Boolean
) {

    var shouldShowTile by mutableStateOf(false)
        private set

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            updateShowTileState()
        }
    }

    private var registeredSettingsObserver = false

    init {
        updateShowTileState()
    }

    private fun updateShowTileState() {
        val defaultNavMode = context.resources.getInteger(R.integer.config_navBarInteractionMode)
        coroutineScope.launch {
            shouldShowTile = withContext(Dispatchers.IO) {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.NAVIGATION_MODE,
                    defaultNavMode
                ) == GESTURAL_NAV_MODE
            }
        }
    }

    fun toggleGestureLock() {
        onToggleState(!isLocked)
    }

    internal fun registerSettingsObserver() {
        if (registeredSettingsObserver) return
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.NAVIGATION_MODE),
            false,
            settingsObserver
        )
        registeredSettingsObserver = true
    }

    internal fun unregisterSettingsObserver() {
        if (!registeredSettingsObserver) return
        context.contentResolver.unregisterContentObserver(settingsObserver)
        registeredSettingsObserver = false
    }

    companion object {
        private const val GESTURAL_NAV_MODE = 2
    }
}

@Composable
fun rememberLockGestureTileState(
    config: Bundle,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    serviceCallback: GameSpaceServiceCallback?,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): LockGestureTileState {
    val isLocked = remember(config) { config.getBoolean(CONFIG_BACK_GESTURE_LOCKED) }
    val state = remember(isLocked, context, coroutineScope, serviceCallback) {
        LockGestureTileState(
            isLocked = isLocked,
            context = context,
            coroutineScope = coroutineScope,
            onToggleState = {
                serviceCallback?.setGesturalNavigationLocked(it)
            },
        )
    }
    DisposableEffect(state, lifecycle) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            state.registerSettingsObserver()
        }
        onDispose {
            state.unregisterSettingsObserver()
        }
    }
    return state
}