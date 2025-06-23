package com.example.sporttracker.presentation.fragment

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import com.example.sporttracker.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import org.json.JSONArray
import androidx.appcompat.app.AlertDialog

@AndroidEntryPoint
class AddSupplementFragment : Fragment() {

    private var _binding: FragmentAddSupplementBinding? = null
    private val binding get() = _binding!!
    private var specificDates: MutableList<String> = mutableListOf()
    private val viewModel: SupplementViewModel by viewModels()
    private val doseMap = mutableMapOf<String, String>()
    private var selectedScheduleType: SupplementScheduleType = SupplementScheduleType.EVERY_DAY
    private val weekdayOptions = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    private val selectedWeekdays = mutableSetOf<Int>()
    private var selectedStartDate: String? = null
    private var savedScheduleJson: String? = null
    private lateinit var types: List<SupplementScheduleType>
    private var ignoreNextScheduleSelection = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSupplementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScheduleSpinner()
        updateScheduleFieldsVisibility()
        setupDatePickers()
        setupUiListeners()
        setupSaveButton()

        setupFragmentResultListeners()
        loadSupplementIfEditing()

    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener("specificDatesResult", viewLifecycleOwner) { _, bundle ->
            val resultJson = bundle.getString("specificDatesJson") ?: return@setFragmentResultListener
            val jsonArray = JSONArray(resultJson)
            specificDates.clear()
            for (i in 0 until jsonArray.length()) {
                specificDates.add(jsonArray.getString(i))
            }
            updateSpecificDatesSummary()
        }
        parentFragmentManager.setFragmentResultListener("doseScheduleResult", viewLifecycleOwner) { _, bundle ->
            val json = bundle.getString("doseMapJson") ?: return@setFragmentResultListener
            val parsed = JSONObject(json)
            doseMap.clear()
            parsed.keys().forEach { key ->
                doseMap[key] = parsed.getString(key)
            }
            updateScheduleSummary()
        }
    }

    private fun loadSupplementIfEditing() {
        val supplementId = arguments?.getInt("supplementId", -1) ?: -1
        if (supplementId != -1) {
            viewModel.getSupplementById(supplementId).onEach { supplement ->
                if (supplement != null) {
                    fillFields(supplement)
                    binding.buttonSave.setOnClickListener {
                        saveSupplement(supplement)
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }



    private fun setupUiListeners() {
        binding.containerWeekdays.setOnClickListener {
            selectedScheduleType = SupplementScheduleType.SPECIFIC_WEEKDAYS
            binding.spinnerScheduleType.setSelection(types.indexOf(SupplementScheduleType.SPECIFIC_WEEKDAYS))
            updateScheduleFieldsVisibility()

            val initialSelection = selectedWeekdays.toMutableSet()
            val checked = BooleanArray(7) { i -> initialSelection.contains(i + 1) }

            AlertDialog.Builder(requireContext())
                .setTitle("Выберите дни недели")
                .setMultiChoiceItems(weekdayOptions, checked) { _, which, isChecked ->
                    if (isChecked) initialSelection.add(which + 1)
                    else initialSelection.remove(which + 1)
                }
                .setPositiveButton("OK") { _, _ ->
                    selectedWeekdays.clear()
                    selectedWeekdays.addAll(initialSelection)
                    updateWeekdaysText()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        binding.containerSpecificDates.setOnClickListener {
            selectedScheduleType = SupplementScheduleType.SPECIFIC_DATES
            binding.spinnerScheduleType.setSelection(types.indexOf(SupplementScheduleType.SPECIFIC_DATES))
            updateScheduleFieldsVisibility()

            val json = JSONArray(specificDates).toString()
            findNavController().navigate(
                R.id.action_addSupplementFragment_to_specificDatesEditorFragment,
                bundleOf("specificDatesJson" to json)
            )
        }

        binding.containerFixedSchedule.setOnClickListener {
            if (doseMap.isEmpty()) {
                val json = savedScheduleJson
                if (!json.isNullOrEmpty()) {
                    val obj = JSONObject(json)
                    obj.keys().forEach { key -> doseMap[key] = obj.getString(key) }
                }
            }
            findNavController().navigate(
                R.id.action_addSupplementFragment_to_supplementScheduleEditorFragment,
                bundleOf(
                    "doseMapJson" to JSONObject(doseMap as Map<*, *>).toString(),
                    "singleEntry" to (selectedScheduleType != SupplementScheduleType.SPECIFIC_WEEKDAYS),
                    "scheduleType" to selectedScheduleType.name
                )
            )
        }
    }

    private fun setupDatePickers() {
        binding.containerStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selected = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }

                    selectedStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(selected.time)

                    binding.textStartDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(selected.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }
    }

    private fun setupScheduleSpinner() {
        types = SupplementScheduleType.values().toList()

        val typeNames = types.map {
            when (it) {
                SupplementScheduleType.EVERY_DAY -> "Каждый день"
                SupplementScheduleType.EVERY_N_DAYS -> "Каждые N дней"
                SupplementScheduleType.INTERVAL_HOURS -> "Через каждые N часов"
                SupplementScheduleType.SPECIFIC_WEEKDAYS -> "По выбранным дням недели"
                SupplementScheduleType.SPECIFIC_DATES -> "Конкретные даты"
            }
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerScheduleType.adapter = adapter

        // Установим выбранный тип, если он уже был задан
        ignoreNextScheduleSelection = true
        binding.spinnerScheduleType.setSelection(types.indexOf(selectedScheduleType))
        binding.spinnerScheduleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val newType = types[position]
                if (ignoreNextScheduleSelection) {
                    ignoreNextScheduleSelection = false
                    return
                }
                if (newType != selectedScheduleType) {
                    selectedScheduleType = newType
                    updateScheduleFieldsVisibility()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun saveSupplement(existing: Supplement?) {
        if (!validateRecord()) return
        val name = binding.editName.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim().ifEmpty { null }
        val scheduleJson = if (doseMap.isNotEmpty()) JSONObject(doseMap as Map<*, *>).toString() else null
        val intervalHours = if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS)
            binding.editIntervalHours.text.toString().toIntOrNull() else null
        val intervalDays = if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS)
            binding.editIntervalDays.text.toString().toIntOrNull() else null
        val weekdays = if (selectedWeekdays.isNotEmpty())
            selectedWeekdays.sorted().joinToString(",") else null
        val startDate = selectedStartDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val specificDatesJson = JSONArray(specificDates).toString()

        val supplement = if (existing != null) {
            existing.copy(
                name = name,
                notes = notes,
                scheduleType = selectedScheduleType,
                scheduleMapJson = scheduleJson,
                intervalHours = intervalHours,
                intervalDays = intervalDays,
                weekdays = weekdays,
                specificDates = specificDatesJson,
                startDate = startDate
            )
        } else {
            Supplement(
                name = name,
                notes = notes,
                scheduleType = selectedScheduleType,
                scheduleMapJson = scheduleJson,
                intervalHours = intervalHours,
                intervalDays = intervalDays,
                weekdays = weekdays,
                specificDates = specificDatesJson,
                startDate = startDate
            )
        }

        if (existing != null) {
            viewModel.update(supplement)
            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(supplement)
            Toast.makeText(requireContext(), "Добавка сохранена", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun updateWeekdaysText() {
        val text = if (selectedWeekdays.isEmpty()) {
            "Не выбрано"
        } else {
            selectedWeekdays.sorted()
                .joinToString(", ") { weekdayName(it) }
        }
        binding.textWeekdaysSelected.text = text
    }

    private fun updateScheduleFieldsVisibility() = with(binding) {
        editIntervalDays.visibility =
            if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS) View.VISIBLE else View.GONE
        containerWeekdays.visibility =
            if (selectedScheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) View.VISIBLE else View.GONE
        containerIntervalHours.visibility =
            if (selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS) View.VISIBLE else View.GONE
        containerFixedSchedule.visibility = View.VISIBLE
        containerSpecificDates.visibility =
            if (selectedScheduleType == SupplementScheduleType.SPECIFIC_DATES) View.VISIBLE else View.GONE
        containerStartDate.visibility =
            if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS ||
                selectedScheduleType == SupplementScheduleType.INTERVAL_HOURS)
                View.VISIBLE else View.GONE
    }

    private fun validateRecord(): Boolean {
        val name = binding.editName.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Введите название добавки", Toast.LENGTH_SHORT).show()
            return false
        }

        if (doseMap.isEmpty()) {
            Toast.makeText(requireContext(), "Добавьте хотя бы одну запись \"время — доза\"", Toast.LENGTH_SHORT).show()
            return false
        }

        when (selectedScheduleType) {
            SupplementScheduleType.EVERY_DAY ->{

            }
            SupplementScheduleType.EVERY_N_DAYS -> {
                val days = binding.editIntervalDays.text.toString().toIntOrNull()
                if (days == null || days <= 0) {
                    Toast.makeText(requireContext(), "Введите интервал (в днях)", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (selectedStartDate == null) {
                    Toast.makeText(requireContext(), "Укажите дату начала", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            SupplementScheduleType.INTERVAL_HOURS -> {
                val hours = binding.editIntervalHours.text.toString().toIntOrNull()
                if (hours == null || hours <= 0) {
                    Toast.makeText(requireContext(), "Введите интервал (в часах)", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (doseMap.size != 1) {
                    Toast.makeText(requireContext(), "Для режима \"через N часов\" допустима одна запись", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (selectedStartDate == null) {
                    Toast.makeText(requireContext(), "Укажите дату начала", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            SupplementScheduleType.SPECIFIC_DATES -> {
                if (specificDates.isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите хотя бы одну дату", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            SupplementScheduleType.SPECIFIC_WEEKDAYS -> {
                if (selectedWeekdays.isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите хотя бы один день недели", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        return true
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            saveSupplement(null)
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
        savedScheduleJson = s.scheduleMapJson
        if (doseMap.isEmpty()) {
            s.scheduleMapJson?.let {
                val obj = JSONObject(it)
                doseMap.clear()
                obj.keys().forEach { key -> doseMap[key] = obj.getString(key) }
                updateScheduleSummary()
            }
        }

        updateScheduleFieldsVisibility()

        selectedStartDate = s.startDate
        binding.textStartDate.text = selectedStartDate ?: "Не выбрана"

        editName.setText(s.name)
        editNotes.setText(s.notes)

        if (s.scheduleType == SupplementScheduleType.INTERVAL_HOURS) {
            editIntervalHours.setText(s.intervalHours?.toString() ?: "")
        }

        if (s.scheduleType == SupplementScheduleType.EVERY_N_DAYS) {
            editIntervalDays.setText(s.intervalDays?.toString() ?: "")
        }

        if (s.scheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) {
            selectedWeekdays.clear()
            s.weekdays?.split(",")?.mapNotNull { it.toIntOrNull() }?.let { selectedWeekdays.addAll(it) }
            updateWeekdaysText()
        }
        if (s.scheduleType == SupplementScheduleType.SPECIFIC_DATES) {
            specificDates.clear()
            s.specificDates?.let {
                val array = JSONArray(it)
                for (i in 0 until array.length()) {
                    specificDates.add(array.getString(i))
                }
            }
            updateSpecificDatesSummary()
        }
        updateStartDateSummary()
    }

    private fun updateSpecificDatesSummary() {
        binding.textSpecificDatesSummary.text = if (specificDates.isEmpty()) {
            "Не выбраны"
        } else {
            specificDates.joinToString("\n") { it }
        }
    }

    private fun weekdayName(day: Int): String = when (day) {
        1 -> "Пн"
        2 -> "Вт"
        3 -> "Ср"
        4 -> "Чт"
        5 -> "Пт"
        6 -> "Сб"
        7 -> "Вс"
        else -> "?"
    }

    private fun updateStartDateSummary() {
        binding.textStartDate.text = selectedStartDate ?: "Не выбрана"
    }

    override fun onResume() {
        super.onResume()
        updateSpecificDatesSummary()
        updateScheduleSummary()
        updateStartDateSummary()
        updateWeekdaysText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
