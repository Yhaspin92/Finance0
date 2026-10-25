package com.example.financeflow.data.notification

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.financeflow.data.repository.FinanceRepository

class BankNotificationListenerService : NotificationListenerService() {

    private val tag = "BankNotifyService"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: ""

        if (title.isBlank() && text.isBlank()) return

        Log.d(tag, "Notification received from [$packageName]: $title - $text")

        // Filtrar notificaciones propias de la app para no crear bucles
        if (packageName == applicationContext.packageName) return

        try {
            val repository = FinanceRepository.getInstance(applicationContext)
            repository.processNotification(packageName, title, text)
        } catch (e: Exception) {
            Log.e(tag, "Error processing notification: ${e.message}", e)
        }
    }

    companion object {
        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!flat.isNullOrEmpty()) {
                val names = flat.split(":")
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && cn.packageName == packageName) {
                        return true
                    }
                }
            }
            return false
        }

        fun openNotificationAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
