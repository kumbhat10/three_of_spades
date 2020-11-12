@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@SuppressLint("SimpleDateFormat")
class UserBasicInfo(val name: String, val score: Int,val scoreDaily: Int,
	val photoURL: String, val played: Int, val playedDaily: Int,
	val lastSeen: Int,
	private val won: Int, private val bid: Int,
	private val wonDaily: Int, private val bidDaily: Int) {

	val userScore: String =
		String.format("%,d", played) + " " + Emoji().gamePlayed + "\n" +
			String.format("%,d", won) + " " + Emoji().trophy + "\n" +
			String.format("%,d", bid) + " " + Emoji().score

	val userScoreDaily: String =
		String.format("%,d", playedDaily) + " " + Emoji().gamePlayed + "\n" +
			String.format("%,d", wonDaily) + " " + Emoji().trophy + "\n" +
			String.format("%,d", bidDaily) + " " + Emoji().score

	val userScoreFill: String =
		"Play " + Emoji().gamePlayed + "\n" +
				"Win " + Emoji().trophy + "\n" +
				"Bid "  + Emoji().score

	val lastSeenDate: String = SimpleDateFormat("d-MMM").format(SimpleDateFormat("yyyyMMdd").parse(lastSeen.toString()))

	val userInfo: String =
		"Last seen "  + lastSeenDate + "\n\n" +
		"Win rate " + (if(played>0) round((100 * won / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().trophy + "\n" +
		"Bid rate " + (if(played>0) round((100 * bid / played).toDouble()).toInt() else 0).toString() + "% " + Emoji().score


	val userCoins: String = "$ " + String.format("%,d", score)
	val userCoinsDaily: String = "$ " + String.format("%,d", scoreDaily)

}

fun rankExtFromInt(i: Int): String {
	val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
	return when (i % 100) {
		11, 12, 13 -> "th"
		else -> suffixes[i % 10]
	}

}



