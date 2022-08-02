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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_DURATION
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_ENABLED
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE
import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
import com.flamingo.gamespace.ui.Route
import com.flamingo.gamespace.ui.states.NotificationOverlayScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.flamingo.support.compose.ui.preferences.DiscreteSeekBarPreference
import com.flamingo.support.compose.ui.preferences.Preference
import com.flamingo.support.compose.ui.preferences.PrimarySwitchPreference
import com.flamingo.support.compose.ui.preferences.TopIntroPreference

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationOverlayScreen(
    state: NotificationOverlayScreenState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val enabled by state.notificationOverlayEnabled.collectAsState(
        DEFAULT_NOTIFICATION_OVERLAY_ENABLED
    )
    CollapsingToolbarLayout(
        modifier = modifier,
        title = stringResource(id = R.string.notification_overlay),
        onBackButtonPressed = {
            navHostController.popBackStack()
        }
    ) {
        item(key = R.string.notification_overlay_intro_text) {
            TopIntroPreference(
                modifier = Modifier.animateItemPlacement(),
                text = stringResource(id = R.string.notification_overlay_intro_text)
            )
        }
        item(key = R.string.enable_notification_overlay) {
            PrimarySwitchPreference(
                modifier = Modifier.animateItemPlacement(),
                title = stringResource(id = R.string.enable_notification_overlay),
                checked = enabled,
                onCheckedChange = {
                    state.setNotificationOverlayEnabled(it)
                }
            )
        }
        if (enabled) {
            item(key = R.string.size_in_portrait) {
                val savedPortraitSize by state.notificationOverlaySizePortrait.collectAsState(
                    DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT
                )
                var portraitSize by remember(savedPortraitSize) { mutableStateOf(savedPortraitSize) }
                DiscreteSeekBarPreference(
                    modifier = Modifier.animateItemPlacement(),
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
            item(key = R.string.size_in_landscape) {
                val savedLandscapeSize by state.notificationOverlaySizeLandscape.collectAsState(
                    DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE
                )
                var landscapeSize by remember(savedLandscapeSize) {
                    mutableStateOf(
                        savedLandscapeSize
                    )
                }
                DiscreteSeekBarPreference(
                    modifier = Modifier.animateItemPlacement(),
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
            item(key = R.string.visible_duration) {
                val savedDuration by state.notificationOverlayDuration.collectAsState(
                    DEFAULT_NOTIFICATION_OVERLAY_DURATION
                )
                var duration by remember(savedDuration) { mutableStateOf(savedDuration.toInt() / 1000) }
                DiscreteSeekBarPreference(
                    modifier = Modifier.animateItemPlacement(),
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
            item(key = R.string.blacklisted_apps) {
                Preference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.blacklisted_apps),
                    summary = stringResource(id = R.string.blacklisted_apps_summary),
                    onClick = {
                        navHostController.navigate(Route.NotificationOverlay.NOTIFICATION_OVERLAY_BLACKLIST_SCREEN)
                    }
                )
            }
        }
    }
}