package com.glovo.test.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.glovo.test.R
import com.glovo.test.common.permissions.PermissionUtils
import com.glovo.test.data.models.City
import com.glovo.test.di.interactors.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val MY_LOCATION_REQUEST_CODE = 101
    }

    private var googleMap: GoogleMap? = null

    private lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.apply {

            citiesResponse.observe(this@MainActivity, Observer { response ->
                when (response.status) {
                    Response.Status.SUCCESS -> {

                        //TODO compare working areas of newly arrived CityWithPolygons with my location
                        // and drawPolygonOptions for my location only
                        response.data?.let { cityWithPolygonOptions ->
                            val myLocation = getMyLocation()
                            val myLatLng = LatLng(myLocation.latitude, myLocation.longitude)

                            //TODO refactor!!
                            val points = mutableListOf<LatLng>()
                            cityWithPolygonOptions.polygonOptionsAvailable.forEach {
                                points.addAll(it.points)
                            }

//                            println("Got city ${cityWithPolygonOptions.name}")
                            val iAmInArea = PolyUtil.containsLocation(myLatLng, points, true)
                            if (iAmInArea) {
                                println("I am in the area of ${cityWithPolygonOptions.name}")
                                drawPolygonOptions(cityWithPolygonOptions.polygonOptionsAvailable)
                                mainViewModel.getCityByCode(cityWithPolygonOptions.code)
                            }
                        }

                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.LOADING -> {

                    }
                }
            })

            cityByCodeResponse.observe(this@MainActivity, Observer { response ->
                when (response.status) {
                    Response.Status.SUCCESS -> {
                        response.data?.also { showCityInformation(it) }
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.LOADING -> {

                    }
                }
            })
        }

        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)?.also { mapFragment ->
            mapFragment.getMapAsync(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.select_city -> showSelectCityFragment()
        }
        return super.onOptionsItemSelected(item)
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
            showSelectCityFragment()
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

            zoomToLocation(getMyLocation())

            mainViewModel.getCities()
        }
    }

    //TODO add permissions and case when user selected manually
    @SuppressLint("MissingPermission")
    private fun getMyLocation(): Location {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        return locationManager.getLastKnownLocation(provider)
    }

    private fun zoomToLocation(location: Location) {
        val coordinate = LatLng(location.latitude, location.longitude)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 14f))
    }

    private fun drawPolygonOptions(polygonOptionsAvailable: List<PolygonOptions>) {
        polygonOptionsAvailable.forEach { polygonOption ->

            Handler().postDelayed({
                googleMap?.addPolygon(polygonOption)
            }, 200)

        }
    }

    private fun showCityInformation(city: City) {
        cityInformationTextView.visibility = View.VISIBLE
        cityInformationTextView.text = getString(
            R.string.city_information,
            city.name,
            city.countryCode,
            city.timeZone,
            city.languageCode,
            city.currency
        )
    }

    private fun showSelectCityFragment() {
        val selectCityFragment = SelectCityFragment()
        selectCityFragment.show(supportFragmentManager, "selectCity")
    }

    fun onCitySelected(cityCode: String) {
        println("City selected was $cityCode")
    }
}
