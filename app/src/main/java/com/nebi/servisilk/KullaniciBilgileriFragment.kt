package com.nebi.servisilk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.nebi.servisilk.databinding.FragmentKullaniciBilgileriBinding

class KullaniciBilgileriFragment : Fragment() {

    private var _binding: FragmentKullaniciBilgileriBinding? = null
    private val binding get() = _binding!!
    private var veliKayit: VeliKayit? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKullaniciBilgileriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argument veya Intent ile veri alma
        val veliId = arguments?.getString("veliId")
            ?: activity?.intent?.getStringExtra("veliId")

        Log.d("KullaniciBilgileriFragment", "Veri Aktarılıyor: veliId = $veliId")

        if (veliId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Veliler").child(veliId)
            dbRef.get().addOnSuccessListener { dataSnapshot ->
                val veliKayit = dataSnapshot.getValue(VeliKayit::class.java)
                if (veliKayit != null) {
                    binding.veliAdBilgi.text = veliKayit.VeliAd
                    binding.veliSoyadBilgi.text = veliKayit.VeliSoyad
                    binding.velitcKimlikBilgi.text = veliKayit.VeliTC
                    binding.telefonNoBilgi.text = veliKayit.VeliTelefon
                    binding.ogrenciAdBilgi.text = veliKayit.OgrenciAd
                    binding.ogrenciSoyadBilgi.text = veliKayit.OgrenciSoyad
                    binding.sifreBilgi.text = veliKayit.Sifre
                    binding.adresBilgi.text = veliKayit.Adres
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Bilgiler alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editButon.setOnClickListener {
            val intent = Intent(requireContext(), VeliGuncelleActivity::class.java)
            intent.putExtra("veliId",arguments?.getString("veliId")?:activity?.intent?.getStringExtra("veliId"))
            startActivity(intent)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

