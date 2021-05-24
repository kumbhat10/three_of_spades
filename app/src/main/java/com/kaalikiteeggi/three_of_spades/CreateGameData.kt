package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
@SuppressLint("SimpleDateFormat")
class CreateGameData(uid: String, selfName: String) {
	private val cardsShuffled7 = (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards each player)
	private val cardsShuffled4 = (0..51).shuffled()  // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)
	private val randPlayerTurn7 = Random.nextInt(1, 8)
	private val randPlayerTurn4 = Random.nextInt(1, 5)

	val gameData7 = mutableMapOf("A_Date" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 7,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0, "p5" to 0, "p6" to 0, "p7" to 0),
		"CH" to mutableMapOf("p1" to cardsShuffled7.slice(0..13)
			.sortedBy { it }, "p2" to cardsShuffled7.slice(14..27)
			.sortedBy { it }, "p3" to cardsShuffled7.slice(28..41)
			.sortedBy { it }, "p4" to cardsShuffled7.slice(42..55)
			.sortedBy { it }, "p5" to cardsShuffled7.slice(56..69)
			.sortedBy { it }, "p6" to cardsShuffled7.slice(70..83)
			.sortedBy { it }, "p7" to cardsShuffled7.slice(84..97)
			.sortedBy { it }),
		"G" to gameData7()	)

	val gameDataDummy7 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 7,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0, "p5" to 0, "p6" to 0, "p7" to 0),
		"CH" to mutableMapOf("p1" to cardsShuffled7.slice(0..13)
			.sortedBy { it }, "p2" to cardsShuffled7.slice(14..27)
			.sortedBy { it }, "p3" to cardsShuffled7.slice(28..41)
			.sortedBy { it }, "p4" to cardsShuffled7.slice(42..55)
			.sortedBy { it }, "p5" to cardsShuffled7.slice(56..69)
			.sortedBy { it }, "p6" to cardsShuffled7.slice(70..83)
			.sortedBy { it }, "p7" to cardsShuffled7.slice(84..97)
			.sortedBy { it }),
		"G" to gameData7(dummy = true)	)

	val gameData4 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 4,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0),
		"CH" to mutableMapOf("p1" to cardsShuffled4.slice(0..12)
			.sortedBy { it }, "p2" to cardsShuffled4.slice(13..25)
			.sortedBy { it }, "p3" to cardsShuffled4.slice(26..38)
			.sortedBy { it }, "p4" to cardsShuffled4.slice(39..51)
			.sortedBy { it }), "CT" to mutableMapOf("p1" to 53, "p2" to 53, "p3" to 53, "p4" to 53), "GS" to 1,
		"G" to gameData4()	)

	val gameDataDummy4 = mutableMapOf("ADate" to SimpleDateFormat("yyyyMMdd").format(Date())
		.toInt(), "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "n" to 4,
		"OL" to mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0),
		"CH" to mutableMapOf("p1" to cardsShuffled4.slice(0..12)
			.sortedBy { it }, "p2" to cardsShuffled4.slice(13..25)
			.sortedBy { it }, "p3" to cardsShuffled4.slice(26..38)
			.sortedBy { it }, "p4" to cardsShuffled4.slice(39..51)
			.sortedBy { it }),
		"G" to gameData4(dummy = true)	)

}
