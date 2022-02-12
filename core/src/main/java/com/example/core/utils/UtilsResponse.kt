package com.example.core.utils

import com.example.core.db.Response
import java.util.*
import kotlin.collections.ArrayList

class UtilsResponse {
    companion object {
        fun selectLocationsAccordingToTheCurrentDate(
            date: Date,
            responses: MutableList<Response>
        ): MutableList<Response> {

            val result: MutableList<Response> = ArrayList()

            val cur = date.toString().split(" ").toTypedArray()
            val yearFromCur: String = UtilsConvertersForMaps.converterForYear(cur).toString()
            val monthFromCur: String = UtilsConvertersForMaps.converterForMonth(cur[1]).toString()
            val dayFromCur: String = UtilsConvertersForMaps.converterForDay(cur).toString()

            for (response in responses) {
                val res: Array<String> = UtilsConvertersForMaps.convertLongIntoDate(response.time)
                    .toString().split(" ").toTypedArray()
                val yearFromResponse: String? = UtilsConvertersForMaps.converterForYear(res)
                val monthFromResponse: String? = UtilsConvertersForMaps.converterForMonth(res[1])
                val dayFromResponse: String? = UtilsConvertersForMaps.converterForDay(res)

                if (yearFromCur == yearFromResponse && monthFromCur == monthFromResponse && dayFromCur == dayFromResponse) {
                    result.add(response)
                }
            }
            return result
        }

        fun selectLocationsAccordingToTheCurrentDate(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            responses: MutableList<Response>
        ): MutableList<Response> {

            val result: MutableList<Response> = ArrayList()

            val d: String = if (dayOfMonth < 10) {
                "0$dayOfMonth"
            } else dayOfMonth.toString()

            for (response in responses) {
                val res: Array<String> = UtilsConvertersForMaps.convertLongIntoDate(response.time)
                    .toString().split(" ").toTypedArray()
                val yearFromResponse: String? = UtilsConvertersForMaps.converterForYear(res)
                val monthFromResponse: String? = UtilsConvertersForMaps.converterForMonth(res[1])
                val dayFromResponse: String? = UtilsConvertersForMaps.converterForDay(res)

                if (year.toString() == yearFromResponse && month.toString() == monthFromResponse && d == dayFromResponse) {
                    result.add(response)
                }
            }
            return result
        }
    }
}