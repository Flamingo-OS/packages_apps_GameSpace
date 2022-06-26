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

import android.content.Context
import android.content.res.Configuration
import android.view.WindowInsets
import android.view.WindowManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.content.getSystemService

import com.flamingo.gamespace.data.settings.Settings
import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GameToolsHandleState(
    context: Context,
    val orientation: Int,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) {

    val windowSize: IntSize

    init {
        val wm = context.getSystemService<WindowManager>()!!
        val bounds = wm.currentWindowMetrics.bounds
        val systemBarsInsets =
            wm.currentWindowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
        windowSize = IntSize(
            bounds.width() - (systemBarsInsets.left + systemBarsInsets.right),
            bounds.height() - (systemBarsInsets.top + systemBarsInsets.bottom)
        )
    }

    fun getGameToolsHandlePosition(packageName: String): Flow<Offset> =
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> settingsRepository.getGameToolsHandlePortraitOffset(
                packageName
            )
            Configuration.ORIENTATION_LANDSCAPE -> settingsRepository.getGameToolsHandleLandscapeOffset(
                packageName
            )
            else -> throw IllegalStateException("Unknown orientation $orientation")
        }.map {
            if (it == null) {
                Offset.Zero
            } else {
                Offset(x = it.x, y = it.y)
            }
        }.distinctUntilChanged()

    fun setGameToolsHandleOffset(packageName: String, offset: Offset) {
        coroutineScope.launch {
            val settingOffset = Settings.Offset.newBuilder()
                .setX(offset.x)
                .setY(offset.y)
                .build()
            when (orientation) {
                Configuration.ORIENTATION_PORTRAIT -> settingsRepository.setGameToolsHandlePortraitOffset(
                    packageName,
                    settingOffset
                )
                Configuration.ORIENTATION_LANDSCAPE -> settingsRepository.setGameToolsHandleLandscapeOffset(
                    packageName,
                    settingOffset
                )
                else -> throw IllegalStateException("Unknown orientation $orientation")
            }
        }
    }
}

@Composable
fun rememberGameToolsHandleState(
    context: Context = LocalContext.current,
    orientation: Int = LocalConfiguration.current.orientation,
    settingsRepository: SettingsRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(context, orientation, settingsRepository) {
    GameToolsHandleState(
        context = context,
        orientation = orientation,
        settingsRepository = settingsRepository,
        coroutineScope = coroutineScope
    )
}