package com.nebi.servisilk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nebi.servisilk.databinding.ActivityVeliUyeOlBinding

class VeliUyeOlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVeliUyeOlBinding
    private lateinit var veliAd:  EditText
    private lateinit var veliSoyad:  EditText
    private lateinit var veliTc:  EditText
    private lateinit var veliTel:  EditText
    private lateinit var ogrAd:  EditText
    private lateinit var ogrSoyad:  EditText
    private lateinit var veliSifre:  EditText
    private lateinit var adresTextView: TextView
    private lateinit var veliUyeOlButton: Button
    private lateinit var dbRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityVeliUyeOlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_veli_uye_ol)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        veliAd=findViewById(R.id.VeliAdText)
        veliSoyad=findViewById(R.id.VeliSoyadText)
        veliTc=findViewById(R.id.VeliTcText)
        veliTel=findViewById(R.id.VeliTelText)
        ogrAd=findViewById(R.id.OgrAdText)
        ogrSoyad=findViewById(R.id.OgrSoyadText)
        veliSifre=findViewById(R.id.VeliSifreText)
        adresTextView=findViewById(R.id.AdresTextView)
        veliUyeOlButton=findViewById(R.id.VeliUyeOlbutton)
        dbRef=FirebaseDatabase.getInstance().getReference("Veliler")

        veliUyeOlButton.setOnClickListener{
            VeliKayit()
        }

        veliTel.addTextChangedListener(object : TextWatcher {
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

                veliTel.setText(formattedText)
                veliTel.setSelection(formattedText.length) // İmleci sona taşı
                isFormatting = false
            }
        })

    }
    public fun VeliKayit(){
        val Vad=veliAd.text.toString()
        val Vsoyad=veliSoyad.text.toString()
        val Vtc=veliTc.text.toString()
        val Vtel=veliTel.text.toString()
        val Oad=ogrAd.text.toString()
        val Osoyad=ogrSoyad.text.toString()
        val Sifre=veliSifre.text.toString()
        val Adres=adresTextView.text.toString()


        if(Vad.isEmpty()){
            veliAd.error="Adınızı giriniz"
            return
        }
        if(Vsoyad.isEmpty()){
            veliSoyad.error="Soyadınızı giriniz"
            return
        }
        if(Vtc.isEmpty()){
            veliTc.error="TC kimlik numaranızı giriniz"
            return
        }
        if(Vtc.startsWith("0")){
            veliTc.error="TC kimlik numarası 0 ile başlayamaz."
            return
        }
        if(Vtc.length <11){
            veliTc.error="Hatalı TC kimlik numarası girdiniz "
            return
        }
        if(Vtel.isEmpty()){
            veliTel.error="Telefon numaranızı giriniz"
            return
        }
        if(Vtel.startsWith("0")){
            veliTel.error="Telefon numarası 0 ile başlayamaz"
            return
        }
        val telefon = veliTel.text.toString().replace(" ", "")
        // Aşağıdaki regex ifadesi sadece rakamları sayar ve arada boşluk kullanılırsa hata verir.
        //  Yani regex() kullanıyorsan 535 846 formatı hata verir 535846 şeklinde yazmalısın
        // Tasarımım 535 846 58 55 şeklinde kalıp aşağıdaki kontrolü yapabilmek için de yukarıdaki kod yazılır
        if(!telefon.matches(Regex("\\d{10}"))){
            veliTel.error="Eksik Telefon numarası girdiniz"
            return
        }
        if(Oad.isEmpty()){
            ogrAd.error="Öğrencinizin adını giriniz"
            return
        }
        if(Osoyad.isEmpty()){
            ogrSoyad.error="Öğrencinizin soyadını giriniz"
            return
        }
        if(Sifre.isEmpty()){
            veliSifre.error="Şifrenizi giriniz"
            return
        }
        else {// Aşağıdaki kod sayesinde ilk başta bir Id oluşturduk ki firebase'e kayıt olsun.Sonrasında ise VeliKayit
            // sınıfına ekleyeceğimiz değerler ile gittik ve firebase'e ekledik
            val VeliId = dbRef.push().key!!
            val veli = VeliKayit(VeliId, Vad, Vsoyad, Vtc, Vtel, Oad, Osoyad, Sifre,Adres)
            // Eğer ekleme sonucu eklenebilirse aşağıdaki kod satırı bize ya eklendi ya da hata verdi mesajı verecek
            // try-catch gibi bir işe yarıyor aşağıdaki kod satırı
            dbRef.child(VeliId).setValue(veli).addOnCompleteListener {
                Toast.makeText(this, "Başarıyla sisteme kayıt oldunuz", Toast.LENGTH_LONG).show()
                veliAd.text.clear()
                veliSoyad.text.clear()
                veliTc.text.clear()
                veliTel.text.clear()
                ogrAd.text.clear()
                ogrSoyad.text.clear()
                veliSifre.text.clear()
                val intent= Intent(this@VeliUyeOlActivity,VeliAnaActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    public fun GeriButonu(view: View){
        val intent= Intent(this@VeliUyeOlActivity,VeliGirisActivity::class.java)
        startActivity(intent)

    }
    public fun Maps(view: View){
        val intent= Intent(this@VeliUyeOlActivity,MapsActivity::class.java)
        adresSecici.launch(intent)

    }

    public fun sifre(view: View){
        val passwordEditText = findViewById<EditText>(R.id.VeliSifreText)
        val toggleButton = findViewById<ImageButton>(R.id.toggleButton)

        var isPasswordVisible = false

        toggleButton.setOnClickListener {
            if (isPasswordVisible) {
                // Şifreyi gizle
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                // Şifreyi göster
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
                toggleButton.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPasswordVisible = !isPasswordVisible
            passwordEditText.setSelection(passwordEditText.text.length) // İmleci doğru konuma getirval passwordEditText = findViewById<EditText>(R.id.SifreText)
        }
    }
    private val adresSecici = //Maps activityden gelen adresi Text içinde göstermeyi sağlar.
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->//başka bir aktivite açılıyor ve sonuç bekleniyor.
            if (result.resultCode == Activity.RESULT_OK) {//kullanıcı bir adres seçip onayladıysa, devam et yoksa bir şey yapma.
                val adres = result.data?.getStringExtra("SEÇİLEN_ADRES") ?: ""//Intent'in içindeki "SEÇİLEN_ADRES" anahtarını okuyoruz.
                adresTextView.text = adres
            }
        }
}