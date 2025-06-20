package com.example.sporttracker.presentation.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sporttracker.R
import com.example.sporttracker.databinding.FragmentSpecificDatesEditorBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import com.example.sporttracker.presentation.adapter.DatesAdapter

class SpecificDatesEditorFragment : Fragment() {

    private var _binding: FragmentSpecificDatesEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DatesAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dates = mutableSetOf<String>() // уникальные ISO-строки

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecificDatesEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val jsonArray = arguments?.getString("specificDatesJson")?.let {
            runCatching { JSONArray(it) }.getOrNull()
        }

        jsonArray?.let {
            for (i in 0 until it.length()) {
                dates.add(it.getString(i))
            }
        }

        adapter = DatesAdapter(
            dates = dates.toMutableList(),
            onEdit = { oldDate -> showEditDateDialog(oldDate) },
            onDelete = { date ->
                dates.remove(date)
                adapter.updateDates(dates.toMutableList())
            }
        )

        binding.recyclerDates.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDates.adapter = adapter

        binding.buttonAddDate.setOnClickListener { showDatePicker() }
    }

    private fun showEditDateDialog(oldDate: String) {
        val calendar = Calendar.getInstance()
        try {
            val date = dateFormat.parse(oldDate)
            date?.let { calendar.time = it }
        } catch (_: Exception) {}

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val newDate = dateFormat.format(calendar.time)

                if (newDate == oldDate || dates.contains(newDate)) {
                    Toast.makeText(context, "Эта дата уже есть", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                dates.remove(oldDate)
                dates.add(newDate)
                adapter.updateDates(dates.toMutableList())
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateStr = dateFormat.format(calendar.time)
                if (dates.add(dateStr)) {
                    adapter.updateDates(dates.toMutableList())
                } else {
                    Toast.makeText(context, "Дата уже выбрана", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_schedule_editor, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
            val resultJson = JSONArray(dates.toList()).toString()
            setFragmentResult("specificDatesResult", bundleOf("specificDatesJson" to resultJson))
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
