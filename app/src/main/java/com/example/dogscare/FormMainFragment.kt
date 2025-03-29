package com.example.dogscare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.dogscare.databinding.FragmentFormMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FormMainFragment : Fragment() {
    private lateinit var binding: FragmentFormMainBinding
    private lateinit var database: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormMainBinding.inflate(inflater, container, false)
       //dodac if - jesli pies istnieje - Informacje
        //jesli nie - dodaj psa
        (activity as? FormDogActivity)?.setToolbarTitle("Informacje")
        database = FirebaseFirestore.getInstance()

        binding.buttonSave.setOnClickListener{
            saveDogToDatabase()
        }
        return binding.root
    }

    private fun saveDogToDatabase() {
        //uzytkownik
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        //pobieranie danych
        val name = binding.editTextName.text.toString().trim()
        val breed = binding.editTextBreed.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(requireContext(), "Wypełnij pole IMIĘ.",Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //tworzenie psa
        val dogId = database.collection("users").document(userId)
            .collection("dogs").document()  //tworzenie ścieżki zapisu w bazie
        val dog = Dog(
            id = dogId.toString(),
            name = name,
            breed = breed
        )

        //zapis w bazie danych
        dogId.set(dog).addOnSuccessListener {
            Toast.makeText(requireContext(), "Zapisano pieska!",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.",Toast.LENGTH_LONG).show()
        }

    }

}