package com.kaalikiteeggi.three_of_spades

import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class CreateGameData(uid: String, selfName: String) {
    private val cardsShuffled7 =  (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
    private val cardsShuffled4 =  (0..51).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
    private val playerTurn7 = Random.nextInt(1, 7)
    private val playerTurn4 = Random.nextInt(1, 4)

//// region real data
    val gameData7 = mutableMapOf(
    "A_Date" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
    "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
    "A_Uid" to uid,
    "A_Name" to selfName,
    "Bid" to mutableMapOf("BV" to 350, "BT" to playerTurn7,"BB" to playerTurn7,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1)),
    "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
    "BU1" to mutableMapOf("b1" to 8,"s1" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
    "BU2" to mutableMapOf("b2" to 8,"s2" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
    "CH" to mutableMapOf("p1" to cardsShuffled7.slice(0..13).sortedBy {it},"p2" to cardsShuffled7.slice(14..27).sortedBy {it},
        "p3" to cardsShuffled7.slice(28..41).sortedBy {it},"p4" to cardsShuffled7.slice(42..55).sortedBy {it},"p5" to cardsShuffled7.slice(56..69).sortedBy {it},
        "p6" to cardsShuffled7.slice(70..83).sortedBy {it},"p7" to cardsShuffled7.slice(84..97).sortedBy {it}),
    "CT" to mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99),
    "GS" to 1, // 0 means joining state
    "M" to "",
    "OL" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0),
    "R" to 1,
    "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
    "SC" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0),
    "Tr" to "")

    val gameDataDummy7 = mutableMapOf(
        "ADate" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "Bid" to mutableMapOf("BV" to 350, "BT" to 1,"BB" to 3,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1)),
        "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
        "BU1" to mutableMapOf("b1" to 8,"s1" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
        "BU2" to mutableMapOf("b2" to 8,"s2" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
        "CH" to mutableMapOf<String ,List<Int>>("p1" to cardsShuffled7.slice(0..13).sortedBy {it},"p2" to cardsShuffled7.slice(14..27).sortedBy {it},
            "p3" to cardsShuffled7.slice(28..41).sortedBy {it},"p4" to cardsShuffled7.slice(42..55).sortedBy {it},"p5" to cardsShuffled7.slice(56..69).sortedBy {it},
            "p6" to cardsShuffled7.slice(70..83).sortedBy {it},"p7" to cardsShuffled7.slice(84..97).sortedBy {it}),
        "CT" to mutableMapOf("p1" to 83,"p2" to 73,"p3" to 31,"p4" to 45,"p5" to 67,"p6" to 78,"p7" to 89),
        "GS" to 1, // 0 means joining state
        "M" to "",
        "OL" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0),
        "R" to 1,
        "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
        "SC" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0),
        "Tr" to "")

    val gameData4 = mutableMapOf(
        "ADate" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "Bid" to mutableMapOf("BV" to 175, "BT" to playerTurn4,"BB" to playerTurn4,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1)),
    "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
    "BU1" to mutableMapOf("b1" to 8,"s1" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
    "CH" to mutableMapOf("p1" to cardsShuffled4.slice(0..12).sortedBy {it},"p2" to cardsShuffled4.slice(13..25).sortedBy {it},
        "p3" to cardsShuffled4.slice(26..38).sortedBy {it},"p4" to cardsShuffled4.slice(39..51).sortedBy {it}),
    "CT" to mutableMapOf("p1" to 53,"p2" to 53,"p3" to 53,"p4" to 53),
    "GS" to 1, // 0 means joining state
    "M" to "",
    "OL" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0),
    "R" to 1,
    "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
    "SC" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0),
    "Tr" to "")

    val gameDataDummy4 = mutableMapOf(
        "ADate" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
        "A_Time" to SimpleDateFormat("HH:mm:ss z").format(Date()),
        "A_Uid" to uid,
        "A_Name" to selfName,
        "Bid" to mutableMapOf("BV" to 175, "BT" to 1,"BB" to 3,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1)),
    "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
    "BU1" to mutableMapOf("b1" to 8,"s1" to 0),  // b1 = player number = 8 means not declared. b1s = 0 ND, 2 Not sure, 1 Locked
    "CH" to mutableMapOf("p1" to cardsShuffled4.slice(0..12).sortedBy {it},"p2" to cardsShuffled4.slice(13..25).sortedBy {it},
        "p3" to cardsShuffled4.slice(26..38).sortedBy {it},"p4" to cardsShuffled4.slice(39..51).sortedBy {it}),
//    "CT" to mutableMapOf("p1" to 23,"p2" to 45,"p3" to 51,"p4" to 36),
        "CT" to mutableMapOf("p1" to 53,"p2" to 53,"p3" to 53,"p4" to 53),
    "GS" to 1, // 0 means joining state
    "M" to "",
    "OL" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0),
    "R" to 1,
    "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
    "SC" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0),
    "Tr" to "")


////     endregion
    //region Test data

// endregion

 }

// cc cards collected
// ct cards on table
// gs game state
// pj = players joined
// bs bid stauts
//bv bid value
// bb bider
//bt bidertur
// ro round
//ro/r rung
// r roundturn (1-14)
