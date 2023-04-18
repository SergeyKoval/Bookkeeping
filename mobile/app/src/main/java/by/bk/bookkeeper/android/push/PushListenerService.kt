package by.bk.bookkeeper.android.push

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import by.bk.bookkeeper.android.util.Logger

class PushListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val log = Logger.generateLog(sbn)
        sendBroadcast(Intent(ACTION_ON_NOTIFICATION_POSTED).apply {
            putExtra(PUSH_MESSAGE_LOG, log)
        })
    }

    companion object {
        const val PUSH_TITLE_EXTRA = "PUSH_TITLE_EXTRA"
        const val PUSH_MESSAGE_EXTRA = "PUSH_MESSAGE_EXTRA"
        const val PUSH_MESSAGE_LOG = "PUSH_MESSAGE_LOG"
        const val ACTION_ON_NOTIFICATION_POSTED = "on_notification_posted"
    }

}
