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
import com.example.dogscare.databinding.FragmentFormAdoptBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormAdoptFragment : Fragment() {
    private lateinit var binding: FragmentFormAdoptBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private var archiveMap: Map<*, *>? = null

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormAdoptBinding.inflate(inflater, container, false)
        val fireId = arguments?.getString("fireId")
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)

        if(fireId != null){
            fetchName(fireId) { name ->
                binding.textViewName.text = name.uppercase()
            }

            if(activity is ActivityAdd) {
                (activity as? ActivityAdd)?.setToolbarTitle("Adopcja")

                buttonSave.setOnClickListener{
                    saveNewAdoptionData(fireId)
                }

            }else if(activity is ActivityDogDetails) {
                (activity as? ActivityDogDetails)?.setToolbarTitle("Adopcja")
                fetchAdoptionData(fireId)

                buttonSave.setOnClickListener{
                    updateAdoptionData(fireId)
                }
            }
        }

        //data początkowa
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.editTextAdoptionData.setText(formatter.format(defaultDate.time))

        binding.editTextAdoptionData.setOnClickListener{
            showDatePickerDialog()
        }

        return binding.root
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

    private fun showDatePickerDialog() {
        val year = defaultDate.get(Calendar.YEAR)
        val month = defaultDate.get(Calendar.MONTH)
        val day = defaultDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = myDateFormat(year, monthOfYear, dayOfMonth) //format: dd.MM.rrrr
                binding.editTextAdoptionData.setText(selectedDate)

                defaultDate.set(year, monthOfYear, dayOfMonth)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveNewAdoptionData(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobieranie danych z formularza
        val name = binding.editTextName.text.toString().trim()
        val surname = binding.editTextSurname.text.toString().trim()
        val city = binding.editTextCity.text.toString().trim()
        val street = binding.editTextStreet.text.toString().trim()
        val houseNr = binding.editTextHouseNumber.text.toString().trim()
        val phoneNr = binding.editTextPhone.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val adoptionDate = binding.editTextAdoptionData.text.toString().trim()

        //każde pole musi oprócz email być wypełnione
        if (name.isEmpty() || surname.isEmpty() || city.isEmpty() || street.isEmpty()
            || phoneNr.isEmpty() || adoptionDate.isEmpty()) {

            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }

        //dane adopcji
        val adoptionData = Adoption(
            name = name,
            surname = surname,
            city = city,
            street = street,
            houseNr = houseNr,
            phoneNr = phoneNr,
            email = email,
            adoptionDate =  stringToTimestamp(adoptionDate),
        )

        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        dogRef.update("archiveData", adoptionData)
            .addOnSuccessListener {
                confirmAdoption(fireId, userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateAdoptionData(fireId : String = "") {
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobieranie danych z formularza
        val name = binding.editTextName.text.toString().trim()
        val surname = binding.editTextSurname.text.toString().trim()
        val city = binding.editTextCity.text.toString().trim()
        val street = binding.editTextStreet.text.toString().trim()
        val houseNr = binding.editTextHouseNumber.text.toString().trim()
        val phoneNr = binding.editTextPhone.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val adoptionDate = binding.editTextAdoptionData.text.toString().trim()

        //każde pole musi być wypełnione
        if (name.isEmpty() || surname.isEmpty() || city.isEmpty() || street.isEmpty()
            || phoneNr.isEmpty() || adoptionDate.isEmpty()) {

            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }

        //dane adopcji
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId) // używa podanego ID

        val updatedFields = mutableMapOf<String, Any>()
        if(archiveMap != null){
            if (name != (archiveMap!!.get("name") as? String ?: "")) updatedFields["archiveData.name"] = name
            if (surname != (archiveMap!!["surname"] as? String ?: "")) updatedFields["archiveData.surname"] = surname
            if (city != (archiveMap!!["city"] as? String ?: "")) updatedFields["archiveData.city"] = city
            if (street != (archiveMap!!["street"] as? String ?: "")) updatedFields["archiveData.street"] = street
            if (houseNr != (archiveMap!!["houseNr"] as? String ?: "")) updatedFields["archiveData.houseNr"] = houseNr
            if (phoneNr != (archiveMap!!["phoneNr"] as? String ?: "")) updatedFields["archiveData.phoneNr"] = phoneNr
            if (email != (archiveMap!!["email"] as? String ?: "")) updatedFields["archiveData.email"] = email
            //data
            val newTimestamp = stringToTimestamp(adoptionDate)
            val oldTimestamp = archiveMap!!["adoptionDate"] as? Timestamp

            if (newTimestamp != oldTimestamp) {
                updatedFields["archiveData.adoptionDate"] = newTimestamp
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

    private fun confirmAdoption(fireId : String, userId: String){
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        dogRef.update("archive", ArchiveType.ADOPTION)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Zapisano adopcję!", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd w zapisie.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAdoptionData(fireId : String){
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
                    archiveMap = document.get("archive") as? Map<*, *> //zapamiętanie dla późniejszego porównania

                    if (archiveMap != null) {
                        binding.editTextName.setText(archiveMap!!["name"] as? String ?: "")
                        binding.editTextSurname.setText(archiveMap!!["surname"] as? String ?: "")
                        binding.editTextCity.setText(archiveMap!!["city"] as? String ?: "")
                        binding.editTextStreet.setText(archiveMap!!["street"] as? String ?: "")
                        binding.editTextHouseNumber.setText(archiveMap!!["houseNr"] as? String ?: "")
                        binding.editTextPhone.setText(archiveMap!!["phoneNr"] as? String ?: "")
                        binding.editTextEmail.setText(archiveMap!!["email"] as? String ?: "")

                        val timestamp = archiveMap!!["adoptionDate"] as? Timestamp
                        if(timestamp != null)
                            binding.editTextAdoptionData.setText(timestampToString(timestamp))
                    } else {
                        Toast.makeText(requireContext(), "Błąd: pies nie adoptowany!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: nie udało się pobrać danych.", Toast.LENGTH_SHORT).show()
            }
    }
}