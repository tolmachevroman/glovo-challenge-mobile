package com.glovo.test.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.glovo.test.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.maps.SupportMapFragment
import com.glovo.test.common.permissions.PermissionUtils
import dagger.android.AndroidInjection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLng
import android.location.Criteria
import android.location.LocationManager
import android.content.Context
import android.location.Location


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val MY_LOCATION_REQUEST_CODE = 101
    }

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.also { mapFragment ->
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
        enableMyLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != MY_LOCATION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(
                permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // TODO ask to enter the city manually
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(
                this, MY_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap?.isMyLocationEnabled = true


            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)
            val location = locationManager.getLastKnownLocation(provider)
            zoomToLocation(location)
        }
    }

    private fun zoomToLocation(location: Location) {
        val coordinate = LatLng(location.latitude, location.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 14f))
    }
}
