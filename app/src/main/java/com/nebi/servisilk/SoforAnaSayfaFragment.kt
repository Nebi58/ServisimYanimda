package com.nebi.servisilk

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SoforAnaSayfaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val client = OkHttpClient()
    private var polyline: Polyline? = null

    private var waypoints = mutableListOf<LatLng>()


    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var userLocation: LatLng? = null

    private var isUserLocationReady = false
    private var isWaypointsReady = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sofor_ana_sayfa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val soforId = arguments?.getString("soforId")
        Log.d("SoforAnaSayfaFragment", "Alınan SoforId: $soforId")

        if (soforId != null) {
            val database = FirebaseDatabase.getInstance()
            val soforRef = database.getReference("Soforler").child(soforId).child("OgrenciVeli")


            soforRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adresListesi = mutableListOf<String>()

                    for (ogrenciSnapshot in snapshot.children) {
                        val adres = ogrenciSnapshot.child("adres").getValue(String::class.java)
                        adres?.let { adresListesi.add(it) }
                    }
                    Log.d("FirebaseData", "Toplam adres alındı: ${adresListesi.size}")
                    convertAddressesToCoordinates(adresListesi)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Veri çekme hatası: ${error.message}")
                }
            })
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        checkLocationPermission()
    }
    // İZİNLER
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getUserLocation()
        }
    }
    // İZİNLER
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                Toast.makeText(requireContext(), "Konum izni gerekli!", Toast.LENGTH_LONG).show()
            }
        }
    }
    // KULLANICI KONUMUNU ALMA
    private fun getUserLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("LocationService", "GPS hizmeti kapalı!")
            Toast.makeText(requireContext(), "GPS hizmetini açın!", Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    isUserLocationReady = true  // Konum alındı!
                    Log.d("UserLocation", "Konum Alındı: ${location.latitude}, ${location.longitude}")

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 12f))
                    mMap.addMarker(
                        MarkerOptions()
                            .position(userLocation!!)
                            .title("Mevcut Konum")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    checkAndStartRoute() // Konum alındıktan sonra kontrol et
                    fusedLocationClient.removeLocationUpdates(this) // Konum alındıktan sonra durdur
                } else {
                    Log.e("UserLocation", "Konum alınamadı!")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            Log.e("UserLocation", "Konum izni verilmedi!")
        }
    }


    // FİREBASE'dEN GELEN EVLERE MARKER EKLER
    private fun addWaypointsMarkers() {
        waypoints.forEachIndexed { index, latLng ->
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Ev ${index + 1}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }
    private fun convertAddressesToCoordinates(adresListesi: List<String>) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        waypoints.clear()  // Önce listeyi temizleyelim

        for (adres in adresListesi) {
            try {
                val addresses: MutableList<Address>? = geocoder.getFromLocationName(adres, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val latitude = address.latitude
                    val longitude = address.longitude
                    Log.d("AddressCoordinates", "Adres: $adres, Enlem: $latitude, Boylam: $longitude")

                    waypoints.add(LatLng(latitude, longitude)) // Yeni waypoint ekle
                } else {
                    Log.d("AddressCoordinates", "Adres bulunamadı: $adres")
                }
            } catch (e: Exception) {
                Log.e("AddressCoordinates", "Adres çevirmede hata: ${e.message}")
            }
        }

        Log.d("FirebaseWaypoints", "Toplam Waypoint: ${waypoints.size}")

        addWaypointsMarkers()

        isWaypointsReady = waypoints.isNotEmpty() // En az bir tane adres varsa tamamdır
        checkAndStartRoute() // Adresler alındıktan sonra kontrol et
    }

    private fun checkAndStartRoute() {
        if (isUserLocationReady && isWaypointsReady) {
            Log.d("RoutesApi", "Hem konum hem de adresler alındı. Rota hesaplanıyor...")
            getRouteWithGoogleRoutesApi(userLocation!!, waypoints) // Bütün adresleri gönder
        } else {
            Log.d("RoutesApi", "Henüz tüm veriler hazır değil! Konum: $isUserLocationReady, Adresler: $isWaypointsReady")
        }
    }




    private fun getRouteWithGoogleRoutesApi(start: LatLng, waypoints: List<LatLng>) {
        val url = "https://routes.googleapis.com/directions/v2:computeRoutes"

        val intermediatesJson = JSONArray()
        // Aradaki evleri ekliyoruz
        for (i in 1 until waypoints.size) { // waypoints[0] destination oluyor, kalanlar ara nokta
            val point = waypoints[i]
            val intermediate = JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", point.latitude)
                        put("longitude", point.longitude)
                    })
                })
            }
            intermediatesJson.put(intermediate)
        }

        val requestBody = JSONObject().apply {
            put("origin", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", start.latitude)
                        put("longitude", start.longitude)
                    })
                })
            })
            put("destination", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", waypoints[0].latitude)  // İlk hedef
                        put("longitude", waypoints[0].longitude)
                    })
                })
            })
            put("travelMode", "DRIVE")
            if (intermediatesJson.length() > 0) {
                put("intermediates", intermediatesJson)  // Ara noktaları ekle
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", "AIzaSyDyLHqDYvuT1P3lc6nAnWhmTXbJTsrjBVc")
            .addHeader("X-Goog-FieldMask", "routes.legs.steps.polyline")
            .post(RequestBody.create("application/json".toMediaType(), requestBody.toString()))
            .build()

        Log.d("RoutesAPI", "API isteği gönderiliyor. Waypoints: ${waypoints.size}")

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("RoutesAPI", "API çağrısı başarısız: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseData ->
                        Log.d("RoutesAPI", "API yanıtı başarılı!")
                        parseRoute(responseData)
                    }
                } else {
                    Log.e("RoutesAPI", "API cevabı başarısız! Durum kodu: ${response.code}")
                }
            }
        })
    }


    private fun parseRoute(responseData: String) {
        Log.d("RoutesAPI", "Yol verisi işleniyor...")
        try {
            val jsonResponse = JSONObject(responseData)
            val routes = jsonResponse.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")
                val polylinePoints = mutableListOf<LatLng>()

                for (i in 0 until legs.length()) {
                    val steps = legs.getJSONObject(i).getJSONArray("steps")
                    for (j in 0 until steps.length()) {
                        val polyline = steps.getJSONObject(j).getJSONObject("polyline").getString("encodedPolyline")
                        polylinePoints.addAll(PolyUtil.decode(polyline))
                    }
                }
                drawRouteOnMap(polylinePoints)
            } else {
                Log.e("RoutesAPI", "Rota verisi bulunamadı!")
            }
        } catch (e: Exception) {
            Log.e("RoutesAPI", "Yol verisi işlenirken hata: ${e.message}")
        }
    }



    // Haritaya rota çizin
    private fun drawRouteOnMap(route: List<LatLng>) {
        if (route.isEmpty()) {
            Log.e("RoutesAPI", "Çizilecek rota yok!")
            return
        }

        Log.d("RoutesAPI", "Rota çizilmeye başlanıyor...")
        val polylineOptions = PolylineOptions().apply {
            addAll(route)
            width(8f)
            color(Color.BLUE)
            geodesic(true)
        }

        requireActivity().runOnUiThread {
            polyline?.remove()
            polyline = mMap.addPolyline(polylineOptions)
        }
        Log.d("RoutesAPI", "Rota başarıyla çizildi!")
    }

    // ŞOFOR ID'SİNİ ALMA
    companion object {
        fun newInstance(soforId: String): SoforAnaSayfaFragment {
            val fragment = SoforAnaSayfaFragment()
            val bundle = Bundle()
            bundle.putString("soforId", soforId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
