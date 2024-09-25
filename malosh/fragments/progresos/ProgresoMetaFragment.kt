package com.seba.malosh.fragments.progresos

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.*

class ProgresoMetaFragment : Fragment() {

    private lateinit var calendarioMeta: CalendarView
    private lateinit var estadoDiaTextView: TextView
    private lateinit var mesSpinner: Spinner
    private lateinit var habitos: ArrayList<String>
    private var fechaInicio: Long = 0
    private var fechaFin: Long = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    // Almacenar el estado de los días solo en memoria usando HashMap
    private val estadoDias = mutableMapOf<String, String>()

    companion object {
        private const val FECHA_INICIO_KEY = "fecha_inicio"
        private const val FECHA_FIN_KEY = "fecha_fin"
        private const val HABITOS_KEY = "habitos"

        fun newInstance(fechaInicio: Long, fechaFin: Long, habitos: ArrayList<String>): ProgresoMetaFragment {
            val fragment = ProgresoMetaFragment()
            val args = Bundle()
            args.putLong(FECHA_INICIO_KEY, fechaInicio)
            args.putLong(FECHA_FIN_KEY, fechaFin)
            args.putStringArrayList(HABITOS_KEY, habitos)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progreso_meta, container, false)

        calendarioMeta = view.findViewById(R.id.calendarioMeta)
        estadoDiaTextView = view.findViewById(R.id.estadoDiaTextView)
        mesSpinner = view.findViewById(R.id.mesSpinner)

        fechaInicio = arguments?.getLong(FECHA_INICIO_KEY) ?: 0L
        fechaFin = arguments?.getLong(FECHA_FIN_KEY) ?: 0L
        habitos = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        configurarCalendario()
        configurarMesesSpinner()

        return view
    }

    private fun configurarCalendario() {
        calendarioMeta.minDate = fechaInicio
        calendarioMeta.maxDate = fechaFin

        calendarioMeta.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val today = Calendar.getInstance()

            // Verifica si el día seleccionado es el día actual
            if (esDiaActual(selectedDate, today)) {
                val fechaSeleccionada = dateFormat.format(selectedDate.time)
                mostrarDialogoEstadoDia(fechaSeleccionada)
            } else {
                Toast.makeText(context, "Solo puedes marcar el día actual.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para verificar si la fecha seleccionada es el día actual
    private fun esDiaActual(selectedDate: Calendar, today: Calendar): Boolean {
        return selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    private fun configurarMesesSpinner() {
        val mesesList = obtenerMesesDentroDeRango()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mesesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mesSpinner.adapter = adapter

        mesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val mesSeleccionadoNombre = mesesList[position]
                val calendar = Calendar.getInstance()

                calendar.timeInMillis = fechaInicio
                val mes = obtenerMesDesdeString(mesSeleccionadoNombre)

                calendar.set(Calendar.MONTH, mes)
                mostrarEstadoDiasParaMes(mesSeleccionadoNombre)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun mostrarEstadoDiasParaMes(mesSeleccionado: String) {
        val calendar = Calendar.getInstance()
        val estados = StringBuilder()

        var fechaActual = fechaInicio
        while (fechaActual <= fechaFin) {
            calendar.timeInMillis = fechaActual
            val fechaFormateada = dateFormat.format(calendar.time)
            val mesFormateado = monthFormat.format(calendar.time)

            if (mesFormateado == mesSeleccionado) {
                val estado = estadoDias[fechaFormateada]
                if (estado != null) {
                    estados.append("Día: $fechaFormateada - Estado: $estado\n")
                }
            }

            fechaActual += 24 * 60 * 60 * 1000
        }

        estadoDiaTextView.text = estados.toString()
    }

    private fun mostrarDialogoEstadoDia(fecha: String) {
        val opciones = arrayOf("Completado", "Fallido")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¿Cómo te fue el $fecha?")
        builder.setItems(opciones) { _, which ->
            val estado = if (which == 0) "Completado" else "Fallido"

            // Guardar el estado del día en el HashMap
            estadoDias[fecha] = estado

            Toast.makeText(context, "Día $fecha marcado como $estado", Toast.LENGTH_SHORT).show()

            estadoDiaTextView.text = "Día $fecha marcado como $estado"
        }
        builder.show()
    }

    private fun obtenerMesDesdeString(mes: String): Int {
        return when (mes) {
            "Enero" -> Calendar.JANUARY
            "Febrero" -> Calendar.FEBRUARY
            "Marzo" -> Calendar.MARCH
            "Abril" -> Calendar.APRIL
            "Mayo" -> Calendar.MAY
            "Junio" -> Calendar.JUNE
            "Julio" -> Calendar.JULY
            "Agosto" -> Calendar.AUGUST
            "Septiembre" -> Calendar.SEPTEMBER
            "Octubre" -> Calendar.OCTOBER
            "Noviembre" -> Calendar.NOVEMBER
            "Diciembre" -> Calendar.DECEMBER
            else -> Calendar.JANUARY
        }
    }

    private fun obtenerMesesDentroDeRango(): List<String> {
        val mesesList = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        var fechaActual = fechaInicio
        while (fechaActual <= fechaFin) {
            calendar.timeInMillis = fechaActual
            val mesFormateado = monthFormat.format(calendar.time)
            if (!mesesList.contains(mesFormateado)) {
                mesesList.add(mesFormateado)
            }

            calendar.add(Calendar.MONTH, 1)
            fechaActual = calendar.timeInMillis
        }

        return mesesList
    }
}
