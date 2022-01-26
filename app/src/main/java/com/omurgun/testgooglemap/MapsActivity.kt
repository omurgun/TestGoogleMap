package com.omurgun.testgooglemap

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.omurgun.testgooglemap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import android.content.IntentSender.SendIntentException
import android.os.SystemClock.sleep
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.Task


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var sharedPreferences : SharedPreferences
    private var isPermissionGranted: Boolean = false


    private var localeList : ArrayList<LatLng> = arrayListOf(
        LatLng(38.71475857143577, 35.53202598454766),
        LatLng(38.7148066195429, 35.5320670371437),
        LatLng(38.714864277228806, 35.532153247595396),
        LatLng(38.71494115407101, 35.53250424729156),
        LatLng(38.714966779666725, 35.53281624702149),
        LatLng(38.71495657470719, 35.53297251626972),
        LatLng(38.71492302969278, 35.532965350987695),
        LatLng(38.71486712130044, 35.53298684683379),
        LatLng(38.714808417441425, 35.53301550796191),
        LatLng(38.714741327257805, 35.533033421167)
    )
    var trackBoolean : Boolean? = null
    private var selectDrawOption : MapDrawState = MapDrawState.circle

    var selectedLatitude: Double? = null
    var selectedLongitude: Double? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("com.omurgun.testgooglemap", MODE_PRIVATE)
        trackBoolean = false
        //registerLauncher()
        selectedLatitude = 0.0
        selectedLongitude = 0.0


        if (isGpsEnabled())
        {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }


        binding.btnCircle.setOnClickListener {
            selectDrawOption = MapDrawState.circle
            it.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_circle_enable_24)
            binding.btnSquare.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_crop_square_disable_24)
            binding.btnLine.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_gesture_disable_24)
            mMap.clear()
            addCircle(LatLng(selectedLatitude!!, selectedLongitude!!))
        }

        binding.btnSquare.setOnClickListener{
            selectDrawOption = MapDrawState.square
            it.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_crop_square_enable_24)
            binding.btnCircle.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_circle_disable_24)
            binding.btnLine.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_gesture_disable_24)
            mMap.clear()
            addPolygon(LatLng(selectedLatitude!!, selectedLongitude!!))
        }

        binding.btnLine.setOnClickListener{
            selectDrawOption = MapDrawState.line
            it.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_gesture_enable_24)
            binding.btnCircle.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_circle_disable_24)
            binding.btnSquare.background = AppCompatResources.getDrawable(this,R.drawable.ic_baseline_crop_square_disable_24)
            mMap.clear()
            addPolyline(LatLng(selectedLatitude!!, selectedLongitude!!))
        }


    }


    private fun isGpsEnabled() : Boolean{
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        val providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (providerEnable)
            return true

        return false

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        mMap.uiSettings.isTiltGesturesEnabled = true


        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                println("onLocationChanged")
                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                if (!trackBoolean!!) {
                    mMap.clear()
                    val userLocation = LatLng(location.latitude, location.longitude)
                    addCircle(LatLng(location.latitude, location.longitude))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                }
            }

            override fun onProviderEnabled(provider: String) {
                println("onProviderEnabled")
            }

            override fun onProviderDisabled(provider: String) {
                println("onProviderDisabled")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                println("onStatusChanged")
            }

        }


        // click go my location show circle on map
        mMap.setOnMyLocationButtonClickListener {

            getUserLocation(MapOpenState.longPress)
        }

        getUserLocation(MapOpenState.first)
    }
    private fun getUserLocation(mapOpenState : MapOpenState) : Boolean {
        mMap.clear()

        if (ContextCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

            Thread(Runnable {
                var lastLocation : Location? = null
                while (true) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null)
                    {
                        println("lastLocation : GPS_PROVIDER")
                        break
                    }

                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (lastLocation != null)
                    {
                        println("lastLocation : NETWORK_PROVIDER")
                        break
                    }

                }

                println("lastLocation : ${lastLocation?.latitude},${lastLocation?.longitude}")
                if (lastLocation != null) {
                    isPermissionGranted = true
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    selectedLongitude = lastLocation.longitude
                    selectedLatitude = lastLocation.latitude
                    if (mapOpenState == MapOpenState.first) {
                        addCircle(lastUserLocation)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    } else if (mapOpenState == MapOpenState.longPress) {
                        mapLongClickHandle(lastUserLocation)
                    }
                }
            }).run()


        }
        return false
    }














    private fun mapLongClickHandle(p0: LatLng) {
        mMap.clear()
        when (selectDrawOption) {
            MapDrawState.circle -> {
                addCircle(p0)
            }
            MapDrawState.square -> {
                addPolygon(p0)
            }
            MapDrawState.line -> {
                addPolyline(p0)
            }
        }
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
    }
    override fun onMapLongClick(p0: LatLng) {
        mapLongClickHandle(p0)
    }
    private fun addCircle(latLng: LatLng) {
        val mCircleOptions = CircleOptions().center(latLng)
            .radius(300.0)
            .fillColor(getColor(R.color.yellow))
            .strokeColor(Color.BLUE)
            .strokeWidth(3f)
        mMap.addCircle(mCircleOptions)
    }
    private fun addPolygon(latLng: LatLng) {
        val mPolygonOptions = PolygonOptions().clickable(true)
            .add(
                LatLng(latLng.latitude + 0.002, latLng.longitude + 0.002),
                LatLng(latLng.latitude - 0.002, latLng.longitude + 0.002),
                LatLng(latLng.latitude - 0.002, latLng.longitude - 0.002),
                LatLng(latLng.latitude + 0.002, latLng.longitude - 0.002))
            .strokeColor(Color.BLUE)
            .strokeWidth(3f)
            .fillColor(getColor(R.color.yellow))
        mMap.addPolygon(mPolygonOptions)
    }
    private fun addMarker(index: Int?,latLng: LatLng) {
        if (index == null)
        {
            val mMarkerOptions = MarkerOptions().position(latLng)
                .title("Kayseri")
                .snippet("omur gun : 1453")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.map))
            mMap.addMarker(mMarkerOptions)
        }
        else
        {
            if (index == 0)
            {
                val mMarkerOptions = MarkerOptions().position(latLng)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.map))
                mMap.addMarker(mMarkerOptions)
            }
            else if(index == localeList.size - 1)
            {
                val mMarkerOptions = MarkerOptions().position(latLng)
                    .title("End")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.map))
                mMap.addMarker(mMarkerOptions)
            }
        }

    }
    private fun addPolyline(latLng: LatLng) {
        val mPolylineOptions = PolylineOptions().clickable(true)
            .color(Color.RED)
            .addAll(localeList)

        addMarker(0,localeList[0])
        addMarker(localeList.size - 1,localeList[localeList.size - 1])
        mMap.addPolyline(mPolylineOptions)
    }
}

enum class MapDrawState {
    circle,
    square,
    line
}

enum class MapOpenState {
    longPress,
    first
}
