package com.glovo.test.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.glovo.test.R
import com.glovo.test.common.permissions.PermissionUtils
import com.glovo.test.common.rx.ResponseObserver
import com.glovo.test.data.models.City
import com.glovo.test.ui.fragments.SelectCityFragment
import com.glovo.test.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    companion object {
        const val MY_LOCATION_REQUEST_CODE = 101
    }

    private val overlayColor by lazy {
        ResourcesCompat.getColor(resources, R.color.polygonFillColor, null)
    }

    private var googleMap: GoogleMap? = null
    private var inMarkersMode: Boolean = false

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

            /**
             * Observes city details (currency, language code...) request
             */
            cityDetailsByCodeResponse.observe(this@MainActivity, ResponseObserver(this@MainActivity.localClassName,
                onSuccess = { city ->
                    showCityDetails(city)
                },
                onError = {
                    showErrorToast()
                },
                showLoading = {
                    showLoading()
                },
                hideLoading = {
                    hideLoading()
                }
            ))

            /**
             * Observes decoded list of working areas and draws them on the map
             */
            workingAreasDecodedResponse.observe(this@MainActivity, ResponseObserver(this@MainActivity.localClassName,
                onSuccess = { workingAreasDecoded ->
                    drawWorkingAreaDecoded(workingAreasDecoded)
                },
                onError = {
                    showErrorToast()
                },
                showLoading = {
                    showLoading()
                },
                hideLoading = {
                    hideLoading()
                }
            ))

            /**
             * Observes list of city markers and draws them on the map
             */
            cityMarkersResponse.observe(this@MainActivity, ResponseObserver(this@MainActivity.localClassName,
                onSuccess = { cityMarkers ->
                    showCityMarkers(cityMarkers)
                    hideCityDetails()
                },
                onError = {
                    showErrorToast()
                },
                showLoading = {
                    showLoading()
                },
                hideLoading = {
                    hideLoading()
                }
            ))
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
        googleMap?.setOnCameraMoveListener(this)
        googleMap?.setOnMarkerClickListener(this)
        googleMap?.setOnMyLocationButtonClickListener(this)
        enableMyLocation()
    }

    override fun onCameraMove() {
        googleMap?.cameraPosition?.also { cameraPosition ->
            if (cameraPosition.zoom < 7.5 && !inMarkersMode) {
                inMarkersMode = true
                mainViewModel.getCityMarkers()
            } else if (cameraPosition.zoom >= 7.5) {
                inMarkersMode = false
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        enableMyLocation()
        return true
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.snippet?.also { cityCode ->
            mainViewModel.getCityByCode(cityCode, decodeArea = true)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != MY_LOCATION_REQUEST_CODE) {
            return
        }

        /**
         * Load cities and check if any contains the current location, or manually select a city
         */
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
            PermissionUtils.requestPermission(this,
                MY_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap?.isMyLocationEnabled = true

            getMyLocation()?.also { currentLocation ->
                val currentCoordinate = LatLng(currentLocation.latitude, currentLocation.longitude)
                zoomToCoordinate(currentCoordinate)
                mainViewModel.getCityByCurrentLocation(currentCoordinate)
            }
        }
    }

    /**
     * Obtain my current location if permissions are given
     */
    private fun getMyLocation(): Location? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)
            return locationManager.getLastKnownLocation(provider)
        }

        return null
    }

    private fun zoomToCoordinate(coordinate: LatLng) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13f))
    }

    private fun zoomToBounds(bounds: LatLngBounds) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 40))
    }

    /**
     * Draw Polygons based on decoded points from City working area, find the center of the whole
     * region and zoom to the center coordinate
     */
    private fun drawWorkingAreaDecoded(workingAreaDecoded: List<MainViewModel.WorkingAreaDecoded>) {

        googleMap?.clear()

        // calculate center of working area
        val builder = LatLngBounds.Builder()

        workingAreaDecoded.forEach { area ->
            if (area.points.isNotEmpty()) {
                val polygonOptions = PolygonOptions()
                    .strokeWidth(1.0f)
                    .fillColor(overlayColor)
                    .addAll(area.points)
                googleMap?.addPolygon(polygonOptions)

                area.points.forEach { builder.include(it) }
            }
        }

        val bounds: LatLngBounds = builder.build()

        // zoom to fit the working area
        zoomToBounds(bounds)
    }

    private fun showCityMarkers(cityMarkers: List<MainViewModel.CityMarker>) {
        googleMap?.clear()

        cityMarkers.forEach { cityMarker ->
            googleMap?.addMarker(MarkerOptions().position(cityMarker.center).snippet(cityMarker.code))
        }
    }

    private fun showCityDetails(city: City) {
        cityDetailsTextView.visibility = View.VISIBLE
        cityDetailsTextView.text = getString(
            R.string.city_details,
            city.name,
            city.countryCode,
            city.timeZone,
            city.languageCode,
            city.currency
        )
    }

    private fun hideCityDetails() {
        cityDetailsTextView.visibility = View.GONE
    }

    private fun showSelectCityFragment() {
        val selectCityFragment = SelectCityFragment()
        selectCityFragment.show(supportFragmentManager, "SelectCityFragment")
    }

    private fun showErrorToast() {
        Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    /**
     * Direct method of getting city code back from the SelectCityFragment
     */
    fun onCitySelected(cityCode: String) {
        mainViewModel.getCityByCode(cityCode, decodeArea = true)
    }
}
