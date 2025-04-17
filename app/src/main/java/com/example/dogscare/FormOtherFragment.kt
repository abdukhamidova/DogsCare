package com.example.dogscare

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.example.dogscare.databinding.FragmentConfirmDeathBinding
import com.example.dogscare.databinding.FragmentFormOtherBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormOtherFragment : Fragment() {
    private lateinit var binding: FragmentFormOtherBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormOtherBinding.inflate(inflater, container, false)
        val fireId = arguments?.getString("fireId")

        //data początkowa
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.editTextArchiveDate.setText(formatter.format(defaultDate.time))

        binding.editTextArchiveDate.setOnClickListener{
            showDatePickerDialog()
        }

        return binding.root
    }

    private fun showDatePickerDialog() {
        val year = defaultDate.get(Calendar.YEAR)
        val month = defaultDate.get(Calendar.MONTH)
        val day = defaultDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = myDateFormat(year, monthOfYear, dayOfMonth) //format: dd.MM.rrrr
                binding.editTextArchiveDate.setText(selectedDate)

                defaultDate.set(year, monthOfYear, dayOfMonth)
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}