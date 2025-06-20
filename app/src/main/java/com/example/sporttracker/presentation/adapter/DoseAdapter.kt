package com.example.sporttracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sporttracker.R
import com.example.sporttracker.databinding.ItemDoseBinding
import android.widget.PopupMenu

class DoseAdapter(
    private var doses: List<Pair<String, String>>,
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    inner class DoseViewHolder(private val binding: ItemDoseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(time: String, dose: String) {
            binding.textDoseTime.text = time
            binding.textDoseAmount.text = dose
            binding.buttonMenu.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.menu_dose_item)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEdit(time)
                            true
                        }
                        R.id.action_delete -> {
                            onDelete(time)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val binding = ItemDoseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoseViewHolder(binding)
    }

    override fun getItemCount() = doses.size

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val (time, dose) = doses[position]
        holder.bind(time, dose)
    }
}
