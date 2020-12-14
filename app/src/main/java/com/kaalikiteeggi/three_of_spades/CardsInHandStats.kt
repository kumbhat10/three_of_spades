package com.kaalikiteeggi.three_of_spades

import kotlin.math.round

class CardsInHandStats (var cards:MutableList<Int>, var trump:String="S"){

	private val factCard = 8 // value of single card
	private val trumpList = listOf("s", "h", "c", "d")
	private val trumpListCap = listOf("S", "H", "C","D" )
	var totalPoints = pointsInHand(cards)
	var nEachSuits = suitsSummary(cards)[0]
	var pEachSuits = suitsSummary(cards)[1]
	var vEachSuits = suitsSummary(cards)[2]
	private val trumpChoosenIndex = vEachSuits.indexOf(vEachSuits.maxOrNull())   // index of return the first max element found - better for spades suit if value score is same
	val trumpChoosen = trumpList[trumpChoosenIndex]   // index of return the first max element found - better for spades suit if value score is same
	val otherSuitPoints = pEachSuits.filterIndexed { index, _ -> index != trumpChoosenIndex  }.sum()
	val bidChoosen = vEachSuits.maxOrNull()?.plus(otherSuitPoints)?.times(1.75)?.let { round(it) }?.let { nearestTen(it) }

	var playRandomCard = cards.random()
	var cardSelectedIndex = PlayingCards().cardSuit4.slice(cards as Iterable<Int>).lastIndexOf(trump)


	private fun suitsSummary(cards:MutableList<Int>): List<List<Int>> {
		var nSpade = 0;		var nHeart = 0;		var nClub = 0;		var nDiamond = 0
		var nSpade0 = 0;	var nHeart0 = 0;	var nClub0 = 0;		var nDiamond0 = 0
		var pSpade = 0;		var pHeart = 0;		var pClub = 0;		var pDiamond = 0
		var vSpade = 0;		var vHeart = 0;		var vClub = 0;		var vDiamond = 0
		for(iCard in cards){
			when(PlayingCards().cardSuit4[iCard]){
				"S" -> {
					nSpade+= 1
					pSpade+= PlayingCards().cardsPoints4[iCard]
					nSpade0 += if(PlayingCards().cardsPoints4[iCard]==0) 1 else 0  // increment for only - zero value cards
				}
				"H" -> {
					nHeart+=1
					pHeart+= PlayingCards().cardsPoints4[iCard]
					nHeart0 += if(PlayingCards().cardsPoints4[iCard]==0) 1 else 0  // increment for only - zero value cards
				}
				"C" -> {
					nClub+=1
					pClub+= PlayingCards().cardsPoints4[iCard]
					nClub0 += if(PlayingCards().cardsPoints4[iCard]==0) 1 else 0  // increment for only - zero value cards
				}
				"D" -> {
					nDiamond+=1
					pDiamond+= PlayingCards().cardsPoints4[iCard]
					nDiamond0 += if(PlayingCards().cardsPoints4[iCard]==0) 1 else 0  // increment for only - zero value cards
				}
			}
			vSpade = nSpade0*factCard + pSpade
			vHeart = nHeart0*factCard + pHeart
			vClub = nClub0*factCard + pClub
			vDiamond = nDiamond0*factCard + pDiamond
		}
		return listOf(listOf(nSpade, nHeart, nClub, nDiamond), listOf(pSpade, pHeart, pClub, pDiamond), listOf(vSpade, vHeart, vClub, vDiamond))
	}
}

fun pointsInHand(cards:MutableList<Int>): Int{
	var points = 0
	for(iCard in cards){
		points += PlayingCards().cardsPoints4[iCard]
	}
	return points
}