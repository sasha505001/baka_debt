package com.example.sporttracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sporttracker.R
import com.example.sporttracker.databinding.ItemDateBinding
import android.widget.PopupMenu

class DatesAdapter(
    private var dates: MutableList<String>,
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<DatesAdapter.DateViewHolder>() {

    fun updateDates(newDates: MutableList<String>) {
        dates = newDates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: String) = with(binding) {
            textDate.text = date
            buttonMenu.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.menu_date_item)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEdit(date)
                            true
                        }
                        R.id.action_delete -> {
                            onDelete(date)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}
