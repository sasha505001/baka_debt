package com.example.sporttracker.presentation.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.sporttracker.data.model.SupplementScheduleType
import com.example.sporttracker.databinding.FragmentSupplementDetailBinding
import com.example.sporttracker.presentation.viewmodel.SupplementViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
            viewModel.getSupplementById(supplementId).collectLatest { s ->
                s?.let {
                    binding.textName.text = it.name
                    binding.textNotes.text = it.notes?.takeIf { n -> n.isNotBlank() } ?: "Без заметок"

                    // --- расписание ---
                    val scheduleText = when (it.scheduleType) {
                        SupplementScheduleType.EVERY_DAY -> "Каждый день"

                        SupplementScheduleType.EVERY_N_DAYS -> {
                            val start = it.startDate ?: "?"
                            val days = it.intervalDays ?: "?"
                            "С $start, каждые $days дн."
                        }

                        SupplementScheduleType.INTERVAL_HOURS -> {
                            val baseTime = it.scheduleMapJson?.let { json ->
                                runCatching { JSONObject(json) }.getOrNull()
                                    ?.keys()?.asSequence()?.firstOrNull()
                            } ?: "время не указано"
                            val hours = it.intervalHours ?: "?"
                            "$baseTime + каждые $hours ч"
                        }

                        SupplementScheduleType.SPECIFIC_WEEKDAYS -> {
                            val days = it.weekdays.orEmpty().split(",").mapNotNull { d -> d.toIntOrNull() }
                                .map { d -> weekdayName(d) }
                            "По дням: ${days.joinToString(", ")}"
                        }

                        SupplementScheduleType.SPECIFIC_DATES -> {
                            val array = runCatching { JSONArray(it.specificDates ?: "[]") }.getOrNull()
                            if (array != null && array.length() > 0) {
                                val preview = (0 until minOf(3, array.length()))
                                    .joinToString(", ") { i -> array.getString(i) }
                                "Даты: $preview" + if (array.length() > 3) " + ещё ${array.length() - 3}" else ""
                            } else {
                                "Даты не указаны"
                            }
                        }
                    }
                    binding.textSchedule.text = scheduleText

                    // --- расписание: время — доза ---
                    val scheduleMap = it.scheduleMapJson?.let { json ->
                        runCatching { JSONObject(json) }.getOrNull()
                    }
                    val summary = scheduleMap?.keys()?.asSequence()
                        ?.joinToString("\n") { time -> "$time — ${scheduleMap.optString(time)}" }
                    binding.textDosage.text = summary ?: "Нет данных"
                    binding.textTime.text = ""
                }
            }
        }
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
