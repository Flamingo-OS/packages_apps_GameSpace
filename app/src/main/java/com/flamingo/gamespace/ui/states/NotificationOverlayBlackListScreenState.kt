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

package com.flamingo.gamespace.ui.states

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

import com.flamingo.gamespace.data.settings.SettingsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.androidx.compose.get

class NotificationOverlayBlackListScreenState(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val lifecycle: Lifecycle,
    context: Context
) {

    private val pm = context.packageManager

    val appList = mutableStateListOf<AppInfo>()

    init {
        coroutineScope.launch {
            loadAllAppsList()
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsRepository.notificationOverlayBlackList.collect {
                    updateList(it)
                }
            }
        }
    }

    private suspend fun loadAllAppsList() {
        val selectedList = getSelectedList()
        val sortedList = withContext(Dispatchers.Default) {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter {
                    !it.isSystemApp()
                }
                .map {
                    AppInfo(
                        icon = it.loadIcon(pm).toBitmap().asImageBitmap(),
                        label = it.loadLabel(pm).toString(),
                        packageName = it.packageName,
                        isGame = it.category == ApplicationInfo.CATEGORY_GAME,
                        selected = selectedList.contains(it.packageName)
                    )
                }
                .sortedWith { first, second ->
                    first.label.compareTo(second.label, true)
                }
        }
        appList.addAll(sortedList)
    }

    private suspend fun getSelectedList() = settingsRepository.notificationOverlayBlackList.first()

    private suspend fun updateList(selectedList: List<String>) {
        withContext(Dispatchers.Default) {
            val appListClone = appList.toList()
            appListClone.forEachIndexed { index, appInfo ->
                if (appInfo.selected && !selectedList.contains(appInfo.packageName)) {
                    withContext(Dispatchers.Main) {
                        appList[index] = appInfo.copy(selected = false)
                    }
                } else if (!appInfo.selected && selectedList.contains(appInfo.packageName)) {
                    withContext(Dispatchers.Main) {
                        appList[index] = appInfo.copy(selected = true)
                    }
                }
            }
        }
    }

    fun setAppSelected(appInfo: AppInfo, selected: Boolean) {
        coroutineScope.launch {
            val selectedList = getSelectedList()
            withContext(Dispatchers.Default) {
                if (selected && !selectedList.contains(appInfo.packageName)) {
                    val newList = selectedList.toMutableList()
                    newList.add(appInfo.packageName)
                    settingsRepository.setNotificationOverlayBlacklist(newList.toList())
                } else if (!selected && selectedList.contains(appInfo.packageName)) {
                    val newList = selectedList.toMutableList()
                    newList.remove(appInfo.packageName)
                    settingsRepository.setNotificationOverlayBlacklist(newList.toList())
                }
            }
        }
    }
}

@Composable
fun rememberNotificationOverlayBlackListScreenState(
    context: Context = LocalContext.current,
    settingsRepository: SettingsRepository = get(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
) = remember(context, settingsRepository, coroutineScope, lifecycle) {
    NotificationOverlayBlackListScreenState(
        context = context,
        settingsRepository = settingsRepository,
        coroutineScope = coroutineScope,
        lifecycle = lifecycle
    )
}