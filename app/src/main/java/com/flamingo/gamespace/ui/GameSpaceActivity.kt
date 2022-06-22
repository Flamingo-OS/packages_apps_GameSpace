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
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

import com.flamingo.gamespace.ui.screens.AppSelectScreen
import com.flamingo.gamespace.ui.screens.MainScreen
import com.flamingo.gamespace.ui.theme.GameSpaceTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private const val TransitionAnimationDuration = 500

class GameSpaceActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GameSpaceTheme {
                val systemUiController = rememberSystemUiController()
                val surfaceColor = MaterialTheme.colorScheme.surface
                LaunchedEffect(surfaceColor) {
                    systemUiController.setNavigationBarColor(
                        Color.Transparent,
                        navigationBarContrastEnforced = false
                    )
                }
                val navHostController = rememberAnimatedNavController()
                AnimatedNavHost(
                    navController = navHostController,
                    startDestination = Routes.MAIN
                ) {
                    composable(
                        Routes.MAIN,
                        exitTransition = {
                            when (targetState.destination.route) {
                                Routes.SELECT_APPS -> slideOutOfContainer(
                                    AnimatedContentScope.SlideDirection.Start,
                                    tween(TransitionAnimationDuration)
                                )
                                else -> null
                            }
                        },
                        popEnterTransition = {
                            when (initialState.destination.route) {
                                Routes.SELECT_APPS -> slideIntoContainer(
                                    AnimatedContentScope.SlideDirection.End,
                                    tween(TransitionAnimationDuration)
                                )
                                else -> null
                            }
                        },
                    ) {
                        MainScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackPressed = {
                                finish()
                            },
                            onSelectAppsScreenOpenRequest = {
                                navHostController.navigate(Routes.SELECT_APPS)
                            },
                            systemUiController = systemUiController
                        )
                    }
                    composable(
                        Routes.SELECT_APPS,
                        enterTransition = {
                            when (initialState.destination.route) {
                                Routes.MAIN -> slideIntoContainer(
                                    AnimatedContentScope.SlideDirection.Start,
                                    tween(TransitionAnimationDuration)
                                )
                                else -> null
                            }
                        },
                        popExitTransition = {
                            when (targetState.destination.route) {
                                Routes.MAIN -> slideOutOfContainer(
                                    AnimatedContentScope.SlideDirection.End,
                                    tween(TransitionAnimationDuration)
                                )
                                else -> null
                            }
                        }
                    ) {
                        AppSelectScreen(
                            onBackPressed = {
                                navHostController.popBackStack()
                            },
                            systemUiController = systemUiController,
                            isEnterAnimationRunning = transition.currentState == EnterExitState.PreEnter
                        )
                    }
                }
            }
        }
    }
}

object Routes {
    const val MAIN = "main"
    const val SELECT_APPS = "select_apps"
}