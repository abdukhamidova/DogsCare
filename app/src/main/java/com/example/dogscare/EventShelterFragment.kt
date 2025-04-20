package com.example.dogscare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.dogscare.databinding.FragmentEventShelterBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventShelterFragment : Fragment() {
    private lateinit var binding: FragmentEventShelterBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private var originalEvent: ShelterEvent? = null

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentEventShelterBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)

        if (activity is ActivityAdd) {
            (activity as? ActivityAdd)?.setToolbarTitle("Dodaj wydarzenie")

            //data
            val selectedDayMillis = arguments?.getLong("clickedDay", -1L) ?: -1L
            if (selectedDayMillis != -1L) {
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedDayMillis
                defaultDate.timeInMillis = selectedDayMillis
            } else {
                defaultDate = Calendar.getInstance()
            }

            buttonSave.setOnClickListener {
                addEventToDatabase()
            }
        }
        else if (activity is ActivityEvent) {
            (activity as? ActivityEvent)?.setToolbarTitle("Wydarzenie")
            val fireId = arguments?.getString("fireId")
            val buttonDelete = requireActivity().findViewById<ImageButton>(R.id.imageButtonArchive)

            if (fireId != null) {
                fetchEventData(fireId)

                buttonSave.setOnClickListener{
                    updateEventData(fireId)
                }

                buttonDelete.setOnClickListener{
                    if(user != null){
                        val ref = database.collection("users").document(user.uid)
                            .collection("events").document(fireId)
                        DeleteDialog(requireContext(), ref).show()
                    }
                }
            }

        }

        //ustawienie domyślnej daty
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.editTextStartDate.setText(formatter.format(defaultDate.time))
        binding.editTextEndDate.setText(formatter.format(defaultDate.time))

        binding.editTextStartDate.setOnClickListener{
            showDatePickerDialog(binding.editTextStartDate) { selectedCalendar ->
                defaultDate = selectedCalendar

                val formattedDate = myDateFormat(
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
                )
                binding.editTextEndDate.setText(formattedDate)
            }
        }

        binding.editTextEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate, defaultDate) { selectedCalendar ->
                if (selectedCalendar.before(defaultDate)) {
                    Toast.makeText(requireContext(), "Data końcowa nie może być wcześniejsza niż początkowa", Toast.LENGTH_SHORT).show()

                    val formattedStart = myDateFormat(
                        defaultDate.get(Calendar.YEAR),
                        defaultDate.get(Calendar.MONTH),
                        defaultDate.get(Calendar.DAY_OF_MONTH)
                    )
                    binding.editTextEndDate.setText(formattedStart)
                }
            }
        }

        binding.editTextEventTime.setOnClickListener{
            showTimePickerDialog()
        }

        return binding.root
    }

    private fun showDatePickerDialog(
        dateEditText: EditText,
        minDate: Calendar? = null,
        onDateSelected: ((Calendar) -> Unit)? = null
    ) {
        val year = defaultDate.get(Calendar.YEAR)
        val month = defaultDate.get(Calendar.MONTH)
        val day = defaultDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val selectedDateStr = myDateFormat(selectedYear, selectedMonth, selectedDay)
                dateEditText.setText(selectedDateStr)

                onDateSelected?.invoke(selectedCalendar)
            },
            year, month, day
        )

        minDate?.let {
            datePickerDialog.datePicker.minDate = it.timeInMillis
        }

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.editTextEventTime.setText(time)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun addEventToDatabase(){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        val startDate = binding.editTextStartDate.text.toString().trim()
        val endDate = binding.editTextEndDate.text.toString().trim()
        val eventTime = binding.editTextEventTime.text.toString().trim()
        val header = binding.editTextHeader.text.toString().trim()
        val notes = binding.editTextAdditonalNote.text.toString().trim()

        //pola tytul i data poczatkowa maja byc uzupelnone
        if(startDate.isEmpty() || header.isEmpty()){
            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }
        //tworzenie wydarzenia (schroniska -> druga kolekcja)
        val eventRef = database.collection("users").document(userId)
            .collection("events").document() //generowanie ID

        //dane
        val eventData = ShelterEvent(
            fireId = eventRef.id,
            startDate = stringToTimestamp(startDate),
            endDate = stringToTimestamp(endDate),
            time = eventTime,
            header = header,
            note = notes
        )

        //zapis w bazie danych (pierwszy raz)
        eventRef.set(eventData).addOnSuccessListener {
            Toast.makeText(requireContext(), "Dodano wydarzenie!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu.", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchEventData(fireId : String){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobranie danych
        database.collection("users").document(userId)
            .collection("events").document(fireId)
            .get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val shelterEvent = document.toObject(ShelterEvent::class.java)

                    shelterEvent?.let { event ->
                        binding.editTextHeader.setText(event.header)
                        binding.editTextEventTime.setText(event.time)
                        binding.editTextAdditonalNote.setText(event.note)

                        //daty
                        binding.editTextStartDate.setText(timestampToString(event.startDate))
                        binding.editTextEndDate.setText(timestampToString(event.endDate))

                        //dalsze porównanie
                        originalEvent = event

                        //domyslna data (startDate)
                        event.startDate.let {
                            val cal = Calendar.getInstance()
                            cal.time = it.toDate()
                            defaultDate = cal
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Błąd: nie udało się pobrać danych.", Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: ${e.message}.",Toast.LENGTH_LONG).show()
            }
    }

    private fun updateEventData(fireId: String){
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid

        val header = binding.editTextHeader.text.toString().trim()
        val time = binding.editTextEventTime.text.toString().trim()
        val notes = binding.editTextAdditonalNote.text.toString().trim()
        val startDateString = binding.editTextStartDate.text.toString().trim()
        val endDateString = binding.editTextEndDate.text.toString().trim()

        val startDate = stringToTimestamp(startDateString)
        val endDate = stringToTimestamp(endDateString)

        if(header.isEmpty() || startDateString.isEmpty()){
            Toast.makeText(requireContext(), "Wypełnij wymagane pola.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()
        originalEvent?.let{original ->
            if(header != original.header) updates["header"] = header
            if(time != original.time) updates["time"] = time
            if(notes != original.note) updates["note"] = notes
            if(startDate != original.startDate) updates["startDate"] = startDate
            if(endDate != original.endDate) updates["endDate"] = endDate
        }

        if(updates.isEmpty()){
            Toast.makeText(requireContext(), "Brak zmian do zapisania.", Toast.LENGTH_SHORT).show()
            return
        }

        val eventRef = database.collection("users").document(userId)
            .collection("events").document(fireId)

        eventRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Zaktualizowano wydarzenie.", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd aktualizacji.", Toast.LENGTH_LONG).show()
            }
    }
}