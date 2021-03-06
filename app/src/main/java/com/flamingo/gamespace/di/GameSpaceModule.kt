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

package com.flamingo.gamespace.di

import android.content.Context
import android.os.IThermalService
import android.os.ServiceManager

import com.flamingo.gamespace.data.settings.SettingsRepository

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val GameSpaceModule = module {
    single {
        IThermalService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.THERMAL_SERVICE))
    }
    single {
        SettingsRepository(context = androidContext())
    }
}