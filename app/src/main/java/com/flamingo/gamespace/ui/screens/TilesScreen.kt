package com.flamingo.gamespace.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.Tile
import com.flamingo.gamespace.ui.states.TileScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.flamingo.support.compose.ui.preferences.SwitchPreference

@Composable
fun TilesScreen(
    state: TileScreenState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollapsingToolbarLayout(
        modifier = modifier,
        title = stringResource(id = R.string.tiles),
        onBackButtonPressed = onBackPressed
    ) {
        items(state.tiles) {
            SwitchPreference(title = tileName(it.tile), checked = it.enabled, onCheckedChange = { checked ->
                if (checked) {
                    state.enableTile(it.tile)
                } else {
                    state.disableTile(it.tile)
                }
            })
        }
    }
}

@Composable
fun tileName(tile: Tile): String {
    val resId = when (tile) {
        Tile.SCREENSHOT -> R.string.screenshot
        Tile.NOTIFICATION_OVERLAY -> R.string.notification_overlay
        Tile.ADAPTIVE_BRIGHTNESS -> R.string.adaptive_brightness
        Tile.RINGER_MODE -> R.string.ringer_mode
        Tile.SCREEN_RECORD -> R.string.screen_record
        Tile.LOCK_GESTURE -> R.string.lock_gestures
        Tile.UNRECOGNIZED -> throw IllegalArgumentException("UNRECOGNIZED tile is not a valid tile")
    }
    return stringResource(id = resId)
}