package com.example.dogscare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate

enum class ArchiveType {
    ADOPTION,            //adopcja
    DEATH,               //śmierć zwierzęcia
    TRANSFER,            //transfer do innego schroniska
    OTHER,                //inne
    ACTIVE,                 //do wyświetlenia na głównej liście
    ARCHIVED            //do wyświetlenia wszystkich psów z archiwum
}

//region ==DOG
data class Dog(
    var fireId: String = "",    //id psa w bazie
    var name: String = "",     // Imię psa
    var chip: String = "",      // Numer chip
    var breed: String = "",    // Rasa
    var gender: String = "",    // Płeć
    var weight: String = "",    // Waga
    var arrivalDate: Timestamp = Timestamp.now(), // domyślna data - aktualna
    var note: String = "",
    var archive: ArchiveType = ArchiveType.ACTIVE // dla archiwizacji
)
//endregion

//region ==ADOPTION
data class Adoption(
    var name: String = "",
    var surname: String = "",
    var city: String = "",
    var street: String = "",
    var houseNr: String = "",
    var phoneNr: String = "",
    var email: String = "",
    var adoptionDate: Timestamp = Timestamp.now()
)
//endregion

//region MEDICAL INFO
data class Health(
    var estimatedDoB: Timestamp = Timestamp.now(),
    var allergies: String = "",
    var illness: String = ""
)
//
//region ==Event Basic
data class Event(
    val id: String = "",
    val date: String = "",
    val name: String = "",
    val note: String = "",

    val type: EventType = EventType.SHELTER_EVENT
)

enum class EventType {
    SHELTER_EVENT, // wydarzenie schroniska
    DOG_EVENT // wydarzenie konkretnego psa
}
//endregion


/*
//region ==Event Process
class EventRepository{
    private val events = mutableListOf<Event>() //lista wydarzen

    fun addEvent(event: Event){
        events.add(event)
    }

*//*    fun getEventsThatDate(date: LocalDate, filter: List<EventType>): List<Event>{
        return events.filter{
            it.date == date && it.type in filter
        }
    }*//*

   // fun getAllEvents(): List<Event> = events
}

class CalendarViewModel : ViewModel(){
    private val repository = EventRepository()
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    *//*private val selectedFilters = MutableLiveData<List<EventType>>(listOf(EventType.SHELTER_EVENT))

    fun loadEvents(date: LocalDate) {
        _events.value = repository.getEventsForDate(date, selectedFilters.value ?: listOf())
    }

    fun updateFilters(filters: List<EventType>) {
        selectedFilters.value = filters
    }*//*
}

//endregion*/
