package com.nebi.servisilk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServisAraFragment : Fragment() {

    private lateinit var dbSoforRef: DatabaseReference
    private lateinit var servisAraText: EditText
    private lateinit var servisAraButon: Button
    private lateinit var servisText: TextView
    private lateinit var katilButon:Button
    private lateinit var kullaniciBilgiCard: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_servis_ara, container, false)

        dbSoforRef = FirebaseDatabase.getInstance().getReference("Soforler")

        // View'leri bağlama
        servisAraText = view.findViewById(R.id.ServisAraText)
        servisAraButon = view.findViewById(R.id.ServisAraButton)
        servisText = view.findViewById(R.id.ServisText)
        kullaniciBilgiCard = view.findViewById(R.id.KulaniciBilgiCard)
        katilButon=view.findViewById(R.id.KatilButon)

        // Arama butonuna tıklama işlemi
        servisAraButon.setOnClickListener {
            val soforId = servisAraText.text.toString().trim()
            if (soforId.isNotEmpty()) {
                soforBilgileriniGetir(soforId)
            } else {
                Toast.makeText(requireContext(), "Lütfen şoför ID'sini giriniz.", Toast.LENGTH_SHORT).show()
            }
        }

        // Veli şoföre kayıt olurken yapılan işlemdir
        katilButon.setOnClickListener {
            // Veli Id'si geldi mi kontrol eder ve logcat'e yazdırır
            val veliId = arguments?.getString("veliId")
                ?: activity?.intent?.getStringExtra("veliId")

            Log.d("KullaniciBilgileriFragment", "Veri Aktarılıyor: veliId = $veliId")

            if (veliId != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("Veliler").child(veliId)
                dbRef.get().addOnSuccessListener { dataSnapshot ->
                    val veliKayit = dataSnapshot.getValue(VeliKayit::class.java)
                    if (veliKayit != null) {
                        veliEkleSofore(veliKayit)

                    }
                }.addOnFailureListener {
                    Log.d("ServisAraFragment","Velinin bilgilerine ulaşılamadı")
                    Toast.makeText(requireContext(), "Bilgiler alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }



        return view
    }

    private fun soforBilgileriniGetir(soforId: String) {
        dbSoforRef.child(soforId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val ad = snapshot.child("soforAd").value.toString()
                val soyad = snapshot.child("soforSoyad").value.toString()
                val telno = snapshot.child("soforTelefon").value.toString()

                // Şoför bilgilerini ekranda göster
                servisText.text = " Ad: $ad $soyad \n Tel No: $telno"

                // CardView'i görünür yap
                kullaniciBilgiCard.visibility = View.VISIBLE
            } else {
                servisText.text = "Şoför bulunamadı."
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun veliEkleSofore(veli: VeliKayit) {
        val soforId = servisAraText.text.toString()  // Şoförün ID'sini burada dinamik olarak alabilirsin

        val soforRef = FirebaseDatabase.getInstance().getReference("Soforler").child(soforId).child("OgrenciVeli")

        // OgrenciVeli listesine yeni veli eklemeden önce mevcut listeyi alalım
        soforRef.get().addOnSuccessListener { snapshot ->
            val ogrenciVeliList = snapshot.children.mapNotNull { it.getValue(VeliKayit::class.java) }.toMutableList()

            // Yeni veliyi listeye ekleyelim
            ogrenciVeliList.add(veli)

            // Güncellenmiş listeyi setValue ile Firebase'e yazalım
            soforRef.setValue(ogrenciVeliList)
                .addOnSuccessListener {
                    Toast.makeText(context, "Veli başarıyla eklendi.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Veli eklenirken hata oluştu.", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(context, "Liste alınırken hata oluştu.", Toast.LENGTH_SHORT).show()
        }
    }


}
