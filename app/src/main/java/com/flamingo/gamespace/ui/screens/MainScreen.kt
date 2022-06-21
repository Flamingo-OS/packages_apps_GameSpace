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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.R
import com.flamingo.gamespace.ui.preferences.PrimarySwitchPreference
import com.flamingo.gamespace.ui.preferences.SwitchPreference
import com.flamingo.gamespace.ui.states.MainScreenState
import com.flamingo.gamespace.ui.states.rememberMainScreenState
import com.flamingo.gamespace.ui.widgets.CollapsingToolbarScreen
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    state: MainScreenState = rememberMainScreenState(),
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    LaunchedEffect(Unit) {
        systemUiController.setNavigationBarColor(
            Color.Transparent,
            navigationBarContrastEnforced = false
        )
    }
    CollapsingToolbarScreen(
        modifier = modifier,
        title = stringResource(id = R.string.app_name),
        onBackButtonPressed = onBackPressed,
        onStatusBarColorUpdateRequest = {
            systemUiController.setStatusBarColor(it)
        },
    ) {
        item {
            Text(
                text = stringResource(id = R.string.main_screen_intro_text),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Light
            )
        }
        item {
            PrimarySwitchPreference(
                title = stringResource(id = R.string.enable_gamespace),
                checked = state.gameSpaceEnabled,
                onCheckedChange = {
                    state.setGameSpaceEnabledSetting(it)
                }
            )
        }
        if (state.gameSpaceEnabled) {
            item {
                SwitchPreference(
                    title = stringResource(id = R.string.dynamic_mode),
                    summary = stringResource(id = R.string.dynamic_mode_summary),
                    checked = state.dynamicMode,
                    onCheckedChange = {
                        state.setDynamicModeEnabledSetting(it)
                    }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(id = R.string.disable_headsup),
                    summary = stringResource(id = R.string.disable_headsup_summary),
                    checked = state.disableHeadsUp,
                    onCheckedChange = {
                        state.setHeadsUpDisabledSetting(it)
                    }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(id = R.string.disable_fullscreen_intent),
                    summary = stringResource(id = R.string.disable_fullscreen_intent_summary),
                    checked = state.disableFullscreenIntent,
                    onCheckedChange = {
                        state.setFullScreenIntentDisabledSetting(it)
                    }
                )
            }
        }
    }
}