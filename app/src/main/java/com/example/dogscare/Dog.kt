package com.example.dogscare

data class Dog(
    var id: String = "",       // ID dokumentu w Firestore
    var name: String = "",     // Imię psa
    var breed: String = "",    // Rasa
    var bday: String = "",     // Data urodzenia
    var adopted: Boolean = false // DLa archiwizacji
)
