package com.example.dogscare

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogscare.databinding.FragmentFormMedicalBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class FormMedicalFragment : Fragment() {
    private lateinit var binding: FragmentFormMedicalBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private lateinit var eventAdapter: EventAdapter
    private val events = mutableListOf<EventDisplayModel>()

    //data
    private var defaultDate: Calendar = Calendar.getInstance() // domyślnie dziś

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormMedicalBinding.inflate(inflater, container, false)
        val buttonSave = requireActivity().findViewById<ImageButton>(R.id.imageButtonSave)
        (activity as? ActivityDogDetails)?.setToolbarTitle("Zdrowie")
        val fireId = arguments?.getString("fireId")

        binding.editTextEstimatedDoB.setOnClickListener{
            showDatePickerDialog()
        }

        if (fireId != null) {
            //uzupełnienie pól
            fetchName(fireId) { name ->
                binding.textViewName.text = name.uppercase()
            }
            fetchMedicalData(fireId)

            //szczepienia
            getEventsFromDatabase(fireId)
            setupRecyclerView(fireId)

            buttonSave.setOnClickListener{
                updateMedicalInfo(fireId)
            }
        }

        return binding.root

    }

    private fun setupRecyclerView(fireDogId: String) {
        eventAdapter = EventAdapter(emptyList())
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

        binding.injectionListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.injectionListRecyclerView.adapter = eventAdapter
    }

    private fun fetchName(fireId: String, onResult: (String) -> Unit) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            onResult("Imię zwierzęcia")
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Imię zwierzęcia"
                    onResult(name)
                } else {
                    Toast.makeText(requireContext(), "Dokument nie istnieje", Toast.LENGTH_SHORT).show()
                    onResult("Imię zwierzęcia")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd pobierania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult("Imię zwierzęcia")
            }
    }

    private fun updateMedicalInfo(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobranie danych
        val allergies = binding.editTextAllergies.text.toString().trim()
        val illness = binding.editTextIllness.text.toString().trim()
        val dateOfBirth = binding.editTextEstimatedDoB.text.toString().trim()

        //ścieżka
        val dogRef = database.collection("users").document(userId)
            .collection("dogs").document(fireId)


        val estimatedDoB = if (dateOfBirth.isNotEmpty()) {
            stringToTimestamp(dateOfBirth)
        } else {
            null
        }
        val health = mapOf(
            "allergies" to allergies,
            "illness" to  illness,
            "estimatedDoB" to estimatedDoB
        )

        //zapis w bazie danych
        dogRef.update("health", health).addOnSuccessListener {
            Toast.makeText(requireContext(), "Zapisano!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(), "Błąd zapisu: ${e.message}.", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchMedicalData(fireId: String) {
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        //pobranie danych
        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val healthMap = document.get("health") as? Map<*, *>

                    if (healthMap != null) {
                        binding.editTextIllness.setText(healthMap["illness"] as? String ?: "")
                        binding.editTextAllergies.setText(healthMap["allergies"] as? String ?: "")

                        val timestamp = healthMap["estimatedDoB"] as? Timestamp
                        if(timestamp != null)
                            binding.editTextEstimatedDoB.setText(timestampToString(timestamp))
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: nie udało się pobrać danych.", Toast.LENGTH_SHORT).show()
            }

    }

    private fun showDatePickerDialog() {
        val year = defaultDate.get(Calendar.YEAR)
        val month = defaultDate.get(Calendar.MONTH)
        val day = defaultDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = myDateFormat(year, monthOfYear, dayOfMonth) //format: dd.MM.rrrr
                binding.editTextEstimatedDoB.setText(selectedDate)

                defaultDate.set(year, monthOfYear, dayOfMonth)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun getEventsFromDatabase(fireDogId : String){
        //uzytkownik
        if (user == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        events.clear()

        //pobieranie wydarzen
        database.collection("users").document(userId)
            .collection("dogs").document(fireDogId)
            .collection("events")
            .whereEqualTo("injection", true)    //filtr
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val event = document.toObject(EventDisplayModel::class.java)
                    event.fireId = document.id

                    events.add(event)   //lista wszystkich szczepień
                }
                if(events.isEmpty()) binding.textViewInjections.text = "Brak szczepień"
                else {
                    binding.textViewInjections.text = "Szczepienia:"
                    events.sortBy { it.startDate }
                }
                eventAdapter.updateList(events)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}