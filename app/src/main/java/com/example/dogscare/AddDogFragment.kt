package com.example.dogscare

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Toast
import com.example.dogscare.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDogFragment : Fragment() {
    private lateinit var binding: FragmentAddDogBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddDogBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
        (activity as? ActivityAdd)?.setToolbarTitle("Dodaj psa")

        //ustawienie dziesiejszej daty
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.editTextDateArrival.setText(formatter.format(defaultDate.time))

        binding.editTextDateArrival.setOnClickListener{
            showDatePickerDialog()
        }

        buttonSave.setOnClickListener{
            saveDogToDatabase()
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
                binding.editTextDateArrival.setText(selectedDate)

                defaultDate.set(year, monthOfYear, dayOfMonth)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = defaultDate.timeInMillis
        datePickerDialog.show()
    }

    private fun saveDogToDatabase() {
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobieranie danych
        val name = binding.editTextName.text.toString().trim()
        val chip = binding.editTextChip.text.toString().trim()
        val breed = binding.editTextBreed.text.toString().trim()
        val gender = binding.editTextGender.text.toString().trim()
        val date = binding.editTextDateArrival.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(requireContext(), "Wypełnij pole IMIĘ.", Toast.LENGTH_SHORT).show()
            return
        }

        //tworzenie psa
        val dogRef = database.collection("users").document(userId)
                .collection("dogs").document() // tworzy nowy dokument z losowym ID

        val dog = Dog(
            fireId = dogRef.id,
            name = name,
            chip = chip,
            breed = breed,
            gender = gender,
            arrivalDate = stringToTimestamp(date)
        )

        //zapis w bazie danych (pierwszy raz)
        dogRef.set(dog).addOnSuccessListener {
            Toast.makeText(requireContext(), "Zapisano pieska!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.", Toast.LENGTH_LONG).show()
        }
    }
}