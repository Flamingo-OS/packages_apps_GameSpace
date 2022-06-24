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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_DURATION
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_ENABLED
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
import com.flamingo.gamespace.ui.Route
import com.flamingo.gamespace.ui.preferences.DiscreteSeekBarPreference
import com.flamingo.gamespace.ui.preferences.Preference
import com.flamingo.gamespace.ui.preferences.PrimarySwitchPreference
import com.flamingo.gamespace.ui.states.NotificationOverlayScreenState
import com.flamingo.gamespace.ui.widgets.CollapsingToolbarScreen
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun NotificationOverlayScreen(
    state: NotificationOverlayScreenState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    systemUiController: SystemUiController = rememberSystemUiController(),
) {
    CollapsingToolbarScreen(
        modifier = modifier,
        title = stringResource(id = R.string.notification_overlay),
        onBackButtonPressed = {
            navHostController.navigate(Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN)
        },
        onStatusBarColorUpdateRequest = {
            systemUiController.setStatusBarColor(it)
        },
    ) {
        item {
            Text(
                text = stringResource(id = R.string.notification_overlay_intro_text),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        item {
            val enabled by state.notificationOverlayEnabled.collectAsState(
                DEFAULT_NOTIFICATION_OVERLAY_ENABLED
            )
            PrimarySwitchPreference(
                modifier = Modifier.padding(
                    top = 24.dp,
                    bottom = 12.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                title = stringResource(id = R.string.enable_notification_overlay),
                checked = enabled,
                onCheckedChange = {
                    state.setNotificationOverlayEnabled(it)
                }
            )
        }
        item {
            val savedPortraitSize by state.notificationOverlaySizePortrait.collectAsState(
                DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
            )
            var portraitSize by remember(savedPortraitSize) { mutableStateOf(savedPortraitSize) }
            DiscreteSeekBarPreference(
                title = stringResource(id = R.string.size_in_portrait),
                min = 30,
                max = 90,
                value = portraitSize,
                showProgressText = true,
                onProgressChanged = {
                    portraitSize = it
                },
                onProgressChangeFinished = {
                    state.setPortraitNotificationOverlaySize(portraitSize)
                }
            )
        }
        item {
            val savedLandscapeSize by state.notificationOverlaySizeLandscape.collectAsState(
                DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE
            )
            var landscapeSize by remember(savedLandscapeSize) { mutableStateOf(savedLandscapeSize) }
            DiscreteSeekBarPreference(
                title = stringResource(id = R.string.size_in_landscape),
                min = 60,
                max = 120,
                value = landscapeSize,
                showProgressText = true,
                onProgressChanged = {
                    landscapeSize = it
                },
                onProgressChangeFinished = {
                    state.setLandscapeNotificationOverlaySize(landscapeSize)
                }
            )
        }
        item {
            val savedDuration by state.notificationOverlayDuration.collectAsState(
                DEFAULT_NOTIFICATION_OVERLAY_DURATION
            )
            var duration by remember(savedDuration) { mutableStateOf(savedDuration.toInt() / 1000) }
            DiscreteSeekBarPreference(
                title = stringResource(id = R.string.visible_duration),
                summary = stringResource(id = R.string.visible_duration_summary),
                min = 1,
                max = 10,
                value = duration,
                showProgressText = true,
                onProgressChanged = {
                    duration = it
                },
                onProgressChangeFinished = {
                    state.setNotificationOverlayDuration(duration)
                }
            )
        }
        item {
            Preference(
                title = stringResource(id = R.string.blacklisted_apps),
                summary = stringResource(id = R.string.blacklisted_apps_summary),
                onClick = {
                    navHostController.navigate(Route.NotificationOverlay.NOTIFICATION_OVERLAY_BLACKLIST_SCREEN)
                }
            )
        }
    }
}