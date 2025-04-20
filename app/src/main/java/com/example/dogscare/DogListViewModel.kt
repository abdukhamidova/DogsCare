package com.example.dogscare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class DogsListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val mutDogList = MutableLiveData<List<Dog>>()
    val dogsList: LiveData<List<Dog>> get() = mutDogList

    //pobieranie psów wg ArchiveType
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

    //filtrowanie psow
    fun filterDogs(query: String, archiveType: ArchiveType){
        val filteredDogs = mutDogList.value?.filter {
            it.name.contains(query, ignoreCase = true) &&
                    it.archive == archiveType
        }
        mutDogList.value = filteredDogs ?: emptyList()
    }

    //pobieranie wszystkich psów zarchiwizowanych
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
}
