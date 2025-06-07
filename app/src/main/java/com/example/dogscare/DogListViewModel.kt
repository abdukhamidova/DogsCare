package com.example.dogscare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class DogsListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val mutDogList = MutableLiveData<List<Dog>>()
    val dogsList: LiveData<List<Dog>> get() = mutDogList

    fun fetchDogsFromDatabase(listFilter: ArchiveType) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).collection("dogs")
                .whereEqualTo("archive", listFilter.name)  //psy wg archive type
                .get()
                .addOnSuccessListener { documents ->
                    val dogs = mutableListOf<Dog>()
                    for (document in documents) {
                        val fireId = document.getString("fireId") ?: ""
                        val dogName = document.getString("name") ?: ""
                        val dogArrivalDate = document.getTimestamp("arrivalDate")
                        val dogArchive = ArchiveType.valueOf(document.getString("archive") ?: "ACTIVE")
                        val dogImageUrl = document.getString("imageUrl") ?: ""

                        if (dogName.isNotEmpty() && dogArrivalDate != null) {
                            dogs.add(
                                Dog(
                                    fireId = fireId,
                                    name = dogName,
                                    arrivalDate = dogArrivalDate,
                                    archive = dogArchive,
                                    imageUrl = dogImageUrl
                                )
                            )
                        }
                    }
                    mutDogList.value = dogs.sortedBy { it.name.lowercase() }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun filterDogs(query: String, archiveType: ArchiveType){
        val filteredDogs = mutDogList.value?.filter {
            it.name.contains(query, ignoreCase = true) &&
                    it.archive == archiveType
        }
        mutDogList.value = filteredDogs ?: emptyList()
    }

    fun fetchArchivedDogsFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId)
                .collection("dogs")
                .get()
                .addOnSuccessListener { documents ->
                    val archivedDogs = mutableListOf<Dog>()
                    for (document in documents) {
                        val fireId = document.getString("fireId") ?: ""
                        val dogName = document.getString("name") ?: ""
                        val dogArrivalDate = document.getTimestamp("arrivalDate")
                        val dogArchive = ArchiveType.valueOf(document.getString("archive") ?: "ACTIVE")
                        val dogImageUrl = document.getString("imageUrl") ?: ""

                        //filtr psow nie aktywnych
                        if (dogName.isNotEmpty() && dogArrivalDate != null && dogArchive != ArchiveType.ACTIVE) {
                            archivedDogs.add(
                                Dog(
                                    fireId = fireId,
                                    name = dogName,
                                    arrivalDate = dogArrivalDate,
                                    archive = dogArchive,
                                    imageUrl = dogImageUrl
                                )
                            )
                        }
                    }
                    mutDogList.value = archivedDogs.sortedBy { it.name.lowercase() }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun fetchDogsWithInjectionEvents(fStartDate: Calendar, fEndDate: Calendar) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).collection("dogs")
                .get()
                .addOnSuccessListener { dogDocuments ->
                    val dogs = mutableListOf<Dog>()
                    var processedDogs = 0
                    val totalDogs = dogDocuments.size()

                    if (totalDogs == 0) {
                        mutDogList.value = emptyList()
                        return@addOnSuccessListener
                    }

                    for (dogDoc in dogDocuments) {
                        val fireDogId = dogDoc.getString("fireId") ?: continue
                        val dogName = dogDoc.getString("name") ?: ""
                        val dogImageUrl = dogDoc.getString("imageUrl") ?: ""

                        db.collection("users").document(userId)
                            .collection("dogs").document(fireDogId)
                            .collection("events")
                            .whereEqualTo("injection", true)
                            .get()
                            .addOnSuccessListener { documents ->
                                var hasMatchingEvent = false

                                for (document in documents) {
                                    val event = document.toObject(EventDisplayModel::class.java)
                                    event.fireId = document.id

                                    val startDate = event.startDate.toDate()

                                    // Czyszczenie godzin, minut, sekund w obu datach
                                    val clearStartDate = clearTime(Calendar.getInstance().apply { time = startDate })
                                    val clearStartFilterDate = clearTime(fStartDate)

                                    // Porównanie dat
                                    if (clearStartDate >= clearStartFilterDate && clearStartDate <= clearTime(fEndDate)) {
                                        hasMatchingEvent = true
                                        break
                                    }
                                }

                                if (hasMatchingEvent) {
                                    val dog = Dog(
                                        fireId = fireDogId,
                                        name = dogName,
                                        arrivalDate = dogDoc.getTimestamp("arrivalDate") ?: Timestamp.now(),
                                        archive = ArchiveType.valueOf(dogDoc.getString("archive") ?: "ACTIVE"),
                                        imageUrl = dogImageUrl
                                    )
                                    dogs.add(dog)
                                }

                                processedDogs++

                                // Kiedy wszystkie psy skończą swoje zapytania
                                if (processedDogs == totalDogs) {
                                    mutDogList.value = dogs.sortedBy { it.name.lowercase() }
                                }
                            }
                            .addOnFailureListener {
                                processedDogs++
                                if (processedDogs == totalDogs) {
                                    mutDogList.value = dogs.sortedBy { it.name.lowercase() }
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun clearTime(calendar: Calendar): Calendar {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }
}
