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

package com.flamingo.gamespace.ui

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation

import com.flamingo.gamespace.data.settings.DEFAULT_NOTIFICATION_OVERLAY_ENABLED
import com.flamingo.gamespace.data.settings.SettingsRepository
import com.flamingo.gamespace.ui.screens.AppSelectScreen
import com.flamingo.gamespace.ui.screens.MainScreen
import com.flamingo.gamespace.ui.screens.NotificationOverlayBlackListScreen
import com.flamingo.gamespace.ui.screens.NotificationOverlayScreen
import com.flamingo.gamespace.ui.screens.TilesScreen
import com.flamingo.gamespace.ui.states.NotificationOverlayScreenState
import com.flamingo.gamespace.ui.states.rememberMainScreenState
import com.flamingo.gamespace.ui.states.rememberNotificationOverlayBlackListScreenState
import com.flamingo.gamespace.ui.states.rememberNotificationOverlayScreenState
import com.flamingo.gamespace.ui.states.rememberTileScreenState
import com.flamingo.gamespace.ui.theme.GameSpaceTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

private const val TransitionAnimationDuration = 500

@AndroidEntryPoint
class GameSpaceActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GameSpaceTheme {
                val navHostController = rememberAnimatedNavController()
                val notificationOverlayScreenState =
                    rememberNotificationOverlayScreenState(settingsRepository)
                val notificationOverlayEnabled by notificationOverlayScreenState.notificationOverlayEnabled.collectAsState(
                    DEFAULT_NOTIFICATION_OVERLAY_ENABLED
                )
                val latestNotificationOverlayEnabled by rememberUpdatedState(
                    notificationOverlayEnabled
                )
                val notificationOverlayStateChangeCallback by rememberUpdatedState(newValue = { enabled: Boolean ->
                    notificationOverlayScreenState.setNotificationOverlayEnabled(enabled)
                })
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navHostController,
                        startDestination = Route.Main.MAIN_SCREEN,
                        route = Route.Main.name
                    ) {
                        mainGraph(
                            navHostController = navHostController,
                            settingsRepository = settingsRepository,
                            notificationOverlayEnabled = latestNotificationOverlayEnabled,
                            onNotificationOverlayStateChanged = notificationOverlayStateChangeCallback,
                            onFinishActivityRequest = {
                                finish()
                            }
                        )
                        notificationOverlayGraph(
                            notificationOverlayScreenState = notificationOverlayScreenState,
                            navHostController = navHostController,
                            settingsRepository = settingsRepository
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.childComposable(
    route: String,
    home: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route,
        enterTransition = {
            when (initialState.destination.route) {
                home -> slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Start,
                    tween(TransitionAnimationDuration)
                )
                else -> null
            }
        },
        popExitTransition = {
            when (targetState.destination.route) {
                home -> slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.End,
                    tween(TransitionAnimationDuration)
                )
                else -> null
            }
        },
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.mainGraph(
    navHostController: NavHostController,
    settingsRepository: SettingsRepository,
    notificationOverlayEnabled: Boolean,
    onNotificationOverlayStateChanged: (Boolean) -> Unit,
    onFinishActivityRequest: () -> Unit,
) {
    composable(
        Route.Main.MAIN_SCREEN,
        exitTransition = {
            when (targetState.destination.route) {
                Route.Main.SELECT_APPS_SCREEN,
                Route.Main.TILES_SCREEN,
                Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN -> slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Start,
                    tween(TransitionAnimationDuration)
                )
                else -> null
            }
        },
        popEnterTransition = {
            when (initialState.destination.route) {
                Route.Main.SELECT_APPS_SCREEN,
                Route.Main.TILES_SCREEN,
                Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN -> slideIntoContainer(
                    AnimatedContentScope.SlideDirection.End,
                    tween(TransitionAnimationDuration)
                )
                else -> null
            }
        },
    ) {
        MainScreen(
            modifier = Modifier.fillMaxSize(),
            onBackPressed = onFinishActivityRequest,
            navHostController = navHostController,
            state = rememberMainScreenState(settingsRepository = settingsRepository),
            notificationOverlayEnabled = notificationOverlayEnabled,
            onNotificationOverlayStateChanged = onNotificationOverlayStateChanged
        )
    }
    childComposable(
        Route.Main.SELECT_APPS_SCREEN,
        Route.Main.MAIN_SCREEN
    ) {
        AppSelectScreen(
            onBackPressed = {
                navHostController.popBackStack()
            },
            isEnterAnimationRunning = transition.currentState == EnterExitState.PreEnter
        )
    }
    childComposable(
        Route.Main.TILES_SCREEN,
        Route.Main.MAIN_SCREEN
    ) {
        TilesScreen(
            onBackPressed = {
                navHostController.popBackStack()
            },
            state = rememberTileScreenState(settingsRepository = settingsRepository)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.notificationOverlayGraph(
    notificationOverlayScreenState: NotificationOverlayScreenState,
    navHostController: NavHostController,
    settingsRepository: SettingsRepository
) {
    navigation(
        startDestination = Route.Main.MAIN_SCREEN,
        route = Route.NotificationOverlay.name
    ) {
        composable(
            Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN,
            enterTransition = {
                when (initialState.destination.route) {
                    Route.Main.MAIN_SCREEN -> slideIntoContainer(
                        AnimatedContentScope.SlideDirection.Start,
                        tween(TransitionAnimationDuration)
                    )
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Route.NotificationOverlay.NOTIFICATION_OVERLAY_BLACKLIST_SCREEN -> slideOutOfContainer(
                        AnimatedContentScope.SlideDirection.Start,
                        tween(TransitionAnimationDuration)
                    )
                    else -> null
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Route.NotificationOverlay.NOTIFICATION_OVERLAY_BLACKLIST_SCREEN -> slideIntoContainer(
                        AnimatedContentScope.SlideDirection.End,
                        tween(TransitionAnimationDuration)
                    )
                    else -> null
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    Route.Main.MAIN_SCREEN -> slideOutOfContainer(
                        AnimatedContentScope.SlideDirection.End,
                        tween(TransitionAnimationDuration)
                    )
                    else -> null
                }
            },
        ) {
            NotificationOverlayScreen(
                state = notificationOverlayScreenState,
                navHostController = navHostController
            )
        }
        childComposable(
            Route.NotificationOverlay.NOTIFICATION_OVERLAY_BLACKLIST_SCREEN,
            Route.NotificationOverlay.NOTIFICATION_OVERLAY_SCREEN
        ) {
            NotificationOverlayBlackListScreen(
                onBackButtonPressed = {
                    navHostController.popBackStack()
                },
                isEnterAnimationRunning = transition.currentState == EnterExitState.PreEnter,
                state = rememberNotificationOverlayBlackListScreenState(settingsRepository = settingsRepository)
            )
        }
    }
}

sealed class Route(val name: String) {
    object Main : Route("main") {
        const val MAIN_SCREEN = "main_screen"
        const val SELECT_APPS_SCREEN = "select_apps_screen"
        const val TILES_SCREEN = "tiles_screen"
    }

    object NotificationOverlay : Route("notification_overlay") {
        const val NOTIFICATION_OVERLAY_SCREEN = "notification_overlay_screen"
        const val NOTIFICATION_OVERLAY_BLACKLIST_SCREEN = "notification_overlay_blacklist_screen"
    }
}