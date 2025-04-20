package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.example.dogscare.databinding.FragmentCalendarDogBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CalendarDogFragment : Fragment() {
    private lateinit var binding: FragmentCalendarDogBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private lateinit var eventAdapter: EventAdapter
    private var fireDogId: String = ""

    private var dates: MutableSet<Timestamp> = mutableSetOf()
    private val events = mutableListOf<EventDisplayModel>()
    private var calendarDays = mutableListOf<CalendarDay>()
    private var clickedDay: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarDogBinding.inflate(inflater, container, false)
        fireDogId = arguments?.getString("fireId").toString()

        if(fireDogId.isNotEmpty()){
            if(activity is ActivityDogDetails) {
                (activity as? ActivityDogDetails)?.setToolbarTitle("Kalendarz psa")
                val buttonAdd = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
                val buttonArchive = requireActivity().findViewById<ImageButton>(R.id.imageButtonArchive)
                buttonArchive.visibility = View.GONE

                buttonAdd.setOnClickListener{
                    val intent = Intent(requireContext(), ActivityAdd::class.java)
                    intent.putExtra("openCommand", "AddDogEvent")
                    intent.putExtra("fireId", fireDogId)
                    startActivity(intent)
                }

                binding.imageButtonAddDayEvent.setOnClickListener{
                    val intent = Intent(requireContext(), ActivityAdd::class.java)
                    intent.putExtra("openCommand", "AddDogEvent")
                    intent.putExtra("clickedDay", clickedDay.timeInMillis)
                    intent.putExtra("fireId", fireDogId)
                    startActivity(intent)
                }
            }
            else if(activity is ActivityArchiveDetails){
                binding.imageButtonAddDayEvent.visibility = View.GONE

            }

            //wydarzenia
            getEventsFromDatabase(fireDogId)
            setupRecyclerView(fireDogId)

            binding.calendarViewDogCalendar.setOnCalendarDayClickListener(object :
                OnCalendarDayClickListener {
                override fun onClick(selectedDay: CalendarDay) {
                    selectedDay.labelColor = R.color.clickedDay_label
                    if (calendarDays.isNotEmpty()) {
                        calendarDays[calendarDays.lastIndex].labelColor = null
                        calendarDays[calendarDays.lastIndex] = selectedDay
                    } else {
                        calendarDays.add(selectedDay)
                    }
                    binding.calendarViewDogCalendar.setCalendarDays(calendarDays)

                    clickedDay = selectedDay.calendar
                    val clickedDate = Timestamp(selectedDay.calendar.time)
                    val dayEvents = events.filter { event ->
                        val start = event.startDate.toDate()
                        val end = event.endDate.toDate()
                        val clicked = clickedDate.toDate()
                        !clicked.before(start) && !clicked.after(end)
                    }

                    setInformationLabel(dayEvents)
                    eventAdapter.updateList(dayEvents)
                }
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getTodayEvents()
        getEventsFromDatabase(fireDogId)
    }

    private fun setupRecyclerView(fireDogId: String) {
        eventAdapter = EventAdapter(emptyList())
        getTodayEvents()
        eventAdapter.setOnItemClickListener(object : EventAdapter.OnItemClickListener{
            override fun onItemClick(event: EventDisplayModel) {
                val intent = Intent(requireContext(), ActivityEvent::class.java)

                if (requireActivity() is ActivityArchiveDetails) {
                    intent.putExtra("isArchive", true)
                }
                intent.putExtra("openCommand", "DogEvent")
                intent.putExtra("fireId", event.fireId)
                intent.putExtra("fireDogId", fireDogId)
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

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("dogs").document(fireDogId)
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

    private fun getEventsFromDatabase(fireDogId : String){
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobieranie wydarzen
        database.collection("users").document(userId)
            .collection("dogs").document(fireDogId)
            .collection("events")
            .get()
            .addOnSuccessListener { documents ->

                events.clear()
                dates.clear()

                for (document in documents) {
                    val event = document.toObject(EventDisplayModel::class.java)
                    event.fireId = document.id

                    events.add(event)   //lista wszystkich wydarzen
                    addToDates(event.startDate, event.endDate)  //lista unikalnych dat
                }

                markEventDays() //oznaczenie dni z wydarzeniami
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun addToDates(start: Timestamp, end: Timestamp) {
        val calendar = Calendar.getInstance()
        calendar.time = start.toDate()  //data początkowa
        val endDate = end.toDate()  //data końcowa

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

        binding.calendarViewDogCalendar.setCalendarDays(calendarDays)
    }
}