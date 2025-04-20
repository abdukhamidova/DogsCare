package com.example.dogscare

import com.google.firebase.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

fun myDateFormat(year: Int, monthOfYear: Int, dayOfMonth: Int) : String{
    val monthString: String
    val dayString: String
    if(monthOfYear + 1 < 10) monthString = "0${monthOfYear+1}"
    else monthString = "${monthOfYear+1}"
    if(dayOfMonth < 10) dayString = "0${dayOfMonth}"
    else dayString = "${dayOfMonth}"

    return "$dayString.$monthString.$year"
}

fun stringToTimestamp(dateString: String): Timestamp {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return try {
        val date = dateFormat.parse(dateString) // konwertuje string na Date
        Timestamp(date) // konwertuje Date na Timestamp
    } catch (e: ParseException) {
        e.printStackTrace()
        Timestamp.now() // W przypadku błędu, zwróć bieżący Timestamp
    }
}

fun timestampToString(timestamp: Timestamp?): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(timestamp?.toDate() ?: 0) // konwertuje Timestamp na Date a potem na String
}

