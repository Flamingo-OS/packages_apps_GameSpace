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
import android.view.ViewTreeObserver

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex

import com.flamingo.gamespace.R
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback
import com.flamingo.gamespace.ui.ingame.states.GameToolsHandleState
import com.flamingo.gamespace.ui.ingame.states.rememberGameToolsDialogState

private const val DialogLabel = "ToolsDialog"
const val AnimationDuration = 300

@Composable
fun GameToolsHandle(
    state: GameToolsHandleState,
    position: Offset,
    onHandleDragged: (Offset) -> Unit,
    onDragStop: () -> Unit,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        var handleBounds by remember { mutableStateOf(Rect.Zero) }
        val handleTouchableRegion by remember {
            derivedStateOf {
                Region(handleBounds.toAndroidRect())
            }
        }
        var showToolsDialog by remember { mutableStateOf(false) }
        val view = LocalView.current
        val internalInsetsComputer =
            rememberUpdatedState(ViewTreeObserver.OnComputeInternalInsetsListener { info: ViewTreeObserver.InternalInsetsInfo ->
                info.contentInsets.setEmpty()
                info.visibleInsets.setEmpty()
                info.touchableRegion.set(handleTouchableRegion)
                info.setTouchableInsets(
                    if (showToolsDialog) {
                        // Allow intercepting touches outside dialog to close the dialog
                        ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_CONTENT
                    } else
                    // Only allow drag on handle to prevent underlying window
                    // from getting touches
                        ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION
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
            config = config,
            serviceCallback = serviceCallback
        )
        val windowBounds by rememberUpdatedState(newValue = state.windowBounds)
        val updatedPosition by rememberUpdatedState(newValue = position)
        Image(
            painter = painterResource(id = R.drawable.baseline_gamepad_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.White),
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
                                    x = (updatedPosition.x + dragAmount.x).coerceIn(
                                        windowBounds.left,
                                        windowBounds.width - handleBounds.width
                                    ),
                                    y = (updatedPosition.y + dragAmount.y).coerceIn(
                                        windowBounds.top,
                                        windowBounds.height - handleBounds.height,
                                    )
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

enum class DialogPosition {
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    TOP_RIGHT,
    TOP_LEFT
}

@Composable
fun ToolsDialog(
    showDialog: Boolean,
    handleBounds: Rect,
    onDismissRequest: () -> Unit,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?,
    modifier: Modifier = Modifier
) {
    var dialogPosition by remember { mutableStateOf(DialogPosition.TOP_RIGHT) }
    val calculatePositionCallback by rememberUpdatedState(newValue = { bounds: Rect, size: IntSize ->
        val anchorX = bounds.center.x
        val anchorY = bounds.center.y
        val xOffset = if (anchorX > size.width) {
            anchorX - size.width
        } else {
            anchorX
        }
        val yOffset = if (anchorY > size.height) {
            anchorY - size.height
        } else {
            anchorY
        }
        dialogPosition = if (anchorX > size.width) {
            if (anchorY > size.height) {
                DialogPosition.TOP_LEFT
            } else {
                DialogPosition.BOTTOM_LEFT
            }
        } else {
            if (anchorY > size.height) {
                DialogPosition.TOP_RIGHT
            } else {
                DialogPosition.BOTTOM_RIGHT
            }
        }
        Offset(xOffset, yOffset).round()
    })
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = showDialog
    if (expandedState.currentState || expandedState.targetState) {
        Box(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        onDismissRequest()
                    })
                }
        ) {
            Box(modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(
                        calculatePositionCallback(
                            handleBounds,
                            IntSize(placeable.width, placeable.height)
                        )
                    )
                }
            }) {
                val transformOrigin by remember {
                    derivedStateOf {
                        when (dialogPosition) {
                            DialogPosition.TOP_LEFT -> TransformOrigin(1f, 1f)
                            DialogPosition.TOP_RIGHT -> TransformOrigin(0f, 1f)
                            DialogPosition.BOTTOM_RIGHT -> TransformOrigin(0f, 0f)
                            DialogPosition.BOTTOM_LEFT -> TransformOrigin(1f, 0f)
                        }
                    }
                }
                DialogContent(
                    expandedState = expandedState,
                    transformOrigin = transformOrigin,
                    onDismissRequest = onDismissRequest,
                    config = config,
                    serviceCallback = serviceCallback
                )
            }
        }
    }
}

@Composable
fun DialogContent(
    expandedState: MutableTransitionState<Boolean>,
    transformOrigin: TransformOrigin,
    onDismissRequest: () -> Unit,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(expandedState, DialogLabel)

    val scale by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = AnimationDuration,
                easing = LinearOutSlowInEasing
            )
        },
        label = DialogLabel
    ) {
        if (it) 1f else 0.6f
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(AnimationDuration / 2) },
        label = DialogLabel
    ) {
        if (it) 1f else 0f
    }

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                this.transformOrigin = transformOrigin
            }
    ) {
        val dialogState =
            rememberGameToolsDialogState(config = config, serviceCallback = serviceCallback)
        GameToolsDialog(
            modifier = Modifier.width(
                if (maxWidth < maxHeight) {
                    0.7 * maxWidth
                } else {
                    0.4 * maxWidth
                }
            ),
            onDismissRequest = onDismissRequest,
            state = dialogState
        )
    }
}