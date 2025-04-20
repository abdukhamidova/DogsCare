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
    var appearance: String = "",    // Wygląd
    var arrivalDate: Timestamp = Timestamp.now(), // domyślna data - aktualna
    var note: String = "",
    var imageUrl: String = "", //URL zdjęcia psa
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

//region ==Shelter Event
data class ShelterEvent(
    var fireId: String = "",
    var startDate:Timestamp = Timestamp.now(),
    var endDate:Timestamp = Timestamp.now(),
    var time:String = "",
    var header: String = "",
    var note: String = ""
)

//endregion

data class EventDisplayModel(
    var fireId: String = "",
    var header: String = "",
    var startDate: Timestamp = Timestamp.now(),
    var endDate: Timestamp = Timestamp.now()
)

// region ==Dog Event
data class DogEvent(
    var fireId: String = "",
    var startDate:Timestamp = Timestamp.now(),
    var endDate:Timestamp = Timestamp.now(),
    var time:String = "",
    var header: String = "",
    var note: String = "",
    var injection: Boolean = false
)
//endregion

