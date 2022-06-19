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

package com.flamingo.gamespace.ui.ingame.states

import android.content.res.Configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

import com.flamingo.gamespace.data.settings.Settings
import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GameToolsHandleState(
    density: Density,
    private val configuration: Configuration,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) {

    val orientation: Int
        get() = configuration.orientation

    var windowBounds by mutableStateOf(Rect.Zero)
        private set

    init {
        val screenHeight = configuration.screenHeightDp * density.density
        val screenWidth = configuration.screenWidthDp * density.density
        windowBounds = Rect(0f, 0f, screenWidth, screenHeight)
    }

    fun getGameToolsHandlePosition(packageName: String): Flow<Offset> =
        when(orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                settingsRepository.getGameToolsHandlePortraitOffset(packageName)
                    .map {
                        if (it == null) {
                            Offset.Zero
                        } else {
                            Offset(x = it.x, y = it.y)
                        }
                    }
                    .distinctUntilChanged()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                settingsRepository.getGameToolsHandleLandscapeOffset(packageName)
                    .map {
                        if (it == null) {
                            Offset.Zero
                        } else {
                            Offset(x = it.x, y = it.y)
                        }
                    }
                    .distinctUntilChanged()
            }
            else -> {
                throw IllegalStateException("Unknown orientation $orientation")
            }
        }

    fun setGameToolsHandleOffset(packageName: String, offset: Offset) {
        coroutineScope.launch {
            when(orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    settingsRepository.setGameToolsHandlePortraitOffset(
                        packageName,
                        Settings.Offset.newBuilder()
                            .setX(offset.x)
                            .setY(offset.y)
                            .build()
                    )
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    settingsRepository.setGameToolsHandleLandscapeOffset(
                        packageName,
                        Settings.Offset.newBuilder()
                            .setX(offset.x)
                            .setY(offset.y)
                            .build()
                    )
                }
                else -> {
                    throw IllegalStateException("Unknown orientation $orientation")
                }
            }
        }
    }
}

@Composable
fun rememberGameToolsHandleState(
    density: Density = LocalDensity.current,
    configuration: Configuration = LocalConfiguration.current,
    settingsRepository: SettingsRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(density, configuration.orientation, settingsRepository) {
    GameToolsHandleState(
        density = density,
        configuration = configuration,
        settingsRepository = settingsRepository,
        coroutineScope = coroutineScope
    )
}