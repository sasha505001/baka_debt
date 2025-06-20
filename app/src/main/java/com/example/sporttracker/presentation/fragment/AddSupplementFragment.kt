package com.example.sporttracker.presentation.fragment

import android.app.TimePickerDialog
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

@AndroidEntryPoint
class AddSupplementFragment : Fragment() {

    private var _binding: FragmentAddSupplementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SupplementViewModel by viewModels()

    private var selectedTime: String = ""
    private var selectedScheduleType: SupplementScheduleType = SupplementScheduleType.EVERY_DAY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSupplementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupScheduleSpinner()
        setupTimePicker()
        setupSaveButton()
    }

    private fun setupTimePicker() {
        binding.editTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(requireContext(),
                { _, hour, minute ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    binding.editTime.setText(selectedTime)
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
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
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val dosage = binding.editDosage.text.toString().trim()
            val notes = binding.editNotes.text.toString().trim()
            val intervalDays = binding.editIntervalDays.text.toString().toIntOrNull()
            val weekdays = binding.editWeekdays.text.toString().takeIf { it.isNotBlank() }

            if (name.isEmpty() || dosage.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, заполните все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val supplement = Supplement(
                name = name,
                dosage = dosage,
                time = selectedTime,
                scheduleType = selectedScheduleType,
                intervalDays = if (selectedScheduleType == SupplementScheduleType.EVERY_N_DAYS) intervalDays else null,
                weekdays = if (selectedScheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) weekdays else null,
                notes = notes
            )

            viewModel.insert(supplement)
            Toast.makeText(requireContext(), "Добавка сохранена", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
