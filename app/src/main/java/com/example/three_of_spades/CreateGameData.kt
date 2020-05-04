package com.example.three_of_spades

import kotlin.random.Random

class CreateGameData(hostName: String) {
    private val cardsShuffled =  (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
    private val playerTurn = Random.nextInt(2, 7)
// region real data
//    val gameData = mutableMapOf("CH" to mutableMapOf<String ,List<Int>>("p1" to cardsShuffled.slice(0..13).sortedBy {it},"p2" to cardsShuffled.slice(14..27).sortedBy {it}
//        ,"p3" to cardsShuffled.slice(28..41).sortedBy {it},"p4" to cardsShuffled.slice(42..55).sortedBy {it},"p5" to cardsShuffled.slice(56..69).sortedBy {it}
//        ,"p6" to cardsShuffled.slice(70..83).sortedBy {it},"p7" to cardsShuffled.slice(84..97).sortedBy {it}) ,
//        "CC" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""),
//        "CT" to mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99),
//        "GS" to 0, // 0 means joining state
//        "M" to "",
//        "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
//        "R" to 1,
//        "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
//        "SC" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""),
//        "OL" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1),
//        "Bid" to mutableMapOf("BV" to 350,
//            "BT" to playerTurn,"BB" to 1,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1)),
//        "Tr" to "") // 1 - spades,2-heart 3-diamonds or 4- clubs
//    val roomData = mutableMapOf("Players" to mutableMapOf<String ,String>("p1" to hostName,"p2" to "",
//    "p3" to "","p4" to "","p5" to "","p6" to "","p7" to "") ,
//        "PlayersJoined" to 1,
//        "Sex" to mutableMapOf("p1" to 0 ,"p2" to 0,"p3" to 1,"p4" to 0,"p5" to 1,"p6" to 0,"p7" to 1)) // 0 male 1 female

    // endregion
    //region Test data
    val gameData = mutableMapOf("CH" to mutableMapOf<String ,List<Int>>("p1" to cardsShuffled.slice(0..13).sortedBy {it},"p2" to cardsShuffled.slice(14..27).sortedBy {it}
        ,"p3" to cardsShuffled.slice(28..41).sortedBy {it},"p4" to cardsShuffled.slice(42..55).sortedBy {it},"p5" to cardsShuffled.slice(56..69).sortedBy {it}
        ,"p6" to cardsShuffled.slice(70..83).sortedBy {it},"p7" to cardsShuffled.slice(84..97).sortedBy {it}) ,
        "CC" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""),
//        "CT" to mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99),
        "CT" to mutableMapOf("p1" to 94,"p2" to 90,"p3" to 89,"p4" to 87,"p5" to 85,"p6" to 84,"p7" to 82),

        "GS" to 0, // 0 means joining state
        "M" to "",
        "BU" to mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""),
        "R" to 1,
        "RO" to mutableMapOf("T" to 1,"P" to 0,"R" to ""), // t game turn, p player turn, R is rung
        "SC" to mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0),
        "OL" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1),
        "Bid" to mutableMapOf("BV" to 350,
            "BT" to playerTurn,"BB" to 1,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1)),
        "Tr" to "") // 1 - spades,2-heart 3-diamonds or 4- clubs
    val roomData = mutableMapOf("Players" to mutableMapOf<String ,String>("p1" to hostName,"p2" to "Chandler","p3" to "Monica","p4" to "Ross","p5" to "Rachael","p6" to "Joe","p7" to "Phoebe") ,
        "PlayersJoined" to 7,
        "Sex" to mutableMapOf("p1" to 0 ,"p2" to 0,"p3" to 1,"p4" to 0,"p5" to 1,"p6" to 0,"p7" to 1)) // 0 male 1 female

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
