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

import android.media.AudioManager
import android.os.Bundle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_ENABLED
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback
import com.flamingo.gamespace.ui.ingame.states.AdaptiveBrightnessTileState
import com.flamingo.gamespace.ui.ingame.states.GameToolsDialogState
import com.flamingo.gamespace.ui.ingame.states.LockGestureTileState
import com.flamingo.gamespace.ui.ingame.states.NotificationOverlayTileState
import com.flamingo.gamespace.ui.ingame.states.RingerModeTileState
import com.flamingo.gamespace.ui.ingame.states.ScreenRecordTileState
import com.flamingo.gamespace.ui.ingame.states.ScreenshotTileState
import com.flamingo.gamespace.ui.ingame.states.rememberAdaptiveBrightnessTileState
import com.flamingo.gamespace.ui.ingame.states.rememberLockGestureTileState
import com.flamingo.gamespace.ui.ingame.states.rememberNotificationOverlayTileState
import com.flamingo.gamespace.ui.ingame.states.rememberRingerModeTileState
import com.flamingo.gamespace.ui.ingame.states.rememberScreenRecordTileState
import com.flamingo.gamespace.ui.ingame.states.rememberScreenshotTileState

private val CornerSize = 16.dp

@Composable
fun GameToolsDialog(
    state: GameToolsDialogState,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(CornerSize)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(IntrinsicSize.Min),
            verticalArrangement = Arrangement.Top,
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
            HorizontalGrid(
                columns = 3,
                columnPadding = 8.dp,
                rowPadding = 8.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                ScreenshotTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    onDismissDialogRequest = onDismissRequest
                )
                LockGestureTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    state = rememberLockGestureTileState(
                        config = config,
                        serviceCallback = serviceCallback,
                    )
                )
                NotificationOverlayTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    state = rememberNotificationOverlayTileState(settingsRepository = state.settingsRepository)
                )
                RingerModeTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    state = rememberRingerModeTileState(
                        config = config,
                        serviceCallback = serviceCallback,
                    )
                )
                AdaptiveBrightnessTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    state = rememberAdaptiveBrightnessTileState(
                        serviceCallback = serviceCallback,
                    )
                )
                ScreenRecordTile(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    state = rememberScreenRecordTileState(
                        config = config,
                        serviceCallback = serviceCallback
                    )
                )
            }
            state.memoryInfo?.let {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_memory_24),
                        contentDescription = stringResource(id = R.string.memory_info_content_desc),
                        tint = if (state.isLowMemory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = it)
                }
            }
        }
    }
}

@Composable
fun HorizontalGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    columnPadding: Dp = 0.dp,
    rowPadding: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val columnPaddingPx = with(density) { columnPadding.roundToPx() }
    val rowPaddingPx = with(density) { rowPadding.roundToPx() }
    val measurePolicy = remember(columns) {
        object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                val availableMaxWidth = constraints.maxWidth - ((columns - 1) * columnPaddingPx)
                val cellMaxWidth = availableMaxWidth / columns
                val cellMinWidth = availableMaxWidth / columns
                val cellConstraints = Constraints(
                    minWidth = cellMinWidth,
                    minHeight = cellMinWidth,
                    maxWidth = cellMaxWidth,
                    maxHeight = cellMaxWidth
                )
                val placeables = measurables.map { it.measure(cellConstraints) }
                val rows = placeables.chunked(columns)
                val layoutMaxHeight = rows.map { row -> row.maxOf { it.height } }.sumOf { it }
                return layout(constraints.maxWidth, layoutMaxHeight) {
                    var xPos = 0
                    var yPos = 0
                    placeables.forEachIndexed { index, placeable ->
                        placeable.placeRelative(xPos, yPos)
                        if (((index + 1) % columns) == 0) {
                            xPos = 0
                            yPos += placeable.height + rowPaddingPx
                        } else {
                            xPos += placeable.width + columnPaddingPx
                        }
                    }
                }
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int {
                return measurables.chunked(columns)
                    .map { row -> row.sumOf { cell -> cell.minIntrinsicWidth(height) } }
                    .maxOf { it }
            }

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                return measurables.chunked(columns)
                    .map { row -> row.maxOf { cell -> cell.minIntrinsicHeight(width) } }
                    .minOf { it }
            }

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                return measurables.chunked(columns)
                    .map { row -> row.maxOf { cell -> cell.maxIntrinsicHeight(width) } }
                    .maxOf { it }
            }
        }
    }
    Layout(
        content = content,
        measurePolicy = measurePolicy,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalAnimationApi::class)
