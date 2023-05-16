package by.bk.bookkeeper.android.push

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import by.bk.bookkeeper.android.R


object PushPermissionHelper {

    private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

    fun isNotificationServiceEnabled(context: Context): Boolean = Settings.Secure
        .getString(context.contentResolver, ENABLED_NOTIFICATION_LISTENERS)?.contains(context.packageName) == true

    fun buildPushAccessSettingsPrompt(context: Context): AlertDialog = AlertDialog.Builder(context).apply {
        setTitle(R.string.notification_listener_service_title)
        setMessage(R.string.notification_listener_service_explanation)
        setNegativeButton(android.R.string.cancel) { _, _ -> }
        setPositiveButton(android.R.string.ok) { _, _ ->
            context.startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            })
        }
    }.create()
}