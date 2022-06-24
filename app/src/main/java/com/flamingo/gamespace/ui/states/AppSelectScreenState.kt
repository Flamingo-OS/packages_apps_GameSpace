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
import android.database.ContentObserver
import android.provider.Settings

import androidx.annotation.GuardedBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppSelectScreenState(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val pm = context.packageManager

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            coroutineScope.launch {
                updateList()
            }
        }
    }

    private val listMutex = Mutex()

    @GuardedBy("listMutex")
    val appList = mutableStateListOf<AppInfo>()

    init {
        coroutineScope.launch {
            loadAllAppsList()
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
        listMutex.withLock {
            appList.addAll(sortedList)
        }
    }

    private suspend fun getSelectedList() =
        withContext(Dispatchers.IO) {
            Settings.System.getString(
                context.contentResolver,
                Settings.System.GAMESPACE_PACKAGE_LIST
            )?.split(DELIMITER)?.sorted() ?: emptyList()
        }

    private suspend fun updateList() {
        val selectedList = getSelectedList()
        withContext(Dispatchers.Default) {
            listMutex.withLock {
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
    }

    fun setAppSelected(appInfo: AppInfo, selected: Boolean) {
        coroutineScope.launch {
            val selectedList = getSelectedList()
            withContext(Dispatchers.IO) {
                if (selected && !selectedList.contains(appInfo.packageName)) {
                    val newList = selectedList.toMutableList()
                    newList.add(appInfo.packageName)
                    Settings.System.putString(
                        context.contentResolver,
                        Settings.System.GAMESPACE_PACKAGE_LIST,
                        newList.joinToString(DELIMITER)
                    )
                } else if (!selected && selectedList.contains(appInfo.packageName)) {
                    val newList = selectedList.toMutableList()
                    newList.remove(appInfo.packageName)
                    Settings.System.putString(
                        context.contentResolver,
                        Settings.System.GAMESPACE_PACKAGE_LIST,
                        newList.joinToString(DELIMITER)
                    )
                }
            }
        }
    }

    internal fun registerSettingsObserver() {
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.GAMESPACE_PACKAGE_LIST),
            false,
            settingsObserver
        )
    }

    internal fun unregisterSettingsObserver() {
        context.contentResolver.unregisterContentObserver(settingsObserver)
    }

    companion object {
        private const val DELIMITER = ";"
    }
}

@Composable
fun rememberAppSelectScreenState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): AppSelectScreenState {
    val state = remember(context, coroutineScope) {
        AppSelectScreenState(context = context, coroutineScope = coroutineScope)
    }
    DisposableEffect(state) {
        state.registerSettingsObserver()
        onDispose {
            state.unregisterSettingsObserver()
        }
    }
    return state
}