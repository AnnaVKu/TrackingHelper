package com.example.trackinglocationhelper.background

import android.content.Context
import androidx.work.*
import com.example.trackinglocationhelper.TrackingConstants
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.Hawk.init
import java.util.ArrayList

class WorkHawk {

    companion object {
        private var isHasDataForSendIntoFb = false
        private val LOOK = Any()
        private lateinit var myContext: Context
        private val dataForSaving: MutableList<String> = ArrayList()

        fun createInstance(context: Context) {
            kotlin.synchronized(LOOK) {
                myContext = context
                if(!Hawk.isBuilt()) {
                    init(myContext).build()
                }
            }
        }

        fun storageCurrentLocation(currentLocation: String?) {
            if (currentLocation != null) {
                dataForSaving.add(currentLocation)
                Hawk.put(TrackingConstants.HAWK_KEY, dataForSaving)
                isHasDataForSendIntoFb = true
            }
        }

        fun hawkHasData(): Boolean {
            return isHasDataForSendIntoFb
        }

        fun sendLocationsFromHawkInFirebase() {
            if (Hawk.contains(TrackingConstants.HAWK_KEY)) {
                val constraints: Constraints.Builder = Constraints.Builder()
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
                val container = Hawk.get<List<String>>(TrackingConstants.HAWK_KEY)
                Hawk.deleteAll()
                dataForSaving.clear()
                isHasDataForSendIntoFb = false
                // Passing params
                val data: Data.Builder = Data.Builder()
                val arr = arrayOfNulls<String>(container.size)
                for (i in container.indices) {
                    arr[i] = container[i]
                }
                data.putStringArray(TrackingConstants.MY_WORKER_KEY, arr)
                val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<MyWorkerLocation>()
                    .setInputData(data.build())
                    .setConstraints(constraints.build())
                    .build()
                WorkManager.getInstance(myContext).enqueue(workRequest)
            }
        }

        fun onDestroyHawk() {
            if (Hawk.isBuilt()) {
                Hawk.deleteAll()
                Hawk.destroy()
            }
        }
    }
}