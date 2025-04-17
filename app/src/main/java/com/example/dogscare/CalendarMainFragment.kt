package com.example.dogscare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogscare.databinding.FragmentCalendarMainBinding

class CalendarMainFragment : Fragment() {
    private lateinit var binding: FragmentCalendarMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarMainBinding.inflate(inflater, container, false)
        (activity as? ActivityMain)?.setToolbarTitle("Kalendarz")

        //ustawienie obecnej daty na kalendarzu
        //binding.calendarViewShelterCalendar.setDate(Calendar.getInstance())




        return binding.root
    }
}