package com.seba.malosh.fragments.metas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R

class DefinirMetaFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var definirMetaButton: Button
    private lateinit var habitosLayout: LinearLayout
    private val checkboxes = mutableListOf<CheckBox>()

    companion object {
        private const val REGISTERED_HABITS_KEY = "registered_habits"

        fun newInstance(registeredHabits: ArrayList<String>): DefinirMetaFragment {
            val fragment = DefinirMetaFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(REGISTERED_HABITS_KEY, registeredHabits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_definir_meta, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        definirMetaButton = view.findViewById(R.id.definirMetaButton)
        habitosLayout = view.findViewById(R.id.habitosLayout)

        val registeredHabits = arguments?.getStringArrayList(REGISTERED_HABITS_KEY)

        registeredHabits?.forEach { habito ->
            val checkBox = CheckBox(context)
            checkBox.text = habito
            checkboxes.add(checkBox)
            habitosLayout.addView(checkBox)
        }

        definirMetaButton.setOnClickListener {
            val selectedHabits = checkboxes.filter { it.isChecked }

            when {
                selectedHabits.isEmpty() -> {
                    Toast.makeText(context, "Selecciona al menos un mal hábito para definir una meta", Toast.LENGTH_SHORT).show()
                }
                selectedHabits.size > 2 -> {
                    Toast.makeText(context, "Solo puedes seleccionar hasta 2 hábitos", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "Meta definida para: ${selectedHabits.joinToString { it.text }}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
