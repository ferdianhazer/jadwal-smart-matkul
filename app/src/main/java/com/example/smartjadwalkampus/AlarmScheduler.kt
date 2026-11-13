package com.example.smartjadwalkampus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    // Fungsi untuk memasang alarm
    fun pasangAlarm(jadwal: JadwalMatkul) {
        val calendar = hitungWaktuAlarm(jadwal.hari, jadwal.jam) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("NAMA_MATKUL", jadwal.namaMatkul)
            putExtra("RUANGAN", jadwal.ruangan)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            jadwal.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set Alarm Tepat Waktu
        try {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Fungsi untuk menghapus alarm saat jadwal dihapus
    fun hapusAlarm(jadwal: JadwalMatkul) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            jadwal.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
        }
    }

    // Menghitung tanggal & jam alarm terdekat
    private fun hitungWaktuAlarm(hariStr: String, jamStr: String): Calendar? {
        val targetDay = when (hariStr.lowercase()) {
            "senin" -> Calendar.MONDAY
            "selasa" -> Calendar.TUESDAY
            "rabu" -> Calendar.WEDNESDAY
            "kamis" -> Calendar.THURSDAY
            "jumat" -> Calendar.FRIDAY
            "sabtu" -> Calendar.SATURDAY
            "minggu" -> Calendar.SUNDAY
            else -> return null
        }

        // Ambil jam awal dari teks (contoh "10:00 - 12:00" dipisah jadi "10:00")
        val jamAwal = jamStr.split("-")[0].trim()
        val parts = jamAwal.split(":")
        if (parts.size < 2) return null

        val jam = parts[0].trim().toIntOrNull() ?: return null
        val menit = parts[1].trim().toIntOrNull() ?: return null

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, jam)
            set(Calendar.MINUTE, menit)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Ubah hari sesuai target hari
        while (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
            calendar.add(Calendar.DATE, 1)
        }

        // Jika waktu untuk minggu ini sudah lewat, jadwalkan untuk minggu depan
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 7)
        }

        return calendar
    }
}