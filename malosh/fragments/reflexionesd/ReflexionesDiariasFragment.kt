package com.seba.malosh.fragments.reflexiones

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.*


class ReflexionesDiariasFragment : Fragment() {

    private lateinit var reflexionInput: EditText
    private lateinit var guardarButton: Button
    private lateinit var listaReflexiones: ListView
    private lateinit var listaReflexionesAdapter: ArrayAdapter<String>
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val reflexionesList = mutableListOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val REFLEXIONES_PREFS = "ReflexionesPrefs"
        private const val REFLEXION_KEY_PREFIX = "reflexion_"

        fun newInstance(): ReflexionesDiariasFragment {
            return ReflexionesDiariasFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reflexiones_diarias, container, false)

        reflexionInput = view.findViewById(R.id.reflexionInput)
        guardarButton = view.findViewById(R.id.guardarButton)
        listaReflexiones = view.findViewById(R.id.listaReflexiones)

        sharedPreferences = requireContext().getSharedPreferences(REFLEXIONES_PREFS, Context.MODE_PRIVATE)

        // Configuración del adaptador para la ListView
        listaReflexionesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, reflexionesList)
        listaReflexiones.adapter = listaReflexionesAdapter

        guardarButton.setOnClickListener {
            guardarReflexion()
        }

        listaReflexiones.setOnItemClickListener { _, _, position, _ ->
            mostrarReflexion(reflexionesList[position])
        }

        mostrarReflexionesGuardadas()

        return view
    }

    private fun guardarReflexion() {
        val reflexion = reflexionInput.text.toString()

        if (reflexion.isNotEmpty()) {
            val fechaActual = dateFormat.format(Date())
            val claveReflexion = REFLEXION_KEY_PREFIX + fechaActual

            val editor = sharedPreferences.edit()
            editor.putString(claveReflexion, reflexion)
            editor.apply()

            Toast.makeText(context, "Reflexión guardada para el día $fechaActual", Toast.LENGTH_SHORT).show()
            reflexionInput.text.clear()
            reflexionesList.add(claveReflexion)  // Añadir la nueva reflexión al listado
            listaReflexionesAdapter.notifyDataSetChanged()  // Refrescar la lista
        } else {
            Toast.makeText(context, "Por favor, ingresa una reflexión antes de guardar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarReflexionesGuardadas() {
        val allEntries = sharedPreferences.all
        reflexionesList.clear()

        for ((key, _) in allEntries) {
            if (key.startsWith(REFLEXION_KEY_PREFIX)) {
                reflexionesList.add(key)
            }
        }

        listaReflexionesAdapter.notifyDataSetChanged()  // Refrescar la lista
    }

    private fun mostrarReflexion(claveReflexion: String) {
        val reflexionGuardada = sharedPreferences.getString(claveReflexion, "Reflexión no encontrada.")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reflexión del $claveReflexion")
        builder.setMessage(reflexionGuardada)
        builder.setPositiveButton("Cerrar", null)
        builder.show()
    }
}
