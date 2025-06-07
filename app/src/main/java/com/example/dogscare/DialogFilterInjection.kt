package com.example.dogscare

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.dogscare.databinding.DialogFilterInjectionBinding
import java.util.Calendar

class FilterInjectionDialog(private val context: Context){
    private lateinit var binding: DialogFilterInjectionBinding
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar = Calendar.getInstance()


    interface DateSelectionListener {
        fun onDateSelected(allDogs: Boolean, startDate: Calendar, endDate: Calendar)
    }

    private var dateSelectionListener: DateSelectionListener? = null

    fun setDateSelectionListener(listener: DateSelectionListener) {
        this.dateSelectionListener = listener
    }

    fun show() {
        binding = DialogFilterInjectionBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        //ustawienia wyboru dat
        binding.editTextStartDate.setOnClickListener {
            binding.radioButtonAllDogs.isChecked = false
            showDatePickerDialog(binding.editTextStartDate) { selectedCalendar ->
                selectedStartDate = selectedCalendar
                selectedEndDate = selectedCalendar

                val formattedDate = myDateFormat(
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
                )
                binding.editTextStartDate.setText(formattedDate)
                binding.editTextEndDate.setText(formattedDate)
            }
        }

        binding.editTextEndDate.setOnClickListener {
            binding.radioButtonAllDogs.isChecked = false
            showDatePickerDialog(binding.editTextEndDate, selectedStartDate) { selectedCalendar ->
                if (selectedCalendar.before(selectedStartDate)) {
                    Toast.makeText(context, "Data końcowa nie może być wcześniejsza niż początkowa", Toast.LENGTH_SHORT).show()

                    val formattedStart = myDateFormat(
                        selectedStartDate.get(Calendar.YEAR),
                        selectedStartDate.get(Calendar.MONTH),
                        selectedStartDate.get(Calendar.DAY_OF_MONTH)
                    )
                    binding.editTextEndDate.setText(formattedStart)
                    selectedEndDate = selectedStartDate
                } else {
                    selectedEndDate = selectedCalendar
                    val formattedEnd = myDateFormat(
                        selectedCalendar.get(Calendar.YEAR),
                        selectedCalendar.get(Calendar.MONTH),
                        selectedCalendar.get(Calendar.DAY_OF_MONTH)
                    )
                    binding.editTextEndDate.setText(formattedEnd)
                }
            }
        }


        binding.buttonConfirm.setOnClickListener {
            dateSelectionListener?.onDateSelected(binding.radioButtonAllDogs.isChecked,selectedStartDate, selectedEndDate)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDatePickerDialog(
        dateEditText: EditText,
        minDate: Calendar? = null,
        onDateSelected: ((Calendar) -> Unit)? = null
    ) {
        val year = defaultDate.get(Calendar.YEAR)
        val month = defaultDate.get(Calendar.MONTH)
        val day = defaultDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val selectedDateStr = myDateFormat(selectedYear, selectedMonth, selectedDay)
                dateEditText.setText(selectedDateStr)

                onDateSelected?.invoke(selectedCalendar)
            },
            year, month, day
        )

        minDate?.let {
            datePickerDialog.datePicker.minDate = it.timeInMillis
        }

        datePickerDialog.show()
    }

}