package com.example.bike.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import android.widget.Toast.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bike.BuildConfig
import com.example.bike.R
import com.example.bike.databinding.FragmentLocationBinding
import com.example.bike.datasources.Route
import com.example.bike.repository.RouteRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.google.maps.model.LatLng
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.text.SimpleDateFormat
import java.util.*


object DistanceTracker {
    var totalDistance: Long = 0L
}

@Suppress("DEPRECATION")
class LocationFragment: Fragment(), OnMapReadyCallback {

    private lateinit var locBinding: FragmentLocationBinding;

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var client: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null

    private lateinit var startDateTime: LocalDateTime
    private var startLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locBinding = FragmentLocationBinding.inflate(inflater, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Инициализация провайдера для работы с локацией
        client = LocationServices.getFusedLocationProviderClient(requireContext())

        // Список из вариантов темы для карты
        setHasOptionsMenu(true)

        // Логика для кнопки "Старт"
        locBinding.startLoc.setOnClickListener {
            startLocationTracking()

            it.visibility = View.INVISIBLE
            locBinding.locationInfo.visibility = View.VISIBLE
            locBinding.stopLoc.visibility = View.VISIBLE
        }

        // Логика для кнопки "Стоп"
        locBinding.stopLoc.setOnClickListener {
            stopLocationTracking()

            it.visibility = View.INVISIBLE
            locBinding.locationInfo.visibility = View.INVISIBLE
            locBinding.startLoc.visibility = View.VISIBLE
        }

        return locBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.types_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        map.mapType = when (item.itemId) {
            R.id.normal -> GoogleMap.MAP_TYPE_NORMAL;
            R.id.terrain -> GoogleMap.MAP_TYPE_TERRAIN;
            R.id.satelite -> GoogleMap.MAP_TYPE_SATELLITE;
            R.id.hybrid -> GoogleMap.MAP_TYPE_HYBRID;

            else -> GoogleMap.MAP_TYPE_NONE
        }

        return super.onOptionsItemSelected(item)
    }

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    private fun checkPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true;
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            false;
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment().apply { arguments = Bundle().apply {} }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map

        if (checkPermission()) {
            this.map.isMyLocationEnabled = true;

            client.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastLocation = task.result

                    if (lastLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            com.google.android.gms.maps.model.LatLng(
                                lastLocation.latitude,
                                lastLocation.longitude),
                            15f))
                    }
                }
            }
        }
    }

    private fun startLocationTracking() {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            smallestDisplacement = 10.0F
        }

        DistanceTracker.totalDistance = 0L
        startLocation = null
        lastLocation = null
        startDateTime = LocalDateTime.now()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(loc: LocationResult) {
                loc?.let {
                    if(lastLocation == null){
                        lastLocation  = it.lastLocation
                        startLocation = it.lastLocation
                        return@let
                    }

                    it.lastLocation?.let { its_last ->

                        val distanceInMeters = its_last.distanceTo(lastLocation!!)
                        DistanceTracker.totalDistance += distanceInMeters.toLong()

                        var msg = "Completed: ${DistanceTracker.totalDistance} meters"

                        if(BuildConfig.DEBUG){
                            Log.d("TRACKER", "$msg, (added $distanceInMeters)")
                        }
                        locBinding.locationInfo.text = msg
                    }
                    lastLocation = it.lastLocation
                }

                super.onLocationResult(loc)
            }
        }

        client = LocationServices.getFusedLocationProviderClient(requireContext())

        if (checkPermission()) {
            client.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun postRouteCommand() {
        val route: Route = Route(
            UUID.randomUUID().toString(),
            FirebaseAuth.getInstance().currentUser!!.uid,
            startDateTime.toString(),
            LocalDateTime.now().toString(),
            DistanceTracker.totalDistance.toString(),
            startLocation!!.latitude.toString(),
            startLocation!!.longitude.toString(),
            lastLocation!!.latitude.toString(),
            lastLocation!!.longitude.toString()
        )

        RouteRepository.addItem(route)
    }

    private fun stopLocationTracking() {
        client.removeLocationUpdates(locationCallback)

        val between: Period = Period.fieldDifference(startDateTime, LocalDateTime.now())

        postRouteCommand()

        makeText(context, "Прошли ${DistanceTracker.totalDistance} метров", LENGTH_SHORT).show()
    }
}