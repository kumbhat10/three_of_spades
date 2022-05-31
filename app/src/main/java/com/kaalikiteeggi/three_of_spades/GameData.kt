package com.kaalikiteeggi.three_of_spades

import kotlin.random.Random

class GameData(nPlayers: Int = 4) {
	private var a = 4

	var bb: Int = 0 // current bidder
	var bs: MutableList<Int> = mutableListOf(1,1,1,1) // bid status
	var bv: Int = 175 // current bid value
	var bvo: Int = 175 // previous(old) bid value
	var ct: MutableList<Int> = mutableListOf(53, 53, 53, 53) // list of cards on table
	var ct1: MutableList<Int> = mutableListOf(53, 53, 53, 53) // list of cards on table
	var ch1: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch2: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch3: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch4: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch5: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch6: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var ch7: MutableList<Int> = mutableListOf(99) //Cards in hand for player 1
	var gs: Int = 1 // Game state - 1 to 6 -> 1 - shuffling, resetting and biding, 3-Trump select, 4-partner selection, 5- play rounds, 6- GameOver, results declaration back to 1
	var gn: Int = 1 // Current Game number
	var p1: Int = 8 // Partner 1 (player number)
	var p1s: Int = 0 // Partner 1 status -> 0:Not found, 2:Found but not confirmed, 1:found & confirmed
	var p2: Int = 8 // Partner 2 (player number)
	var p2s: Int = 0 // Partner 2 status -> 0:Not found, 2:Found but not confirmed, 1:found & confirmed
	var pc1: Int = 53  //partner card -1
	var pc1s: Int = 13   //partner card 1 status - 10:Any, 11:Only, 12:Both
	var pc2: Int = 53 //partner card - 2
	var pc2s: Int = 13  //partner card 2 status - 10:Any, 11:Only, 12:Both
	var pt: Int = 0 // Player Turn
	var rn: Int = 1     // Round number -> 1 to 13/14 (4 players/7 Players)
	var rt: Int = 1     // Round turn number -> 1 to 5/8 (4 players/7 Players) with +1 for declaring round winner
	var rtr: String = ""  // Round Trump - First card Suite
	var sc: MutableList<Int> = mutableListOf(0,0,0,0)  // local reward score list for that game
	var s: MutableList<Int> = mutableListOf(0,0,0,0,0) // Individual final score list for current game after win/defeat
	var tr: String = ""  // Master Trump for whole game
}

fun getGameData4(dummy:Boolean = false, lastGameBidder:Int = Random.nextInt(1, 5), gameNumber:Int = 1, s: MutableList<Int> = mutableListOf(0,0,0,0,0)): GameData {
	val gameData = GameData()
//	val randPlayer = Random.nextInt(1, 5)
	val randPlayerNext = if(lastGameBidder==4) 1 else lastGameBidder+1
	val cardsShuffled = (0..51).shuffled()  // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)

	gameData.bb = if(!dummy) lastGameBidder else 4
	gameData.ch1 = cardsShuffled.slice(0..12).sortedBy { it } as MutableList<Int>
	gameData.ch2 = cardsShuffled.slice(13..25).sortedBy { it } as MutableList<Int>
	gameData.ch3 = cardsShuffled.slice(26..38).sortedBy { it } as MutableList<Int>
	gameData.ch4 = cardsShuffled.slice(39..51).sortedBy { it } as MutableList<Int>
	gameData.gn = gameNumber
	gameData.pt = if(!dummy) randPlayerNext else 1
	gameData.s = s
	return gameData
}

fun getGameData7(dummy:Boolean = false, lastGameBidder:Int = Random.nextInt(1, 8), gameNumber:Int = 1, s: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0)):GameData {
	val gameData = GameData()
	val randPlayerNext = if(lastGameBidder==7) 1 else lastGameBidder+1
	val cardsShuffled = (0..97).shuffled() // create shuffled pack of 1 deck with no cards removed ( 4Player x 13 = 52 cards each player)

	gameData.bb = if(!dummy) lastGameBidder else 7
	gameData.bs = mutableListOf(1,1,1,1,1,1,1)
	gameData.bv = 350
	gameData.bvo = 350
	gameData.ct = mutableListOf(99,99,99,99,99,99,99)
	gameData.ct1 = mutableListOf(99,99,99,99,99,99,99)
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
	gameData.s = s
	return gameData
}

//ch cards in hand for individual player
// ct cards on table
// gs game state
// pj = players joined
// bs bid status
// bv bid value
// bb bidder
// bt bidder turn
// ro round
// ro/r rung
// r round turn (1-14)
