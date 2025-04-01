package com.example.dogscare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogscare.databinding.FragmentArchiveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArchiveFragment : Fragment() {
    private lateinit var binding: FragmentArchiveBinding
    private lateinit var adapter: DogAdapter
    private val db = FirebaseFirestore.getInstance()
    private val dogs = mutableListOf<Dog>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArchiveBinding.inflate(inflater, container, false)
        (activity as? MainActivity)?.setToolbarTitle("Archiwum")

        //ustawianie listy psow
        setupRecyclerView()
        fetchDogsFromDatabase()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = DogAdapter(dogs)
        binding.dogListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dogListRecyclerView.adapter = adapter
    }

    private fun fetchDogsFromDatabase() {
        //user ID obecnego zalogowanego uzytkownika
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            //pobieranie psow
            db.collection("users").document(userId).collection("dogs")
                .get().addOnSuccessListener { documents ->
                    //od kazdego psa pobieram pola do wyswietlenia
                    for (document in documents) {
                        val dogName = document.getString("name") //pobieranie imienia
                        val ifAdopted = document.getBoolean("adopted") //dla sprawdzenia czy pies byl adoptowany

                        if (dogName != null && ifAdopted == true) {
                            dogs.add(Dog(name = dogName))
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }
}