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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationOverlayBlackListScreen(
    onBackButtonPressed: () -> Unit,
    isEnterAnimationRunning: Boolean,
    state: NotificationOverlayBlackListScreenState
) {
    CollapsingToolbarLayout(
        title = stringResource(id = R.string.blacklisted_apps),
        onBackButtonPressed = onBackButtonPressed,
    ) {
        if (isEnterAnimationRunning) {
            item(key = "Loading progress") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            }
        } else {
            items(state.appList, key = { it.packageName }) { appInfo ->
                AppItemPreference(
                    modifier = Modifier.animateItemPlacement(),
                    appInfo = appInfo,
                    onCheckedChange = {
                        state.setAppSelected(appInfo, it)
                    },
                )
            }
        }
    }
}

@Composable
fun AppItemPreference(
    appInfo: AppInfo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Preference(
        modifier = modifier,
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