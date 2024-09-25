package com.seba.malosh.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seba.malosh.R
import com.seba.malosh.fragments.progresos.Logro

class LogroAdapter(private val logros: List<Logro>) : RecyclerView.Adapter<LogroAdapter.LogroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_logro, parent, false)
        return LogroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        val logro = logros[position]
        holder.bind(logro)
    }

    override fun getItemCount(): Int = logros.size

    inner class LogroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logroTitulo: TextView = itemView.findViewById(R.id.logroTitulo)
        private val logroDescripcion: TextView = itemView.findViewById(R.id.logroDescripcion)
        private val logroIcon: ImageView = itemView.findViewById(R.id.logroIcon)

        fun bind(logro: Logro) {
            logroTitulo.text = logro.titulo
            logroDescripcion.text = logro.descripcion

            // Verificar si el logro está desbloqueado o bloqueado
            if (logro.desbloqueado) {
                logroIcon.setImageResource(logro.iconoDesbloqueado) // Icono de logro desbloqueado
            } else {
                logroIcon.setImageResource(logro.iconoBloqueado) // Icono de logro bloqueado
            }
        }
    }
}
