package com.example.sporttracker.presentation.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.data.model.SupplementScheduleType
import com.example.sporttracker.databinding.FragmentSupplementDetailBinding
import com.example.sporttracker.presentation.viewmodel.SupplementViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class SupplementDetailFragment : Fragment() {

    private var _binding: FragmentSupplementDetailBinding? = null
    private val binding get() = _binding!!

    private val args: SupplementDetailFragmentArgs by navArgs()
    private val viewModel: SupplementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val supplementId = args.supplementId
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getSupplementById(supplementId).onEach {
                if (it != null) bindSupplement(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun bindSupplement(supplement: Supplement) = with(binding) {
        textName.text = supplement.name

        // Расписание
        textSchedule.text = when (supplement.scheduleType) {
            SupplementScheduleType.EVERY_DAY -> "Каждый день"
            SupplementScheduleType.EVERY_N_DAYS -> "Каждые ${supplement.intervalDays ?: "?"} дней"
            SupplementScheduleType.INTERVAL_HOURS -> "Через каждые ${supplement.intervalHours ?: "?"} часов"
            SupplementScheduleType.SPECIFIC_WEEKDAYS -> "В определённые дни недели"
            SupplementScheduleType.SPECIFIC_DATES -> "В указанные даты"
        }

        // Дата начала
        textStartDate.text = supplement.startDate?.let { "Дата начала: $it" } ?: ""

        // Интервалы
        textInterval.text = when (supplement.scheduleType) {
            SupplementScheduleType.EVERY_N_DAYS -> "Интервал: ${supplement.intervalDays} дней"
            SupplementScheduleType.INTERVAL_HOURS -> "Интервал: ${supplement.intervalHours} ч"
            else -> ""
        }

        // Дни недели
        textWeekdays.text = if (supplement.scheduleType == SupplementScheduleType.SPECIFIC_WEEKDAYS) {
            supplement.weekdays?.split(",")?.mapNotNull { it.toIntOrNull() }?.joinToString(", ") {
                weekdayName(it)
            }?.let { "Дни недели: $it" } ?: "Дни недели не указаны"
        } else ""

        // Конкретные даты
        textSpecificDates.text = if (supplement.scheduleType == SupplementScheduleType.SPECIFIC_DATES) {
            supplement.specificDates?.let {
                val array = JSONArray(it)
                if (array.length() == 0) "Даты не указаны"
                else (0 until array.length()).joinToString(", ") { i -> array.getString(i) }
            } ?: "Даты не указаны"
        } else ""

        // Приёмы (время — доза)
        if (!supplement.scheduleMapJson.isNullOrEmpty()) {
            val obj = JSONObject(supplement.scheduleMapJson)
            val lines = obj.keys().asSequence().map { time ->
                val dose = obj.getString(time)
                "$time — $dose"
            }.joinToString("\n")
            textDosage.text = lines
        } else {
            textDosage.text = "Нет данных о приёмах"
        }

        // Примечания
        textNotes.text = supplement.notes?.ifBlank { "Нет примечаний" } ?: "Нет примечаний"
    }

    private fun weekdayName(day: Int): String {
        return when (day) {
            1 -> "Пн"
            2 -> "Вт"
            3 -> "Ср"
            4 -> "Чт"
            5 -> "Пт"
            6 -> "Сб"
            7 -> "Вс"
            else -> "?"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
