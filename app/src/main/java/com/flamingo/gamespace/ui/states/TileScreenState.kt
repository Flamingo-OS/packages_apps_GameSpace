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

class TileScreenState(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) {

    private val isRingerModeTileAvailable =
        !context.resources.getBoolean(com.android.internal.R.bool.config_hasAlertSlider)

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            coroutineScope.launch {
                tilesMutex.withLock {
                    when (uri?.lastPathSegment) {
                        Settings.Secure.NAVIGATION_MODE -> updateLockGestureTile()
                        Settings.System.GAMESPACE_DISABLE_HEADSUP -> updateNotificationOverlayTile()
                    }
                }
            }
        }
    }

    private val tilesMutex = Mutex()

    @GuardedBy("tilesMutex")
    val tiles = mutableStateListOf<TileInfo>()

    init {
        coroutineScope.launch {
            tilesMutex.withLock {
                val enabledTiles = settingsRepository.tiles.first()
                updateEnabledTilesList(enabledTiles)
            }
            settingsRepository.tiles.collect {
                tilesMutex.withLock {
                    tiles.clear()
                    updateEnabledTilesList(it)
                }
            }
        }
    }

    private suspend fun updateEnabledTilesList(enabledTiles: List<Tile>) {
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

    private suspend fun updateLockGestureTile() {
        val enabledTiles = settingsRepository.tiles.first()
        if (isGestureNavigationEnabled()) {
            val isTileEnabled = enabledTiles.contains(Tile.LOCK_GESTURE)
            val tileInfo = tiles.find { it.tile == Tile.LOCK_GESTURE }
            if (tileInfo == null) {
                tiles.add(TileInfo(tile = Tile.LOCK_GESTURE, enabled = isTileEnabled))
            }
        } else {
            tiles.removeIf { it.tile == Tile.LOCK_GESTURE }
        }
    }

    private suspend fun updateNotificationOverlayTile() {
        val enabledTiles = settingsRepository.tiles.first()
        if (isHeadsUpDisabled()) {
            val isTileEnabled = enabledTiles.contains(Tile.NOTIFICATION_OVERLAY)
            val tileInfo = tiles.find { it.tile == Tile.NOTIFICATION_OVERLAY }
            if (tileInfo == null) {
                tiles.add(TileInfo(tile = Tile.NOTIFICATION_OVERLAY, enabled = isTileEnabled))
            }
        } else {
            tiles.removeIf { it.tile == Tile.NOTIFICATION_OVERLAY }
        }
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
    }

    internal fun unregisterSettingsObservers() {
        context.contentResolver.unregisterContentObserver(settingsObserver)
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
    settingsRepository: SettingsRepository,
): TileScreenState {
    val state = remember(context, settingsRepository, coroutineScope) {
        TileScreenState(
            context = context,
            coroutineScope = coroutineScope,
            settingsRepository = settingsRepository
        )
    }
    DisposableEffect(state) {
        state.registerSettingsObservers()
        onDispose {
            state.unregisterSettingsObservers()
        }
    }
    return state
}