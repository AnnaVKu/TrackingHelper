package com.example.trackinglocationhelper.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.auth.di.DaggerAuthComponent
import com.example.core.MyApplication
import com.example.core.ResponseWrapper
import com.example.core.viewModel.MyViewModel
import com.example.core.viewModel.ViewModelFactory
import com.example.trackinglocationhelper.TrackingConstants
import com.example.trackinglocationhelper.R
import com.example.trackinglocationhelper.background.LocationService
import com.example.trackinglocationhelper.databinding.FragmentMapsBinding
import com.example.trackinglocationhelper.di.DaggerTrackingComponent
import com.example.trackinglocationhelper.utils.UtilGpsNetwork
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewBinding: FragmentMapsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[MyViewModel::class.java]
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var locationManager: LocationManager? = null

    private var currentLocation: Location? = null

    private val PERMISSIONS_FOREGROUND_LOCATION = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private lateinit var locationService: LocationService
    private var mBound: Boolean = false

    // to show Toast about GPS
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as LocationService.LocationBinder
            locationService = binder.getService()

            locationService.let {
                it.getGpsStatus()?.observe(requireActivity(), { res ->
                    if (res == true) {
                        val toast = Toast.makeText(
                            requireContext(),
                            getString(R.string.user_info_gps_not_available),
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                })

                locationService.getLocationUpdate().observe(requireActivity(), { location ->
                    currentLocation = location
                })
            }
            mBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
        }
    }

    // Work with permissions
    @SuppressLint("MissingPermission")
    private val requestForegroundLocationPermissionLauncher =
        registerForActivityResult(RequestMultiplePermissions(),
            ActivityResultCallback<Map<String?, Boolean?>> { isGranted: Map<String?, Boolean?> ->
                if (isGranted.containsValue(false)) {
                    requestPermissions()
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (!gpsEnabled()) {
                            requestGps()
                        } else {
                            fusedLocationProviderClient!!.requestLocationUpdates(
                                locationRequest, locationCallback, Looper.getMainLooper()
                            )
                        }
                    }
                }
            })

    @SuppressLint("MissingPermission")
    private val requestBackgroundLocationPermission = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (!gpsEnabled()) {
                requestGps()
            } else {
                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                startLocationService()
            }
        }
    }

    private fun requestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!checkForegroundPermissions(PERMISSIONS_FOREGROUND_LOCATION)!!) {
                requestForegroundLocationPermissionLauncher.launch(PERMISSIONS_FOREGROUND_LOCATION)

                MyDialogFragment.getInstanceOfDialog(
                    target = TrackingConstants.TARGET_LOCATION,
                    msg = getString(R.string.dialog_msg_location),
                    positiveButton = getString(R.string.dialog_positive_button_location),
                    negativeButton = getString(R.string.dialog_negative_button_location)
                ).show(childFragmentManager, MyDialogFragment.TAG)
                return false
            }
            true
        } else {
            if (!checkForegroundPermissions(PERMISSIONS_FOREGROUND_LOCATION)!!) {
                requestForegroundLocationPermissionLauncher.launch(PERMISSIONS_FOREGROUND_LOCATION)
                false
            } else {
                true
            }
        }
    }

    private fun checkForegroundPermissions(permissions: Array<String>?): Boolean? {
        if (permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
        return false
    }

    // Work with GPS
    @SuppressLint("MissingPermission")
    private val requestGpsLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        if (gpsEnabled()) {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            // start service
            startLocationService()
        }
    }

    private fun gpsEnabled(): Boolean {
        return locationManager?.let { UtilGpsNetwork.isGpsEnabled(it) } == true
    }

    private fun requestGps() {
        MyDialogFragment.getInstanceOfDialog(
            target = TrackingConstants.TARGET_GPS,
            getString(R.string.dialog_msg_gps),
            getString(R.string.dialog_positive_button_gps),
            getString(R.string.dialog_negative_button_gps)
        ).show(childFragmentManager, MyDialogFragment.TAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewBinding = FragmentMapsBinding.inflate(inflater, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?

        val appComponent = (requireActivity().applicationContext as MyApplication).appComponent
        val authComponent = DaggerAuthComponent.factory().create(appComponent)
        DaggerTrackingComponent.factory().create(appComponent, authComponent).injectFragmentMaps(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        childFragmentManager.setFragmentResultListener(
            TrackingConstants.REQUEST_KEY,
            this
        ) { _, bundle ->
            val res = bundle.getBoolean(TrackingConstants.BUNDLE_KEY_RES)
            val target = bundle.getString(TrackingConstants.BUNDLE_KEY_TARGET)
            if (target == TrackingConstants.TARGET_LOCATION) {
                if (res) {
                    requestBackgroundLocationPermission.launch(PERMISSION_BACKGROUND_LOCATION)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.warning_for_background_permissions),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (target == TrackingConstants.TARGET_GPS) {
                if (res) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requestGpsLauncher.launch(intent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.warning_for_gps),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewBinding.toolbar.inflateMenu(R.menu.main_menu)
        viewBinding.toolbar.title = getString(R.string.toolbar_app_name)
        viewBinding.toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemSignOut -> {
                    stopLocationService()
                    viewModel.signOutWasClicked()
                    true
                }
                else -> false
            }
        })

        viewModel.getLiveDataIsSignOut().observe(requireActivity(), { result ->
            if (result.status == ResponseWrapper.Status.SUCCESS) {
                viewModel.clearDataForMapsFragmentTrackingApp()
                viewModel.returnFromMapsFragment()
            }
        })

        //requestPermissions();
        if (mapFragment != null) {
            mapFragment.getMapAsync(OnMapReadyCallback { googleMap: GoogleMap? ->
                onMapReady(
                    googleMap!!
                )
            })
        }

        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        if (requestPermissions()) {
            if (gpsEnabled()) {
                startLocationService()
                // Bind to LocalService
                Intent(requireContext(), LocationService::class.java).also { intent ->
                    requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
                }
            } else requestGps()
        }
    }

    override fun onStop() {
        super.onStop()
        if(mBound) {
            locationService.getGpsStatus().removeObservers(requireActivity())
            locationService.getLocationUpdate().removeObservers(requireActivity())
            requireActivity().unbindService(mConnection)
            mBound = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        locationRequest = LocationRequest.create()
        locationRequest?.interval = TrackingConstants.time
        locationRequest?.fastestInterval = TrackingConstants.time
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (currentLocation != null) {
                    val point = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
                    googleMap.isMyLocationEnabled = true;
                } else {
                    val moscow = LatLng(55.751244, 37.618423)
                    googleMap.addMarker(MarkerOptions().position(moscow).title(activity?.getString(R.string.marker_Moscow)))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(moscow))
                    googleMap.setMinZoomPreference(11.0f)
                }
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // work with service

    private fun startLocationService() {
        if (!LocationService.isServiceWork) {
            val intent = Intent(requireContext(), LocationService::class.java)
            intent.action = TrackingConstants.ACTION_START_LOCATION_SERVICE
            requireActivity().startService(intent)
        }
    }

    private fun stopLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireActivity().stopService(intent)
    }

}