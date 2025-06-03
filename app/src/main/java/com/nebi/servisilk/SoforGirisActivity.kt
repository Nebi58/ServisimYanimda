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
import com.nebi.servisilk.databinding.ActivitySoforGirisBinding
import com.nebi.servisilk.databinding.ActivityVeliGirisBinding

class SoforGirisActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoforGirisBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var soforTc:EditText
    private lateinit var soforSifre:EditText
    private lateinit var soforGiris: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySoforGirisBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        soforTc=findViewById(R.id.soforTc)
        soforSifre=findViewById(R.id.soforSifre)
        soforGiris=findViewById(R.id.btnSoforGiris)
        dbRef= FirebaseDatabase.getInstance().getReference("Soforler")

        soforGiris.setOnClickListener {
            SoforGiris()
        }
    }


    public fun SoforGiris(){
        val TC=soforTc.text.toString()
        val Sifre=soforSifre.text.toString()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundUser = false // Kullanıcı var mı yok mu kontrol eden değişken

                for(soforSnapshot in snapshot.children){
                    val Stc=soforSnapshot.child("soforTC").value.toString()  // Burada firebase'deki tc alınır
                    val Ssifre=soforSnapshot.child("soforSifre").value.toString()
                    val SoforId = soforSnapshot.child("soforId").value.toString() // Firebase'deki sofor ID alınır

                    // Eğer tc ve şifre doğruysa main sayfasına gidiyoruz
                    if(Stc == TC && Ssifre == Sifre){
                        foundUser=true
                        val intent= Intent(this@SoforGirisActivity,SoforAnaActivity::class.java)
                        intent.putExtra("soforId", SoforId) // SoforId'yi Intent'e ekliyoruz.Böylece veriyi diğer activity'e aktarıyoruz
                        Log.d("MainActivity", "Veri Aktarılıyor: soforId = $SoforId")
                        startActivity(intent)

                        return
                    }
                }
                // Eğer hiçbir kullanıcı eşleşmezse hata mesajları göster
                if (!foundUser) {
                    soforTc.error = "TC kimlik numaranız veya şifreniz yanlış."
                    soforSifre.error = "TC kimlik numaranız veya şifreniz yanlış."
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }


    fun sifre(view: View) {
        val passwordEditText = findViewById<EditText>(R.id.soforSifre)
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
            passwordEditText.setSelection(passwordEditText.text.length) // İmleci doğru konuma getir
        }
    }
    fun SRegisterButon(view: View) {
        val intent = Intent(this@SoforGirisActivity, SoforUyeOlActivity::class.java)
        startActivity(intent)
    }


    fun GeriButonu(view: View){
        val intent= Intent(this@SoforGirisActivity,AnaSayfaActivity::class.java)
        startActivity(intent)

    }

}