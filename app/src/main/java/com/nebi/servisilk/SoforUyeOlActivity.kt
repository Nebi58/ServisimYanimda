package com.nebi.servisilk

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nebi.servisilk.databinding.ActivitySoforUyeOlBinding

class SoforUyeOlActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySoforUyeOlBinding
    private lateinit var dbSoforRef:DatabaseReference
    private lateinit var soforAd:EditText
    private lateinit var soforSoyad:EditText
    private lateinit var soforTc:EditText
    private lateinit var soforTel:EditText
    private lateinit var soforSifre:EditText
    private lateinit var soforKaydet:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySoforUyeOlBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setContentView(R.layout.activity_sofor_uye_ol)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        soforAd=findViewById(R.id.SoforAdText)
        soforSoyad=findViewById(R.id.SoforSoyadText)
        soforTc=findViewById(R.id.SoforTcText)
        soforTel=findViewById(R.id.SoforNumaraText)
        soforSifre=findViewById(R.id.SoforSifreText)
        soforKaydet=findViewById(R.id.btnSoforKaydet)

        dbSoforRef= FirebaseDatabase.getInstance().getReference("Soforler")

        soforKaydet.setOnClickListener {
            SoforKayit()
        }
        soforTel.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true
                val text = s.toString().replace(" ", "") // Boşlukları kaldır

                val formattedText = when {
                    text.length <= 3 -> text
                    text.length <= 6 -> "${text.substring(0, 3)} ${text.substring(3)}"
                    text.length <= 8 -> "${text.substring(0, 3)} ${text.substring(3, 6)} ${text.substring(6)}"
                    else -> "${text.substring(0, 3)} ${text.substring(3, 6)} ${text.substring(6, 8)} ${text.substring(8)}"
                }

                soforTel.setText(formattedText)
                soforTel.setSelection(formattedText.length) // İmleci sona taşı
                isFormatting = false
            }
        })
    }
    public fun SoforKayit(){
        val Sad=soforAd.text.toString()
        val Ssoyad=soforSoyad.text.toString()
        val Stc=soforTc.text.toString()
        val Stel=soforTel.text.toString()
        val Ssifre=soforSifre.text.toString()

        if(Sad.isEmpty()){
            soforAd.error="Adınızı giriniz"
            return
        }
        if(Ssoyad.isEmpty()){
            soforSoyad.error="Soyadınızı giriniz"
            return
        }
        if(Stc.isEmpty()){
            soforTc.error="TC kimlik numaranızı giriniz"
            return
        }
        if(Stc.startsWith("0")){
            soforTc.error="TC kimlik numarası 0 ile başlayamaz."
            return
        }
        if(Stc.length <11){
            soforTc.error="Hatalı TC kimlik numarası girdiniz "
            return
        }
        if(Stel.isEmpty()){
            soforTel.error="Telefon numaranızı giriniz"
            return
        }
        if(Stel.startsWith("0")){
            soforTel.error="Telefon numarası 0 ile başlayamaz"
            return
        }
        val telefon = soforTel.text.toString().replace(" ", "")
        if(!telefon.matches(Regex("\\d{10}"))){
            soforTel.error="Eksik Telefon numarası girdiniz"
            return
        }
        if(Ssifre.isEmpty()){
            soforSifre.error="Şifrenizi giriniz"
            return
        }
        else {

            val SoforId = dbSoforRef.push().key!!
            val sofor = SoforKayit(SoforId,Sad,Ssoyad,Stc,Stel,Ssifre,OgrenciVeli = listOf())  // Burada boş bir Veliler listesi oluşturduk
            // Eğer ekleme sonucu eklenebilirse aşağıdaki kod satırı bize ya eklendi ya da hata verdi mesajı verecek
            // try-catch gibi bir işe yarıyor aşağıdaki kod satırı
            dbSoforRef.child(SoforId).setValue(sofor).addOnCompleteListener {
                Toast.makeText(this, "Başarıyla sisteme kayıt oldunuz", Toast.LENGTH_LONG).show()
                soforAd.text.clear()
                soforSoyad.text.clear()
                soforTc.text.clear()
                soforTel.text.clear()
                soforSifre.text.clear()

            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    public fun GeriButonu(view: View){
        val intent= Intent(this@SoforUyeOlActivity,SoforGirisActivity::class.java)
        startActivity(intent)

    }
    public fun sifre(view: View){
        val passwordEditText = findViewById<EditText>(R.id.SoforSifreText)
        val toggleButton = findViewById<ImageButton>(R.id.toggleButton2)

        var isPasswordVisible = false

        toggleButton.setOnClickListener {
            if (isPasswordVisible) {
                // Şifreyi gizle
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                // Şifreyi göster
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
                toggleButton.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPasswordVisible = !isPasswordVisible
            passwordEditText.setSelection(passwordEditText.text.length) // İmleci doğru konuma getir
        }

    }
}