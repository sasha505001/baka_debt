package com.example.sporttracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.databinding.ItemSupplementBinding
import com.example.sporttracker.data.model.SupplementScheduleType
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.example.sporttracker.R
import org.json.JSONObject

class SupplementAdapter(
    private val onDetailClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onDelete: ((Supplement) -> Unit)? = null
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
            val scheduleMap = supplement.scheduleMapJson?.let { json ->
                runCatching { org.json.JSONObject(json) }.getOrNull()
            } ?: org.json.JSONObject()

            val scheduleSummary = scheduleMap.keys().asSequence().joinToString("\n") { time ->
                val dose = scheduleMap.optString(time)
                "$time — $dose"
            }

            textSupplementDosage.text = scheduleSummary
            textSupplementTime.text = ""

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
                SupplementScheduleType.INTERVAL_HOURS -> "Каждые ${supplement.intervalHours} ч"
            }
            itemView.setOnClickListener {
                onDetailClick(supplement.id) // теперь открывает SupplementDetailFragment
            }
            buttonMore.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.menuInflater.inflate(R.menu.supplement_item_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onEditClick(supplement.id) // теперь редактирование
                            true
                        }
                        R.id.menu_delete -> {
                            AlertDialog.Builder(it.context)
                                .setTitle("Удалить добавку?")
                                .setMessage("Вы уверены, что хотите удалить добавку?")
                                .setPositiveButton("Да") { _, _ ->
                                    onDelete?.invoke(supplement)
                                }
                                .setNegativeButton("Отмена", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
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
