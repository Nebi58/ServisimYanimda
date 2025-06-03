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
import com.nebi.servisilk.databinding.FragmentSoforBilgileriBinding

class SoforBilgileriFragment : Fragment() {

    private var _binding: FragmentSoforBilgileriBinding? = null
    private val binding get() = _binding!!
    private var soforKayit: SoforKayit? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoforBilgileriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argument veya Intent ile veri alma
        val soforId = arguments?.getString("soforId")
            ?: activity?.intent?.getStringExtra("soforId")

        Log.d("SoforBilgileriFragment", "Veri Aktarılıyor: soforId = $soforId")

        if (soforId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Soforler").child(soforId)
            dbRef.get().addOnSuccessListener { dataSnapshot ->
                val soforKayit = dataSnapshot.getValue(SoforKayit::class.java)
                if (soforKayit != null) {
                    binding.soforAdBilgi.text = soforKayit.SoforAd
                    binding.soforSoyadBilgi.text = soforKayit.SoforSoyad
                    binding.sofortcKimlikBilgi.text = soforKayit.SoforTC
                    binding.sofortelefonNoBilgi.text = soforKayit.SoforTelefon
                    binding.soforsifreBilgi.text = soforKayit.SoforSifre
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Bilgiler alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editButonSofor.setOnClickListener {
            val intent = Intent(requireContext(), SoforGuncelleActivity::class.java)
            intent.putExtra("soforId",arguments?.getString("soforId")?:activity?.intent?.getStringExtra("soforId"))
            startActivity(intent)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

