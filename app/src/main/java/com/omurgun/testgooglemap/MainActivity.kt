package com.omurgun.testgooglemap

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.omurgun.testgooglemap.databinding.ActivityMainBinding
import com.omurgun.testgooglemap.databinding.ActivityMapsBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var locationRequest: LocationRequest? = null
    private val REQUEST_CHECK_SETTINGS = 10001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissionLauncher()
        checkPermission()
    }

    private fun checkPermissionLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result)
            {
                //permission granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    println("checkPermissionLauncher : Permission Okay")
                    checkGpsPermission()
                }
            }
            else
            {
                //permission denied
                println("checkPermissionLauncher : Permission needed")
            }
        }
    }
    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            }
            else
            {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        else
        {
            println("checkPermission : Permission Okay")
            checkGpsPermission()
        }
    }
    private fun checkGpsPermission(){
        turnOnLocation()
    }


    private fun turnOnLocation() {
        locationRequest = LocationRequest.create()
        if (locationRequest != null)
        {
            locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest!!.interval = 5000
            locationRequest!!.fastestInterval = 2000

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!)
            builder.setAlwaysShow(true)

            val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
                applicationContext
            )
                .checkLocationSettings(builder.build())

            result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
                try {
                    val response = task.getResult(ApiException::class.java)
                    Toast.makeText(this, "GPS is already tured on", Toast.LENGTH_SHORT)
                        .show()
                    goMap()
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val resolvableApiException = e as ResolvableApiException
                            resolvableApiException.startResolutionForResult(this,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (ex: IntentSender.SendIntentException) {
                            ex.printStackTrace()
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        }
                    }
                }
            })
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "GPS is tured on", Toast.LENGTH_SHORT).show()
                    goMap()
                }
                RESULT_CANCELED -> Toast.makeText(
                    this,
                    "GPS required to be tured on",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun goMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
        finish()

    }
}
