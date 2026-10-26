package com.example.financeflow.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.financeflow.MainActivity
import com.example.financeflow.R
import com.example.financeflow.domain.model.BillReminder
import com.example.financeflow.domain.model.CreditCard
import com.example.financeflow.ui.components.Formatters
import java.util.Calendar

object PaymentReminderManager {
    const val CHANNEL_ID = "payment_and_service_alerts"
    const val CHANNEL_NAME = "Avisos de Pagos y Servicios"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Notificaciones automáticas para avisar vencimientos de pago de tarjetas de crédito y servicios"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Verifica tarjetas de crédito con deuda y servicios pendientes por pagar.
     * Envía notificaciones locales si el vencimiento es hoy o en los próximos 3 días.
     */
    fun checkAndNotifyDuePayments(
        context: Context,
        creditCards: List<CreditCard>,
        billReminders: List<BillReminder>
    ) {
        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val maxDayInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // 1. Avisos de Pago de Tarjetas de Crédito
        creditCards.filter { it.currentDebt > 0 }.forEach { card ->
            val daysToPayment = if (card.paymentDueDay >= todayDay) {
                card.paymentDueDay - todayDay
            } else {
                (maxDayInMonth - todayDay) + card.paymentDueDay
            }

            if (daysToPayment in 0..3) {
                val title = if (daysToPayment == 0) {
                    "⚠️ ¡HOY Vence Pago de Tarjeta!"
                } else {
                    "⚠️ Próximo Pago: ${card.cardName}"
                }

                val debtFormatted = Formatters.formatCurrency(card.currentDebt)
                val text = if (daysToPayment == 0) {
                    "Tu tarjeta ${card.cardName} ($debtFormatted) vence HOY. ¡Págala para evitar comisiones e intereses!"
                } else {
                    "Tu tarjeta ${card.cardName} ($debtFormatted) vence en $daysToPayment días (Día ${card.paymentDueDay})."
                }

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(("card_" + card.id).hashCode(), notification)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Avisos de Pago de Servicios Programados
        billReminders.filter { !it.isPaid }.forEach { bill ->
            val daysToDue = if (bill.dueDay >= todayDay) {
                bill.dueDay - todayDay
            } else {
                (maxDayInMonth - todayDay) + bill.dueDay
            }

            if (daysToDue in 0..3) {
                val title = if (daysToDue == 0) {
                    "🔔 ¡HOY Vence Servicio: ${bill.title}!"
                } else {
                    "🔔 Recordatorio de Servicio: ${bill.title}"
                }

                val amountFormatted = Formatters.formatCurrency(bill.amount)
                val text = if (daysToDue == 0) {
                    "El servicio ${bill.title} ($amountFormatted) vence HOY. No olvides realizar el pago a tiempo."
                } else {
                    "El servicio ${bill.title} ($amountFormatted) vence en $daysToDue días (Día ${bill.dueDay})."
                }

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(("bill_" + bill.id).hashCode(), notification)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
