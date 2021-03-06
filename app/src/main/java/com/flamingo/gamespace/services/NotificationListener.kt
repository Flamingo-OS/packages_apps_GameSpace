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

package com.flamingo.gamespace.services

import android.app.Notification
import android.app.Notification.CallStyle
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    private var onNotificationPosted: ((Int, String) -> Unit)? = null
    private var onNotificationRemoved: ((Int) -> Unit)? = null

    private var blackList = emptyList<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationPosted(sbn, rankingMap)
        if (sbn.isContentSecure || blackList.contains(sbn.packageName)) return
        if (!isImportantNotification(sbn) && (!sbn.isClearable || sbn.isOngoing)) return

        val ranking = Ranking()
        if (rankingMap.getRanking(
                sbn.key,
                ranking
            ) && ranking.importance < NotificationManager.IMPORTANCE_DEFAULT
        ) {
            return
        }

        val extras = sbn.notification.extras
        var notificationText = ""
        val title = extras.getString(Notification.EXTRA_TITLE)
            ?: extras.getString(Notification.EXTRA_TITLE_BIG)
        if (title != null) {
            notificationText += "[$title]"
        }
        val text = extras.getString(Notification.EXTRA_TEXT)
        if (text?.isNotBlank() == true) {
            notificationText += if (title != null) {
                ": $text"
            } else {
                "$text"
            }
        }
        onNotificationPosted?.invoke(sbn.id, notificationText)
    }

    private fun isImportantNotification(sbn: StatusBarNotification): Boolean =
        sbn.notification.isStyle(CallStyle::class.java) ||
                sbn.notification.category == Notification.CATEGORY_CALL ||
                sbn.notification.category == Notification.CATEGORY_ALARM

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationRemoved(sbn, rankingMap)
        onNotificationRemoved?.invoke(sbn.id)
    }

    fun registerCallbacks(
        onNotificationPosted: (Int, String) -> Unit,
        onNotificationRemoved: (Int) -> Unit
    ) {
        this.onNotificationPosted = onNotificationPosted
        this.onNotificationRemoved = onNotificationRemoved
    }

    fun unregisterCallbacks() {
        onNotificationPosted = null
        onNotificationRemoved = null
    }

    fun setBlackList(list: List<String>) {
        blackList = list
    }
}