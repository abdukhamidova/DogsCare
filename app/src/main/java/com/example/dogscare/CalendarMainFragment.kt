package com.example.dogscare

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.applandeo.materialcalendarview.EventDay
import com.example.dogscare.databinding.FragmentArchiveBinding
import com.example.dogscare.databinding.FragmentCalendarMainBinding
import java.util.Calendar

class CalendarMainFragment : Fragment() {
    private lateinit var binding: FragmentCalendarMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarMainBinding.inflate(inflater, container, false)
        (activity as? MainActivity)?.setToolbarTitle("Kalendarz")

        //ustawienie obecnej daty na kalendarzu
        //binding.calendarViewShelterCalendar.setDate(Calendar.getInstance())




        return binding.root
    }
}