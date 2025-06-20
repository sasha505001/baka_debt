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
            viewModel.getSupplementById(supplementId).collectLatest { supplement ->
                supplement?.let {
                    binding.textName.text = it.name
                    binding.textDosage.text = it.dosage
                    binding.textTime.text = "Время: ${it.time}"

                    binding.textSchedule.text = when (it.scheduleType) {
                        SupplementScheduleType.EVERY_DAY -> "Каждый день"
                        SupplementScheduleType.EVERY_N_DAYS -> "Каждые ${it.intervalDays} дней"
                        SupplementScheduleType.WEEKDAYS_ONLY -> "Будние дни"
                        SupplementScheduleType.WEEKENDS_ONLY -> "Выходные"
                        SupplementScheduleType.SPECIFIC_WEEKDAYS -> {
                            val days = it.weekdays.orEmpty().split(",").mapNotNull { d -> d.toIntOrNull() }
                                .map { d -> weekdayName(d) }
                            "По дням: ${days.joinToString(", ")}"
                        }
                    }

                    binding.textNotes.text = if (it.notes.isNullOrBlank()) {
                        "Без заметок"
                    } else {
                        "Заметки: ${it.notes}"
                    }
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
