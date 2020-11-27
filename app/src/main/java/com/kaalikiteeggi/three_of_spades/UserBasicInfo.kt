@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@SuppressLint("SimpleDateFormat")
class UserBasicInfo(val empty: Boolean = true, val index: Int = 0, val uid: String = "", val name: String = "", var score: Int = 0, val scoreDaily: Int = 0,
	val photoURL: String = "", val played: Int = 0, val playedDaily: Int = 0, val lastSeen: Int = SimpleDateFormat("yyyyMMdd").format(Date())
	.toInt(), val won: Int = 0, val bid: Int = 0, private val wonDaily: Int = 0, private val bidDaily: Int = 0, joinDate: Int=0) {

	val userScore: String = String.format("%,d", played) + " " + Emoji().gamePlayed + "\n" + String.format("%,d", won) + " " + Emoji().trophy + "\n" + String.format("%,d", bid) + " " + Emoji().score
	val userScoreDaily: String = String.format("%,d", playedDaily) + " " + Emoji().gamePlayed + "\n" + String.format("%,d", wonDaily) + " " + Emoji().trophy + "\n" + String.format("%,d", bidDaily) + " " + Emoji().score
	val userScoreFill: String = "Play " + Emoji().gamePlayed + "\n" + "Win " + Emoji().trophy + "\n" + "Bid " + Emoji().score

	val lastSeenDate: String = SimpleDateFormat("d-MMM").format(SimpleDateFormat("yyyyMMdd").parse(lastSeen.toString()))

	val userInfo: String = "Last seen " + lastSeenDate + "\n\n" + "Win rate " + (if (played > 0) round((100 * won / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().trophy + "\n" + "Bid rate " + (if (played > 0) round((100 * bid / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().score
	val newUser:Boolean = joinDate >= getChangedDate(CreateUser().todayDate)
	val userCoins: String = "$ " + String.format("%,d", score)
	val userCoinsDaily: String = "$ " + String.format("%,d", scoreDaily)

}

fun extractUserData(document: DocumentSnapshot, index:Int=0): UserBasicInfo {
	val uid = document.id
	val won = if (document.contains("w_bot")) document["w_bot"].toString()
		.toInt() + document["w"].toString().toInt()
	else document["w"].toString().toInt()
	val played = if (document.contains("p_bot")) document["p_bot"].toString()
		.toInt() + document["p"].toString().toInt()
	else document["p"].toString().toInt()
	val bid = if (document.contains("b_bot")) document["b_bot"].toString()
		.toInt() + document["b"].toString().toInt()
	else document["b"].toString().toInt()

	val name = document["n"].toString()
	val score = document["sc"].toString().toInt()
	val scoreDaily = if (document.contains("scd")) document["scd"].toString().toInt() else 0
	val playedDaily = if (document.contains("p_daily")) document["p_daily"].toString()
		.toInt() else 0
	val wonDaily = if (document.contains("w_daily")) document["w_daily"].toString().toInt() else 0
	val bidDaily = if (document.contains("b_daily")) document["b_daily"].toString().toInt() else 0
	val photoURL = document["ph"].toString()
	val lastSeen = document["LSD"].toString().toInt()
	val joinDate = if (document.contains("JD")) document["JD"].toString().toInt() else 0
	return UserBasicInfo(empty = false,index= index,uid = uid, name = name, score = score, scoreDaily = scoreDaily, photoURL = photoURL, played = played, playedDaily = playedDaily, lastSeen = lastSeen, joinDate= joinDate, won = won, wonDaily = wonDaily, bid = bid, bidDaily = bidDaily)
}

fun createUserArrayFromSnapshot(querySnapshot: QuerySnapshot, filterLastSeen:Boolean = false, lsdLimit:Int = 0): ArrayList<UserBasicInfo> {
	val tempArray = ArrayList<UserBasicInfo>()
	for (document in querySnapshot) {
		if(!(filterLastSeen && document["LSD"].toString().toInt() < lsdLimit)) tempArray.add(extractUserData(document))
	}
	return tempArray
}

fun rankExtFromInt(i: Int): String {
	val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
	return when (i % 100) {
		11, 12, 13 -> "th"
		else -> suffixes[i % 10]
	}

}



