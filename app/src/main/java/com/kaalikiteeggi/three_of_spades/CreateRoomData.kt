package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class CreateRoomData(username: String, userPhotoUrl: String) {
    @SuppressLint("SimpleDateFormat")
    val dummyData = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p2" to "Kriti",
        "p2h" to "https://i.pinimg.com/564x/a0/31/cf/a031cfdf6498a40a345fce8320a563dc.jpg",
        "p3" to "Kabir",
        "p3h" to "https://i.pinimg.com/564x/8f/03/b8/8f03b858c3bfcfa8a5027d52d5a23f78.jpg",
        "p5" to "Priyanka",
        "p5h" to "https://i.pinimg.com/564x/41/81/04/418104b86d8f53976b224131d0eb1801.jpg\n",
        "p4" to "Tiger",
        "p4h" to "https://static.toiimg.com/photo/70973522.cms",
        "p6" to "Jacqueline",
        "p6h" to "https://www.ecopetit.cat/wpic/mpic/231-2317649_jacqueline-fernandez-jacqueline-fernandez-photos-hd.jpg",
        "p7" to "Hrithik",
        "p7h" to "https://imagevars.gulfnews.com/2020/01/22/Hrithik-Roshan--3--1579703264814_16fcda6e62f_large.jpg",
        "PJ" to 7 )
    val data = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p2" to "",
        "p2h" to "",
        "p3" to "",
        "p3h" to "",
        "p5" to "",
        "p5h" to "",
        "p4" to "",
        "p4h" to "",
        "p6" to "",
        "p6h" to "",
        "p7" to "",
        "p7h" to "",
        "PJ" to 1 )

}
