package com.seba.malosh.fragments.progresos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seba.malosh.R

class MetasCumplidasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var metasCumplidasAdapter: MetasCumplidasAdapter
    private var metasCumplidasList: List<String> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_metas_cumplidas, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMetasCumplidas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        metasCumplidasList = obtenerMetasCumplidas()

        metasCumplidasAdapter = MetasCumplidasAdapter(metasCumplidasList)
        recyclerView.adapter = metasCumplidasAdapter

        return view
    }

    private fun obtenerMetasCumplidas(): List<String> {
        return listOf(
            "Plan de seguimiento completado para: Poco Ejercicio"
        )
    }
}
