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
import com.example.dogscare.databinding.FragmentConfirmDeathBinding
import com.example.dogscare.databinding.FragmentFormOtherBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormOtherFragment : Fragment() {
    private lateinit var binding: FragmentFormOtherBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private var archiveMap: Map<*, *>? = null

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormOtherBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
        buttonSave.visibility = View.VISIBLE

        val fireId = arguments?.getString("fireId")

        if(fireId != null){
            fetchName(fireId) { name ->
                binding.textViewName.text = name.uppercase()
            }

            if(activity is ActivityAdd){
                (activity as? ActivityAdd)?.setToolbarTitle("Inne")
                buttonSave.setOnClickListener{
                    saveArchiveData(fireId)
                }
            }
            else if(activity is ActivityArchiveDetails){
                fetchArchiveData(fireId)

                buttonSave.setOnClickListener{
                    updateArchiveData(fireId)
                }
            }
        }
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

    private fun saveArchiveData(fireId: String){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        val archiveDate = binding.editTextArchiveDate.text.toString().trim()
        val cause = binding.editTextCause.text.toString().trim()
        val notes = binding.editTextAdditonalNote.text.toString().trim()

        //pola data i miejsce powinny być uzupełnione
        if (archiveDate.isEmpty() || cause.isEmpty()) {
            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }

        //dane
        val otherData = mapOf(
            "archiveDate" to stringToTimestamp(archiveDate),
            "cause" to cause,
            "notes" to notes
        )

        //zapis
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        dogRef.update("archiveData", otherData)
            .addOnSuccessListener {
                confirmArchivization(fireId, userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmArchivization(fireId : String, userId: String){
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        dogRef.update("archive", ArchiveType.OTHER)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Przeniesiono do archiwum.", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd w zapisie.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchName(fireId: String, onResult: (String) -> Unit) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            onResult("Imię zwierzęcia")
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Imię zwierzęcia"
                    onResult(name)
                } else {
                    Toast.makeText(requireContext(), "Dokument nie istnieje", Toast.LENGTH_SHORT).show()
                    onResult("Imię zwierzęcia")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd pobierania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult("Imię zwierzęcia")
            }
    }

    private fun updateArchiveData(fireId : String) {
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobieranie danych z formularza
        val archiveDate = binding.editTextArchiveDate.text.toString().trim()
        val cause = binding.editTextCause.text.toString().trim()
        val notes = binding.editTextAdditonalNote.text.toString().trim()

        //pola data i miejsce powinny być uzupełnione
        if (archiveDate.isEmpty() || cause.isEmpty()) {
            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }

        //dane
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId) // używa podanego ID

        val updatedFields = mutableMapOf<String, Any>()
        if(archiveMap != null){
            if (cause != (archiveMap!!["cause"] as? String ?: "")) updatedFields["archiveData.cause"] = cause
            if (notes != (archiveMap!!["notes"] as? String ?: "")) updatedFields["archiveData.notes"] = notes
            //data
            val newTimestamp = stringToTimestamp(archiveDate)
            val oldTimestamp = archiveMap!!["archiveDate"] as? Timestamp

            if (newTimestamp != oldTimestamp) {
                updatedFields["archiveData.archiveDate"] = newTimestamp
            }
        }

        if (updatedFields.isNotEmpty()) {
            dogRef.update(updatedFields)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Dane zostały zaktualizowane.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(requireContext(), "Brak zmian do zapisania.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchArchiveData(fireId : String){
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
                    archiveMap = document.get("archiveData") as? Map<*, *> //zapamiętanie dla późniejszego porównania

                    if (archiveMap != null) {
                        binding.editTextCause.setText(archiveMap!!["cause"] as? String ?: "")
                        binding.editTextAdditonalNote.setText(archiveMap!!["notes"] as? String ?: "")

                        val timestamp = archiveMap!!["archiveDate"] as? Timestamp
                        if(timestamp != null)
                            binding.editTextArchiveDate.setText(timestampToString(timestamp))
                    } else {
                        Toast.makeText(requireContext(), "Błąd", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: nie udało się pobrać danych.", Toast.LENGTH_SHORT).show()
            }
    }


}