@Composable
fun Tile(
    icon: @Composable () -> Unit,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(targetState = enabled) { highlightTile ->
        Surface(
            modifier = modifier.aspectRatio(1f),
            shape = RoundedCornerShape(CornerSize),
            color = if (highlightTile) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                icon()
                Spacer(modifier = Modifier.height(16.dp))
                var shouldScrollText by remember { mutableStateOf(false) }
                val scrollState = rememberScrollState()
                LaunchedEffect(shouldScrollText) {
                    while (shouldScrollText) {
                        scrollState.animateScrollTo(
                            scrollState.maxValue,
                            animationSpec = tween(1500)
                        )
                        scrollState.scrollTo(0)
                    }
                }
                Text(
                    modifier = Modifier.then(
                        if (shouldScrollText)
                            Modifier.horizontalScroll(scrollState, enabled = false)
                        else
                            Modifier
                    ),
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextUnit(14f, TextUnitType.Sp),
                    maxLines = 1,
                    onTextLayout = {
                        if (it.hasVisualOverflow) {
                            shouldScrollText = true
                        }
                    }
                )
            }
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
    if (state.shouldShowTile) {
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
}

@Composable
fun NotificationOverlayTile(
    state: NotificationOverlayTileState,
    modifier: Modifier = Modifier,
) {
    if (state.shouldShowTile) {
        val isEnabled by state.isEnabled.collectAsState(initial = DEFAULT_NOTIFICATION_OVERLAY_ENABLED)
        Tile(
            modifier = modifier,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification_overlay),
                    contentDescription = stringResource(id = R.string.notification_overlay_content_desc)
                )
            },
            title = stringResource(id = R.string.notification_overlay),
            enabled = isEnabled,
            onClick = {
                state.setNotificationOverlayEnabled(!isEnabled)
            }
        )
    }
}

@Composable
fun RingerModeTile(
    state: RingerModeTileState,
    modifier: Modifier = Modifier,
) {
    if (state.shouldShowTile) {
        Tile(
            modifier = modifier,
            icon = {
                when (state.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_ringer),
                            contentDescription = stringResource(id = R.string.ringer_mode_tile_normal_content_desc),
                        )
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_ringer_vibrate),
                            contentDescription = stringResource(id = R.string.ringer_mode_tile_vibrate_content_desc),
                        )
                    }
                    AudioManager.RINGER_MODE_SILENT -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_ringer_mute),
                            contentDescription = stringResource(id = R.string.ringer_mode_tile_silent_content_desc),
                        )
                    }
                    else -> throw IllegalStateException("Invalid ringer mode ${state.ringerMode}")
                }
            },
            title = stringResource(
                id = when (state.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> R.string.ring
                    AudioManager.RINGER_MODE_VIBRATE -> R.string.vibrate
                    AudioManager.RINGER_MODE_SILENT -> R.string.silent
                    else -> throw IllegalStateException("Invalid ringer mode ${state.ringerMode}")
                }
            ),
            enabled = false,
            onClick = {
                state.cycleToNextMode()
            }
        )
    }
}

@Composable
fun AdaptiveBrightnessTile(
    state: AdaptiveBrightnessTileState,
    modifier: Modifier = Modifier,
) {
    Tile(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_brightness_auto_24),
                contentDescription = stringResource(id = R.string.adaptive_brightness_content_desc)
            )
        },
        title = stringResource(id = R.string.adaptive_brightness),
        enabled = state.isEnabled,
        onClick = {
            state.toggleAdaptiveBrightness()
        }
    )
}

@Composable
fun ScreenRecordTile(
    state: ScreenRecordTileState,
    modifier: Modifier = Modifier,
) {
    Tile(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_screenrecord),
                contentDescription = stringResource(id = R.string.screen_record_content_desc)
            )
        },
        title = stringResource(id = R.string.screen_record),
        enabled = state.isRecording,
        onClick = {
            state.toggleRecordingState()
        },
    )
}