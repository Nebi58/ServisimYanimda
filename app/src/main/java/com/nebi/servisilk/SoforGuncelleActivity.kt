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
import com.nebi.servisilk.databinding.ActivitySoforGuncelleBinding
import com.nebi.servisilk.databinding.ActivityVeliGuncelleBinding

class SoforGuncelleActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySoforGuncelleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySoforGuncelleBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sofor_guncelle)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val soforId = intent.getStringExtra("soforId")
        if (soforId != null) {
            veriyiGetirVeAlanlaraYaz(soforId)
        }
        registerEventHandlers();
    }


    private fun veriyiGetirVeAlanlaraYaz(soforId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Soforler").child(soforId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val sofor = snapshot.getValue(SoforKayit::class.java)
                if (sofor != null) {
                    binding.SoforAdGun.setText(sofor.SoforAd)
                    binding.SoforSoyadGun.setText(sofor.SoforSoyad)
                    binding.SoforTcGun.setText(sofor.SoforTC)
                    binding.SoforTelGun.setText(sofor.SoforTelefon)
                    binding.SoforSifreGun.setText(sofor.SoforSifre)
                    // Eğer adresi göstermek istersen buraya ekleyebilirsin
                }
            } else {
                Toast.makeText(this, "Şoför bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun registerEventHandlers() {
        binding.SoforGuncelle.setOnClickListener{
            val soforId = intent.getStringExtra("soforId")
            if (soforId != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("Soforler").child(soforId)

                val updatedVeli = SoforKayit(
                    SoforAd = binding.SoforAdGun.text.toString(),
                    SoforSoyad = binding.SoforSoyadGun.text.toString(),
                    SoforTC = binding.SoforTcGun.text.toString(),
                    SoforTelefon = binding.SoforTelGun.text.toString(),
                    SoforSifre = binding.SoforSifreGun.text.toString()
                )

                dbRef.setValue(updatedVeli).addOnSuccessListener {
                    Toast.makeText(this, "Bilgiler güncellendi", Toast.LENGTH_SHORT).show()
                    finish() // Aktiviteyi kapat ve fragment'a dön
                }.addOnFailureListener {
                    Toast.makeText(this, "Güncelleme başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Şoför ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Activity'de işimiz bitince finish'e basınca activity'i kapatıyor ve önceki fragment'a dönüyor.
    public fun GeriButonu(view: View){
        finish()
    }
}