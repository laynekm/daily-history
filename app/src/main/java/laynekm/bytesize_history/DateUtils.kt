package laynekm.bytesize_history

import java.text.DateFormatSymbols
import java.util.*

data class Date(val month: Int, val day: Int)

fun datesEqual(date1: Date, date2: Date): Boolean {
    return date1.month === date2.month && date1.day === date2.day
}

fun datesEqual(date1: String, date2: String): Boolean {
    return datesEqual(stringToDate(date1), stringToDate(date2))
}

fun getToday(): Date {
    val date: Calendar = Calendar.getInstance()
    return Date(date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
}

fun buildDateURL(date: Date): String {
    return "${DateFormatSymbols().months[date.month]}_${date.day}"
}

fun buildDateLabel(date: Date): String {
    val today = getToday()
    if (datesEqual(date, today)) return "Today (${DateFormatSymbols().months[date.month]} ${date.day})"
    return "${DateFormatSymbols().months[date.month]} ${date.day}"
}

fun dateToString(date: Date): String {
    return "${date.month}-${date.day}"
}

fun stringToDate(date: String): Date {
    return Date(date.substringBefore("-").toInt(), date.substringAfter("-").toInt())
}