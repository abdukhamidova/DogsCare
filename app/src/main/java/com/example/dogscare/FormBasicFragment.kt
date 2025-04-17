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
import com.example.dogscare.databinding.FragmentFormBasicBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class FormBasicFragment : Fragment() {
    private lateinit var binding: FragmentFormBasicBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private var adopted = false
    private var originalDog: Dog? = null

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś
   //private lateinit var storage: FirebaseStorage

    //zdjęcie
    //private lateinit var imageView: ImageView
    //private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormBasicBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)

        //fragment do podglądu/edycji psa
        (activity as? ActivityDogDetails)?.setToolbarTitle("Informacje")
        val fireId = arguments?.getString("fireId")

        //uzupełnienie pól
        if (fireId != null) {
            fetchDogFromDatabase(fireId)
            fillAgeField(fireId)
        }

        binding.editTextDateArrival.setOnClickListener{
            showDatePickerDialog()
        }

        //zapisanie zmian
        buttonSave.setOnClickListener{
            if (fireId != null) {
                updateDogInDatabase(fireId)
            }
            val intent = Intent(requireContext(), ActivityMain::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        //region DOG PICTURE
        /*binding.imageViewPicture.setOnClickListener{
            ImagePicker.with(this)
                .crop() // Możesz usunąć, jeśli nie chcesz przycinać zdjęcia
                .galleryOnly()
                .compress(1024) // Kompresja do 1MB
                .maxResultSize(1080, 1080) // Maksymalna wielkość
                .createIntent { intent ->
                    startForImageResult.launch(intent)
                }
        }*/
        //endregion


        return binding.root
    }

    //region PICTURE FUNCTIONS
    /*// Obsługa wybranego zdjęcia
    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data // Pobierz Uri zdjęcia
                imageView.setImageURI(imageUri) // Wyświetl w ImageView
                uploadImageToFirebase() // Prześlij do Firebase
            } else {
                Toast.makeText(this, "Nie wybrano zdjęcia", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImageToFirebase() {
        if (imageUri == null) return

        val storageReference = FirebaseStorage.getInstance().reference
            .child("dogs/${UUID.randomUUID()}.jpg") // Tworzy unikalną nazwę pliku

        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString()) // Zapisz URL do Firestore
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd przesyłania", Toast.LENGTH_SHORT).show()
            }
    }*/
    //endregion

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

    private fun updateDogInDatabase(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid

        val name = binding.editTextName.text.toString().trim()
        val chip = binding.editTextChip.text.toString().trim()
        val breed = binding.editTextBreed.text.toString().trim()
        val gender = binding.editTextGender.text.toString().trim()
        val weight = binding.editTextWeight.text.toString().trim()
        val dateString = binding.editTextDateArrival.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim()
        val arrivalDate = stringToTimestamp(dateString)

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Wypełnij pole IMIĘ.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()
        originalDog?.let { original ->
            if (name != original.name) updates["name"] = name
            if (chip != original.chip) updates["chip"] = chip
            if (breed != original.breed) updates["breed"] = breed
            if (gender != original.gender) updates["gender"] = gender
            if (weight != original.weight) updates["weight"] = weight
            if (notes != original.note) updates["note"] = notes
            if (arrivalDate != original.arrivalDate) updates["arrivalDate"] = arrivalDate
        }

        if (updates.isEmpty()) {
            Toast.makeText(requireContext(), "Brak zmian do zapisania.", Toast.LENGTH_SHORT).show()
            return
        }

        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)

        dogRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Zaktualizowano pieska!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd aktualizacji: ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchDogFromDatabase(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get().addOnSuccessListener { document ->
                if(document != null && document.exists()){
                    val name = document.getString("name") ?: ""
                    val chip = document.getString("chip") ?: ""
                    val breed = document.getString("breed") ?: ""
                    val gender = document.getString("gender") ?: ""
                    val weight = document.getString("weight") ?: ""
                    val notes = document.getString("note") ?: ""

                    adopted = document.getBoolean("adopted") ?: false
                    //data
                    val arrivalDateTimestamp = document.getTimestamp("arrivalDate")
                    val arrivalDate = timestampToString(arrivalDateTimestamp)

                    //zapisanie w polach
                    binding.editTextName.setText(name)
                    binding.editTextChip.setText(chip)
                    binding.editTextBreed.setText(breed)
                    binding.editTextGender.setText(gender)
                    binding.editTextWeight.setText(weight)
                    binding.editTextNotes.setText(notes)

                    //zapisanie aby potem porównywać
                    originalDog = arrivalDateTimestamp?.let {
                        Dog(
                            fireId = fireId,
                            name = name,
                            chip = chip,
                            breed = breed,
                            gender = gender,
                            weight = weight,
                            note = notes,
                            arrivalDate = it
                        )
                    }

                    //data
                    arrivalDateTimestamp?.let {
                        val cal = Calendar.getInstance()
                        cal.time = it.toDate() //ustawienie domyślnej daty
                        defaultDate = cal
                    }
                    binding.editTextDateArrival.setText(arrivalDate)
                } else {
                    // jeśli dokument nie istnieje
                    Toast.makeText(requireContext(), "Błąd: nie udało się pobrać psa!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: ${e.message}.",Toast.LENGTH_LONG).show()
            }
    }

    fun calculateDogAge(estimatedDoB: Timestamp): String {
        val currentDate = Calendar.getInstance()    //teraz
        val dogBirthDate = estimatedDoB.toDate()    //data urodzenia psa

        val birthDateCalendar = Calendar.getInstance()
        birthDateCalendar.time = dogBirthDate

        //obliczanie różnicy
        val years = currentDate.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR)
        var months = currentDate.get(Calendar.MONTH) - birthDateCalendar.get(Calendar.MONTH)

        if (months < 0) {
            months += 12
        }

        return "$years lat i $months miesięcy"
    }

    private fun fillAgeField(fireId: String){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val healthMap = document.get("health") as? Map<*, *>
                    val estimatedDoB = healthMap?.get("estimatedDoB") as? Timestamp

                    if (estimatedDoB != null) {
                        binding.editTextAge.setText(calculateDogAge(estimatedDoB))
                    }
                }
            }
    }

}