package com.example.sporttracker.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.databinding.FragmentAddExerciseBinding
import com.example.sporttracker.presentation.viewmodel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddExerciseFragment : Fragment() {

    private var _binding: FragmentAddExerciseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels()
    private val args: AddExerciseFragmentArgs by navArgs()

    private var currentExercise: Exercise? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val exerciseId = args.exerciseId
        if (exerciseId != -1) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getExerciseById(exerciseId).collectLatest { exercise ->
                    exercise?.let {
                        currentExercise = it
                        binding.editName.setText(it.name)
                        binding.editDescription.setText(it.description)
                    }
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val description = binding.editDescription.text.toString().trim()

            if (name.isNotEmpty()) {
                val updated = currentExercise?.copy(
                    name = name,
                    description = description
                )
                if (updated != null) {
                    viewModel.update(updated)
                    Toast.makeText(requireContext(), "Упражнение обновлено", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.insert(Exercise(name = name, description = description))
                    Toast.makeText(requireContext(), "Упражнение добавлено", Toast.LENGTH_SHORT).show()
                }
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
