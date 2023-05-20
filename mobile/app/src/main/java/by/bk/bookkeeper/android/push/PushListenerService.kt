package by.bk.bookkeeper.android.push

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PushListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        sendBroadcast(Intent(ACTION_ON_NOTIFICATION_POSTED).apply {
            putExtra(
                PUSH_MESSAGE,
                PushMessage(
                    sbn.packageName ?: "",
                    sbn.notification.extras?.getString(Notification.EXTRA_TEXT) ?: "",
                    sbn.postTime
                )
            )
        })
    }

    companion object {
        const val PUSH_MESSAGE = "PUSH_MESSAGE"
        const val ACTION_ON_NOTIFICATION_POSTED = "on_notification_posted"
    }

}
