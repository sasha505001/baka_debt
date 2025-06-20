package com.example.sporttracker.presentation.fragment

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.data.model.SupplementScheduleType
import com.example.sporttracker.databinding.FragmentAddSupplementBinding
import com.example.sporttracker.presentation.viewmodel.SupplementViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import androidx.lifecycle.lifecycleScope
import com.example.sporttracker.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import org.json.JSONArray

@AndroidEntryPoint
class AddSupplementFragment : Fragment() {

    private var _binding: FragmentAddSupplementBinding? = null
    private val binding get() = _binding!!
    private var specificDates: MutableList<String> = mutableListOf()
    private val viewModel: SupplementViewModel by viewModels()
    private val doseMap = mutableMapOf<String, String>()
    private var selectedScheduleType: SupplementScheduleType = SupplementScheduleType.EVERY_DAY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSupplementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentFragmentManager.setFragmentResultListener("specificDatesResult", viewLifecycleOwner) { _, bundle ->
            val resultJson = bundle.getString("specificDatesJson") ?: return@setFragmentResultListener
            val jsonArray = JSONArray(resultJson)
            specificDates.clear()
            for (i in 0 until jsonArray.length()) {
                specificDates.add(jsonArray.getString(i))
            }
            updateSpecificDatesSummary()
        }
        binding.containerSpecificDates.setOnClickListener {
            val json = JSONArray(specificDates).toString()
            findNavController().navigate(
                R.id.action_addSupplementFragment_to_specificDatesEditorFragment,
                bundleOf("specificDatesJson" to json)
            )
        }
        setupScheduleSpinner()
        setupSaveButton()
        binding.containerFixedSchedule.setOnClickListener {
            findNavController().navigate(
                R.id.action_addSupplementFragment_to_supplementScheduleEditorFragment,
                bundleOf(
                    "doseMapJson" to JSONObject(doseMap as Map<*, *>).toString(),
                    "singleEntry" to (selectedScheduleType != SupplementScheduleType.SPECIFIC_WEEKDAYS)
                )
            )
        }
        val supplementId = arguments?.getInt("supplementId", -1) ?: -1
        if (supplementId != -1) {
            viewModel.getSupplementById(supplementId).onEach { supplement ->
                if (supplement != null) {
                    fillFields(supplement)
                    binding.buttonSave.setOnClickListener {
                        val updated = supplement.copy(
                            name = binding.editName.text.toString().trim(),
                            scheduleType = selectedScheduleType,
                            scheduleMapJson = if (selectedScheduleType != SupplementScheduleType.INTERVAL_HOURS)
                                JSONObject(doseMap as Map<*, *>).toString() else null,
                            intervalHours = if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS)
                                binding.editIntervalHours.text.toString().toIntOrNull() else null,
                            intervalDays = if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS)
                                binding.editIntervalDays.text.toString().toIntOrNull() else null,
                            weekdays = if (selectedScheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS)
                                binding.editWeekdays.text.toString().takeIf { it.isNotBlank() } else null,
                            notes = binding.editNotes.text.toString().trim()
                        )
                        viewModel.update(updated)
                        Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }


                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        setFragmentResultListener("doseScheduleResult") { _, bundle ->
            val json = bundle.getString("doseMapJson") ?: return@setFragmentResultListener
            val parsed = JSONObject(json)
            doseMap.clear()
            parsed.keys().forEach { key ->
                doseMap[key] = parsed.getString(key)
            }
            updateScheduleSummary()
        }

    }



    private fun setupScheduleSpinner() {
        val types = SupplementScheduleType.values()
        val typeNames = types.map {
            when (it) {
                SupplementScheduleType.EVERY_DAY -> "Каждый день"
                SupplementScheduleType.EVERY_N_DAYS -> "Каждые N дней"
                SupplementScheduleType.WEEKDAYS_ONLY -> "Будние дни"
                SupplementScheduleType.WEEKENDS_ONLY -> "Выходные"
                SupplementScheduleType.SPECIFIC_WEEKDAYS -> "По дням недели"
                SupplementScheduleType.INTERVAL_HOURS -> "Через каждые N часов"
            }
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerScheduleType.adapter = adapter

        binding.spinnerScheduleType.setSelection(0)
        selectedScheduleType = SupplementScheduleType.EVERY_DAY
        updateFieldVisibility()

        binding.spinnerScheduleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedScheduleType = types[position]
                updateFieldVisibility()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateFieldVisibility() = with(binding) {
        editIntervalDays.visibility =
            if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS) View.VISIBLE else View.GONE
        editWeekdays.visibility =
            if (selectedScheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) View.VISIBLE else View.GONE
        containerIntervalHours.visibility =
            if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS) View.VISIBLE else View.GONE
        containerFixedSchedule.visibility = View.VISIBLE

    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val notes = binding.editNotes.text.toString().trim()
            val intervalDays = binding.editIntervalDays.text.toString().toIntOrNull()
            val weekdays = binding.editWeekdays.text.toString().takeIf { it.isNotBlank() }

            val supplement = Supplement(
                name = name,
                notes = notes.ifEmpty { null },
                scheduleType = selectedScheduleType,
                scheduleMapJson = if (selectedScheduleType != SupplementScheduleType.INTERVAL_HOURS)
                    JSONObject(doseMap as Map<*, *>).toString() else null,
                intervalHours = if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS)
                    binding.editIntervalHours.text.toString().toIntOrNull() else null,
                intervalDays = if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS)
                    intervalDays else null,
                weekdays = if (selectedScheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS)
                    weekdays else null,
                specificDates = JSONArray(specificDates).toString(),
                startDate = null // или передай нужную дату, если появится DatePicker
            )

            viewModel.insert(supplement)
            Toast.makeText(requireContext(), "Добавка сохранена", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun updateScheduleSummary() {
        val summary = if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS) {
            val interval = binding.editIntervalHours.text.toString().toIntOrNull()
            if (interval != null) "Каждые $interval ч" else "Не задано"
        } else {
            if (doseMap.isEmpty()) "Не задано"
            else doseMap.entries.joinToString("\n") { (time, dose) -> "$time — $dose" }
        }

        binding.textScheduleSummary.text = summary
    }

    private fun fillFields(s: Supplement) = with(binding) {
        selectedScheduleType = s.scheduleType
        spinnerScheduleType.setSelection(SupplementScheduleType.values().indexOf(s.scheduleType))
        updateFieldVisibility()

        editName.setText(s.name)
        editNotes.setText(s.notes)

        if (s.scheduleType == SupplementScheduleType.INTERVAL_HOURS) {
            editIntervalHours.setText(s.intervalHours?.toString() ?: "")
        } else {
            s.scheduleMapJson?.let {
                val map = JSONObject(it)
                doseMap.clear()
                map.keys().forEach { key -> doseMap[key] = map.getString(key) }
                updateScheduleSummary()
            }
        }

        if (s.scheduleType == SupplementScheduleType.EVERY_N_DAYS) {
            editIntervalDays.setText(s.intervalDays?.toString() ?: "")
        }

        if (s.scheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) {
            editWeekdays.setText(s.weekdays.orEmpty())
        }
    }
    private fun updateSpecificDatesSummary() {
        binding.textSpecificDatesSummary.text = if (specificDates.isEmpty()) {
            "Не выбраны"
        } else {
            specificDates.joinToString("\n") { it }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSpecificDatesSummary()
        updateScheduleSummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
