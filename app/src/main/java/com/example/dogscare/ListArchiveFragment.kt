package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogscare.databinding.FragmentArchiveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListArchiveFragment : Fragment() {
    private lateinit var binding: FragmentArchiveBinding
    private lateinit var adapter: DogAdapter
    private val db = FirebaseFirestore.getInstance()
    private val dogs = mutableListOf<Dog>()
    private lateinit var viewModel: DogsListViewModel
    private var selectedArchiveType = ArchiveType.ARCHIVED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArchiveBinding.inflate(inflater, container, false)
        (activity as? ActivityMain)?.setToolbarTitle("Adoptowane psy")

        binding.imageButtonFilter.setOnClickListener{
            FilterArchiveDialog(requireContext(), selectedArchiveType) { archiveType ->
                selectedArchiveType = archiveType
                if(archiveType == ArchiveType.ARCHIVED)
                    viewModel.fetchArchivedDogsFromDatabase()
                else viewModel.fetchDogsFromDatabase(archiveType)
            }.show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DogsListViewModel::class.java)

        //ustawienie recycleview
        setupRecyclerView()
        viewModel.dogsList.observe(viewLifecycleOwner) { dogs ->
            //adapter
            adapter.updateList(dogs)
            binding.textViewNotFound.visibility = if (dogs.isEmpty()) View.VISIBLE else View.GONE
        }

        //pobieranie psow z bazy
        viewModel.fetchArchivedDogsFromDatabase()

        //wyszukaj
        binding.editTextSearch.addTextChangedListener{ text ->
            val query = text.toString().trim()
            if(query.isNotEmpty()) viewModel.filterDogs(query)
            else viewModel.fetchArchivedDogsFromDatabase()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchArchivedDogsFromDatabase() // ponowne pobranie danych
    }

    private fun setupRecyclerView() {
        adapter = DogAdapter(dogs)

        adapter.setOnItemClickListener(object : DogAdapter.OnItemClickListener{
            override fun onItemClick(dog: Dog) {
                val intent = Intent(requireContext(), ActivityDogDetails::class.java)
                intent.putExtra("openCommand", "DogInfo")
                intent.putExtra("fireId", dog.fireId)
                startActivity(intent)
            }
        })

        binding.dogListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dogListRecyclerView.adapter = adapter
    }
}