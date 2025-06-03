package com.nebi.servisilk

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nebi.servisilk.databinding.ActivityVeliGirisBinding

class VeliGirisActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVeliGirisBinding
    private lateinit var dbRef:DatabaseReference
    private lateinit var veliTc:EditText
    private lateinit var veliSifre:EditText
    private lateinit var veliGiris:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityVeliGirisBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        veliTc=findViewById(R.id.veliGirisTcText)
        veliSifre=findViewById(R.id.veliGirisSifreText)
        veliGiris=findViewById(R.id.btnGiris)
        dbRef= FirebaseDatabase.getInstance().getReference("Veliler")

        veliGiris.setOnClickListener {
            VeliGiris()
        }
    }
    public fun VeliGiris(){
        val TC=veliTc.text.toString()
        val Sifre=veliSifre.text.toString()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundUser = false // Kullanıcı var mı yok mu kontrol eden değişken

                for(veliSnapshot in snapshot.children){
                    val Vtc=veliSnapshot.child("veliTC").value.toString()  // Burada firebase'deki tc alınır
                    val Vsifre=veliSnapshot.child("sifre").value.toString()
                    val VeliId = veliSnapshot.child("veliId").value.toString() // Firebase'deki veli ID alınır

                    // Eğer tc ve şifre doğruysa main sayfasına gidiyoruz
                    if(Vtc == TC && Vsifre == Sifre){
                        foundUser=true
                        val intent= Intent(this@VeliGirisActivity,VeliAnaActivity::class.java)
                        intent.putExtra("veliId", VeliId) // VeliId'yi Intent'e ekliyoruz
                        Log.d("MainActivity", "Veri Aktarılıyor: veliId = $VeliId")
                        startActivity(intent)
                        return
                    }
                }
                // Eğer hiçbir kullanıcı eşleşmezse hata mesajları göster
                if (!foundUser) {
                    veliTc.error = "TC kimlik numaranız veya şifreniz yanlış."
                    veliSifre.error = "TC kimlik numaranız veya şifreniz yanlış."
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    public fun GeriButonu(view: View){
        val intent= Intent(this@VeliGirisActivity,AnaSayfaActivity::class.java)
        startActivity(intent)

    }

    public fun RegisterButon(view: View){
        val intent= Intent(this@VeliGirisActivity,VeliUyeOlActivity::class.java)
        startActivity(intent)

    }

    public fun sifre(view: View){
        val passwordEditText = findViewById<EditText>(R.id.veliGirisSifreText)
        val toggleButton = findViewById<ImageButton>(R.id.toggleButton3)

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