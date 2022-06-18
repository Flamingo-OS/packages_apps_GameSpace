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

import android.content.Context
import android.graphics.PixelFormat
import android.os.Binder
import android.view.WindowManager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

import com.flamingo.gamespace.ui.theme.GameSpaceTheme

class GameModeUI(
    context: Context,
    lifecycle: Lifecycle,
    savedStateRegistryOwner: SavedStateRegistryOwner
) {

    private val wm = context.getSystemService<WindowManager>()!!

    private val rootComposeView: ComposeView
    private val rootViewLP = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    )

    init {
        val windowContext = context.createWindowContext(
            context.display!!,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            null
        )
        rootComposeView = ComposeView(windowContext)
        rootViewLP.token = Binder("GameSpace Overlay Token")
        rootViewLP.setTrustedOverlay()
        ViewTreeLifecycleOwner.set(rootComposeView) { lifecycle }
        rootComposeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        rootComposeView.fitsSystemWindows = true
    }

    fun addToWindow() {
        if (!rootComposeView.isAttachedToWindow) {
            rootComposeView.setContent {
                GameSpaceTheme {
                    Box(
                        modifier = Modifier
                            .systemBarsPadding()
                            .fillMaxSize()
                    ) {
                        GameToolsHandle(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            wm.addView(rootComposeView, rootViewLP)
        }
    }

    fun removeFromWindow() {
        if (rootComposeView.isAttachedToWindow) {
            wm.removeView(rootComposeView)
        }
    }
}