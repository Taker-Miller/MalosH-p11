package com.seba.malosh.fragments.progresos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seba.malosh.R

class MetasCumplidasAdapter(private val metasCumplidasList: List<String>) :
    RecyclerView.Adapter<MetasCumplidasAdapter.MetasCumplidasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetasCumplidasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meta_cumplida, parent, false)
        return MetasCumplidasViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetasCumplidasViewHolder, position: Int) {
        val meta = metasCumplidasList[position]
        holder.metaTextView.text = meta
    }

    override fun getItemCount(): Int {
        return metasCumplidasList.size
    }

    class MetasCumplidasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val metaTextView: TextView = itemView.findViewById(R.id.metaTextView)
    }
}
