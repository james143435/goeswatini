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
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.item_list_content.view.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_main2.*

val MY_PERMISSIONS_REQUEST_LOCATION = 1234


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() , LocationListener {

    private lateinit var pageViewModel: PageViewModel
    private lateinit var selectedLocation:LatLng
    private var inte:String = ""

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

            Log.e("Section","${Bundle().get(ARG_SECTION_NUMBER)}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(com.goeswatini.R.layout.fragment_main2, container, false)
        if (savedInstanceState?.getString(ARG_SECTION_NUMBER) != null){
            inte = savedInstanceState?.getString(ARG_SECTION_NUMBER)!!
        }

        Log.e("ID","INTE ${inte}")
        val mapView: SupportMapFragment = childFragmentManager.findFragmentById(com.goeswatini.R.id.map) as SupportMapFragment
        mapView.getMapAsync {

            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Getting location service instance
            val enabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            val map = it

            map.setOnInfoWindowClickListener {
                Log.e("INFO WINDOW",it.title)
                item_ly.visibility = View.VISIBLE
                item_title.text = it.title
                selectedLocation = it.position

            }

            if (!enabled) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)

            val db = FirebaseFirestore.getInstance()

            navigate.setOnClickListener{
                val gmmIntentUri = Uri.parse("google.navigation:q=${selectedLocation.latitude},${selectedLocation.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

            button_close.setOnClickListener { item_ly.visibility = View.GONE }

            try {
                val location:Location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100L,100F,this)
                location?.let {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.latitude, it.longitude))
                            .title("Marker")
                    )

                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 9.0f));
                    db
                        .collection("aoi")
                        .whereEqualTo("type",inte)
                        .get()
                        .addOnSuccessListener {
                            map.clear()
                            for(doc in it){
                                Log.e("Data from server",doc.data.toString())
                                val loc = doc.data.get("location") as Map<String,Double>
                                map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(loc.get("lat")!!,loc.get("lng")!!))
                                        .title(doc.get("name") as String)
                                )
                            }

                        }
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
        fun newInstance(sectionNumber: String): PlaceholderFragment {
            return PlaceholderFragment().apply {
                Log.e("SECTIOIN NUMBER","$sectionNumber")
                inte = "$sectionNumber"
                arguments = Bundle().apply {
                    putString(ARG_SECTION_NUMBER, "$sectionNumber")
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