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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.R
import com.flamingo.gamespace.ui.states.AppInfo
import com.flamingo.gamespace.ui.states.NotificationOverlayBlackListScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.flamingo.support.compose.ui.preferences.Preference
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun NotificationOverlayBlackListScreen(
    onBackButtonPressed: () -> Unit,
    isEnterAnimationRunning: Boolean,
    state: NotificationOverlayBlackListScreenState,
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    CollapsingToolbarLayout(
        title = stringResource(id = R.string.blacklisted_apps),
        onBackButtonPressed = onBackButtonPressed,
        systemUiController = systemUiController,
    ) {
        if (isEnterAnimationRunning) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            }
        } else {
            items(state.appList) { appInfo ->
                AppItemPreference(
                    appInfo = appInfo,
                    onCheckedChange = {
                        state.setAppSelected(appInfo, it)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItemPreference(appInfo: AppInfo, onCheckedChange: (Boolean) -> Unit) {
    Preference(
        title = appInfo.label,
        summary = appInfo.packageName,
        startWidget = {
            Image(
                bitmap = appInfo.icon,
                contentDescription = stringResource(
                    id = R.string.app_icon_content_desc,
                    appInfo.label
                )
            )
        },
        endWidget = {
            Checkbox(
                checked = appInfo.selected,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}