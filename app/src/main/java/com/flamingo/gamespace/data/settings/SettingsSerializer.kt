/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.flamingo.gamespace.data.settings

import android.content.Context

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore

import com.google.protobuf.InvalidProtocolBufferException

import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = Settings.newBuilder()
        .setEnableNotificationOverlay(DEFAULT_NOTIFICATION_OVERLAY_ENABLED)
        .setNotificationOverlayDuration(DEFAULT_NOTIFICATION_OVERLAY_DURATION)
        .setNotificationOverlaySizePortrait(DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT)
        .setNotificationOverlaySizeLandscape(DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE)
        .setRingerMode(DEFAULT_RINGER_MODE)
        .setDisableAdaptiveBrightness(DEFAULT_DISABLE_ADAPTIVE_BRIGHTNESS)
        .build()

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Settings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot parse gamespace proto", exception)
        }
    }

    override suspend fun writeTo(
        t: Settings,
        output: OutputStream
    ) = t.writeTo(output)
}

const val DEFAULT_NOTIFICATION_OVERLAY_ENABLED = true
const val DEFAULT_NOTIFICATION_OVERLAY_DURATION = 2000L
const val DEFAULT_NOTIFICATION_OVERLAY_SIZE_PORTRAIT = 60
const val DEFAULT_NOTIFICATION_OVERLAY_SIZE_LANDSCAPE = 90
val DEFAULT_RINGER_MODE = RingerMode.SILENT
const val DEFAULT_DISABLE_ADAPTIVE_BRIGHTNESS = true

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "gamespace_settings",
    serializer = SettingsSerializer
)