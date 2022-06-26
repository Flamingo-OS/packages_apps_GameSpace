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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_DURATION
import com.flamingo.gamespace.ui.ingame.states.NotificationOverlayState

import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationOverlay(
    state: NotificationOverlayState,
    modifier: Modifier = Modifier
) {
    val showNotificationOverlay = remember { MutableTransitionState(false) }
    LaunchedEffect(state.showNotificationOverlay) {
        showNotificationOverlay.targetState = state.showNotificationOverlay
    }
    val isFullyGone by remember {
        derivedStateOf {
            showNotificationOverlay.isIdle && !showNotificationOverlay.currentState
        }
    }
    LaunchedEffect(isFullyGone) {
        if (isFullyGone) {
            state.removeNotification()
        }
    }
    BoxWithConstraints(modifier = modifier.padding(top = 24.dp), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.width(maxWidth * 0.7f)) {
            AnimatedVisibility(
                visibleState = showNotificationOverlay,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                AnimatedContent(
                    targetState = state.notification,
                    contentAlignment = Alignment.TopCenter
                ) {
                    val size by state.size.collectAsState(state.defaultSize)
                    val sizeInSp = with(LocalDensity.current) { size.toSp() }
                    Text(
                        text = it ?: "",
                        color = Color.White,
                        fontSize = sizeInSp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val isFullyVisible by remember {
                        derivedStateOf {
                            this@AnimatedContent.transition.currentState == EnterExitState.Visible
                        }
                    }
                    val visibleDuration by state.visibleDuration.collectAsState(
                        DEFAULT_NOTIFICATION_OVERLAY_DURATION
                    )
                    LaunchedEffect(isFullyVisible, visibleDuration) {
                        if (isFullyVisible) {
                            delay(visibleDuration)
                            showNotificationOverlay.targetState = false
                        }
                    }
                }
            }
        }
    }
}