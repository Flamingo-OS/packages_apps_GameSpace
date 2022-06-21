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

package com.flamingo.gamespace.ui.ingame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.R
import com.flamingo.gamespace.ui.ingame.states.GameToolsDialogState
import com.flamingo.gamespace.ui.ingame.states.LockGestureTileState
import com.flamingo.gamespace.ui.ingame.states.ScreenshotTileState
import com.flamingo.gamespace.ui.ingame.states.rememberLockGestureTileState
import com.flamingo.gamespace.ui.ingame.states.rememberScreenshotTileState

private val CornerSize = 16.dp

@Composable
fun GameToolsDialog(
    state: GameToolsDialogState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(CornerSize)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(IntrinsicSize.Min),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(text = state.time, style = MaterialTheme.typography.headlineSmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = state.batteryText, style = MaterialTheme.typography.bodyLarge)
                Text(text = state.date, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScreenshotTile(
                        modifier = Modifier.width(IntrinsicSize.Min),
                        onDismissDialogRequest = onDismissRequest
                    )
                    val lockGestureTileState = rememberLockGestureTileState(
                        config = state.config,
                        onToggleState = {
                            state.setGesturalNavigationLocked(it)
                        },
                    )
                    if (lockGestureTileState.shouldShowTile) {
                        LockGestureTile(
                            modifier = Modifier.width(IntrinsicSize.Min),
                            state = lockGestureTileState
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class)
@Composable
fun Tile(
    icon: @Composable () -> Unit,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(CornerSize),
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextUnit(14f, TextUnitType.Sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ScreenshotTile(
    onDismissDialogRequest: () -> Unit,
    modifier: Modifier = Modifier,
    state: ScreenshotTileState = rememberScreenshotTileState()
) {
    Tile(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_qs_screenshot),
                contentDescription = stringResource(id = R.string.screenshot_tile_content_desc),
                modifier = Modifier.offset(x = (-4).dp)
            )
        },
        title = stringResource(id = R.string.screenshot),
        enabled = false,
        onClick = {
            onDismissDialogRequest()
            state.takeScreenshot(AnimationDuration.toLong())
        }
    )
}

@Composable
fun LockGestureTile(
    state: LockGestureTileState,
    modifier: Modifier = Modifier,
) {
    Tile(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock_gesture),
                contentDescription = stringResource(id = R.string.lock_gestures_content_desc),
                modifier = Modifier.offset(x = (-4).dp)
            )
        },
        title = stringResource(id = R.string.lock_gestures),
        enabled = state.isLocked,
        onClick = {
            state.toggleGestureLock()
        }
    )
}