package com.example.sporttracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sporttracker.R
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val onItemClick: (Exercise) -> Unit,
    private val onEdit: (Exercise) -> Unit,
    private val onDelete: (Exercise) -> Unit
) : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onEdit, onDelete)
    }

    class ExerciseViewHolder(private val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Exercise,
                 onItemClick: (Exercise) -> Unit,
                 onEdit: (Exercise) -> Unit,
                 onDelete: (Exercise) -> Unit) = with(binding) {
            textExerciseName.text = item.name
            textExerciseDescription.text = item.description ?: "Описание отсутствует"

            root.setOnClickListener {
                onItemClick(item)
            }
            menuButton.setOnClickListener {
                val popup = PopupMenu(root.context, menuButton)
                popup.inflate(R.menu.exercise_item_menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            onEdit(item)
                            true
                        }
                        R.id.menu_delete -> {
                            onDelete(item)
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
