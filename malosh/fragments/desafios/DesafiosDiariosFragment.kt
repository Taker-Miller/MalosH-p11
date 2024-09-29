package com.seba.malosh.fragments.desafios

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import java.util.concurrent.TimeUnit

class DesafiosDiariosFragment : Fragment() {

    private lateinit var contenedorDesafios: LinearLayout
    private lateinit var volverButton: Button
    private lateinit var aceptarDesafioButton: Button
    private lateinit var cancelarDesafioButton: Button
    private lateinit var desafioDescripcion: TextView
    private lateinit var temporizadorTextView: TextView

    // Checkboxes para el progreso
    private lateinit var inicioCheckBox: CheckBox
    private lateinit var enProgresoCheckBox: CheckBox
    private lateinit var casiPorTerminarCheckBox: CheckBox
    private lateinit var completadoCheckBox: CheckBox

    private val desafiosList = mutableListOf<String>()
    private var currentDesafio: String? = null
    private var desafioEnProgreso = false
    private val handler = Handler()
    private var tiempoRestante: Long = 0
    private var temporizadorHandler = Handler()
    private var temporizadorRunnable: Runnable? = null

    private lateinit var registeredHabits: ArrayList<String>

    companion object {
        private const val HABITOS_KEY = "habitos_registrados"
        private const val TEMPORIZADOR_INICIO_KEY = "temporizador_inicio"
        private const val TEMPORIZADOR_DURACION = 60000L // 60 segundos en milisegundos (1 minuto)
        private const val TEMPORIZADOR_ESPERA = 20000L // 20 segundos en milisegundos

        fun newInstance(habits: ArrayList<String>): DesafiosDiariosFragment {
            val fragment = DesafiosDiariosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(HABITOS_KEY, habits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_diarios, container, false)

        // Inicializar los elementos de la vista
        contenedorDesafios = view.findViewById(R.id.contenedorDesafios)
        volverButton = view.findViewById(R.id.volverButton)
        aceptarDesafioButton = view.findViewById(R.id.aceptarDesafioButton)
        cancelarDesafioButton = view.findViewById(R.id.cancelarDesafioButton)
        desafioDescripcion = view.findViewById(R.id.desafioDescripcion)
        temporizadorTextView = view.findViewById(R.id.temporizadorTextView)

        // Inicializar los checkboxes
        inicioCheckBox = view.findViewById(R.id.inicioCheckBox)
        enProgresoCheckBox = view.findViewById(R.id.enProgresoCheckBox)
        casiPorTerminarCheckBox = view.findViewById(R.id.casiPorTerminarCheckBox)
        completadoCheckBox = view.findViewById(R.id.completadoCheckBox)

        // Obtener los hábitos registrados desde el argumento que se pasó al crear el fragmento
        registeredHabits = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        // Restablecer el estado visual de los checkboxes y botones al entrar en el fragment
        limpiarEstadoCheckBoxes()
        setCheckBoxesVisibility(View.GONE) // Asegurarse de que los checkboxes estén ocultos
        aceptarDesafioButton.visibility = View.VISIBLE
        aceptarDesafioButton.isEnabled = true
        cancelarDesafioButton.visibility = View.GONE
        temporizadorTextView.visibility = View.GONE

        // Verificar si hay un temporizador en progreso
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val inicioTemporizador = sharedPreferences.getLong(TEMPORIZADOR_INICIO_KEY, 0L)
        currentDesafio = obtenerDesafioEnProgreso(requireContext()) // Obtener el desafío actual en progreso

        if (inicioTemporizador > 0L && currentDesafio != null) {
            // Reanudar el temporizador del desafío en progreso
            reanudarTemporizador(inicioTemporizador)
            mostrarDesafioEnProgreso()
        } else {
            // Si no hay temporizador en progreso, generar un nuevo desafío
            generarDesafiosSiEsNecesario()
        }

        // Manejo de click en el botón Aceptar Desafío
        aceptarDesafioButton.setOnClickListener {
            aceptarDesafio()
        }

        // Manejo de click en el botón Cancelar Desafío
        cancelarDesafioButton.setOnClickListener {
            cancelarDesafio()
        }

        // Manejo del botón Volver
        volverButton.setOnClickListener {
            (activity as? BienvenidaActivity)?.mostrarElementosUI()
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun iniciarTemporizadorEspera() {
        temporizadorTextView.visibility = View.VISIBLE
        desafioDescripcion.visibility = View.GONE // Ocultar el desafío en progreso mientras se espera el próximo desafío
        temporizadorHandler.removeCallbacksAndMessages(null)

        temporizadorRunnable = object : Runnable {
            var tiempoRestante = TEMPORIZADOR_ESPERA

            override fun run() {
                if (tiempoRestante > 0) {
                    temporizadorTextView.text = "Próximo desafío disponible en: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos"
                    tiempoRestante -= 1000
                    temporizadorHandler.postDelayed(this, 1000)
                } else {
                    temporizadorTextView.visibility = View.GONE
                    desafioDescripcion.visibility = View.VISIBLE // Mostrar el desafío cuando termine el temporizador
                    generarDesafiosSiEsNecesario()
                }
            }
        }
        temporizadorHandler.post(temporizadorRunnable!!)
    }

    private fun cancelarDesafio() {
        // Detener cualquier temporizador actual
        handler.removeCallbacksAndMessages(null)

        currentDesafio = null
        desafioEnProgreso = false
        limpiarEstadoCheckBoxes()

        setCheckBoxesVisibility(View.GONE)
        desafioDescripcion.text = "Próximo desafío disponible."

        // Limpiar el estado guardado en SharedPreferences para el temporizador
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("inicio_desafio")  // Eliminar el tiempo de inicio
        editor.apply()

        // Utiliza la función guardarDesafioEnProgreso para actualizar el estado del desafío
        guardarDesafioEnProgreso(requireContext(), null, false)

        // Mostrar la interfaz para aceptar un nuevo desafío
        aceptarDesafioButton.visibility = View.VISIBLE
        aceptarDesafioButton.isEnabled = true

        iniciarTemporizadorEspera()
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val inicioDesafio = sharedPreferences.getLong("inicio_desafio", 0L)
        val desafioEnProgreso = sharedPreferences.getBoolean("desafio_en_progreso", false)
        val desafioGuardado = sharedPreferences.getString("desafio_actual", null)

        if (desafioEnProgreso && inicioDesafio > 0 && desafioGuardado != null) {
            // Recuperar el desafío guardado y mostrarlo
            currentDesafio = desafioGuardado
            desafioDescripcion.text = currentDesafio
            desafioDescripcion.visibility = View.VISIBLE

            // Calcular el tiempo restante del desafío
            val tiempoActual = System.currentTimeMillis()
            val tiempoRestante = TEMPORIZADOR_DURACION - (tiempoActual - inicioDesafio)

            if (tiempoRestante > 0) {
                desafioDescripcion.text = "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos"
                reanudarTemporizador(inicioDesafio)
            } else {
                // Si el tiempo ha terminado, completar el desafío
                validarDesafioCompletado()
            }
        } else {
            // No hay desafío en progreso, restablecer la interfaz para permitir aceptar un nuevo desafío
            desafioDescripcion.visibility = View.GONE
            aceptarDesafioButton.visibility = View.VISIBLE
            aceptarDesafioButton.isEnabled = true
            setCheckBoxesVisibility(View.GONE)
            iniciarTemporizadorEspera()
        }
    }

    private fun validarDesafioCompletado() {
        if (inicioCheckBox.isChecked && enProgresoCheckBox.isChecked &&
            casiPorTerminarCheckBox.isChecked && completadoCheckBox.isChecked) {
            Toast.makeText(context, "¡Desafío completado exitosamente!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Desafío fallido. No completaste todas las etapas.", Toast.LENGTH_SHORT).show()
        }

        setCheckBoxesVisibility(View.GONE)
        limpiarEstadoCheckBoxes()

        aceptarDesafioButton.visibility = View.VISIBLE
        aceptarDesafioButton.isEnabled = true

        iniciarTemporizadorEspera()
    }

    private fun iniciarTemporizador1Minuto() {
        temporizadorTextView.visibility = View.GONE // Ocultar el temporizador de espera
        desafioDescripcion.visibility = View.VISIBLE // Mostrar el desafío en progreso

        // Código existente para iniciar el temporizador del desafío
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val tiempoInicio = System.currentTimeMillis()
        editor.putLong(TEMPORIZADOR_INICIO_KEY, tiempoInicio)
        editor.apply()

        setCheckBoxesVisibility(View.VISIBLE)
        resetCheckBoxes()

        reanudarTemporizador(tiempoInicio)
    }

    private fun reanudarTemporizador(tiempoInicio: Long) {
        val tiempoActual = System.currentTimeMillis()
        val tiempoRestante = TEMPORIZADOR_DURACION - (tiempoActual - tiempoInicio)

        if (tiempoRestante > 0) {
            aceptarDesafioButton.visibility = View.GONE
            cancelarDesafioButton.visibility = View.VISIBLE

            desafioDescripcion.text = "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
            setCheckBoxesVisibility(View.VISIBLE)

            handler.postDelayed(object : Runnable {
                var tiempoRestanteActualizado = tiempoRestante
                override fun run() {
                    if (tiempoRestanteActualizado > 0) {
                        tiempoRestanteActualizado -= 1000
                        desafioDescripcion.text = "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestanteActualizado)} segundos."
                        actualizarCheckBoxes(tiempoRestanteActualizado)
                        handler.postDelayed(this, 1000)
                    } else {
                        validarDesafioCompletado()
                    }
                }
            }, 1000)
        } else {
            validarDesafioCompletado()
        }
    }

    private fun actualizarCheckBoxes(tiempoRestante: Long) {
        val porcentajeRestante = 100 - ((tiempoRestante.toDouble() / TEMPORIZADOR_DURACION) * 100).toInt()

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (porcentajeRestante >= 25) {
            inicioCheckBox.isEnabled = true
            editor.putBoolean("inicio_check", inicioCheckBox.isChecked)
        }
        if (porcentajeRestante >= 50) {
            enProgresoCheckBox.isEnabled = true
            editor.putBoolean("en_progreso_check", enProgresoCheckBox.isChecked)
        }
        if (porcentajeRestante >= 75) {
            casiPorTerminarCheckBox.isEnabled = true
            editor.putBoolean("casi_terminado_check", casiPorTerminarCheckBox.isChecked)
        }
        if (porcentajeRestante >= 90) {
            completadoCheckBox.isEnabled = true
            editor.putBoolean("completado_check", completadoCheckBox.isChecked)
        }

        editor.apply()
    }

    private fun resetCheckBoxes() {
        inicioCheckBox.isChecked = false
        enProgresoCheckBox.isChecked = false
        casiPorTerminarCheckBox.isChecked = false
        completadoCheckBox.isChecked = false

        inicioCheckBox.isEnabled = false
        enProgresoCheckBox.isEnabled = false
        casiPorTerminarCheckBox.isEnabled = false
        completadoCheckBox.isEnabled = false
    }

    private fun limpiarEstadoCheckBoxes() {
        inicioCheckBox.isChecked = false
        enProgresoCheckBox.isChecked = false
        casiPorTerminarCheckBox.isChecked = false
        completadoCheckBox.isChecked = false

        inicioCheckBox.isEnabled = false
        enProgresoCheckBox.isEnabled = false
        casiPorTerminarCheckBox.isEnabled = false
        completadoCheckBox.isEnabled = false
    }

    private fun setCheckBoxesVisibility(visibility: Int) {
        val progresoChecklist = view?.findViewById<LinearLayout>(R.id.progresoChecklist)
        progresoChecklist?.visibility = visibility
    }

    private fun mostrarDesafioEnProgreso() {
        aceptarDesafioButton.isEnabled = false
        cancelarDesafioButton.visibility = View.VISIBLE

        contenedorDesafios.removeAllViews()
        val textView = TextView(context).apply {
            text = "Desafío en progreso: $currentDesafio"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.white))
        }
        contenedorDesafios.addView(textView)

        setCheckBoxesVisibility(View.VISIBLE)
        actualizarCheckBoxesRestaurados()
    }

    private fun actualizarCheckBoxesRestaurados() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)

