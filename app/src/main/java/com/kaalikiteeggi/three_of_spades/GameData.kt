package com.kaalikiteeggi.three_of_spades

import kotlin.random.Random

class GameData() {
	var bb:Int? = null
	var bs:MutableList<Int>? = null
	var bv:Int? = null
	var bvo:Int? = null
	var ct:MutableList<Int>? = null
	var ch1:MutableList<Int>? = null
	var ch2:MutableList<Int>? = null
	var ch3:MutableList<Int>? = null
	var ch4:MutableList<Int>? = null
	var ch5:MutableList<Int>? = null
	var ch6:MutableList<Int>? = null
	var ch7:MutableList<Int>? = null
	var gs:Int = 1
	var gn:Int = 1
	var p1:Int = 8
	var p1s:Int = 0
	var p2:Int = 8
	var p2s:Int = 0
	var pc1:Int? = null
	var pc1s:Int = 13
	var pc2:Int? = null
	var pc2s:Int = 13
	var pt:Int? = null
	var rn:Int = 1
	var rt:Int = 1
	var rtr:String = ""
	var sc:MutableList<Int>? = null
	var s:MutableList<Int>? = null
	var tr:String = ""
}

fun getGameData4(dummy:Boolean = false, gameNumber:Int = 1):GameData {
	val gameData = GameData()
	val randPlayer = Random.nextInt(1, 5)
	val randPlayerNext = if(randPlayer==4) 1 else randPlayer+1
	val cardsShuffled = (0..51).shuffled()  // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)

	gameData.bb = if(!dummy) randPlayer else 4
	gameData.bs = mutableListOf(1,1,1,1)
	gameData.bv = 175
	gameData.bvo = 175
	gameData.ct = mutableListOf(53, 53, 53, 53)
	gameData.ch1 = cardsShuffled.slice(0..12).sortedBy { it } as MutableList<Int>
	gameData.ch2 = cardsShuffled.slice(13..25).sortedBy { it } as MutableList<Int>
	gameData.ch3 = cardsShuffled.slice(26..38).sortedBy { it } as MutableList<Int>
	gameData.ch4 = cardsShuffled.slice(39..51).sortedBy { it } as MutableList<Int>
	gameData.gn = gameNumber
	gameData.pc1 = 53
	gameData.pc2 = 53
	gameData.pt = if(!dummy) randPlayerNext else 1
	gameData.sc = mutableListOf(0,0,0,0)
	gameData.s = mutableListOf(0,0,0,0,0)
	return gameData
}

fun getGameData7(dummy:Boolean = false, gameNumber:Int = 1):GameData {
	val gameData = GameData()
	val randPlayer = Random.nextInt(1, 8)
	val randPlayerNext = if(randPlayer==7) 1 else randPlayer+1
	val cardsShuffled = (0..97).shuffled() // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)


	gameData.bb = if(!dummy) randPlayer else 7
	gameData.bs = mutableListOf(1,1,1,1,1,1,1)
	gameData.bv = 350
	gameData.bvo = 350
	gameData.ct = mutableListOf(99,99,99,99,99,99,99)
	gameData.ch1 = cardsShuffled.slice( 0..13).sortedBy { it } as MutableList<Int>
	gameData.ch2 = cardsShuffled.slice(14..27).sortedBy { it } as MutableList<Int>
	gameData.ch3 = cardsShuffled.slice(28..41).sortedBy { it } as MutableList<Int>
	gameData.ch4 = cardsShuffled.slice(42..55).sortedBy { it } as MutableList<Int>
	gameData.ch5 = cardsShuffled.slice(56..69).sortedBy { it } as MutableList<Int>
	gameData.ch6 = cardsShuffled.slice(70..83).sortedBy { it } as MutableList<Int>
	gameData.ch7 = cardsShuffled.slice(84..97).sortedBy { it } as MutableList<Int>
	gameData.gn = gameNumber
	gameData.pc1 = 99
	gameData.pc2 = 99
	gameData.pt = if(!dummy) randPlayerNext else 1
	gameData.sc = mutableListOf(0,0,0,0,0,0,0)
	gameData.s = mutableListOf(0,0,0,0,0,0,0,0)
	return gameData
}

//ch cards in hand for individual player
// ct cards on table
// gs game state
// pj = players joined
// bs bid status
// bv bid value
// bb bider
// bt bider turn
// ro round
// ro/r rung
// r round turn (1-14)
