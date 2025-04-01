package com.example.dogscare

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dogscare.databinding.FragmentFormMainBinding
import com.google.android.gms.cast.framework.media.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FormInfoFragment : Fragment() {
    private lateinit var binding: FragmentFormMainBinding
    private lateinit var database: FirebaseFirestore
   //private lateinit var storage: FirebaseStorage

    //zdjęcie
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormMainBinding.inflate(inflater, container, false)
       //dodac if - jesli pies istnieje - Informacje
        //jesli nie - dodaj psa
        (activity as? FormDogActivity)?.setToolbarTitle("Informacje")
        database = FirebaseFirestore.getInstance()

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

        binding.buttonSave.setOnClickListener{
            saveDogToDatabase()
        }
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

    private fun saveDogToDatabase() {
        //uzytkownik
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        //pobieranie danych
        val name = binding.editTextName.text.toString().trim()
        val chip = binding.editTextChip.text.toString().trim()
        val breed = binding.editTextBreed.text.toString().trim()
        val gender = binding.editTextGender.text.toString().trim()
        val weight = binding.editTextWeight.text.toString().trim()

        val age = binding.editTextAgeArrival.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(requireContext(), "Wypełnij pole IMIĘ.",Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //tworzenie psa
        val dogId = database.collection("users").document(userId)
            .collection("dogs").document()  //tworzenie ścieżki zapisu w bazie

        val dogData = hashMapOf(
            "name" to name,
            "chip" to chip,
            "breed" to breed,
            "gender" to gender,
            "weight" to weight,
            "age" to age,
            "userId" to userId,  // ID użytkownika, który dodał psa
            "timestamp" to System.currentTimeMillis()  // Znacznik czasu dodania
        )

        //zapis w bazie danych
        dogId.set(dogData).addOnSuccessListener {
            Toast.makeText(requireContext(), "Zapisano pieska!",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.",Toast.LENGTH_LONG).show()
        }

    }

}