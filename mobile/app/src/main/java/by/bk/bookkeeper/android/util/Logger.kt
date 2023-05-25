package by.bk.bookkeeper.android.util

import android.app.Notification
import android.service.notification.StatusBarNotification


object Logger {

    fun generateLog(sbn: StatusBarNotification): String {
        val key = sbn.key
        val id = sbn.id
        val postTime = sbn.postTime
        val packageName = sbn.packageName ?: null
        val tickerText = sbn.notification?.tickerText
        val title = sbn.notification.extras?.getString(Notification.EXTRA_TITLE)
        val text = sbn.notification.extras?.getString(Notification.EXTRA_TEXT)
        return "Notification: key = $key || id = $id || package = $packageName || " +
                "postTime = $postTime || tickerText = $tickerText || " +
                "title = $title || text = $text "
    }

}