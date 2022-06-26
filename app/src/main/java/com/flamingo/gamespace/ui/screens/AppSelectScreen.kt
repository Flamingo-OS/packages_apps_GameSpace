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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.flamingo.gamespace.R
import com.flamingo.gamespace.ui.states.AppInfo
import com.flamingo.gamespace.ui.states.AppSelectScreenState
import com.flamingo.gamespace.ui.states.rememberAppSelectScreenState
import com.flamingo.support.compose.ui.layout.CollapsingToolbarLayout
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun AppSelectScreen(
    onBackPressed: () -> Unit,
    isEnterAnimationRunning: Boolean,
    state: AppSelectScreenState = rememberAppSelectScreenState(),
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    CollapsingToolbarLayout(
        title = stringResource(id = R.string.select_apps),
        onBackButtonPressed = onBackPressed,
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
                SelectableAppItem(
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
fun SelectableAppItem(
    appInfo: AppInfo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            onCheckedChange(!appInfo.selected)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 12.dp,
                    bottom = 12.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = appInfo.icon,
                    contentDescription = stringResource(
                        id = R.string.app_icon_content_desc,
                        appInfo.label
                    )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = appInfo.label,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                    )
                    if (appInfo.isGame) {
                        GameChip(modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                    maxLines = 4,
                )
            }
            Box(
                modifier = Modifier.padding(start = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = appInfo.selected,
                    onCheckedChange = onCheckedChange,
                )
            }
        }
    }
}

@Composable
fun GameChip(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = stringResource(id = R.string.game_chip_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
    }
}