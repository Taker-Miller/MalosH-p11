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

    // Checkboxes para el progreso
    private lateinit var inicioCheckBox: CheckBox
    private lateinit var enProgresoCheckBox: CheckBox
    private lateinit var casiPorTerminarCheckBox: CheckBox
    private lateinit var completadoCheckBox: CheckBox

    private val desafiosList = mutableListOf<String>()
    private var currentDesafio: String? = null
    private var desafioEnProgreso = false
    private val handler = Handler()

    private lateinit var registeredHabits: ArrayList<String>

    companion object {
        private const val HABITOS_KEY = "habitos_registrados"
        private const val TEMPORIZADOR_INICIO_KEY = "temporizador_inicio"
        private const val TEMPORIZADOR_DURACION = 60000L // 60 segundos en milisegundos (1 minuto)
        private const val TEMPORIZADOR_ESPERA = 20000L // 20 segundos en milisegundos para un nuevo desafío

        // Aquí aceptamos una lista de hábitos para pasarla como un ArrayList<String>
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

        // Inicializar los checkboxes
        inicioCheckBox = view.findViewById(R.id.inicioCheckBox)
        enProgresoCheckBox = view.findViewById(R.id.enProgresoCheckBox)
        casiPorTerminarCheckBox = view.findViewById(R.id.casiPorTerminarCheckBox)
        completadoCheckBox = view.findViewById(R.id.completadoCheckBox)

        // Inicialmente ocultamos los checkboxes
        setCheckBoxesVisibility(View.GONE)

        // Restaurar los estados de los checkboxes y su visibilidad si hay un desafío en progreso
        actualizarCheckBoxesRestaurados()

        // Obtener los hábitos registrados desde el argumento que se pasó al crear el fragmento
        registeredHabits = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        // Verificar si hay un temporizador en progreso
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val inicioTemporizador = sharedPreferences.getLong(TEMPORIZADOR_INICIO_KEY, 0L)
        currentDesafio = obtenerDesafioEnProgreso(requireContext()) // Obtener el desafío actual en progreso

        if (inicioTemporizador > 0L && currentDesafio != null) {
            // Si el temporizador ya está en progreso y hay un desafío guardado, reanudar el temporizador
            reanudarTemporizador(inicioTemporizador)
            mostrarDesafioEnProgreso() // Asegurarse de mostrar el nombre del desafío y el progreso
        } else {
            // Verificar si el desafío fue completado o está en progreso
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


    private fun generarDesafiosSiEsNecesario() {
        val sharedPreferences = requireContext().getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        val desafioGuardado = obtenerDesafioEnProgreso(requireContext())

        if (desafioGuardado != null) {
            // Si ya hay un desafío guardado, mostrarlo y no generar uno nuevo
            currentDesafio = desafioGuardado
            mostrarDesafioEnProgreso()
        } else {
            // Si no hay un desafío guardado, generar uno nuevo
            generarDesafios(registeredHabits)
            mostrarDesafio()
        }
    }

    private fun iniciarTemporizador1Minuto() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Guardar el tiempo de inicio en SharedPreferences
        val tiempoInicio = System.currentTimeMillis()
        editor.putLong(TEMPORIZADOR_INICIO_KEY, tiempoInicio)
        editor.apply()

        // Mostrar los checkboxes cuando el temporizador comienza
        setCheckBoxesVisibility(View.VISIBLE)
        resetCheckBoxes() // Resetear los checkboxes

        reanudarTemporizador(tiempoInicio)
    }

    private fun reanudarTemporizador(tiempoInicio: Long) {
        val tiempoActual = System.currentTimeMillis()
        val tiempoRestante = TEMPORIZADOR_DURACION - (tiempoActual - tiempoInicio)

        if (tiempoRestante > 0) {
            aceptarDesafioButton.visibility = View.GONE
            cancelarDesafioButton.visibility = View.VISIBLE

            desafioDescripcion.text =
                "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
            setCheckBoxesVisibility(View.VISIBLE) // Mostrar los CheckBox

            handler.postDelayed(object : Runnable {
                var tiempoRestanteActualizado = tiempoRestante
                override fun run() {
                    if (tiempoRestanteActualizado > 0) {
                        tiempoRestanteActualizado -= 1000
                        desafioDescripcion.text = "Desafío en progreso. Tiempo restante: ${
                            TimeUnit.MILLISECONDS.toSeconds(tiempoRestanteActualizado)
                        } segundos."

                        actualizarCheckBoxes(tiempoRestanteActualizado)

                        handler.postDelayed(this, 1000)
                    } else {
                        // Cuando el temporizador llega a 0
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

        // Actualizamos los estados de los CheckBoxes con base en el tiempo transcurrido
        if (porcentajeRestante >= 25) {
            inicioCheckBox.isEnabled = true
            inicioCheckBox.isChecked = true
            editor.putBoolean("inicio_check", true) // Guardar el estado
        }
        if (porcentajeRestante >= 50) {
            enProgresoCheckBox.isEnabled = true
            enProgresoCheckBox.isChecked = true
            editor.putBoolean("en_progreso_check", true) // Guardar el estado
        }
        if (porcentajeRestante >= 75) {
            casiPorTerminarCheckBox.isEnabled = true
            casiPorTerminarCheckBox.isChecked = true
            editor.putBoolean("casi_terminado_check", true) // Guardar el estado
        }
        if (porcentajeRestante >= 100) {
            completadoCheckBox.isEnabled = true
            completadoCheckBox.isChecked = true
            editor.putBoolean("completado_check", true) // Guardar el estado
        }

        editor.apply() // Guardar cambios en SharedPreferences
    }



    // Esta función restablece los CheckBoxes deshabilitándolos y desmarcándolos
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
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // Limpiar todos los estados guardados de los checkboxes
    }




    private fun validarDesafioCompletado() {
        if (inicioCheckBox.isChecked && enProgresoCheckBox.isChecked &&
            casiPorTerminarCheckBox.isChecked && completadoCheckBox.isChecked
        ) {
            Toast.makeText(context, "¡Desafío completado exitosamente!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Desafío fallido. No completaste todas las etapas.", Toast.LENGTH_SHORT).show()
        }

        setCheckBoxesVisibility(View.GONE)
        limpiarEstadoCheckBoxes() // Limpiar el estado de los checkboxes al completar o fallar el desafío
        iniciarTemporizador20Segundos()
    }


    private fun iniciarTemporizador20Segundos() {
        var tiempoRestante = TEMPORIZADOR_ESPERA // 20 segundos

        // Mostrar el mensaje inicial del temporizador
        desafioDescripcion.text = "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."

        // Crear un Runnable para contar hacia atrás cada segundo
        val runnable = object : Runnable {
            override fun run() {
                if (tiempoRestante > 0) {
                    tiempoRestante -= 1000
                    desafioDescripcion.text = "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
                    handler.postDelayed(this, 1000) // Actualizar cada segundo
                } else {
                    // Cuando el tiempo llega a 0, generar un nuevo desafío y restablecer el estado
                    desafioDescripcion.text = "¡Nuevo desafío disponible!"

                    // Limpiar el desafío anterior
                    limpiarDesafioAnterior()

                    // Generar y mostrar un nuevo desafío
                    generarDesafios(registeredHabits)
                    mostrarDesafio()

                    // Ajustar visibilidad de los botones y checkboxes
                    aceptarDesafioButton.visibility = View.VISIBLE
                    aceptarDesafioButton.isEnabled = true
                    cancelarDesafioButton.visibility = View.GONE
                    setCheckBoxesVisibility(View.GONE) // Ocultar los checkboxes para el nuevo desafío

                    // Limpiar cualquier temporizador en SharedPreferences si fuera necesario
                    val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove(TEMPORIZADOR_INICIO_KEY).apply() // Eliminar el tiempo de inicio guardado
                }
            }
        }

        // Iniciar el temporizador
        handler.post(runnable)
    }

    private fun limpiarDesafioAnterior() {
        // Limpia el desafío en progreso y actualiza la bandera a "no hay desafío en progreso"
        desafioEnProgreso = false
        currentDesafio = null
        guardarDesafioEnProgreso(requireContext(), null, false) // Actualiza en SharedPreferences
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

        // Mostrar los checkboxes y restaurar su visibilidad
        setCheckBoxesVisibility(View.VISIBLE)

        // Restaura el estado de los checkboxes usando SharedPreferences
        actualizarCheckBoxesRestaurados()
    }



    private fun cancelarDesafio() {
        handler.removeCallbacksAndMessages(null)
        guardarDesafioEnProgreso(requireContext(), null, false)
        desafioEnProgreso = false
        currentDesafio = null
        setCheckBoxesVisibility(View.GONE)
        desafioDescripcion.text = "Próximo desafío disponible en 20 segundos."
        limpiarEstadoCheckBoxes() // Limpiar el estado de los checkboxes al cancelar el desafío
        iniciarTemporizador20Segundos()
    }


    private fun aceptarDesafio() {
        if (desafioEnProgreso) {
            Toast.makeText(
                context,
                "Ya tienes un desafío en progreso. Finaliza o cancela el desafío actual primero.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            desafioEnProgreso = true
            guardarDesafioEnProgreso(requireContext(), currentDesafio, true)
            Toast.makeText(context, "¡Desafío aceptado!", Toast.LENGTH_SHORT).show()

            // Mostrar el contenedor de los CheckBox cuando el desafío es aceptado
            setCheckBoxesVisibility(View.VISIBLE)
            resetCheckBoxes() // Resetear los CheckBox

            iniciarTemporizador1Minuto()
        }
    }




    private fun generarDesafios(habitos: List<String>) {
        desafiosList.clear() // Limpiar la lista antes de generar nuevos desafíos

        for (habito in habitos) {
            when (habito.lowercase().trim()) {
                "cafeína", "consumo de cafeína" -> desafiosList.addAll(
                    listOf(
                        "No tomes café en las próximas 2 horas.",
                        "Reemplaza el café de la tarde con agua.",
                        "No consumas cafeína después del mediodía."
                    )
                )

                "dormir mal", "dormir a deshoras" -> desafiosList.addAll(
                    listOf(
                        "No duermas durante el día.",
                        "Duerme al menos 7 horas esta noche.",
                        "Apaga tus dispositivos electrónicos 30 minutos antes de dormir.",
                        "Evita tomar café después de las 6 p.m.",
                        "Realiza una rutina de relajación antes de dormir.",
                        "Acuéstate antes de las 11 p.m.",
                        "Despiértate a la misma hora mañana."
                    )
                )

                "interrumpir a otros" -> desafiosList.addAll(
                    listOf(
                        "No interrumpas a nadie en una conversación durante las próximas 3 horas.",
                        "Escucha activamente durante una conversación sin interrumpir.",
                        "Deja que los demás terminen de hablar antes de dar tu opinión.",
                        "Practica la paciencia en una reunión evitando interrumpir.",
                        "Asegúrate de dar espacio para que los demás hablen primero."
                    )
                )

                "mala alimentación" -> desafiosList.addAll(
                    listOf(
                        "Evita la comida rápida durante todo el día.",
                        "Come 3 comidas balanceadas hoy.",
                        "Reemplaza los snacks poco saludables por frutas.",
                        "Reduce el consumo de azúcares en tu próxima comida.",
                        "Incluye verduras en tu almuerzo.",
                        "Come una comida casera hoy."
                    )
                )

                "comer a deshoras" -> desafiosList.addAll(
                    listOf(
                        "No comas después de las 10 p.m.",
                        "Establece horarios regulares para tus comidas.",
                        "No comas nada entre comidas durante las próximas 3 horas.",
                        "Desayuna dentro de la primera hora de despertar.",
                        "Evita comer snacks después de la cena.",
                        "Come tus tres comidas a la misma hora todos los días.",
                        "No comas nada durante las próximas 2 horas."
                    )
                )

                "poco ejercicio" -> desafiosList.addAll(
                    listOf(
                        "Realiza una caminata de 30 minutos hoy.",
                        "Haz 15 minutos de estiramientos en casa.",
                        "Realiza 10 flexiones en tu descanso.",
                        "Sube las escaleras en lugar de usar el ascensor.",
                        "Realiza una rutina rápida de ejercicios al levantarte.",
                        "Haz al menos 20 sentadillas hoy.",
                        "Camina en lugar de conducir si es posible hoy."
                    )
                )

                "alcohol" -> desafiosList.addAll(
                    listOf(
                        "No beber alcohol por 2 horas.",
                        "No consumir alcohol durante todo el día.",
                        "Evita tomar más de un vaso de alcohol durante 4 horas.",
                        "No consumas bebidas alcohólicas hasta la noche.",
                        "No tomes alcohol mientras estás en una reunión social.",
                        "Reemplaza el alcohol con agua en tu siguiente comida.",
                        "No consumas bebidas alcohólicas hoy."
                    )
                )

                "fumar" -> desafiosList.addAll(
                    listOf(
                        "No fumes durante las próximas 3 horas.",
                        "Evita fumar un cigarrillo después del almuerzo.",
                        "Intenta reducir tu consumo de cigarrillos a la mitad hoy.",
                        "No fumes en las próximas 5 horas.",
                        "Fuma solo la mitad de tu cigarrillo en tu siguiente descanso.",
                        "Evita fumar mientras trabajas.",
                        "No fumes en espacios cerrados durante todo el día."
                    )
                )

                "mala higiene" -> desafiosList.addAll(
                    listOf(
                        "Cepilla tus dientes después de cada comida hoy.",
                        "Lávate las manos antes y después de cada comida.",
                        "Dedica 10 minutos a limpiar tu espacio personal.",
                        "Toma una ducha antes de acostarte.",
                        "Lávate la cara cada mañana.",
                        "Lávate las manos cada vez que salgas del baño.",
                        "Realiza una limpieza rápida de tu habitación."
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
            currentDesafio = desafiosList.first() // Tomar el primer desafío generado

            // Actualizar la UI con el nuevo desafío
            contenedorDesafios.removeAllViews() // Limpiar el contenedor de desafíos previos
            val textView = TextView(context).apply {
                text = currentDesafio
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.white))
            }
            contenedorDesafios.addView(textView) // Añadir el desafío al contenedor

            // Hacer visible el botón de aceptar el desafío si estaba oculto
            aceptarDesafioButton.visibility = View.VISIBLE
            aceptarDesafioButton.isEnabled = true
        } else {
            Toast.makeText(context, "No hay desafíos disponibles.", Toast.LENGTH_SHORT).show()
        }
    }


    // Función para ocultar o mostrar los checkboxes
    private fun setCheckBoxesVisibility(visibility: Int) {
        val progresoChecklist = view?.findViewById<LinearLayout>(R.id.progresoChecklist)
        progresoChecklist?.visibility = visibility
    }

    // Función para restaurar el estado de los checkboxes
    private fun actualizarCheckBoxesRestaurados() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)

        // Restaurar el estado de los checkboxes desde SharedPreferences
        inicioCheckBox.isChecked = sharedPreferences.getBoolean("inicio_check", false)
        enProgresoCheckBox.isChecked = sharedPreferences.getBoolean("en_progreso_check", false)
        casiPorTerminarCheckBox.isChecked = sharedPreferences.getBoolean("casi_terminado_check", false)
        completadoCheckBox.isChecked = sharedPreferences.getBoolean("completado_check", false)

        // Habilitar los checkboxes que ya estaban marcados
        if (inicioCheckBox.isChecked) {
            enProgresoCheckBox.isEnabled = true
        }
        if (enProgresoCheckBox.isChecked) {
            casiPorTerminarCheckBox.isEnabled = true
        }
        if (casiPorTerminarCheckBox.isChecked) {
            completadoCheckBox.isEnabled = true
        }

        // Mostrar los checkboxes si alguno está marcado
        if (inicioCheckBox.isChecked || enProgresoCheckBox.isChecked || casiPorTerminarCheckBox.isChecked || completadoCheckBox.isChecked) {
            setCheckBoxesVisibility(View.VISIBLE)
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
