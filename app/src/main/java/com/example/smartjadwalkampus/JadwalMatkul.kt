package com.example.smartjadwalkampus

data class JadwalMatkul(
    val id: Long = System.currentTimeMillis() + (0..1000).random(), // ID Unik otomatis untuk Alarm
    val namaMatkul: String,
    val hari: String,
    val jam: String,
    val ruangan: String,
    val dosen: String,
    val warnaHex: String
)