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

import android.graphics.Region
import android.os.Bundle
import android.os.IThermalService
import android.view.ViewTreeObserver.InternalInsetsInfo
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback
import com.flamingo.gamespace.ui.ingame.states.GameToolsHandleState
import com.flamingo.gamespace.ui.ingame.states.rememberGameToolsDialogState

@Composable
fun GameToolsHandle(
    state: GameToolsHandleState,
    position: Offset,
    onHandleDragged: (Offset) -> Unit,
    onDragStop: () -> Unit,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?,
    settingsRepository: SettingsRepository,
    thermalService: IThermalService,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        var handleBounds by remember { mutableStateOf(Rect.Zero) }
        val handleTouchableRegion by remember {
            derivedStateOf {
                if (handleBounds.isInfinite) {
                    Region()
                } else {
                    Region(handleBounds.toAndroidRect())
                }
            }
        }
        var showToolsDialog by remember { mutableStateOf(false) }
        val view = LocalView.current
        val internalInsetsComputer =
            rememberUpdatedState(OnComputeInternalInsetsListener { info: InternalInsetsInfo ->
                info.contentInsets.setEmpty()
                info.visibleInsets.setEmpty()
                info.touchableRegion.set(handleTouchableRegion)
                info.setTouchableInsets(
                    if (showToolsDialog) {
                        // Allow intercepting touches outside dialog to close the dialog
                        InternalInsetsInfo.TOUCHABLE_INSETS_CONTENT
                    } else {
                        // Only allow drag on handle to prevent underlying window
                        // from getting touches
                        InternalInsetsInfo.TOUCHABLE_INSETS_REGION
                    }
                )
            })
        DisposableEffect(view) {
            view.viewTreeObserver.addOnComputeInternalInsetsListener(
                internalInsetsComputer.value
            )
            onDispose {
                view.viewTreeObserver.removeOnComputeInternalInsetsListener(
                    internalInsetsComputer.value
                )
            }
        }
        ToolsDialog(
            showDialog = showToolsDialog,
            handleBounds = handleBounds,
            onDismissRequest = {
                showToolsDialog = false
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
        ) {
            val dialogState = rememberGameToolsDialogState(
                settingsRepository = settingsRepository,
                thermalService = thermalService
            )
            GameToolsDialog(
                modifier = Modifier.defaultMinSize(
                    minWidth = if (maxWidth < maxHeight) {
                        0.75
                    } else {
                        0.35
                    } * maxWidth
                ),
                onDismissRequest = {
                    showToolsDialog = false
                },
                config = config,
                serviceCallback = serviceCallback,
                state = dialogState
            )
        }
        val windowSize = state.windowSize
        val xOffsetRange by remember(windowSize) {
            derivedStateOf {
                0f..(windowSize.width - handleBounds.width)
            }
        }
        val yOffsetRange by remember(windowSize) {
            derivedStateOf {
                0f..(windowSize.height - handleBounds.height)
            }
        }
        val updatedPosition by rememberUpdatedState(newValue = position)
        Handle(
            modifier = Modifier
                .offset {
                    updatedPosition.round()
                }
                .onGloballyPositioned {
                    handleBounds = it.boundsInWindow()
                }
                .padding(8.dp)
                .pointerInput(state.orientation) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            onHandleDragged(
                                Offset(
                                    x = (updatedPosition.x + dragAmount.x).coerceIn(xOffsetRange),
                                    y = (updatedPosition.y + dragAmount.y).coerceIn(yOffsetRange)
                                )
                            )
                            change.consume()
                        },
                        onDragCancel = onDragStop,
                        onDragEnd = onDragStop
                    )
                }
                .pointerInput(state.orientation) {
                    detectTapGestures {
                        showToolsDialog = true
                    }
                }
        )
    }
}

@Composable
fun Handle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            color = Color(0, 0, 0, 120),
            shape = CircleShape
        ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_gamepad_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.White),
            modifier = Modifier.padding(8.dp)
        )
    }
}