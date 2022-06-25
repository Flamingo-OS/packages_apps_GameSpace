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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback

import java.text.DateFormat
import java.util.Locale

class GameToolsDialogState(
    private val context: Context,
    val serviceCallback: GameSpaceServiceCallback?,
    val config: Bundle,
    val settingsRepository: SettingsRepository
) {

    private val locale: Locale
        get() = context.resources.configuration.locales[0]

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK -> {
                    time = getFormattedTime()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryStatus(intent)
                }
            }
        }
    }

    var time by mutableStateOf(getFormattedTime())
        private set

    var batteryText by mutableStateOf(context.getString(R.string.battery_unknown))
        private set

    var date by mutableStateOf(getFormattedDate())
        private set

    private fun getFormattedTime(): String {
        return DateFormat.getTimeInstance(
            DateFormat.SHORT,
            locale
        ).format(System.currentTimeMillis())
    }

    private fun updateBatteryStatus(intent: Intent) {
        val pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
        val percent = (level.toFloat() / scale * 100).toInt()
        batteryText = if (pluggedIn) {
            context.getString(R.string.charging_battery_text, percent)
        } else {
            context.getString(R.string.battery_text, percent)
        }
    }

    private fun getFormattedDate(): String {
        return DateFormat.getDateInstance(
            DateFormat.LONG,
            locale
        ).format(System.currentTimeMillis())
    }

    internal fun registerReceiver() {
        val batteryStatusIntent = context.registerReceiver(
            broadcastReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }
        )
        if (batteryStatusIntent != null) {
            updateBatteryStatus(batteryStatusIntent)
        }
    }

    internal fun unregisterReceiver() {
        context.unregisterReceiver(broadcastReceiver)
    }
}

@Composable
fun rememberGameToolsDialogState(
    context: Context = LocalContext.current,
    config: Bundle,
    settingsRepository: SettingsRepository,
    serviceCallback: GameSpaceServiceCallback?,
): GameToolsDialogState {
    val state = remember(context, config) {
        GameToolsDialogState(
            context = context,
            config = config,
            serviceCallback = serviceCallback,
            settingsRepository = settingsRepository
        )
    }
    DisposableEffect(context) {
        state.registerReceiver()
        onDispose {
            state.unregisterReceiver()
        }
    }
    return state
}