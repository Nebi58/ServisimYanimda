package com.nebi.servisilk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.nebi.servisilk.databinding.ActivitySoforAnaBinding

class SoforAnaActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding:ActivitySoforAnaBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySoforAnaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sofor_ana)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var soforId=intent.getStringExtra("soforId").toString()
        Log.d("SoforAnaActivity", "Alınan SoforId: $soforId") // Şoförün id'si alındı.

        toolbar = findViewById(R.id.sofor_toolbar)
        drawerLayout = findViewById(R.id.sofor_ana)
        navigationView = findViewById(R.id.sofor_nav)

        setSupportActionBar(toolbar)

        navigationView.setNavigationItemSelectedListener(this)
        // Aşağıda yaptığınız kodla hamburger(yani 3 çizgili) ikonu eklemiş oluyorsunuz
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()  // toolbar'ın düzgün şekilde eklenmesini ve hizalanmasını sağlar
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sofor_ana_sayfa -> {
                val soforId = intent.getStringExtra("soforId").toString()
                replaceFragment(SoforAnaSayfaFragment.newInstance(soforId))
            }
            R.id.sofor_kullanici_bilgileri -> replaceFragment(SoforBilgileriFragment())
            R.id.sofor_cikis -> {
                showLogoutDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Fragment'a tıklayınca yapılan işlemdir.Yukarıda onNavigationItemSelected() metotunda yapıldı.
    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.sofor_fragment_container, fragment)
        transaction.commit()
    }

    // AlertDialog'lar sayesinde kullanıcıya çıkış yapmak istiyor musunuz diye sorular sorduk ve evetse çıkıyor hayırsa sayfada kalıyor.
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Çıkış Yap")
        builder.setMessage("Çıkış yapmak istediğinize emin misiniz?")
        // Eğer kullanıcı evet butonuna basarsa dialog'un içerisindeki işlemler gerçekleşir yani önceki Activity'e gidilir.
        builder.setPositiveButton("Evet") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SoforGirisActivity::class.java) // Giriş sayfanın adı
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Aktiviteyi kapat
        }
        // Hayır derse ise dialog kapanır ama çıkış yapmaz
        builder.setNegativeButton("Hayır") { dialog, _ ->
            dialog.dismiss() // Dialogu kapat ama çıkış yapma
        }
        builder.show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}