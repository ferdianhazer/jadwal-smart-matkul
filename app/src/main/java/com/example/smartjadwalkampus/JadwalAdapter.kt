package com.example.smartjadwalkampus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JadwalAdapter(
    private val listJadwal: MutableList<JadwalMatkul>,
    private val onDeleteClick: (position: Int) -> Unit,
    private val onEditClick: (position: Int) -> Unit // Callback baru untuk Edit
) : RecyclerView.Adapter<JadwalAdapter.JadwalViewHolder>() {

    inner class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        val tvHari: TextView = itemView.findViewById(R.id.tvHari)
        val tvNamaMatkul: TextView = itemView.findViewById(R.id.tvNamaMatkul)
        val tvWaktuDanRuang: TextView = itemView.findViewById(R.id.tvWaktuDanRuang)
        val tvDosen: TextView = itemView.findViewById(R.id.tvDosen)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        // Cari tombol Edit dari layout (jika tidak ada ImageButton khusus edit, seluruh item/kartu bisa diklik)
        val btnEdit: ImageButton? = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal, parent, false)
        return JadwalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        val item = listJadwal[position]

        holder.tvHari.text = item.hari
        holder.tvNamaMatkul.text = item.namaMatkul
        holder.tvWaktuDanRuang.text = "🕒 ${item.jam}  |  📍 ${item.ruangan}"
        holder.tvDosen.text = "👤 Dosen: ${item.dosen}"

        try {
            holder.viewColorIndicator.setBackgroundColor(Color.parseColor(item.warnaHex))
        } catch (e: Exception) {
            holder.viewColorIndicator.setBackgroundColor(Color.parseColor("#2196F3"))
        }

        // Aksi Tombol Hapus
        holder.btnDelete.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onDeleteClick(currentPos)
            }
        }

        // Aksi Edit (Jika ada ImageButton btnEdit di item_jadwal.xml)
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onEditClick(currentPos)
                }
            }
        } else {
            // Alternatif: Klik seluruh Kartu Item untuk Edit jika tidak ada tombol khusus
            holder.itemView.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onEditClick(currentPos)
                }
            }
        }
    }

    override fun getItemCount(): Int = listJadwal.size

    fun tambahJadwal(jadwal: JadwalMatkul) {
        listJadwal.add(jadwal)
        notifyItemInserted(listJadwal.size - 1)
    }

    fun hapusJadwal(position: Int) {
        if (position in 0 until listJadwal.size) {
            listJadwal.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listJadwal.size)
        }
    }
}