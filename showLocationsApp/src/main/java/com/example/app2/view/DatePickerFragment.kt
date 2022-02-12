package com.example.app2.view

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(), OnDateSetListener {

    companion object {
        private lateinit var listener: OnDateSetListener

        fun getInstance(listener: OnDateSetListener): DatePickerFragment? {
           DatePickerFragment.listener= listener
            return DatePickerFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]

        // Create a new instance of DatePickerDialog and return
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener.onDateSet(view, year, month + 1, dayOfMonth)
    }
}