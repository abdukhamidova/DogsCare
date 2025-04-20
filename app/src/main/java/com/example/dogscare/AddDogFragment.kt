package com.example.dogscare

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dogscare.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDogFragment : Fragment() {
    private lateinit var binding: FragmentAddDogBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    //zdjęcie
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            //dodawanie zdjęcia
            if (selectedImageUri != null) {
                uploadDogImage(userId, dog.fireId, selectedImageUri!!) { downloadUrl ->
                    dog.imageUrl = downloadUrl

                    // zaktualizuj dokument psa z URL-em zdjęcia
                    dogRef.update("imageUrl", downloadUrl)
                }
            }
            Toast.makeText(requireContext(), "Zapisano pieska!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadDogImage(userId: String, dogId: String, uri: Uri, onComplete: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("dog_images/$userId/$dogId.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Błąd podczas przesyłania zdjęcia", Toast.LENGTH_SHORT).show()
            }
    }

}