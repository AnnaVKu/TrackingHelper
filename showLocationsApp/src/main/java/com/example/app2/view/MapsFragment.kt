package com.example.app2.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.app2.R
import com.example.core.ResponseWrapper
import com.example.core.MyApplication
import com.example.app2.databinding.FragmentMapsBinding
import com.example.app2.di.DaggerShowComponent
import com.example.auth.di.DaggerAuthComponent
import com.example.core.db.Response
import com.example.core.utils.UtilsConvertersForMaps
import com.example.core.utils.UtilsResponse
import com.example.core.viewModel.MyViewModel
import com.example.core.viewModel.ViewModelFactory
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[MyViewModel::class.java]
    }

    private val responses: MutableList<Response> = ArrayList()
    private var selectedResponses: MutableList<Response> = ArrayList()
    private lateinit var selectedDate: Date


    private lateinit var map: GoogleMap
    private var polylineOptions: PolylineOptions? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Work with permissions

    private val PERMISSIONS_FOREGROUND_LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestForegroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.containsValue(false)) requestPermissions()
        }

    private fun requestPermissions() {
        requestForegroundLocationPermissionLauncher.launch(PERMISSIONS_FOREGROUND_LOCATION)
    }

    private fun checkForegroundPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        
        val appComponent = (requireActivity().applicationContext as MyApplication).appComponent
        val authComponent = DaggerAuthComponent.factory().create(appComponent = appComponent)

        DaggerShowComponent.factory().create(appComponent, authComponent).injectFragmentMaps(this)

        // Load data
        if (!viewModel.hasResponses()) viewModel.loadResponses()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        // Toolbar
        binding.toolbar.inflateMenu(R.menu.main_menu)
        binding.toolbar.title = getString(R.string.title_app)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sign_out -> {
                    viewModel.signOutWasClicked()
                    true
                }
                else -> false
            }
        }

        // Calendar
        binding.imageViewCalendar.setOnClickListener { createDatePickerFragment() }

        // Sign out
        viewModel.getLiveDataIsSignOut().observe(requireActivity(), { result ->
            if (result.status == ResponseWrapper.Status.SUCCESS) {
                viewModel.clearDataForMapsFragmentShowApp()
                viewModel.returnFromMapsFragment()
            }
        })

        // Floating button
        binding.floatingActionButtonMyLocation.setOnClickListener {
            if (checkForegroundPermissions(PERMISSIONS_FOREGROUND_LOCATION)) {
                // if date equals to current date
                if (binding.textViewDate.text.toString() == UtilsConvertersForMaps
                        .convertLongToStringForTitleForCardViewTable(System.currentTimeMillis())
                ) {
                    findCurrentLocationOfParent()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.warning_to_find_users_location),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.warning_allow_permissions),
                    Toast.LENGTH_SHORT
                ).show()
                requestForegroundLocationPermissionLauncher.launch(
                    PERMISSIONS_FOREGROUND_LOCATION
                )
            }
        }

        // Drop image
        binding.imageViewDropSelectedDateToTheCurrentDate.setOnClickListener { dropToTheCurrentDate() }

        viewModel.getLiveDataIsSelectedDay().observe(requireActivity(), { it ->
            if (it == true) {
                selectedDate = viewModel.getSelectedDay()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (viewModel.getIsSelectedDay() == true) {
            selectedDate = viewModel.getSelectedDay()
        }

        if (selectedResponses.isNotEmpty()
            && UtilsConvertersForMaps.convertLongToStringForTitleForCardViewTable(selectedResponses[0].time)
            != UtilsConvertersForMaps.convertLongToStringForTitleForCardViewTable(System.currentTimeMillis())
        ) {
            drawMap()
            selectedResponses.clear()
        } else {
            viewModel.getLiveDataResponses().observe(requireActivity(), {
                responses.clear()
                responses.addAll(it)
                if (responses.isNotEmpty()) {
                    selectedResponses.clear()
                    if (viewModel.getIsSelectedDay() == true) {
                        map.clear()
                        selectedResponses = viewModel.selectingResponses(selectedDate)
                        drawMap()
                    } else {
                        map.clear()
                        selectedResponses.addAll(
                            UtilsResponse.selectLocationsAccordingToTheCurrentDate(
                                Date(System.currentTimeMillis()),
                                responses
                            )
                        )
                        if (selectedResponses.isNotEmpty()) drawMap()
                        else emptyMap(googleMap)
                    }
                } else {
                    emptyMap(map)
                }
            })
        }
    }

    private fun emptyMap(map: GoogleMap) {
        val moscow = LatLng(55.751244, 37.618423)
        map.addMarker(MarkerOptions().position(moscow).title(activity?.getString(R.string.marker_title)))
        map.moveCamera(CameraUpdateFactory.newLatLng(moscow))
        map.setMinZoomPreference(11.0f)
        setDate()
    }

    // Inner methods
    private fun drawMap() {
        if (polylineOptions != null) polylineOptions = null
        val list = ArrayList<LatLng>()
        // create polyline
        polylineOptions = PolylineOptions().color(Color.BLUE).width(7f)

        // full polyline
        for (response in selectedResponses) {
            polylineOptions?.let {
                it.add(response.latitude?.let { it1 ->
                    response.longitude?.let { it2 ->
                        LatLng(
                            it1,
                            it2
                        )
                    }
                })
            }
            response.latitude?.let { response.longitude?.let { it1 -> LatLng(it, it1) } }
                ?.let { list.add(it) }
        }

        if (polylineOptions != null) {
            // draw polyline
            map.addPolyline(polylineOptions!!)

            // adding markers on the polyline
            for (i in selectedResponses.indices) {
                map.addMarker(
                    MarkerOptions().position(list[i]).title(
                        UtilsConvertersForMaps.convertLongToStringForTitleForMarker(
                            selectedResponses[i].time
                        )
                    ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            }
            val builder = LatLngBounds.Builder()
            for (latLng in list) {
                builder.include(latLng)
            }
            // val bounds = builder.build()
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }

        // update the CardViewDate
        setDate()
    }

    private fun setDate() {
        if (selectedResponses.isNotEmpty()) {
            binding.textViewDate.text =
                UtilsConvertersForMaps.convertLongToStringForTitleForCardViewTable(
                    selectedResponses[0].time
                )
            val dateArr: Array<String> =
                binding.textViewDate.text.toString().split(" ").toTypedArray()
            val date =
                dateArr[2] + "-" + UtilsConvertersForMaps.converterForMonth(dateArr[1]) + "-" + dateArr[0]
            UtilsConvertersForMaps.parseDate(date)?.let { viewModel.selectedDateWasChanged(it) }
            UtilsConvertersForMaps.parseDate(date)?.let { viewModel.selectedDateWasChanged(it) }
        } else {
            binding.textViewDate.text =
                UtilsConvertersForMaps.convertLongToStringForTitleForCardViewTable(System.currentTimeMillis())
            viewModel.selectedDateWasNotChanged()
        }
    }

    @SuppressLint("MissingPermission")
    fun findCurrentLocationOfParent() {
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            @SuppressLint("MissingPermission")
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val currentLocation = locationResult.lastLocation
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                map.isMyLocationEnabled = true
                map.addMarker(MarkerOptions().position(latLng))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createDatePickerFragment() {
        DatePickerFragment.getInstance { view, year, month, dayOfMonth ->
            Log.i("Result", "createDatePickerFragment $year $month $dayOfMonth")
            // to take right mass of locations
            selectedResponses.clear()
            selectedResponses = viewModel.selectingResponses(
                year = year,
                month = month,
                dayOfMonth = dayOfMonth
            )
            if (selectedResponses.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.warning_not_data),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // show selectedResponses
                map.clear()
                onMapReady(map)
            }
        }?.show(childFragmentManager, "DatePicker")
    }

    private fun dropToTheCurrentDate() {
        viewModel.selectedDateWasNotChanged()
        map.clear()
        selectedResponses.clear()
        onMapReady(map)
    }

}