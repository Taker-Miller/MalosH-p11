package com.seba.malosh.fragments.progresos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import com.seba.malosh.fragments.desafios.DesafiosCompletadosFragment
import java.util.*

class ProgresoFragment : Fragment() {

    private lateinit var progresoSpinner: Spinner
    private lateinit var volverAlMenuButton: Button
    private lateinit var seleccionarButton: Button
    private lateinit var tituloProgresos: TextView
    private lateinit var descripcionProgresos: TextView
    private var opcionSeleccionada: String? = null
    private var metaEnProgreso = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progresos, container, false)

        progresoSpinner = view.findViewById(R.id.progresoSpinner)
        volverAlMenuButton = view.findViewById(R.id.volverAlMenuButton)
        seleccionarButton = view.findViewById(R.id.seleccionarButton)
        tituloProgresos = view.findViewById(R.id.tituloProgresos)
        descripcionProgresos = view.findViewById(R.id.descripcionProgresos)

        metaEnProgreso = verificarMetaEnProgreso()

        val opcionesProgreso = arrayOf("Progreso Meta", "Logros", "Metas Cumplidas", "Desafíos Completados")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesProgreso)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        progresoSpinner.adapter = adapter

        progresoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                opcionSeleccionada = opcionesProgreso[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        seleccionarButton.setOnClickListener {
            when (opcionSeleccionada) {
                "Progreso Meta" -> {
                    if (metaEnProgreso) {
                        val fechaInicio = obtenerFechaInicioMeta()
                        val fechaFin = obtenerFechaFinMeta()
                        val habitos = arrayListOf("Dormir a deshoras")

                        val fragment = ProgresoMetaFragment.newInstance(fechaInicio, fechaFin, habitos)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(
                            context,
                            "No hay ninguna meta en progreso. Define una meta primero.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "Logros" -> {
                    val fragment = LogrosFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "Metas Cumplidas" -> {
                    val fragment = MetasCumplidasFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "Desafíos Completados" -> {
                    val fragment = DesafiosCompletadosFragment.newInstance()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        volverAlMenuButton.setOnClickListener {
            (activity as? BienvenidaActivity)?.mostrarElementosUI()
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun verificarMetaEnProgreso(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("meta_en_progreso", false)
    }

    private fun obtenerFechaInicioMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("fecha_inicio_meta", Calendar.getInstance().timeInMillis)
    }

    private fun obtenerFechaFinMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("fecha_fin_meta", Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.timeInMillis)
    }
}
