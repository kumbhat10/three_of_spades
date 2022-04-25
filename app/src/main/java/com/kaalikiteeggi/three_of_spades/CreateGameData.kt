package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class CreateGameData(uid: String, selfName: String) {
//	private val cardsShuffled7 = (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards each player)
//	private val cardsShuffled4 = (0..51).shuffled()  // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)

	val gameData7 = mutableMapOf("A_Date" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 7,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0, "p5" to 0, "p6" to 0, "p7" to 0),
		"G" to getGameData7()	)

	val gameDataDummy7 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 7,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0, "p5" to 0, "p6" to 0, "p7" to 0),
		"G" to getGameData7(dummy = true)	)

	val gameData4 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 4,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0),
		"G" to getGameData4()	)

	val gameDataDummy4 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 4,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0),
		"G" to getGameData4(dummy = true)	)
}
