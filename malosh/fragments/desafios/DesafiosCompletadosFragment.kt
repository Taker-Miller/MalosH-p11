package com.seba.malosh.fragments.desafios

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.*

class DesafiosCompletadosFragment : Fragment() {

    private lateinit var listaDesafios: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_completados, container, false)

        listaDesafios = view.findViewById(R.id.listaDesafiosCompletados)

        val desafiosCompletados = obtenerDesafiosCompletados()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, desafiosCompletados)
        listaDesafios.adapter = adapter

        return view
    }

    private fun obtenerDesafiosCompletados(): List<String> {
        val sharedPreferences = requireContext().getSharedPreferences("DesafiosCompletados", Context.MODE_PRIVATE)
        val desafiosCompletados = mutableListOf<String>()

        val contador = sharedPreferences.getInt("contador_desafios", 0)
        val formatter = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

        for (i in 0 until contador) {
            val dia = sharedPreferences.getString("fecha_$i", null)
            val desafio = sharedPreferences.getString("desafio_$i", null)

            if (dia != null && desafio != null) {
                desafiosCompletados.add("$dia: $desafio")
            }
        }

        if (desafiosCompletados.isEmpty()) {
            desafiosCompletados.add("Aún no has completado desafíos.")
        }

        return desafiosCompletados
    }

    companion object {
        fun newInstance(): DesafiosCompletadosFragment {
            return DesafiosCompletadosFragment()
        }
    }
}
