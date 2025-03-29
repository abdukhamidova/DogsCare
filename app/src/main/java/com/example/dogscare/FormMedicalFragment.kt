package com.example.dogscare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogscare.databinding.FragmentFormMainBinding
import com.example.dogscare.databinding.FragmentFormMedicalBinding

class FormMedicalFragment : Fragment() {
    private lateinit var binding: FragmentFormMedicalBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormMedicalBinding.inflate(inflater, container, false)
        //dodac if - jesli pies istnieje - Informacje
        //jesli nie - dodaj psa
        (activity as? FormDogActivity)?.setToolbarTitle("Książeczka medyczna")

        return binding.root
    }
}