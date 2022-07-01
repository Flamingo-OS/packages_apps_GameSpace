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

package com.flamingo.gamespace.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

import com.android.systemui.game.IGameSpaceService
import com.android.systemui.game.IGameSpaceServiceCallback
import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.RingerMode
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.ui.GameSpaceActivity
import com.flamingo.gamespace.ui.ingame.GameModeOverlayManager

import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class GameSpaceServiceImpl : LifecycleService(), SavedStateRegistryOwner {

    private lateinit var savedStateRegistryController: SavedStateRegistryController
    private lateinit var gameModeOverlayManager: GameModeOverlayManager

    private lateinit var gameSpaceServiceCallback: GameSpaceServiceCallback

    private val serviceBinder = object : IGameSpaceService.Stub() {
        override fun showGameUI(packageName: String) {
            lifecycleScope.launch {
                gameModeOverlayManager.setPackage(packageName)
                gameModeOverlayManager.addToWindow()
            }
        }

        override fun onGamePackageChanged(packageName: String) {
            lifecycleScope.launch {
                gameModeOverlayManager.setPackage(packageName)
            }
        }

        override fun setCallback(callback: IGameSpaceServiceCallback) {
            lifecycleScope.launch {
                gameSpaceServiceCallback = GameSpaceServiceCallback(callback)
                observeSystemStateSettings()
                gameModeOverlayManager.setGameSpaceServiceCallback(gameSpaceServiceCallback)
            }
        }

        override fun onStateChanged(state: Bundle) {
            lifecycleScope.launch {
                gameModeOverlayManager.setGameSpaceServiceConfig(state)
            }
        }
    }

    private lateinit var activityIntent: PendingIntent
    private lateinit var stopIntent: PendingIntent
    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var oldConfig: Configuration

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val notificationListener = NotificationListener()
    private var registeredNotificationListener = false

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        oldConfig = resources.configuration

        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        gameModeOverlayManager = GameModeOverlayManager(
            this,
            this,
            this,
            settingsRepository,
            notificationListener
        )

        activityIntent = PendingIntent.getActivity(
            this,
            ACTIVITY_REQUEST_CODE,
            Intent(this, GameSpaceActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            null
        )
        stopIntent = PendingIntent.getBroadcast(
            this,
            STOP_REQUEST_CODE,
            Intent(ACTION_STOP_GAME_MODE)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                .setPackage(SYSTEMUI_PACKAGE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
        showNotification()
        observeSettings()
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.gamespace_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_game)
            .setContentText(getString(R.string.gaming_mode_active))
            .setContentIntent(activityIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.baseline_videogame_asset_off_24,
                getString(R.string.stop),
                stopIntent
            )
        notificationManager.notify(GAME_MODE_ACTIVE_NOTIFICATION_ID, builder.build())
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            settingsRepository.enableNotificationOverlay.collect {
                if (it) {
                    registerNotificationListener()
                } else {
                    unregisterNotificationListener()
                }
            }
        }
        lifecycleScope.launch {
            settingsRepository.notificationOverlayBlackList.collect {
                notificationListener.setBlackList(it)
            }
        }
    }

    private fun observeSystemStateSettings() {
        lifecycleScope.launch {
            settingsRepository.ringerMode.collect {
                gameSpaceServiceCallback.setRingerMode(
                    when (it) {
                        RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                        RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                        RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
                        else -> AudioManager.RINGER_MODE_SILENT
                    }
                )
            }
        }
        lifecycleScope.launch {
            settingsRepository.disableAdaptiveBrightness.collect {
                gameSpaceServiceCallback.setAdaptiveBrightnessDisabled(it)
            }
        }
    }

    private fun registerNotificationListener() {
        if (registeredNotificationListener) return
        val componentName = ComponentName(this, NotificationListener::class.java)
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                notificationListener.registerAsSystemService(
                    this@GameSpaceServiceImpl,
                    componentName,
                    UserHandle.USER_CURRENT
                )
                withContext(Dispatchers.Main) {
                    registeredNotificationListener = true
                }
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to register notification listener", e)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return serviceBinder
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val localeChanged = (newConfig.diff(oldConfig) and ActivityInfo.CONFIG_LOCALE) != 0
        if (localeChanged) {
            createNotificationChannel()
        }
    }

    override fun onDestroy() {
        unregisterNotificationListener()
        gameModeOverlayManager.removeFromWindow()
        notificationManager.cancel(GAME_MODE_ACTIVE_NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun unregisterNotificationListener() {
        if (!registeredNotificationListener) return
        try {
            notificationListener.unregisterAsSystemService()
            registeredNotificationListener = false
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister notification listener", e)
        }
    }

    class GameSpaceServiceCallback(
        private val iGameSpaceServiceCallback: IGameSpaceServiceCallback
    ) {
        fun setGesturalNavigationLocked(isLocked: Boolean) {
            try {
                iGameSpaceServiceCallback.setGesturalNavigationLocked(isLocked)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to change gestural navigation lock status", e)
            }
        }

        fun setRingerMode(mode: Int) {
            try {
                iGameSpaceServiceCallback.setRingerMode(mode)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to set ringer mode", e)
            }
        }

        fun setAdaptiveBrightnessDisabled(disabled: Boolean) {
            try {
                iGameSpaceServiceCallback.setAdaptiveBrightnessDisabled(disabled)
            } catch (e: RemoteException) {
                Log.e(
                    TAG,
                    "Failed to ${if (disabled) "disable" else "enable"} adaptive brightness",
                    e
                )
            }
        }

        fun startScreenRecording() {
            try {
                iGameSpaceServiceCallback.startScreenRecording()
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to start screen recording", e)
            }
        }

        fun stopScreenRecording() {
            try {
                iGameSpaceServiceCallback.stopScreenRecording()
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to stop screen recording", e)
            }
        }
    }

    companion object {
        private const val TAG = "GameSpaceServiceImpl"

        private val NOTIFICATION_CHANNEL_ID =
            "${GameSpaceServiceImpl::class.qualifiedName}_NotificationChannel"

        private const val GAME_MODE_ACTIVE_NOTIFICATION_ID = 1

        private const val ACTIVITY_REQUEST_CODE = 1
        private const val STOP_REQUEST_CODE = 2

        private const val SYSTEMUI_PACKAGE = "com.android.systemui"
        private const val ACTION_STOP_GAME_MODE = "com.flamingo.gamespace.action.STOP_GAME_MODE"
    }
}