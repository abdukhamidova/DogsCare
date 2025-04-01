package com.example.dogscare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

//region ==DOG
data class Dog(
    var id: String = "",       // ID dokumentu w Firestore
    var name: String = "",     // Imię psa
    var breed: String = "",    // Rasa
    var bday: String = "",     // Data urodzenia
    var adopted: Boolean = false, // DLa archiwizacji

    //wydarzenia kalendarzowe
    //val events: List<Event> = emptyList()
)
//endregion

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
