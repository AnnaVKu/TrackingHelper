package com.example.trackinglocationhelper.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.trackinglocationhelper.TrackingConstants

class MyDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DialogFragment"
        const val TARGET = "target"
        const val MSG = "msg"
        const val POSITIVE_BUTTON = "positiveButton"
        const val NEGATIVE_BUTTON = "negativeButton"

        fun getInstanceOfDialog(
            target: String,
            msg: String,
            positiveButton: String,
            negativeButton: String
        ): MyDialogFragment = MyDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TARGET, target)
                putString(MSG, msg)
                putString(POSITIVE_BUTTON, positiveButton)
                putString(NEGATIVE_BUTTON, negativeButton)
            }
            Log.i("Result", "DialogFragment getInstance")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val target = arguments?.getString(TARGET)
        val msg = arguments?.getString(MSG)
        val positiveButton = arguments?.getString(POSITIVE_BUTTON)
        val negativeButton = arguments?.getString(NEGATIVE_BUTTON)

        Log.i("Result", "DialogFragment onCreateDialog()")

        return AlertDialog.Builder(activity)
            .setMessage(msg)
            .setPositiveButton(positiveButton) { _, _ ->
                setResult(target, TrackingConstants.MY_DIALOG_POSITIVE_BUTTON)
            }.setNegativeButton(negativeButton) { _, _ ->
                setResult(target, TrackingConstants.MY_DIALOG_NEGATIVE_BUTTON)
                dismiss()
            }.create()
    }

    private fun setResult(target: String?, usersChoice: String) {
        val result = when (usersChoice) {
            TrackingConstants.MY_DIALOG_POSITIVE_BUTTON -> true
            else -> false
        }
        val bundle = Bundle().apply {
            putBoolean(TrackingConstants.BUNDLE_KEY_RES, result)
            putString(TrackingConstants.BUNDLE_KEY_TARGET, target)
        }
        parentFragmentManager.setFragmentResult(
            TrackingConstants.REQUEST_KEY,
            bundle
        )
        Log.i("Result", "DialogFragment setResult(...)")
    }
}