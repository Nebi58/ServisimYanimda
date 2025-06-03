package com.nebi.servisilk

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.nebi.servisilk.databinding.ActivityVeliGuncelleBinding

class VeliGuncelleActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVeliGuncelleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityVeliGuncelleBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val veliId = intent.getStringExtra("veliId")
        if (veliId != null) {
            veriyiGetirVeAlanlaraYaz(veliId)
        }
        registerEventHandlers();
    }


    private fun veriyiGetirVeAlanlaraYaz(veliId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Veliler").child(veliId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val veli = snapshot.getValue(VeliKayit::class.java)
                if (veli != null) {
                    binding.VeliAdGun.setText(veli.VeliAd)
                    binding.VeliSoyadGun.setText(veli.VeliSoyad)
                    binding.VeliTcGun.setText(veli.VeliTC)
                    binding.VeliTelGun.setText(veli.VeliTelefon)
                    binding.OgrAdGun.setText(veli.OgrenciAd)
                    binding.OgrSoyadGun.setText(veli.OgrenciSoyad)
                    binding.VeliSifreGun.setText(veli.Sifre)
                    // Eğer adresi göstermek istersen buraya ekleyebilirsin
                }
            } else {
                Toast.makeText(this, "Veli bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun registerEventHandlers() {
        binding.VeliGuncelle.setOnClickListener{
            val veliId = intent.getStringExtra("veliId")
            if (veliId != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("Veliler").child(veliId)

                val updatedVeli = VeliKayit(
                    VeliAd = binding.VeliAdGun.text.toString(),
                    VeliSoyad = binding.VeliSoyadGun.text.toString(),
                    VeliTC = binding.VeliTcGun.text.toString(),
                    VeliTelefon = binding.VeliTelGun.text.toString(),
                    OgrenciAd = binding.OgrAdGun.text.toString(),
                    OgrenciSoyad = binding.OgrSoyadGun.text.toString(),
                    Sifre = binding.VeliSifreGun.text.toString(),
                    Adres = "" // Eğer adres alanı varsa ekle, yoksa boş geç
                )

                dbRef.setValue(updatedVeli).addOnSuccessListener {
                    Toast.makeText(this, "Bilgiler güncellendi", Toast.LENGTH_SHORT).show()
                    finish() // Aktiviteyi kapat ve fragment'a dön
                }.addOnFailureListener {
                    Toast.makeText(this, "Güncelleme başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veli ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Activity'de işimiz bitince finish'e basınca activity'i kapatıyor ve önceki fragment'a dönüyor.
    public fun GeriButonu(view: View){
        finish()
    }
}