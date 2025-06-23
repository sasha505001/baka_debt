package com.example.sporttracker.presentation.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.databinding.FragmentExerciseDetailBinding
import com.example.sporttracker.presentation.viewmodel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.MediaController
import android.widget.Toast
import com.example.sporttracker.R

@AndroidEntryPoint
class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseId = args.exerciseId

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getExerciseById(exerciseId).collectLatest { exercise ->
                exercise?.let { bindExercise(it) }
            }
        }
    }

    private fun bindExercise(exercise: Exercise) = with(binding) {
        textName.text = exercise.name
        textDescription.text = exercise.description ?: "Описание отсутствует"

        if (!exercise.imageUrl.isNullOrEmpty()) {
            imageExercise.visibility = View.VISIBLE
            Glide.with(this@ExerciseDetailFragment)
                .load(exercise.imageUrl)
                .error(R.drawable.ic_broken_image)
                .into(imageExercise)
        } else {
            imageExercise.visibility = View.GONE
        }

        if (!exercise.videoUrl.isNullOrEmpty()) {
            videoExercise.visibility = View.VISIBLE
            videoExercise.setVideoURI(Uri.parse(exercise.videoUrl))
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(videoExercise)
            videoExercise.setMediaController(mediaController)
            videoExercise.setOnErrorListener { _, _, _ ->
                Toast.makeText(requireContext(), "Ошибка при воспроизведении видео", Toast.LENGTH_SHORT).show()
                true
            }
            videoExercise.setOnPreparedListener {
                it.isLooping = true
                videoExercise.start()
            }
        } else {
            videoExercise.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
