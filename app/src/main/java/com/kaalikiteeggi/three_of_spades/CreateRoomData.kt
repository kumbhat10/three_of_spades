package com.kaalikiteeggi.three_of_spades

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.random.Random

class CreateRoomData(private val userBasicInfo:UserBasicInfo) {

    private val randomList =  (CelebrityData().name.indices).shuffled().shuffled().shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)

    private fun randPercentage(): Int {
        return 100 + Random.nextInt(-90,90) // change upto - from -60% to 60% of original score
    }
    private fun deriveValue(value:Int, randPercentage:Int): Int {
        return round((value/100*randPercentage).toDouble()).toInt()
    }

    private fun randomUser(index:Int=0):UserBasicInfo{
        val randPercentage = randPercentage()
        val rand = Random.nextInt(0, CelebrityData().name.size)
        return UserBasicInfo(empty = false, index= index, name = CelebrityData().name[randomList[rand]][0], score =  nearestTen(deriveValue(userBasicInfo.score, randPercentage)), photoURL = CelebrityData().name[randomList[rand]][1], played =  nearestTen(deriveValue(userBasicInfo.played, randPercentage)), won =  deriveValue(userBasicInfo.won, randPercentage), bid =  deriveValue(userBasicInfo.bid, randPercentage))
    }
    val offlineData = arrayListOf<UserBasicInfo>(userBasicInfo, randomUser(index = 1), randomUser(index = 2), randomUser(index = 3))

    val dummyData4 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to userBasicInfo.name.split(" ")[0],
        "p1h" to userBasicInfo.uid,
        "p2" to "Teena",
        "p2h" to "8nAir6bIi5ToGocuyF24ptUIlOz2",
        "p3" to "Tamanna",
        "p3h" to "8WUKdjBK8kTFI7HGYO92mjPuOG43",
        "p4" to "Prinsi",
        "p4h" to "Mpa3032PKXUOXZiIzS6l7Pw7jM02",
        "PJ" to 4,
        "n" to 4)

    val data4 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to userBasicInfo.name.split(" ")[0],
        "p1h" to userBasicInfo.uid,
        "p2" to "",
        "p2h" to "",
        "p3" to "",
        "p3h" to "",
        "p4" to "",
        "p4h" to "",
        "PJ" to 1,
        "n" to 4)
    val data7 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to userBasicInfo.name.split(" ")[0],
        "p1h" to userBasicInfo.uid,
        "p2" to "",
        "p2h" to "",
        "p3" to "",
        "p3h" to "",
        "p4" to "",
        "p4h" to "",
        "p5" to "",
        "p5h" to "",
        "p6" to "",
        "p6h" to "",
        "p7" to "",
        "p7h" to "",
        "PJ" to 1,
        "n" to 7)
    val dummyData7 = hashMapOf(
        "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "p1" to userBasicInfo.name.split(" ")[0],
        "p1h" to userBasicInfo.uid,
        "p2" to "Teena",
        "p2h" to "8nAir6bIi5ToGocuyF24ptUIlOz2",
        "p3" to "Tamanna",
        "p3h" to "8WUKdjBK8kTFI7HGYO92mjPuOG43",
        "p4" to "Prinsi",
        "p4h" to "Mpa3032PKXUOXZiIzS6l7Pw7jM02",
        "p5" to "Teena",
        "p5h" to "8nAir6bIi5ToGocuyF24ptUIlOz2",
        "p6" to "Tamanna",
        "p6h" to "8WUKdjBK8kTFI7HGYO92mjPuOG43",
        "p7" to "Prinsi",
        "p7h" to "Mpa3032PKXUOXZiIzS6l7Pw7jM02",
        "PJ" to 7,
        "n" to 7)

}
fun nearestTen(value:Any):Int{
    return (round(value.toString().toDouble()/10)*10).toInt()
}