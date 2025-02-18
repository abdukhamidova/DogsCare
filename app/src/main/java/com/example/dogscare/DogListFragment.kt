package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogscare.databinding.FragmentDogListBinding
import java.text.Normalizer.Form

class DogListFragment : Fragment() {
    private lateinit var binding: FragmentDogListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDogListBinding.inflate(inflater, container, false)


        binding.floatingButtonAdd.setOnClickListener{
            val intent = Intent(requireContext(), FormDogActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}