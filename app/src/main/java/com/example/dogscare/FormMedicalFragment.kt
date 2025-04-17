package com.example.dogscare

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Toast
import com.example.dogscare.databinding.FragmentFormMedicalBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class FormMedicalFragment : Fragment() {
    private lateinit var binding: FragmentFormMedicalBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormMedicalBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
        (activity as? ActivityDogDetails)?.setToolbarTitle("Zdrowie")
        val fireId = arguments?.getString("fireId")

        binding.editTextEstimatedDoB.setOnClickListener{
            showDatePickerDialog()
        }

        if (fireId != null) {
            //uzupełnienie pól
            fetchMedicalData(fireId)

            buttonSave.setOnClickListener{
                updateMedicalInfo(fireId)
            }
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
                binding.editTextEstimatedDoB.setText(selectedDate)

                defaultDate.set(year, monthOfYear, dayOfMonth)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateMedicalInfo(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobranie danych
        val allergies = binding.editTextAllergies.text.toString().trim()
        val illness = binding.editTextIllness.text.toString().trim()
        val dateOfBirth = binding.editTextEstimatedDoB.text.toString().trim()

        //ścieżka
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        val health = Health(
            allergies = allergies,
            illness =  illness,
            estimatedDoB = stringToTimestamp(dateOfBirth)
        )

        //zapis w bazie danych
        dogRef.update("health", health).addOnSuccessListener {
            Toast.makeText(requireContext(), "Zapisano!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchMedicalData(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobranie danych
        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val healthMap = document.get("health") as? Map<*, *>

                    if (healthMap != null) {
                        binding.editTextIllness.setText(healthMap["illness"] as? String ?: "")
                        binding.editTextAllergies.setText(healthMap["allergies"] as? String ?: "")

                        val timestamp = healthMap["estimatedDoB"] as? Timestamp
                        if(timestamp != null)
                            binding.editTextEstimatedDoB.setText(timestampToString(timestamp))
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: nie udało się pobrać danych.", Toast.LENGTH_SHORT).show()
            }

    }
}