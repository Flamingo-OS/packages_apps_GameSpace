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

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback
import com.flamingo.gamespace.ui.ingame.states.rememberGameToolsHandleState
import com.flamingo.gamespace.ui.theme.GameSpaceTheme

import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameModeUI(
    settingsRepository: SettingsRepository,
    packageName: String,
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?
) {
    GameSpaceTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val gameToolsHandleState =
                rememberGameToolsHandleState(settingsRepository = settingsRepository)
            var handlePosition by remember { mutableStateOf(Offset.Zero) }
            LaunchedEffect(packageName) {
                gameToolsHandleState.getGameToolsHandlePosition(packageName).collectLatest {
                    handlePosition = it
                }
            }
            val updatedPackageName by rememberUpdatedState(newValue = packageName)
            GameToolsHandle(
                position = handlePosition,
                onHandleDragged = {
                    handlePosition = it
                },
                onDragStop = {
                    gameToolsHandleState.setGameToolsHandleOffset(updatedPackageName, handlePosition)
                },
                state = gameToolsHandleState,
                modifier = Modifier.fillMaxSize(),
                config = config,
                serviceCallback = serviceCallback,
                settingsRepository = settingsRepository
            )
        }
    }
}