        inicioCheckBox.isChecked = sharedPreferences.getBoolean("inicio_check", false)
        enProgresoCheckBox.isChecked = sharedPreferences.getBoolean("en_progreso_check", false)
        casiPorTerminarCheckBox.isChecked = sharedPreferences.getBoolean("casi_terminado_check", false)
        completadoCheckBox.isChecked = sharedPreferences.getBoolean("completado_check", false)

        setCheckBoxesVisibility(View.VISIBLE)
    }

    private fun aceptarDesafio() {
        if (desafioEnProgreso) {
            Toast.makeText(context, "Ya tienes un desafío en progreso. Finaliza o cancela el desafío actual primero.", Toast.LENGTH_SHORT).show()
        } else {
            desafioEnProgreso = true
            val tiempoInicio = System.currentTimeMillis()

            // Guarda el desafío actual y el tiempo de inicio en SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putLong("inicio_desafio", tiempoInicio) // Guardar el tiempo de inicio
            editor.apply()

            // Utiliza la función guardarDesafioEnProgreso para guardar el desafío aceptado
            guardarDesafioEnProgreso(requireContext(), currentDesafio, true)

            Toast.makeText(context, "¡Desafío aceptado!", Toast.LENGTH_SHORT).show()

            // Mostrar el contenedor de los CheckBox cuando el desafío es aceptado
            setCheckBoxesVisibility(View.VISIBLE)
            resetCheckBoxes()

            iniciarTemporizador1Minuto()
        }
    }

    private fun generarDesafiosSiEsNecesario() {
        val sharedPreferences = requireContext().getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        val desafioGuardado = obtenerDesafioEnProgreso(requireContext())

        if (desafioGuardado != null) {
            currentDesafio = desafioGuardado
            mostrarDesafioEnProgreso()
        } else {
            generarDesafios(registeredHabits)
            mostrarDesafio()
        }
    }

    private fun generarDesafios(habitos: List<String>) {
        desafiosList.clear()

        for (habito in habitos) {
            when (habito.lowercase().trim()) {
                "cafeína" -> desafiosList.addAll(
                    listOf(
                        "No tomes café en las próximas 3 horas.",
                        "Reemplaza el café de la tarde con agua.",
                        "No consumas cafeína después de las 3 p.m."
                    )
                )
                "dormir mal" -> desafiosList.addAll(
                    listOf(
                        "No tomes siestas durante el día.",
                        "Duerme al menos 7 horas esta noche.",
                        "Apaga tus dispositivos electrónicos 30 minutos antes de dormir."
                    )
                )
                "mala alimentación" -> desafiosList.addAll(
                    listOf(
                        "Evita la comida rápida durante todo el día.",
                        "Come tres comidas balanceadas hoy.",
                        "Reemplaza los snacks poco saludables por frutas o verduras.",
                        "Reduce el consumo de azúcares en tu próxima comida.",
                        "Añade una porción de verduras en cada comida hoy.",
                        "Come una comida casera en lugar de comida procesada hoy."
                    )
                )
                "comer a deshoras" -> desafiosList.addAll(
                    listOf(
                        "No comas nada después de las 9 p.m.",
                        "Establece horarios regulares para tus comidas y cúmplelos hoy.",
                        "No comas nada entre comidas durante las próximas 3 horas.",
                        "Desayuna dentro de la primera hora después de despertar.",
                        "Evita comer snacks después de la cena.",
                        "Come tus tres comidas principales a la misma hora durante el día."
                    )
                )
                "poco ejercicio" -> desafiosList.addAll(
                    listOf(
                        "Realiza una caminata de al menos 30 minutos hoy.",
                        "Haz 15 minutos de estiramientos esta mañana.",
                        "Realiza 10 flexiones durante tu próximo descanso.",
                        "Sube las escaleras en lugar de usar el ascensor durante el día.",
                        "Realiza una rutina rápida de ejercicios al despertarte mañana.",
                        "Haz al menos 20 sentadillas antes de dormir hoy."
                    )
                )
                "alcohol" -> desafiosList.addAll(
                    listOf(
                        "No consumas alcohol durante las próximas 4 horas.",
                        "No consumas bebidas alcohólicas durante todo el día.",
                        "Evita tomar más de una copa de alcohol durante las próximas 5 horas.",
                        "Reemplaza el alcohol con agua o una bebida sin alcohol en tu próxima comida.",
                        "No consumas bebidas alcohólicas mientras estés en una reunión social hoy."
                    )
                )
                "fumar" -> desafiosList.addAll(
                    listOf(
                        "No fumes durante las próximas 4 horas.",
                        "Evita fumar un cigarrillo después de cada comida hoy.",
                        "Intenta reducir tu consumo de cigarrillos a la mitad durante el día.",
                        "No fumes durante las próximas 6 horas.",
                        "Fuma solo la mitad de tu cigarrillo en tu próximo descanso.",
                        "Evita fumar en espacios cerrados durante todo el día."
                    )
                )
                "mala higiene" -> desafiosList.addAll(
                    listOf(
                        "Cepilla tus dientes después de cada comida hoy.",
                        "Lávate las manos antes y después de cada comida.",
                        "Dedica 10 minutos a limpiar tu espacio personal hoy.",
                        "Toma una ducha antes de acostarte esta noche.",
                        "Lávate la cara al menos dos veces durante el día.",
                        "Realiza una limpieza rápida de tu habitación o escritorio."
                    )
                )
                else -> {
                    Toast.makeText(context, "No se encontraron desafíos para el hábito: $habito", Toast.LENGTH_SHORT).show()
                }
            }
        }

        desafiosList.shuffle() // Mezclar los desafíos generados
        mostrarDesafio() // Mostrar inmediatamente el nuevo desafío
    }

    private fun mostrarDesafio() {
        if (desafiosList.isNotEmpty()) {
            currentDesafio = desafiosList.first()

            contenedorDesafios.removeAllViews()
            val textView = TextView(context).apply {
                text = currentDesafio
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.white))
            }
            contenedorDesafios.addView(textView)

            aceptarDesafioButton.visibility = View.VISIBLE
            aceptarDesafioButton.isEnabled = true
        } else {
            Toast.makeText(context, "No hay desafíos disponibles.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarDesafioEnProgreso(context: Context, desafio: String?, enProgreso: Boolean) {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (enProgreso) {
            editor.putString("desafio_actual", desafio)
            editor.putBoolean("en_progreso", true)
        } else {
            editor.remove("desafio_actual")
            editor.putBoolean("en_progreso", false)
        }

        editor.apply()
    }

    private fun obtenerDesafioEnProgreso(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        return if (sharedPreferences.getBoolean("en_progreso", false)) {
            sharedPreferences.getString("desafio_actual", null)
        } else {
            null
        }
    }
}
