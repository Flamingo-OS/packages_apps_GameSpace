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

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings

import androidx.annotation.GuardedBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

import com.android.systemui.game.DEFAULT_GAMESPACE_DISABLE_HEADSUP
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.data.settings.Tile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import org.koin.androidx.compose.get

class TileScreenState(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val lifecycle: Lifecycle
) {

    private val isRingerModeTileAvailable =
        !context.resources.getBoolean(com.android.internal.R.bool.config_hasAlertSlider)

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            coroutineScope.launch {
                tilesMutex.withLock {
                    when (uri?.lastPathSegment) {
                        Settings.Secure.NAVIGATION_MODE,
                        Settings.System.GAMESPACE_DISABLE_HEADSUP -> {
                            val enabledTiles = settingsRepository.tiles.first()
                            updateEnabledTilesListLocked(enabledTiles)
                        }
                    }
                }
            }
        }
    }

    private val tilesMutex = Mutex()
    @GuardedBy("tilesMutex")
    val tiles = mutableStateListOf<TileInfo>()

    private var registeredSettingsObserver = false

    init {
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsRepository.tiles.collect {
                    tilesMutex.withLock {
                        updateEnabledTilesListLocked(it)
                    }
                }
            }
        }
    }

    private suspend fun updateEnabledTilesListLocked(enabledTiles: List<Tile>) {
        tiles.clear()
        Tile.values().forEach {
            when (it) {
                Tile.UNRECOGNIZED -> {}
                Tile.LOCK_GESTURE -> {
                    if (isGestureNavigationEnabled()) {
                        tiles.add(TileInfo(tile = it, enabled = enabledTiles.contains(it)))
                    }
                }
                Tile.RINGER_MODE -> {
                    if (isRingerModeTileAvailable) {
                        tiles.add(TileInfo(tile = it, enabled = enabledTiles.contains(it)))
                    }
                }
                Tile.NOTIFICATION_OVERLAY -> {
                    if (isHeadsUpDisabled()) {
                        tiles.add(TileInfo(tile = it, enabled = enabledTiles.contains(it)))
                    }
                }
                else -> tiles.add(TileInfo(tile = it, enabled = enabledTiles.contains(it)))
            }
        }
    }

    private suspend fun isGestureNavigationEnabled() =
        withContext(Dispatchers.IO) {
            Settings.Secure.getIntForUser(
                context.contentResolver,
                Settings.Secure.NAVIGATION_MODE,
                0,
                UserHandle.USER_CURRENT
            ) == 2
        }

    private suspend fun isHeadsUpDisabled() =
        withContext(Dispatchers.IO) {
            Settings.System.getIntForUser(
                context.contentResolver,
                Settings.System.GAMESPACE_DISABLE_HEADSUP,
                if (DEFAULT_GAMESPACE_DISABLE_HEADSUP) 1 else 0,
                UserHandle.USER_CURRENT
            ) == 1
        }

    fun enableTile(tile: Tile) {
        coroutineScope.launch {
            settingsRepository.addTile(tile)
        }
    }

    fun disableTile(tile: Tile) {
        coroutineScope.launch {
            settingsRepository.removeTile(tile)
        }
    }

    internal fun registerSettingsObservers() {
        if (registeredSettingsObserver) return
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.NAVIGATION_MODE),
            false,
            settingsObserver,
            UserHandle.USER_CURRENT
        )
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.GAMESPACE_DISABLE_HEADSUP),
            false,
            settingsObserver,
            UserHandle.USER_CURRENT
        )
        registeredSettingsObserver = true
    }

    internal fun unregisterSettingsObservers() {
        if (!registeredSettingsObserver) return
        context.contentResolver.unregisterContentObserver(settingsObserver)
        registeredSettingsObserver = false
    }
}

data class TileInfo(
    val tile: Tile,
    val enabled: Boolean
)

@Composable
fun rememberTileScreenState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    settingsRepository: SettingsRepository = get(),
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): TileScreenState {
    val state = remember(context, settingsRepository, coroutineScope, lifecycle) {
        TileScreenState(
            context = context,
            coroutineScope = coroutineScope,
            settingsRepository = settingsRepository,
            lifecycle = lifecycle
        )
    }
    DisposableEffect(state, lifecycle) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            state.registerSettingsObservers()
        }
        onDispose {
            state.unregisterSettingsObservers()
        }
    }
    return state
}