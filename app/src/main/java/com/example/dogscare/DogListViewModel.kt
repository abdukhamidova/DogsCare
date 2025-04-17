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

                        if (dogName.isNotEmpty() && dogArrivalDate != null) {
                            dogs.add(
                                Dog(
                                    fireId = fireId,
                                    name = dogName,
                                    arrivalDate = dogArrivalDate,
                                    archive = dogArchive
                                )
                            )
                        }
                    }
                    mutDogList.value = dogs
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    //filtrowanie psow
    fun filterDogs(query: String){
        val filteredDogs = mutDogList.value?.filter{
            it.name.contains(query, ignoreCase = true)
        }
        mutDogList.value = filteredDogs ?: emptyList()
    }

    //pobieranie wszystkich psów zarchiwizowanych
    fun fetchArchivedDogsFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).collection("dogs")
                .get()
                .addOnSuccessListener { documents ->
                    val archivedDogs = mutableListOf<Dog>()
                    for (document in documents) {
                        val fireId = document.getString("fireId") ?: ""
                        val dogName = document.getString("name") ?: ""
                        val dogArrivalDate = document.getTimestamp("arrivalDate")
                        val dogArchive = ArchiveType.valueOf(document.getString("archive") ?: "ACTIVE")

                        //filtr psow nie aktywnych
                        if (dogName.isNotEmpty() && dogArrivalDate != null && dogArchive != ArchiveType.ACTIVE) {
                            archivedDogs.add(
                                Dog(
                                    fireId = fireId,
                                    name = dogName,
                                    arrivalDate = dogArrivalDate,
                                    archive = dogArchive
                                )
                            )
                        }
                    }
                    mutDogList.value = archivedDogs
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }
}
