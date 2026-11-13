package com.example.smartjadwalkampus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: JadwalAdapter
    private var dataJadwal = mutableListOf<JadwalMatkul>()

    private lateinit var alarmScheduler: AlarmScheduler
    private val urutanHari = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    private val PREF_NAME = "JadwalKampusPref"
    private val KEY_JADWAL = "key_daftar_jadwal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmScheduler = AlarmScheduler(this)
        mintaIzinNotifikasi()

        val rvJadwal: RecyclerView = findViewById(R.id.rvJadwal)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)

        // 1. Muat data tersimpan
        muatDataJadwal()

        // 2. Setup Adapter
        adapter = JadwalAdapter(
            listJadwal = dataJadwal,
            onDeleteClick = { position ->
                if (position in dataJadwal.indices) {
                    val itemDihapus = dataJadwal[position]
                    alarmScheduler.hapusAlarm(itemDihapus)

                    adapter.hapusJadwal(position)
                    simpanDataJadwal() // Simpan perubahan permanen

                    Toast.makeText(this, "Jadwal dan alarm berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            },
            onEditClick = { position ->
                if (position in dataJadwal.indices) {
                    showEditJadwalDialog(position)
                }
            }
        )

        rvJadwal.layoutManager = LinearLayoutManager(this)
        rvJadwal.adapter = adapter

        fabAdd.setOnClickListener {
            showTambahJadwalDialog()
        }
    }

    // --- PENYIMPANAN DAN PEMBACAAN DATA AMAN ---
    private fun simpanDataJadwal() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val jsonString = Gson().toJson(dataJadwal)
        editor.putString(KEY_JADWAL, jsonString)
        editor.apply()
    }

    private fun muatDataJadwal() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString(KEY_JADWAL, null)

        dataJadwal.clear()

        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<MutableList<JadwalMatkul>>() {}.type
                val listTersimpan: MutableList<JadwalMatkul>? = Gson().fromJson(jsonString, type)

                if (!listTersimpan.isNullOrEmpty()) {
                    dataJadwal.addAll(listTersimpan)
                } else {
                    initDummyData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                initDummyData()
            }
        } else {
            // Pertama kali app dibuka
            initDummyData()
            simpanDataJadwal()
        }

        urutkanJadwal()
    }

    private fun initDummyData() {
        val dummyList = listOf(
            JadwalMatkul(namaMatkul = "Struktur Data", hari = "Senin", jam = "08:30 - 10:10", ruangan = "Ruang A301", dosen = "Dr. Budi Santoso", warnaHex = dapatkanWarnaHari("Senin")),
            JadwalMatkul(namaMatkul = "Pengenalan Pola", hari = "Senin", jam = "12:15 - 14:00", ruangan = "C23", dosen = "Pak Supri", warnaHex = dapatkanWarnaHari("Senin")),
            JadwalMatkul(namaMatkul = "IMK", hari = "Selasa", jam = "07:30 - 09:00", ruangan = "R2", dosen = "Pak Anton", warnaHex = dapatkanWarnaHari("Selasa")),
            JadwalMatkul(namaMatkul = "Algoritma Pemrograman", hari = "Selasa", jam = "10:20 - 12:00", ruangan = "Ruang B105", dosen = "Ir. Mega Wijaya", warnaHex = dapatkanWarnaHari("Selasa")),
            JadwalMatkul(namaMatkul = "Basis Data", hari = "Rabu", jam = "13:00 - 14:40", ruangan = "Lab Komputer 2", dosen = "Anita Sari, M.T.", warnaHex = dapatkanWarnaHari("Rabu"))
        )

        for (jadwal in dummyList) {
            dataJadwal.add(jadwal)
            alarmScheduler.pasangAlarm(jadwal)
        }
    }

    private fun dapatkanWarnaHari(hari: String): String {
        return when (hari.lowercase()) {
            "senin" -> "#FFC107"
            "selasa" -> "#4CAF50"
            "rabu" -> "#FF9800"
            "kamis" -> "#2196F3"
            "jumat" -> "#9C27B0"
            "sabtu" -> "#E91E63"
            "minggu" -> "#F44336"
            else -> "#607D8B"
        }
    }

    private fun urutkanJadwal() {
        dataJadwal.sortWith(compareBy(
            { jadwal ->
                val indexHari = urutanHari.indexOfFirst { it.equals(jadwal.hari, ignoreCase = true) }
                if (indexHari != -1) indexHari else 99
            },
            { jadwal -> jadwal.jam.trim() }
        ))
    }

    private fun showTambahJadwalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambah Jadwal Kuliah")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_jadwal, null)
        val etHari = view.findViewById<EditText>(R.id.etHari)
        val etNama = view.findViewById<EditText>(R.id.etNamaMatkul)
        val etJam = view.findViewById<EditText>(R.id.etJam)
        val etRuang = view.findViewById<EditText>(R.id.etRuang)
        val etDosen = view.findViewById<EditText>(R.id.etDosen)

        builder.setView(view)
        builder.setPositiveButton("Simpan") { _, _ ->
            val rawHari = etHari.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val jam = etJam.text.toString().trim()
            val ruang = etRuang.text.toString().trim()
            val dosen = etDosen.text.toString().trim()

            val hariFormatted = if (rawHari.isNotEmpty()) {
                rawHari.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                "Senin"
            }

            if (nama.isNotEmpty() && jam.isNotEmpty()) {
                val warnaSesuaiHari = dapatkanWarnaHari(hariFormatted)

                val jadwalBaru = JadwalMatkul(
                    namaMatkul = nama,
                    hari = hariFormatted,
                    jam = jam,
                    ruangan = ruang,
                    dosen = dosen,
                    warnaHex = warnaSesuaiHari
                )

                dataJadwal.add(jadwalBaru)
                urutkanJadwal()
                adapter.notifyDataSetChanged()

                alarmScheduler.pasangAlarm(jadwalBaru)
                simpanDataJadwal()

                Toast.makeText(this, "Jadwal & Alarm berhasil dibuat!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nama matkul dan jam wajib diisi!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showEditJadwalDialog(position: Int) {
        val jadwalLama = dataJadwal[position]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Jadwal Kuliah")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_jadwal, null)
        val etHari = view.findViewById<EditText>(R.id.etHari)
        val etNama = view.findViewById<EditText>(R.id.etNamaMatkul)
        val etJam = view.findViewById<EditText>(R.id.etJam)
        val etRuang = view.findViewById<EditText>(R.id.etRuang)
        val etDosen = view.findViewById<EditText>(R.id.etDosen)

        etHari.setText(jadwalLama.hari)
        etNama.setText(jadwalLama.namaMatkul)
        etJam.setText(jadwalLama.jam)
        etRuang.setText(jadwalLama.ruangan)
        etDosen.setText(jadwalLama.dosen)

        builder.setView(view)
        builder.setPositiveButton("Update") { _, _ ->
            val rawHari = etHari.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val jam = etJam.text.toString().trim()
            val ruang = etRuang.text.toString().trim()
            val dosen = etDosen.text.toString().trim()

            val hariFormatted = if (rawHari.isNotEmpty()) {
                rawHari.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                jadwalLama.hari
            }

            if (nama.isNotEmpty() && jam.isNotEmpty()) {
                alarmScheduler.hapusAlarm(jadwalLama)

                val jadwalDiperbarui = JadwalMatkul(
                    id = jadwalLama.id,
                    namaMatkul = nama,
                    hari = hariFormatted,
                    jam = jam,
                    ruangan = ruang,
                    dosen = dosen,
                    warnaHex = dapatkanWarnaHari(hariFormatted)
                )

                // Cari ulang indeks item berdasarkan ID agar tidak salah timpa jika posisi berubah
                val targetIndex = dataJadwal.indexOfFirst { it.id == jadwalLama.id }
                if (targetIndex != -1) {
                    dataJadwal[targetIndex] = jadwalDiperbarui
                } else {
                    dataJadwal[position] = jadwalDiperbarui
                }

                urutkanJadwal()
                adapter.notifyDataSetChanged()

                alarmScheduler.pasangAlarm(jadwalDiperbarui)
                simpanDataJadwal()

                Toast.makeText(this, "Jadwal & Alarm berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nama matkul dan jam wajib diisi!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun mintaIzinNotifikasi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}