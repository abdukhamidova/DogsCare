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
    dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")

    return try {
        val date = dateFormat.parse(dateString)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"))
        cal.time = date!!
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        Timestamp(cal.time)
    } catch (e: ParseException) {
        e.printStackTrace()
        Timestamp.now()
    }
}

fun timestampToString(timestamp: Timestamp?): String {
    if (timestamp == null) {
        return ""
    }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    return dateFormat.format(timestamp.toDate())
}



