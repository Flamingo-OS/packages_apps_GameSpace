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
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext

import com.android.internal.util.ScreenshotHelper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScreenshotTileState(
    context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val screenshotHelper = ScreenshotHelper(context)

    fun takeScreenshot(delay: Long) {
        coroutineScope.launch {
            delay(delay)
            screenshotHelper.takeScreenshot(
                WindowManager.TAKE_SCREENSHOT_FULLSCREEN,
                true /* hasStatus */,
                true /* hasNav */,
                WindowManager.ScreenshotSource.SCREENSHOT_GLOBAL_ACTIONS,
                Handler(Looper.getMainLooper()),
                null /* completionConsumer */
            )
        }
    }
}

@Composable
fun rememberScreenshotTileState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(context, coroutineScope) {
    ScreenshotTileState(context = context, coroutineScope = coroutineScope)
}