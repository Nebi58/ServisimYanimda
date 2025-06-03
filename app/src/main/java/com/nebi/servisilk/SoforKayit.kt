package com.nebi.servisilk

data class SoforKayit (
    var SoforId:String?=null,
    var SoforAd:String?=null,
    var SoforSoyad:String?=null,
    var SoforTC:String?=null,
    var SoforTelefon:String?=null,
    var SoforSifre:String?=null,
    var OgrenciVeli:List<VeliKayit>?=null  // Velilerle ilgili bilgiler geleceği için boş bir liste tanımladık
)