package com.nebi.servisilk

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import com.nebi.servisilk.databinding.ActivityAnaSayfaBinding

class AnaSayfaActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAnaSayfaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnaSayfaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setContentView(R.layout.activity_ana_sayfa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    public fun VeliGiris(view:View){
        val intent= Intent(this@AnaSayfaActivity,VeliGirisActivity::class.java)
        startActivity(intent)

    }
    public fun ServisciGiris(view:View){
        val intent= Intent(this@AnaSayfaActivity,SoforGirisActivity::class.java)
        startActivity(intent)
    }
}