package com.example.sporttracker.presentation.fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sporttracker.R
import com.example.sporttracker.databinding.FragmentSupplementScheduleEditorBinding
import com.example.sporttracker.presentation.adapter.DoseAdapter
import org.json.JSONObject
import java.util.*

class SupplementScheduleEditorFragment : Fragment() {

    private var _binding: FragmentSupplementScheduleEditorBinding? = null
    private val binding get() = _binding!!
    private var scheduleType: String? = null
    private val doseMap = linkedMapOf<String, String>()
    private lateinit var adapter: DoseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplementScheduleEditorBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scheduleType = arguments?.getString("scheduleType")

        // сначала загружаем doseMap из аргументов
        val doseJson = arguments?.getString("doseMapJson")
        if (!doseJson.isNullOrBlank()) {
            val obj = JSONObject(doseJson)
            doseMap.clear()
            obj.keys().forEach { key -> doseMap[key] = obj.getString(key) }
        }

        // теперь создаём и настраиваем адаптер
        adapter = DoseAdapter(
            doseMap.toList(),
            onEdit = { time -> showEditDialog(time) },
            onDelete = { time ->
                doseMap.remove(time)
                updateList()
            }
        )
        binding.recyclerDoseList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDoseList.adapter = adapter

        // кнопка "добавить"
        binding.buttonAdd.setOnClickListener {
            if (scheduleType == "INTERVAL_HOURS" && doseMap.size >= 1) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Для интервала по часам можно задать только одну запись.")
                    .setPositiveButton("ОК", null)
                    .show()
            } else {
                showAddDialog()
            }
        }


    }

    private fun showAddDialog() {
        val now = Calendar.getInstance()
        TimePickerDialog(requireContext(),
            { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                val input = EditText(requireContext()).apply { hint = "Дозировка" }

                AlertDialog.Builder(requireContext())
                    .setTitle("Введите дозировку для $time")
                    .setView(input)
                    .setPositiveButton("Сохранить") { _, _ ->
                        val dose = input.text.toString().trim()
                        if (dose.isNotEmpty()) {
                            doseMap[time] = dose
                            updateList()
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateList() {
        adapter = DoseAdapter(
            doseMap.toList(),
            onEdit = { time -> showEditDialog(time) },
            onDelete = { time ->
                doseMap.remove(time)
                updateList()
            }
        )
        binding.recyclerDoseList.adapter = adapter
    }

    private fun showEditDialog(time: String) {
        val input = EditText(requireContext()).apply {
            setText(doseMap[time])
            hint = "Дозировка"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить дозу для $time")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newDose = input.text.toString().trim()
                if (newDose.isNotEmpty()) {
                    doseMap[time] = newDose
                    updateList()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_schedule_editor, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
            val resultJson = JSONObject(doseMap as Map<*, *>).toString()
            setFragmentResult("doseScheduleResult", Bundle().apply {
                putString("doseMapJson", resultJson)
            })
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
