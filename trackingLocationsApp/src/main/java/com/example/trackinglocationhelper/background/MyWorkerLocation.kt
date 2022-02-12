package com.example.trackinglocationhelper.background

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.core.Constants
import com.example.trackinglocationhelper.TrackingConstants
import com.example.core.db.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MyWorkerLocation(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    // initDb
    private var database: FirebaseDatabase? = null
    private var mRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    override fun doWork(): Result {
        val res = inputData.getStringArray(TrackingConstants.MY_WORKER_KEY)
        database = FirebaseDatabase.getInstance(Constants.PATH_TO_REALTIME_DATABASE)
        mRef = database!!.reference
        mAuth = FirebaseAuth.getInstance()
        for (s in res!!) {
            val location = convertStringIntoLocation(s)
            (mAuth!!.currentUser)?.let {
                mRef!!.child(Constants.DB_CHILD_USERS)
                    .child(Constants.DB_CHILD_USER_ID)
                    .child(it.uid)
                    .push()
                    .setValue(
                        Response(location!!.time, location.latitude, location.longitude)
                    ) { error, ref ->
                        if (error != null) {
                            Log.i(
                                "Result",
                                "MyWorkerLocation Data could not be saved from hawk " + error.message
                            )
                        } else {
                            Log.i("Result", "MyWorkerLocation Data was saved from hawk")
                        }
                    }
            }
        }
        return Result.success()
    }

    private fun convertStringIntoLocation(location: String?): Location? {
        if (location != null && location.contains(",")) {
            val result = Location("Generated_location")
            val locationStrings = location.split(",").toTypedArray()
            if (locationStrings.size == 3) {
                result.time = locationStrings[0].toLong()
                result.latitude = locationStrings[1].toDouble()
                result.longitude = locationStrings[2].toDouble()
                return result
            }
        }
        return null
    }
}