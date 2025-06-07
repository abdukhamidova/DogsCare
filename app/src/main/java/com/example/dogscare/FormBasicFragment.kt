package com.example.dogscare

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.dogscare.databinding.FragmentFormBasicBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar


class FormBasicFragment : Fragment() {
    private lateinit var binding: FragmentFormBasicBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private var originalDog: Dog? = null

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś
   //private lateinit var storage: FirebaseStorage

    //zdjęcie
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    //private lateinit var imageView: ImageView
    //private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormBasicBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)

        val fireId = arguments?.getString("fireId")

        //uzupełnienie pól
        if (fireId != null) {
            if(activity is ActivityDogDetails) {
                (activity as? ActivityDogDetails)?.setToolbarTitle("Informacje")

                //region DOG PICTURE
                pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    uri?.let {
                        binding.imageViewPicture.setImageURI(it)
                        selectedImageUri = it //Uri dla bazy
                    }
                }

                binding.imageViewPicture.setOnClickListener{
                    pickImageLauncher.launch("image/*")
                }
                //endregion
            }

            if(activity is ActivityArchiveDetails) {
                buttonSave.visibility = View.GONE
            }

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
        //datePickerDialog.datePicker.maxDate = defaultDate.timeInMillis
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
        val appearance = binding.editTextAppearance.text.toString().trim()
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
            if (appearance != original.appearance) updates["appearance"] = appearance
            if (notes != original.note) updates["note"] = notes
            if (arrivalDate != original.arrivalDate) updates["arrivalDate"] = arrivalDate
        }

        //UPDATE DOG PICTURE
        val newImageUri = selectedImageUri
        if (newImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("dog_images/$userId/$fireId.jpg") // ścieżka do storage

            //(1) nowe zdjęcie
            imageRef.putFile(newImageUri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    updates["imageUrl"] = uri.toString()

                    //aktualizacja psa
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
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Błąd ładowania zdjęcia.", Toast.LENGTH_LONG).show()
            }
        } else {

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
                if (document != null && document.exists()) {
                    val dog = document.toObject(Dog::class.java)

                    dog?.let { d ->
                        with(binding) {
                            editTextName.setText(d.name)
                            editTextChip.setText(d.chip)
                            editTextBreed.setText(d.breed)
                            editTextGender.setText(d.gender)
                            editTextWeight.setText(d.weight)
                            editTextAppearance.setText(d.appearance)
                            editTextNotes.setText(d.note)
                            editTextDateArrival.setText(timestampToString(d.arrivalDate))
                        }

                        //dalsze porównanie
                        originalDog = d

                        //domyslna data
                        d.arrivalDate.let {
                            val cal = Calendar.getInstance()
                            cal.time = it.toDate()
                            defaultDate = cal
                        }

                        //zdjecie
                        if (d.imageUrl.isNotEmpty()) {
                            Log.d("DogImage", "Loading image from URL: ${d.imageUrl}")
                            Glide.with(requireContext())
                                .load(d.imageUrl)
                                .into(binding.imageViewPicture)
                        } else {
                            Log.d("DogImage", "No image URL found")
                        }
                    }
                } else {
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

        return "lat: $years, miesięcy: $months "
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






