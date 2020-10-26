package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class CreateRoomData(username: String, userPhotoUrl: String, coins: Int) {

    private val randomList =  (CelebrityData().name.indices).shuffled().shuffled().shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)

    @SuppressLint("SimpleDateFormat")
    val dummyData7 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p1c" to coins,
        "p2" to CelebrityData().name[randomList[0]][0],
        "p2h" to CelebrityData().name[randomList[0]][1],
        "p3" to CelebrityData().name[randomList[3]][0],
        "p3h" to CelebrityData().name[randomList[3]][1],
        "p4" to CelebrityData().name[randomList[5]][0],
        "p4h" to CelebrityData().name[randomList[5]][1],
        "p5" to CelebrityData().name[randomList[7]][0],
        "p5h" to CelebrityData().name[randomList[7]][1],
        "p6" to CelebrityData().name[randomList[9]][0],
        "p6h" to CelebrityData().name[randomList[9]][1],
        "p7" to CelebrityData().name[randomList[2]][0],
        "p7h" to CelebrityData().name[randomList[2]][1],
        "PJ" to 7,
        "n" to 7,
        "p2c" to 6200,
        "p3c" to 7800,
        "p4c" to 8500,
        "p5c" to 5000,
        "p6c" to 9000,
        "p7c" to 6500)

    val dummyData4 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p1c" to coins,
        "p2" to CelebrityData().name[randomList[0]][0],
        "p2h" to CelebrityData().name[randomList[0]][1],
        "p3" to CelebrityData().name[randomList[3]][0],
        "p3h" to CelebrityData().name[randomList[3]][1],
        "p4" to CelebrityData().name[randomList[5]][0],
        "p4h" to CelebrityData().name[randomList[5]][1],
        "PJ" to 4,
        "n" to 4,
        "p2c" to 28500,
        "p3c" to 34800,
        "p4c" to 19200)

    val data7 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p1c" to coins,
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
        "PJ" to 1,
        "n" to 7,
        "p2c" to 0,
        "p3c" to 0,
        "p4c" to 0,
        "p5c" to 0,
        "p6c" to 0,
        "p7c" to 0)

    @SuppressLint("SimpleDateFormat")
    val data4 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to username,
        "p1h" to userPhotoUrl,
        "p1c" to coins,
        "p2" to "",
        "p2h" to "",
        "p3" to "",
        "p3h" to "",
        "p4" to "",
        "p4h" to "",
        "PJ" to 1,
        "n" to 4,
        "p2c" to 0,
        "p3c" to 0,
        "p4c" to 0)

}
