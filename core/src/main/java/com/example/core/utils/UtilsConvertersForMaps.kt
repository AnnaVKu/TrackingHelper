package com.example.core.utils

import android.annotation.SuppressLint
import android.location.Location
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UtilsConvertersForMaps {
    companion object {
        fun convertLongToStringForTitleForCardViewTable(time: Long?): String? {
            val s: String = convertLongIntoDate(time).toString()
            val arr = s.split(" ").toTypedArray()
            return arr[2] + " " + arr[1] + " " + arr[5]
        }

        fun convertLongToStringForTitleForMarker(time: Long?): String? {
            val s = convertLongIntoDate(time).toString()
            val arr = s.split(" ").toTypedArray()
            return arr[3]
        }

        fun convertLongIntoDate(time: Long?): Date? {
            return Date(time!!)
        }

        fun converterForDay(date: Array<String>): String? {
            return date[2]
        }

        fun converterForYear(date: Array<String>): String? {
            return date[5]
        }

        fun converterForMonth(month: String?): String? {
            return when (month) {
                "Jan" -> "1"
                "Feb" -> "2"
                "Mar" -> "3"
                "Apr" -> "4"
                "May" -> "5"
                "Jun" -> "6"
                "Jul" -> "7"
                "Aug" -> "8"
                "Sep" -> "9"
                "Oct" -> "10"
                "Nov" -> "11"
                "Dec" -> "12"
                else -> "0"
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun parseDate(date: String?): Date? {
            return try {
                SimpleDateFormat("yyyy-MM-dd").parse(date)
            } catch (e: ParseException) {
                null
            }
        }
    }
}