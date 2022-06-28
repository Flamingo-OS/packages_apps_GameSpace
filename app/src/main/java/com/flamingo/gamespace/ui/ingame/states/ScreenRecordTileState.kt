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

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

import com.android.systemui.statusbar.phone.CONFIG_SCREEN_RECORD
import com.flamingo.gamespace.services.GameSpaceServiceImpl.GameSpaceServiceCallback

class ScreenRecordTileState(
    private val onRecordingStateChangeRequest: (Boolean) -> Unit,
    config: Bundle
) {
    val isRecording = config.getBoolean(CONFIG_SCREEN_RECORD)

    fun toggleRecordingState() {
        onRecordingStateChangeRequest(!isRecording)
    }
}

@Composable
fun rememberScreenRecordTileState(
    config: Bundle,
    serviceCallback: GameSpaceServiceCallback?
): ScreenRecordTileState {
    val callback by rememberUpdatedState(newValue = serviceCallback)
    return remember(config) {
        ScreenRecordTileState(
            config = config,
            onRecordingStateChangeRequest = {
                if (it) {
                    callback?.startScreenRecording()
                } else {
                    callback?.stopScreenRecording()
                }
            }
        )
    }
}