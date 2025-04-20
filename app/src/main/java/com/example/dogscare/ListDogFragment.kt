package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogscare.databinding.FragmentDogListBinding
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider

class ListDogFragment : Fragment() {
    private lateinit var binding: FragmentDogListBinding
    private lateinit var adapter: DogAdapter
    private val dogs = mutableListOf<Dog>()
    private lateinit var viewModel: DogsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDogListBinding.inflate(inflater, container, false)
        (activity as? ActivityMain)?.setToolbarTitle("Lista psów")

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
        viewModel.fetchDogsFromDatabase(ArchiveType.ACTIVE)

        //dodaj psa
        binding.floatingButtonAdd.setOnClickListener{
            val intent = Intent(requireContext(), ActivityAdd::class.java)
            intent.putExtra("openCommand", "AddDog")
            startActivity(intent)
        }

        //wyszukaj
        binding.editTextSearch.addTextChangedListener{ text ->
            val query = text.toString().trim()
            if(query.isNotEmpty()) viewModel.filterDogs(query, ArchiveType.ACTIVE)
            else viewModel.fetchDogsFromDatabase(ArchiveType.ACTIVE)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchDogsFromDatabase(ArchiveType.ACTIVE) // ponowne pobranie danych
    }

    private fun setupRecyclerView() {
        // Przekazujemy funkcję kliknięcia do adaptera
        adapter = DogAdapter(dogs)

        adapter.setOnItemClickListener(object : DogAdapter.OnItemClickListener{
            override fun onItemClick(dog: Dog) {
                val intent = Intent(requireContext(), ActivityDogDetails::class.java)
                intent.putExtra("fireId", dog.fireId)
                startActivity(intent)
            }
        })

        binding.dogListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dogListRecyclerView.adapter = adapter
    }
}