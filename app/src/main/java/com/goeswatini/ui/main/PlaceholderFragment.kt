package com.goeswatini.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.item_list_content.view.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
val MY_PERMISSIONS_REQUEST_LOCATION = 1234


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() , LocationListener{

    private lateinit var pageViewModel: PageViewModel

    override fun onLocationChanged(location: Location?) {
        Log.e("Location Chaged", "$location")
    }

    override fun onProviderDisabled(provider: String?) {
       Log.e("Location Chaged", "$provider")
    }

    override fun onProviderEnabled(provider: String?) {
        Log.e("Location Chaged", "$provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.e("Location Chaged", "$provider")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION)

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(com.goeswatini.R.layout.fragment_main2, container, false)
        Log.e("ID","${com.goeswatini.R.id.map}")
        val mapView: SupportMapFragment = childFragmentManager.findFragmentById(com.goeswatini.R.id.map) as SupportMapFragment
        mapView.getMapAsync {

            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Getting location service instance
            val enabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            val map = it

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
            if (!enabled) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)


            try {
                val location:Location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100L,100F,this)
                location?.let {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.latitude, it.longitude))
                            .title("Marker")
                    )
                }
            }catch (e:SecurityException ){
                Log.e("Error",e.toString())
                Toast.makeText(context,"You need the location Permission to use the app", Toast.LENGTH_LONG).show()
            }
        }
        val textView: TextView = root.findViewById(com.goeswatini.R.id.section_label)
        pageViewModel.text.observe(this, Observer<String> {
            textView.text = it
        })
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**.
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    fun requestPermission(permission:String,enumerated:Int){
        if (ContextCompat.checkSelfPermission(
                this.context!!,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this.activity!!,
                    permission
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this.activity!!,
                    arrayOf(permission),
                    enumerated
                )
            }
        } else {

            // Permission has already been granted
        }

    }
}