package com.example.sporttracker.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sporttracker.databinding.FragmentSupplementListBinding
import com.example.sporttracker.presentation.adapter.SupplementAdapter
import com.example.sporttracker.presentation.viewmodel.SupplementViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.sporttracker.R
import com.example.sporttracker.presentation.fragment.SupplementListFragmentDirections
import androidx.core.os.bundleOf

@AndroidEntryPoint
class SupplementListFragment : Fragment() {

    private var _binding: FragmentSupplementListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SupplementViewModel by viewModels()
    private lateinit var adapter: SupplementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplementListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SupplementAdapter(
            onDetailClick = { supplementId ->
                val action = SupplementListFragmentDirections
                    .actionSupplementListFragmentToSupplementDetailFragment(supplementId)
                findNavController().navigate(action)
            },
            onEditClick = { supplementId ->
                val action = SupplementListFragmentDirections
                    .actionSupplementListFragmentToAddSupplementFragment(supplementId)
                findNavController().navigate(action)
            },
            onDelete = { supplement ->
                viewModel.delete(supplement)
            }
        )
        binding.recyclerSupplements.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSupplements.adapter = adapter

        binding.fabAddSupplement.setOnClickListener {
            findNavController().navigate(R.id.action_supplementListFragment_to_addSupplementFragment)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.supplements.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
