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

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round

private const val DialogLabel = "ToolsDialog"

private enum class DialogPosition {
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    TOP_RIGHT,
    TOP_LEFT
}

internal const val AnimationDuration = 300

@Composable
fun ToolsDialog(
    showDialog: Boolean,
    handleBounds: Rect,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit
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
                    content = content
                )
            }
        }
    }
}

@Composable
fun DialogContent(
    expandedState: MutableTransitionState<Boolean>,
    transformOrigin: TransformOrigin,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit
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
            },
        content = content
    )
}