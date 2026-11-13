package com.example.smartjadwalkampus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val namaMatkul = intent.getStringExtra("NAMA_MATKUL") ?: "Mata Kuliah"
        val ruangan = intent.getStringExtra("RUANGAN") ?: "-"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "jadwal_kampus_channel"

        // Buat Notification Channel untuk Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Jadwal Kuliah",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat jadwal perkuliahan"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Suara Alarm
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Buat Notifikasi
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ Waktunya Kuliah: $namaMatkul")
            .setContentText("Jangan lupa kuliah di $ruangan sekarang!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}