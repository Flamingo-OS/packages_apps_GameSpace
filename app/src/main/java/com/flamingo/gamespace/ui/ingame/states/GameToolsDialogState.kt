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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import java.text.DateFormat
import java.util.Locale

class GameToolsDialogState(
    private val context: Context
) {

    private val locale: Locale
        get() = context.resources.configuration.locales[0]

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_TIME_TICK) return
            time = getFormattedTime()
        }
    }

    var time by mutableStateOf(getFormattedTime())
        private set

    init {
        context.registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun getFormattedTime(): String {
        return DateFormat.getTimeInstance(
            DateFormat.SHORT,
            locale
        ).format(System.currentTimeMillis())
    }

    fun onDispose() {
        context.unregisterReceiver(timeReceiver)
    }
}

@Composable
fun rememberGameToolsDialogState(
    context: Context = LocalContext.current
): GameToolsDialogState {
    val state = remember(context) {
        GameToolsDialogState(context)
    }
    DisposableEffect(context) {
        onDispose {
            state.onDispose()
        }
    }
    return state
}