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

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.IActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.RemoteException
import android.util.Log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import com.flamingo.gamespace.R
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.data.settings.Tile

import java.text.DateFormat
import java.util.Locale

import kotlin.coroutines.coroutineContext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameToolsDialogState(
    private val context: Context,
    val settingsRepository: SettingsRepository,
    coroutineScope: CoroutineScope
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

    private val iActivityManager: IActivityManager = ActivityManager.getService()

    var memoryInfo by mutableStateOf<String?>(null)
        private set

    var isLowMemory by mutableStateOf(false)
        private set

    val tiles: Flow<List<Tile>>
        get() = settingsRepository.tiles

    init {
        coroutineScope.launch(Dispatchers.Default) {
            updateMemoryInfo()
        }
    }

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

    private suspend fun updateMemoryInfo() {
        do {
            try {
                val memInfo = MemoryInfo()
                iActivityManager.getMemoryInfo(memInfo)
                withContext(Dispatchers.Main) {
                    val used = String.format("%.1f", (memInfo.totalMem - memInfo.availMem).toFloat() / GiB)
                    val total = String.format("%.1f", memInfo.totalMem.toFloat() / GiB)
                    memoryInfo = context.getString(R.string.memory_info, used, total)
                    isLowMemory = memInfo.availMem <= memInfo.threshold
                }
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to get memory info", e)
            }
            delay(5000)
        } while (coroutineContext.isActive)
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

    companion object {
        private const val TAG = "GameToolsDialogState"

        private const val GiB = 1024 * 1024 * 1024
    }
}

@Composable
fun rememberGameToolsDialogState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    settingsRepository: SettingsRepository
): GameToolsDialogState {
    val state = remember(context, settingsRepository, coroutineScope) {
        GameToolsDialogState(
            context = context,
            settingsRepository = settingsRepository,
            coroutineScope = coroutineScope
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