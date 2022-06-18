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
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

class GameToolsHandleState(
    density: Density,
    private val configuration: Configuration
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
}

@Composable
fun rememberGameToolsHandleState(
    density: Density = LocalDensity.current,
    configuration: Configuration = LocalConfiguration.current
) = remember(density, configuration.orientation) {
    GameToolsHandleState(density = density, configuration = configuration)
}