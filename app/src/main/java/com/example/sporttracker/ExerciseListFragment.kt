package com.example.sporttracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sporttracker.databinding.FragmentExerciseListBinding
import com.example.sporttracker.presentation.viewmodel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.sporttracker.data.adapter.ExerciseAdapter
import androidx.recyclerview.widget.LinearLayoutManager

@AndroidEntryPoint
class ExerciseListFragment : Fragment() {

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExerciseAdapter
    private val viewModel: ExerciseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExerciseAdapter (
            onItemClick = { exercise ->
                val action = ExerciseListFragmentDirections
                    .actionExerciseListFragmentToExerciseDetailFragment(exercise.id)
                findNavController().navigate(action)
            },
            onEdit = { exercise ->
                Toast.makeText(requireContext(), "Редактировать: ${exercise.name}", Toast.LENGTH_SHORT).show()
                // TODO: переход к экрану редактирования
            },
            onDelete = { exercise ->
                Toast.makeText(requireContext(), "Удалить: ${exercise.name}", Toast.LENGTH_SHORT).show()
                // TODO: показать диалог подтверждения и удалить через ViewModel
            }
        )
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exercises.collectLatest { list ->
                adapter.submitList(list)
            }
        }
        binding.fabAddExercise.setOnClickListener {
            findNavController().navigate(R.id.addExerciseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
