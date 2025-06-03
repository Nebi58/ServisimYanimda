package com.nebi.servisilk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.nebi.servisilk.databinding.ActivityVeliAnaBinding
import com.nebi.servisilk.ui.home.HomeFragment

class VeliAnaActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityVeliAnaBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVeliAnaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setContentView(R.layout.activity_veli_ana)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.veli_ana)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.veli_ana_toolbar)
        drawerLayout = findViewById(R.id.veli_ana)
        navigationView = findViewById(R.id.veli_nav)

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

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.sofor_ara)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.kullanici_bilgileri -> replaceFragment(KullaniciBilgileriFragment())
            R.id.sofor_ara -> replaceFragment(ServisAraFragment())
            R.id.cikis -> {
                showLogoutDialog()  // Kullanıcı çıkış item'ına tıklayınca çalışan fonksiyondur
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
// AlertDialog'lar sayesinde kullanıcıya çıkış yapmak istiyor musunuz diye sorular sorduk ve evetse çıkıyor hayırsa sayfada kalıyor.
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Çıkış Yap")
        builder.setMessage("Çıkış yapmak istediğinize emin misiniz?")
        // Eğer kullanıcı evet butonuna basarsa dialog'un içerisindeki işlemler gerçekleşir yani önceki Activity'e gidilir.
        builder.setPositiveButton("Evet") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, VeliGirisActivity::class.java) // Giriş sayfanın adı
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
}