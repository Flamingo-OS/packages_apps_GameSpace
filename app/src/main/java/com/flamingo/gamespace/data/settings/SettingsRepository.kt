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

import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    private val settingsDataStore = context.settingsDataStore

    val enableNotificationOverlay: Flow<Boolean>
        get() = settingsDataStore.data.map { it.enableNotificationOverlay }

    val notificationOverlaySizePortrait: Flow<Int>
        get() = settingsDataStore.data.map { it.notificationOverlaySizePortrait }

    val notificationOverlaySizeLandscape: Flow<Int>
        get() = settingsDataStore.data.map { it.notificationOverlaySizeLandscape }

    val notificationOverlayBlackList: Flow<List<String>>
        get() = settingsDataStore.data.map { it.notificationOverlayBlacklist.split(DELIMITER) }

    val notificationOverlayDuration: Flow<Long>
        get() = settingsDataStore.data.map { it.notificationOverlayDuration }

    fun getGameToolsHandlePortraitOffset(packageName: String): Flow<Settings.Offset?> =
        settingsDataStore.data.map { it.gameToolsHandlePortraitOffsetMap[packageName] }

    suspend fun setGameToolsHandlePortraitOffset(packageName: String, offset: Settings.Offset) {
        settingsDataStore.updateData {
            it.toBuilder()
                .putGameToolsHandlePortraitOffset(packageName, offset)
                .build()
        }
    }

    fun getGameToolsHandleLandscapeOffset(packageName: String): Flow<Settings.Offset?> =
        settingsDataStore.data.map { it.gameToolsHandleLandscapeOffsetMap[packageName] }

    suspend fun setGameToolsHandleLandscapeOffset(packageName: String, offset: Settings.Offset) {
        settingsDataStore.updateData {
            it.toBuilder()
                .putGameToolsHandleLandscapeOffset(packageName, offset)
                .build()
        }
    }

    suspend fun setNotificationOverlayEnabled(enabled: Boolean) {
        settingsDataStore.updateData {
            it.toBuilder()
                .setEnableNotificationOverlay(enabled)
                .build()
        }
    }

    suspend fun setPortraitNotificationOverlaySize(size: Int) {
        settingsDataStore.updateData {
            it.toBuilder()
                .setNotificationOverlaySizePortrait(size)
                .build()
        }
    }

    suspend fun setLandscapeNotificationOverlaySize(size: Int) {
        settingsDataStore.updateData {
            it.toBuilder()
                .setNotificationOverlaySizeLandscape(size)
                .build()
        }
    }

    suspend fun setNotificationOverlayBlacklist(list: List<String>) {
        settingsDataStore.updateData {
            it.toBuilder()
                .setNotificationOverlayBlacklist(list.joinToString(DELIMITER))
                .build()
        }
    }

    suspend fun setNotificationOverlayDuration(duration: Int) {
        settingsDataStore.updateData {
            it.toBuilder()
                .setNotificationOverlayDuration(duration * 1000L)
                .build()
        }
    }

    companion object {
        private const val DELIMITER = ";"
    }
}