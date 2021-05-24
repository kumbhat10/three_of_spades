package com.kaalikiteeggi.three_of_spades

import kotlin.random.Random

class GameData() {
	var bb:Int? = null
	var bs:MutableList<Int>? = null
	var bv:Int? = null
	var bvo:Int? = null
	var ct:MutableList<Int>? = null
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
	var rn:Int = 0
	var rt:Int = 1
	var rtr:String = ""
	var sc:MutableList<Int>? = null
	var tr:String = ""
}

fun gameData4(dummy:Boolean = false, gameNumber:Int = 1):GameData {
	val gameData = GameData()
	val randPlayer = Random.nextInt(1, 5)
	val randPlayerNext = if(randPlayer==4) 1 else randPlayer+1
	gameData.bb = if(!dummy) randPlayer else 4
	gameData.bs = mutableListOf(1,1,1,1)
	gameData.pt = if(!dummy) randPlayerNext else 1
	gameData.bv = 175
	gameData.bvo = 175
	gameData.ct = mutableListOf(53, 53, 53, 53)
	gameData.gn = gameNumber
	gameData.pc1 = 53
	gameData.pc2 = 53
	gameData.sc = mutableListOf(0,0,0,0)
	return gameData
}

fun gameData7(dummy:Boolean = false, gameNumber:Int = 1):GameData {
	val gameData = GameData()
	val randPlayer = Random.nextInt(1, 8)
	val randPlayerNext = if(randPlayer==7) 1 else randPlayer+1
	gameData.bb = if(!dummy) randPlayer else 7
	gameData.bs = mutableListOf(1,1,1,1,1,1,1)
	gameData.pt = if(!dummy) randPlayerNext else 1
	gameData.bv = 350
	gameData.bvo = 350
	gameData.ct = mutableListOf(99,99,99,99,99,99,99)
	gameData.gn = gameNumber
	gameData.pc1 = 99
	gameData.pc2 = 99
	gameData.sc = mutableListOf(0,0,0,0,0,0,0)
	return gameData
}

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
