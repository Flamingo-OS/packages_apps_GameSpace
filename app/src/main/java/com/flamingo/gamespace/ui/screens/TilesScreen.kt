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

package com.flamingo.gamespace.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.Tile
import com.flamingo.gamespace.ui.states.TileScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.flamingo.support.compose.ui.preferences.SwitchPreference

@OptIn(ExperimentalFoundationApi::class)
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
        items(state.tiles, key = { it.tile }) {
            SwitchPreference(
                modifier = Modifier.animateItemPlacement(),
                title = tileName(it.tile),
                checked = it.enabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        state.enableTile(it.tile)
                    } else {
                        state.disableTile(it.tile)
                    }
                }
            )
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