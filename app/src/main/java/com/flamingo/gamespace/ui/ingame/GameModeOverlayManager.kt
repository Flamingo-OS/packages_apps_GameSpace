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
import android.os.Bundle
import android.view.WindowManager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback
import com.flamingo.gamespace.services.NotificationListener
import com.flamingo.gamespace.ui.ingame.states.rememberNotificationOverlayState

class GameModeOverlayManager(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    savedStateRegistryOwner: SavedStateRegistryOwner,
    private val settingsRepository: SettingsRepository,
    private val notificationListener: NotificationListener
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
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
        PixelFormat.TRANSLUCENT
    )

    private var gamePackageName by mutableStateOf<String?>(null)
    private var serviceCallback by mutableStateOf<GameSpaceServiceCallback?>(null)
    private var serviceConfig by mutableStateOf(Bundle())

    init {
        val windowContext = context.createWindowContext(
            context.display!!,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            null
        )
        rootComposeView = ComposeView(windowContext)
        rootViewLP.token = Binder("GameSpace Overlay Token")
        rootViewLP.setTrustedOverlay()
        ViewTreeLifecycleOwner.set(rootComposeView, lifecycleOwner)
        rootComposeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        rootComposeView.fitsSystemWindows = true
    }

    fun setPackage(packageName: String) {
        gamePackageName = packageName
    }

    fun setGameSpaceServiceCallback(gameSpaceServiceCallback: GameSpaceServiceCallback) {
        serviceCallback = gameSpaceServiceCallback
    }

    fun setGameSpaceServiceConfig(config: Bundle) {
        serviceConfig = config
    }

    fun addToWindow() {
        if (!rootComposeView.isAttachedToWindow) {
            rootComposeView.setContent {
                gamePackageName?.let {
                    GameModeUI(
                        packageName = it,
                        settingsRepository = settingsRepository,
                        config = serviceConfig,
                        serviceCallback = serviceCallback,
                    )
                }
                val state = rememberNotificationOverlayState(
                    notificationListener = notificationListener,
                    settingsRepository = settingsRepository
                )
                NotificationOverlay(state = state, modifier = Modifier.fillMaxSize())
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