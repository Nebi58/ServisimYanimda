package com.nebi.servisilk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.nebi.servisilk.databinding.ActivityMapsBinding
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var secilenAdres:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Haritaya uzun tıklamaları dinlemek için listener ekliyoruz
        mMap.setOnMapLongClickListener(this)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onLocationChanged(location: Location) {
                mMap.clear()

                // Kullanıcının konumuna marker ekleme
                val myPosition = LatLng(location.latitude, location.longitude)
                mMap.addMarker(MarkerOptions().position(myPosition).title("Konumunuz"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15f))

                // Adres alma
                getAddressFromLocation(location.latitude, location.longitude)

                locationManager.removeUpdates(this)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.root, "Konumunuzu almamız gereklidir", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver") {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        }
    }

    private fun registerLauncher(){
        permissionLauncher= registerForActivityResult(ActivityResultContracts.RequestPermission()){
                result->if(result){
            if(ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            }
            else{
                Toast.makeText(this@MapsActivity,"İzne ihtiyacımız var", Toast.LENGTH_LONG).show()
            }
        }
        }
    }
    // Haritaya uzun tıklanınca çalışacak fonksiyon
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapLongClick(p0: LatLng) {
        // Önceki marker temizle
        mMap.clear()

        // Yeni işaretçiyi ekle
        mMap.addMarker(MarkerOptions().position(p0).title("Seçilen Konum"))

        // Adres bilgisini al
        getAddressFromLocation(p0.latitude, p0.longitude)
    }




    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
        try {
            geocoder.getFromLocation(latitude, longitude, 1) { adresListesi ->
                if (adresListesi.isNotEmpty()) {
                    val ilkAdres = adresListesi.first()
                    val ulkeadi = ilkAdres.countryName ?: ""
                    val ilce = ilkAdres.locality ?: "" // İlçe
                    val mahalle = ilkAdres.subLocality ?: "" // Mahalle
                    val sokak = ilkAdres.thoroughfare ?: "" // Sokak
                    val numara = ilkAdres.subThoroughfare ?: "" // Sokak numarası

                    // Konumu daha doğru şekilde oluşturuyoruz
                    val adres = "$ulkeadi, $ilce, $mahalle, $sokak, $numara"
                    Log.d("Harita", "Seçilen Konum Adresi: $adres")

                    secilenAdres = adres // Adresi kaydet
                    // UI thread'inde onay dialogunu göster
                    runOnUiThread {
                        showConfirmationDialog()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konum Onayı")
        builder.setMessage("Seçilen konumu onaylıyor musunuz?")
        builder.setPositiveButton("Evet") { _, _ ->
            onaylaVeGonder() // Konum onaylandıysa işlemi gerçekleştirin
        }
        builder.setNegativeButton("Hayır") { dialog, _ ->
            dialog.dismiss() // Dialogu kapat
        }
        builder.show()
    }

    private fun onaylaVeGonder() {
        val resultIntent = Intent()
        resultIntent.putExtra("SEÇİLEN_ADRES", secilenAdres)
        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Aktiviteyi kapat ve geri dön
    }


}