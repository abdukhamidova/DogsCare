package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.example.dogscare.databinding.FragmentCalendarShelterBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarShelterFragment : Fragment() {
    private lateinit var binding: FragmentCalendarShelterBinding
    private lateinit var eventAdapter: EventAdapter
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    private var dates: MutableSet<Timestamp> = mutableSetOf()
    private val events = mutableListOf<EventDisplayModel>()
    private var calendarDays = mutableListOf<CalendarDay>()
    private var clickedDay: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarShelterBinding.inflate(inflater, container, false)
        val buttonAdd = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
        (activity as? ActivityMain)?.setToolbarTitle("Kalendarz")

        //wydarzenia
        getEventsFromDatabase()
        setupRecyclerView()

        binding.calendarViewShelterCalendar.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(selectedDay: CalendarDay) {
                selectedDay.labelColor = R.color.clickedDay_label
                if (calendarDays.isNotEmpty()) {
                    calendarDays[calendarDays.lastIndex].labelColor = null
                    calendarDays[calendarDays.lastIndex] = selectedDay
                } else {
                    calendarDays.add(selectedDay)
                }
                binding.calendarViewShelterCalendar.setCalendarDays(calendarDays)

                clickedDay = selectedDay.calendar
                val clickedDate = Timestamp(selectedDay.calendar.time)
                val dayEvents = events.filter { event ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val clicked = sdf.parse(sdf.format(clickedDate.toDate()))
                    val start = sdf.parse(sdf.format(event.startDate.toDate()))
                    val end = sdf.parse(sdf.format(event.endDate.toDate()))

                    !clicked.before(start) && !clicked.after(end)
                }


                setInformationLabel(dayEvents)
                eventAdapter.updateList(dayEvents)
            }
        })


        buttonAdd.setOnClickListener{
            val intent = Intent(requireContext(), ActivityAdd::class.java)
            intent.putExtra("openCommand", "AddShelterEvent")
            startActivity(intent)
        }

        binding.imageButtonAddDayEvent.setOnClickListener{
            val intent = Intent(requireContext(), ActivityAdd::class.java)
            intent.putExtra("openCommand", "AddShelterEvent")
            intent.putExtra("clickedDay", clickedDay.timeInMillis)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getTodayEvents()
        getEventsFromDatabase()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList())
        getTodayEvents()
        eventAdapter.setOnItemClickListener(object : EventAdapter.OnItemClickListener{
            override fun onItemClick(event: EventDisplayModel) {
                val intent = Intent(requireContext(), ActivityEvent::class.java)
                intent.putExtra("openCommand", "ShelterEvent")
                intent.putExtra("fireId", event.fireId)
                startActivity(intent)
            }
        })

        binding.eventListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.eventListRecyclerView.adapter = eventAdapter
    }

    private fun getTodayEvents() {
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        val todayString = timestampToString(Timestamp.now())

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val todayEvents = mutableListOf<EventDisplayModel>()

                for (document in documents) {
                    val event = document.toObject(EventDisplayModel::class.java)
                    val eventStartString = timestampToString(event.startDate)

                    //porównanie dat
                    if (eventStartString == todayString) {
                        todayEvents.add(event)
                    }
                }
                setInformationLabel(todayEvents)
                todayEvents.sortBy { it.startDate }
                eventAdapter.updateList(todayEvents)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun setInformationLabel(dayEvents: List<EventDisplayModel>) {
        val todayString = timestampToString(Timestamp.now()) //dzisiaj

        if (dayEvents.isEmpty()) {
            binding.textViewWydarzenia.text = "Brak wydarzeń"
        } else {
            val isTodayEvent = dayEvents.all { event ->
                timestampToString(event.startDate) == todayString
            }

            if (isTodayEvent) {
                binding.textViewWydarzenia.text = "Wydarzenia dzisiejsze"
            } else {
                binding.textViewWydarzenia.text = "Wydarzenia"
            }
        }
    }

    private fun getEventsFromDatabase(){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { documents ->
                events.clear()
                dates.clear()
                for (document in documents) {
                    val event = document.toObject(EventDisplayModel::class.java)
                    event.fireId = document.id

                    events.add(event)
                    addToDates(event.startDate, event.endDate)
                }
                markEventDays()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun addToDates(start: Timestamp, end: Timestamp) {
        val calendar = Calendar.getInstance()
        calendar.time = start.toDate()
        val endDate = end.toDate()

        while (!calendar.time.after(endDate)) {
            val dateTimestamp = Timestamp(calendar.time)
            dates.add(dateTimestamp)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun markEventDays(){
        calendarDays.clear()
        calendarDays.addAll(dates.map {
            val cal = Calendar.getInstance()
            cal.time = it.toDate()
            val day = CalendarDay(cal)
            day.imageResource = R.drawable.icon_event2
            day
        })

        //puste
        val todayCal = Calendar.getInstance()
        val todayDay = CalendarDay(todayCal)
        calendarDays.add(todayDay)

        binding.calendarViewShelterCalendar.setCalendarDays(calendarDays)
    }

}