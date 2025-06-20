package com.example.sporttracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.databinding.ItemSupplementBinding
import com.example.sporttracker.data.model.SupplementScheduleType

class SupplementAdapter(
    private val onClick: (Int) -> Unit
) : ListAdapter<Supplement, SupplementAdapter.SupplementViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Supplement>() {
        override fun areItemsTheSame(oldItem: Supplement, newItem: Supplement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Supplement, newItem: Supplement): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplementViewHolder {
        val binding = ItemSupplementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SupplementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SupplementViewHolder(private val binding: ItemSupplementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(supplement: Supplement) = with(binding) {
            textSupplementName.text = supplement.name
            textSupplementDosage.text = supplement.dosage
            textSupplementTime.text = "В ${supplement.time}"

            textSupplementSchedule.text = when (supplement.scheduleType) {
                SupplementScheduleType.EVERY_DAY -> "Каждый день"
                SupplementScheduleType.EVERY_N_DAYS -> "Каждые ${supplement.intervalDays} дней"
                SupplementScheduleType.WEEKDAYS_ONLY -> "По будням"
                SupplementScheduleType.WEEKENDS_ONLY -> "По выходным"
                SupplementScheduleType.SPECIFIC_WEEKDAYS -> {
                    val days = supplement.weekdays.orEmpty().split(",")
                        .mapNotNull { it.toIntOrNull() }
                        .map { weekdayName(it) }
                    "По дням: ${days.joinToString(", ")}"
                }
            }
            itemView.setOnClickListener {
                onClick(supplement.id)
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
    }
}
