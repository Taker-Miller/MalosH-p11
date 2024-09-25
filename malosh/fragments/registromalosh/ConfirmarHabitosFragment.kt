package com.seba.malosh.fragments.registromalosh

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import com.seba.malosh.fragments.progresos.listaLogros
import com.seba.malosh.fragments.progresos.Logro
import java.util.Calendar

class ConfirmarHabitosFragment : Fragment() {

    private lateinit var siButton: Button
    private lateinit var noButton: Button
    private lateinit var selectedHabitsTextView: TextView
    private var fechaInicio: Calendar? = null  // Variable para la fecha de inicio del hábito

    companion object {
        private const val SELECTED_HABITS_KEY = "selected_habits"

        // Método para instanciar el fragmento con los hábitos seleccionados y la fecha de inicio
        fun newInstance(
            selectedHabits: ArrayList<String>,
            fechaInicio: Calendar?
        ): ConfirmarHabitosFragment {
            val fragment = ConfirmarHabitosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(SELECTED_HABITS_KEY, selectedHabits)
            fragment.arguments = bundle
            fragment.fechaInicio = fechaInicio  // Almacenar la fecha de inicio
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_confirmar_habitos, container, false)

        // Inicializar los elementos del layout
        siButton = view.findViewById(R.id.siButton)
        noButton = view.findViewById(R.id.noButton)
        selectedHabitsTextView = view.findViewById(R.id.selectedHabitsTextView)

        // Mostrar los hábitos seleccionados en el TextView
        val selectedHabits = arguments?.getStringArrayList(SELECTED_HABITS_KEY)
        selectedHabitsTextView.text = selectedHabits?.joinToString(separator = "\n")

        // Acción para el botón "Sí"
        siButton.setOnClickListener {
            val habitCount = selectedHabits?.size ?: 0

            if (habitCount >= 2 && habitCount <= 4) {
                Toast.makeText(
                    context,
                    "Malos hábitos registrados exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Actualizar la lista de hábitos en la actividad principal con la fecha de inicio
                (requireActivity() as BienvenidaActivity).updateRegisteredHabits(
                    selectedHabits ?: emptyList(), fechaInicio
                )

                // Verificar y desbloquear logros relacionados con los malos hábitos registrados
                verificarYDesbloquearLogros(selectedHabits ?: emptyList())

                // Redirigir al menú principal
                requireActivity().supportFragmentManager.popBackStack(null, 1)
            } else {
                Toast.makeText(
                    context,
                    "Debes registrar entre 2 y 4 malos hábitos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Acción para el botón "No"
        noButton.setOnClickListener {
            // Volver al fragmento de selección de hábitos
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    // Función para verificar y desbloquear los logros
    private fun verificarYDesbloquearLogros(nuevosHabitos: List<String>) {
        val sharedPreferences =
            requireContext().getSharedPreferences("LogrosPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Obtener los hábitos ya registrados de las sesiones anteriores
        val habitosRegistrados =
            sharedPreferences.getStringSet("habitos_registrados", mutableSetOf())?.toMutableSet()
                ?: mutableSetOf()

        // Agregar los nuevos hábitos a los registrados (sin afectar los logros previos)
        val habitosTotalesRegistrados = habitosRegistrados.union(nuevosHabitos).toMutableSet()

        // Guardar los hábitos actualizados en SharedPreferences
        editor.putStringSet("habitos_registrados", habitosTotalesRegistrados)

        // Obtener el número de hábitos registrados en la sesión actual
        val totalHabitosSesionActual = nuevosHabitos.size

        // Desbloquear el logro si el usuario registró al menos 2 hábitos en esta sesión
        if (totalHabitosSesionActual >= 2) {
            val logro = listaLogros.find { it.id == 2 } // "Registrando Malos Hábitos"
            if (logro != null && !logro.desbloqueado) {
                logro.desbloqueado = true
                editor.putBoolean("logro_2", true)
                Toast.makeText(context, "¡Logro Desbloqueado: ${logro.titulo}!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Desbloquear el logro si el usuario registró exactamente 4 hábitos en esta sesión
        if (totalHabitosSesionActual == 4) {
            val logro = listaLogros.find { it.id == 4 } // "Cuatro Malos Hábitos"
            if (logro != null && !logro.desbloqueado) {
                logro.desbloqueado = true
                editor.putBoolean("logro_4", true)
                Toast.makeText(context, "¡Logro Desbloqueado: ${logro.titulo}!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Guardar los cambios en SharedPreferences
        editor.apply()
    }
}
