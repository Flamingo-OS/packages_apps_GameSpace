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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.DEFAULT_DISABLE_ADAPTIVE_BRIGHTNESS
import com.flamingo.gamespace.data.settings.DEFAULT_RINGER_MODE
import com.flamingo.gamespace.data.settings.DEFAULT_SHOW_GAME_TOOLS_HANDLE
import com.flamingo.gamespace.data.settings.RingerMode
import com.flamingo.gamespace.ui.Route
import com.flamingo.gamespace.ui.states.MainScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.flamingo.support.compose.ui.preferences.DividerSwitchPreference
import com.flamingo.support.compose.ui.preferences.Entry
import com.flamingo.support.compose.ui.preferences.ListPreference
import com.flamingo.support.compose.ui.preferences.Preference
import com.flamingo.support.compose.ui.preferences.PrimarySwitchPreference
import com.flamingo.support.compose.ui.preferences.SwitchPreference
import com.flamingo.support.compose.ui.preferences.TopIntroPreference

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onBackPressed: () -> Unit,
    navHostController: NavHostController,
    notificationOverlayEnabled: Boolean,
    onNotificationOverlayStateChanged: (Boolean) -> Unit,
    state: MainScreenState,
    modifier: Modifier = Modifier
) {
    val showGameToolsHandle by state.showGameToolsHandle.collectAsState(initial = DEFAULT_SHOW_GAME_TOOLS_HANDLE)
    CollapsingToolbarLayout(
        modifier = modifier,
        title = stringResource(id = R.string.app_name),
        onBackButtonPressed = onBackPressed
    ) {
        item(key = R.string.main_screen_intro_text) {
            TopIntroPreference(
                text = stringResource(id = R.string.main_screen_intro_text),
                modifier = Modifier.animateItemPlacement()
            )
        }
        item(key = R.string.enable_gamespace) {
            PrimarySwitchPreference(
                modifier = Modifier.animateItemPlacement(),
                title = stringResource(id = R.string.enable_gamespace),
                checked = state.gameSpaceEnabled,
                onCheckedChange = {
                    state.setGameSpaceEnabledSetting(it)
                }
            )
        }
        if (state.gameSpaceEnabled) {
            item(key = R.string.select_apps) {
                Preference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.select_apps),
                    summary = stringResource(id = R.string.select_apps_summary),
                    onClick = {
                        navHostController.navigate(Route.Main.SELECT_APPS_SCREEN)
                    }
                )
            }
            item(key = R.string.dynamic_mode) {
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.dynamic_mode),
                    summary = stringResource(id = R.string.dynamic_mode_summary),
                    checked = state.dynamicMode,
                    onCheckedChange = {
                        state.setDynamicModeEnabledSetting(it)
                    }
                )
            }
            item(key = R.string.disable_headsup) {
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.disable_headsup),
                    summary = stringResource(id = R.string.disable_headsup_summary),
                    checked = state.disableHeadsUp,
                    onCheckedChange = {
                        state.setHeadsUpDisabledSetting(it)
                    }
                )
            }
            item(key = R.string.disable_fullscreen_intent) {
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.disable_fullscreen_intent),
                    summary = stringResource(id = R.string.disable_fullscreen_intent_summary),
                    checked = state.disableFullscreenIntent,
                    onCheckedChange = {
                        state.setFullScreenIntentDisabledSetting(it)
                    }
                )
            }
            if (state.disableHeadsUp) {
                item(key = R.string.notification_overlay) {
                    DividerSwitchPreference(
                        modifier = Modifier.animateItemPlacement(),
                        title = stringResource(id = R.string.notification_overlay),
                        onClick = {
                            navHostController.navigate(Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN)
                        },
                        checked = notificationOverlayEnabled,
                        onCheckedChange = onNotificationOverlayStateChanged
                    )
                }
            }
            item(key = R.string.preferred_ringer_mode) {
                val ringerMode by state.ringerMode.collectAsState(initial = DEFAULT_RINGER_MODE)
                ListPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.preferred_ringer_mode),
                    entries = listOf(
                        Entry(stringResource(id = R.string.ring), RingerMode.NORMAL),
                        Entry(stringResource(id = R.string.vibrate), RingerMode.VIBRATE),
                        Entry(stringResource(id = R.string.silent), RingerMode.SILENT)
                    ),
                    value = ringerMode,
                    onEntrySelected = {
                        state.setRingerMode(it)
                    }
                )
            }
            item(key = R.string.disable_adaptive_brightness) {
                val disable by state.disableAdaptiveBrightness.collectAsState(initial = DEFAULT_DISABLE_ADAPTIVE_BRIGHTNESS)
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.disable_adaptive_brightness),
                    checked = disable,
                    onCheckedChange = {
                        state.setAdaptiveBrightnessDisabled(it)
                    }
                )
            }
            item(key = R.string.show_game_tools_handle) {
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.show_game_tools_handle),
                    summary = stringResource(id = R.string.show_game_tools_handle_summary),
                    checked = showGameToolsHandle,
                    onCheckedChange = {
                        state.setShowGameToolsHandle(it)
                    }
                )
            }
            if (showGameToolsHandle) {
                item(key = R.string.game_tools_dialog_tiles) {
                    Preference(
                        modifier = Modifier.animateItemPlacement(),
                        title = stringResource(id = R.string.game_tools_dialog_tiles),
                        summary = stringResource(id = R.string.game_tools_dialog_tiles_summary),
                        onClick = {
                            navHostController.navigate(Route.Main.TILES_SCREEN)
                        }
                    )
                }
            }
            item(key = R.string.disable_call_ringing) {
                SwitchPreference(
                    modifier = Modifier.animateItemPlacement(),
                    title = stringResource(id = R.string.disable_call_ringing),
                    checked = state.disableCallRinging,
                    onCheckedChange = {
                        state.setCallRingingDisabled(it)
                    }
                )
            }
        }
    }
}