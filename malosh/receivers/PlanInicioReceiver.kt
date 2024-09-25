package com.seba.malosh.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.seba.malosh.activities.BienvenidaActivity

class PlanInicioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Verificar si tenemos el permiso para notificaciones
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Construir la notificación
            val notificationIntent = Intent(context, BienvenidaActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(context, "plan_inicio_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Usa un icono predeterminado
                .setContentTitle("¡Tu plan ha comenzado!")
                .setContentText("Es momento de trabajar en el hábito.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            // Mostrar la notificación
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, notificationBuilder.build())
        }
    }
}
