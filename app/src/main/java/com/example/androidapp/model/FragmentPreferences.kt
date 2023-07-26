package com.example.androidapp.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.androidapp.R
import java.text.SimpleDateFormat
import java.util.*

class FragmentPreferences : Fragment() {

    interface OnDateSelectionListener {
        fun onDatesSelected(dates: List<String>, text: String)
    }

    private lateinit var calendarView: CalendarView
    private lateinit var editText: EditText
    private lateinit var checkBoxSelectAll: CheckBox
    private lateinit var buttonConfirm: Button

    private val selectedDates = mutableListOf<String>()
    private lateinit var onDateSelectionListener: OnDateSelectionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preferences, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        editText = view.findViewById(R.id.editText)
        checkBoxSelectAll = view.findViewById(R.id.checkBoxSelectAll)
        buttonConfirm = view.findViewById(R.id.buttonConfirm)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            if (selectedDates.contains(selectedDate)) {
                selectedDates.remove(selectedDate)
            } else {
                selectedDates.add(selectedDate)
            }
        }

        checkBoxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectAllDates()
            } else {
                selectedDates.clear()
            }
        }

        buttonConfirm.setOnClickListener {
            val text = editText.text.toString()

            // add dates and ingredients to forbidden list
            // onDateSelectionListener.onDatesSelected(selectedDates, text)

            val fragmentPreferences = FragmentAccount()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentPreferences)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }

    private fun selectAllDates() {
        // Implement logic to select all dates based on your calendar requirements
    }

    fun setOnDateSelectionListener(listener: OnDateSelectionListener) {
        onDateSelectionListener = listener
    }
}