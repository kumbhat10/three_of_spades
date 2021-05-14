@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@SuppressLint("SimpleDateFormat")
class UserBasicInfo(val empty: Boolean = true, val index: Int = 0, val uid: String = "", val name: String = "", var score: Int = 0, val scoreDaily: Int = 0,
	val photoURL: String = "", val played: Int = 0, val playedDaily: Int = 0, val lastSeen: Int = SimpleDateFormat("yyyyMMdd").format(Date())
	.toInt(), val won: Int = 0, val bid: Int = 0, private val wonDaily: Int = 0, private val bidDaily: Int = 0, joinDate: Int=0, val appVersion:String = "--") {

	val userRank: Spanned? = Html.fromHtml("${index + 1}<sup>${rankExtFromInt(index + 1)}</sup>", HtmlCompat.FROM_HTML_MODE_LEGACY)
	val userScore: String = String.format("%,d", played) + " " + Emoji().gamePlayed + "\n" + String.format("%,d", won) + " " + Emoji().trophy + "\n" + String.format("%,d", bid) + " " + Emoji().score
	val userScoreDaily: String = String.format("%,d", playedDaily) + " " + Emoji().gamePlayed + "\n" + String.format("%,d", wonDaily) + " " + Emoji().trophy + "\n" + String.format("%,d", bidDaily) + " " + Emoji().score
	val userScoreFill: String = "Play " + Emoji().gamePlayed + "\n" + "Win " + Emoji().trophy + "\n" + "Bid " + Emoji().score

	private val lastSeenDate: String = SimpleDateFormat("d MMM-yy").format(SimpleDateFormat("yyyyMMdd").parse(lastSeen.toString()))
	private val joinDateString: String = if(joinDate ==0 ) "No record" else SimpleDateFormat("d MMM-yy").format(SimpleDateFormat("yyyyMMdd").parse(joinDate.toString()))

	val userInfo: String = "Last seen " + lastSeenDate + "\nJoined      " + joinDateString + "\n\n" + "Win rate " + (if (played > 0) round((100 * won / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().trophy + "\n" + "Bid rate " + (if (played > 0) round((100 * bid / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().score
	val newUser:Boolean = joinDate >= getChangedDate(CreateUser().todayDate)
	val userCoins: String = "$ " + String.format("%,d", score)
	val userCoinsDaily: String = "$ " + String.format("%,d", scoreDaily)
}

fun extractUserData(document: DocumentSnapshot, index:Int=0): UserBasicInfo {
	val uid = document.id
	val won = when {
		document.contains("w_bot") -> document["w_bot"].toString().toInt() + document["w"].toString().toInt()
		document.contains("w") -> document["w"].toString().toInt()
		else -> 0
	}
	val played = when {
		document.contains("p_bot") -> document["p_bot"].toString().toInt() + document["p"].toString().toInt()
		document.contains("p") -> document["p"].toString().toInt()
		else -> 0
	}
	val bid = when {
		document.contains("b_bot") -> document["b_bot"].toString().toInt() + document["b"].toString().toInt()
		document.contains("b") -> document["b"].toString().toInt()
		else -> 0
	}

	val appVersion = if(document.contains(("VC"))) document["VC"].toString() else "--"
	val name = if (document.contains("n")) document["n"].toString() else ""
	val score = if (document.contains("sc")) document["sc"].toString().toInt() else 0
	val scoreDaily = if (document.contains("scd")) document["scd"].toString().toInt() else 0
	val playedDaily = if (document.contains("p_daily")) document["p_daily"].toString().toInt() else 0
	val wonDaily = if (document.contains("w_daily")) document["w_daily"].toString().toInt() else 0
	val bidDaily = if (document.contains("b_daily")) document["b_daily"].toString().toInt() else 0
	val photoURL = if (document.contains("ph")) document["ph"].toString() else ""
	val lastSeen = if (document.contains("LSD")) document["LSD"].toString().toInt() else 0
	val joinDate = if (document.contains("JD")) document["JD"].toString().toInt() else 0

	return UserBasicInfo(empty = false,index= index,uid = uid, name = name, score = score, scoreDaily = scoreDaily, photoURL = photoURL,
		played = played, playedDaily = playedDaily, lastSeen = lastSeen, joinDate= joinDate, won = won,
		appVersion = appVersion, wonDaily = wonDaily, bid = bid, bidDaily = bidDaily)
}

fun createUserArrayFromSnapshot(querySnapshot: QuerySnapshot, filterLastSeen:Boolean = false, lsdLimit:Int = 0, startAt:Int = 0): ArrayList<UserBasicInfo> {
	val tempArray = ArrayList<UserBasicInfo>()
	var index = startAt + 1
	for (document in querySnapshot) {
		try {
			if (!(filterLastSeen && document["LSD"].toString().toInt() < lsdLimit)) {
				tempArray.add(extractUserData(document, index = index-1))
				index += 1
			}
		}catch (me:Exception) {		}
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



