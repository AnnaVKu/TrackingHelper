package com.example.trackinglocationhelper.background

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.core.Constants
import com.example.trackinglocationhelper.TrackingConstants.ACTION_START_LOCATION_SERVICE
import com.example.trackinglocationhelper.R
import com.example.core.db.Response
import com.example.trackinglocationhelper.TrackingConstants
import com.example.trackinglocationhelper.utils.UtilGpsNetwork
import com.example.trackinglocationhelper.utils.UtilLocationIntoString.Companion.convertLocationIntoString
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class LocationService : Service() {

    companion object {
        var isServiceWork = false
    }

    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var currentLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var connectivityManager: ConnectivityManager? = null

    private val gpsStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val locationUpdate: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }

    fun getGpsStatus(): LiveData<Boolean> {
        return gpsStatus
    }

    fun getLocationUpdate(): LiveData<Location> {
        return locationUpdate
    }

    // Binder given to clients
    private val binder = LocationBinder()

    inner class LocationBinder(): Binder() {
        fun getService(): LocationService = this@LocationService
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {}

        override fun onProviderDisabled(provider: String) {
            if (locationManager?.let { UtilGpsNetwork.isGpsEnabled(it) } == false) {
                createNotificationChannelToGPS()
                gpsStatus.value = false
            }
        }

        override fun onProviderEnabled(provider: String) {
            if (locationManager?.let { UtilGpsNetwork.isGpsEnabled(it) } == true) {
                gpsStatus.value = true
                startLocationService()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceWork = true
        database = FirebaseDatabase.getInstance(com.example.core.Constants.PATH_TO_REALTIME_DATABASE)
        myRef = database!!.reference
        mAuth = FirebaseAuth.getInstance()

        //Hawk.init(this).build()
        WorkHawk.createInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                if (action == ACTION_START_LOCATION_SERVICE) startLocationService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        createNotificationChannelLocationService()

        connectivityManager =
            applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            TrackingConstants.time,
            0f,
            locationListener
        )
        // request Current Location
        locationRequest = LocationRequest.create()
        locationRequest?.interval =  TrackingConstants.time
        locationRequest?.fastestInterval =  TrackingConstants.time
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                Log.i("Result", "onLocationResult")
                locationUpdate.value = currentLocation
                if (UtilGpsNetwork.isNetworkEnabled(connectivityManager!!)) {
                    // to reduce crush after stop Service
                    if (mAuth != null) {
                        currentLocation?.let { sendCurrentLocationInFirebase(it) }
                        if (WorkHawk.hawkHasData()) {
                            Log.i("Result", "dataForSaving does not empty")
                            WorkHawk.sendLocationsFromHawkInFirebase()
                            //sendLocationsFromHawkInFirebase()
                        }
                    }
                } else {
                    val loc = currentLocation
                    WorkHawk.storageCurrentLocation(loc?.let { convertLocationIntoString(location = it) })

                    //storageCurrentLocation(currentLocation)
                }
            }
        }

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceWork = false
        locationCallback?.let {
            LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(it)
        }
        locationListener.let { locationManager?.removeUpdates(it) }
        stopForeground(true)
        WorkManager.getInstance(this).cancelAllWork()
        WorkHawk.onDestroyHawk()
        stopSelf()
    }

    private fun sendCurrentLocationInFirebase(currentLocation: Location) {
        (mAuth!!.currentUser)?.let {
            myRef!!.child(com.example.core.Constants.DB_CHILD_USERS)
                .child(com.example.core.Constants.DB_CHILD_USER_ID)
                .child(it.uid)
                .push()
                .setValue(
                    Response(
                        currentLocation.time,
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                ) { error, ref ->
                    if (error != null) {
                        Log.i("Result", "Data could not be saved " + error.message)
                    } else {
                        Log.i("Result", "Data was saved ")
                    }
                }
        }
    }

    // notifications
    private fun createNotificationChannelLocationService() {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val resultIntent = Intent()
        // container for intent
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // to create notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,
            TrackingConstants.NOTIFICATION_CHANNEL_TRACKING_ID
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(getString(R.string.title_location_service))
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentText(getString(R.string.text_is_running))
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(false)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TrackingConstants.NOTIFICATION_CHANNEL_TRACKING_ID,
                TrackingConstants.NOTIFICATION_CHANNEL_TRACKING_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        startForeground( TrackingConstants.NOTIFICATION_TRACKING_ID, builder.build())
    }

    private fun createNotificationChannelToGPS() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent()
        // container for intent
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // to create notification
        val builder = NotificationCompat.Builder(
            applicationContext,  TrackingConstants.NOTIFICATION_CHANNEL_GPS_ID
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(getString(R.string.notification_gps_title))
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentText(getString(R.string.notification_gps_text))
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(false)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TrackingConstants.NOTIFICATION_CHANNEL_GPS_ID,
                TrackingConstants.NOTIFICATION_CHANNEL_GPS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        startForeground( TrackingConstants.NOTIFICATION_TRACKING_ID, builder.build())
    }
}