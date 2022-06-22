package com.kaalikiteeggi.three_of_spades

// Two's from diamond, heart and club is not included in the data class
// R.drawable.c2 ,R.drawable.h2,R.drawable.d2
// order - spades, hearts, clubs, diamonds
class PlayingCards {

	//    region Suits Drawable
	val suitsDrawable = listOf(R.drawable.ic_spades, R.drawable.ic_hearts, R.drawable.ic_clubs, R.drawable.ic_diamonds)

	//    endregion
	//     region Cards Drawable
	val cardsDrawablePartner7 = listOf(
        R.drawable.ns2, R.drawable.ns3, R.drawable.ns4, R.drawable.ns5,
        R.drawable.ns6, R.drawable.ns7, R.drawable.ns8, R.drawable.ns9,
        R.drawable.ns10, R.drawable.ns11, R.drawable.ns12, R.drawable.ns13,
        R.drawable.ns14, R.drawable.nh3, R.drawable.nh4, R.drawable.nh5,
        R.drawable.nh6, R.drawable.nh7, R.drawable.nh8, R.drawable.nh9,
        R.drawable.nh10, R.drawable.nh11, R.drawable.nh12, R.drawable.nh13,
        R.drawable.nh14, R.drawable.nc3, R.drawable.nc4, R.drawable.nc5,
        R.drawable.nc6, R.drawable.nc7, R.drawable.nc8, R.drawable.nc9,
        R.drawable.nc10, R.drawable.nc11, R.drawable.nc12, R.drawable.nc13,
        R.drawable.nc14, R.drawable.nd3, R.drawable.nd4, R.drawable.nd5,
        R.drawable.nd6, R.drawable.nd7, R.drawable.nd8, R.drawable.nd9,
        R.drawable.nd10, R.drawable.nd11, R.drawable.nd12, R.drawable.nd13,
        R.drawable.nd14)

	val cardsDrawable4 = listOf(
		R.drawable.ns2, R.drawable.ns3, R.drawable.ns4, R.drawable.ns5,
		R.drawable.ns6, R.drawable.ns7, R.drawable.ns8, R.drawable.ns9,
		R.drawable.ns10, R.drawable.ns11, R.drawable.ns12, R.drawable.ns13,
		R.drawable.ns14, R.drawable.nh2, R.drawable.nh3, R.drawable.nh4, R.drawable.nh5, R.drawable.nh6,
		R.drawable.nh7, R.drawable.nh8, R.drawable.nh9, R.drawable.nh10, R.drawable.nh11,
		R.drawable.nh12, R.drawable.nh13, R.drawable.nh14, R.drawable.nc2, R.drawable.nc3,
		R.drawable.nc4, R.drawable.nc5, R.drawable.nc6, R.drawable.nc7, R.drawable.nc8, R.drawable.nc9,
		R.drawable.nc10, R.drawable.nc11, R.drawable.nc12, R.drawable.nc13, R.drawable.nc14, R.drawable.nd2,
		R.drawable.nd3, R.drawable.nd4, R.drawable.nd5, R.drawable.nd6, R.drawable.nd7, R.drawable.nd8, R.drawable.nd9,
		R.drawable.nd10, R.drawable.nd11, R.drawable.nd12, R.drawable.nd13, R.drawable.nd14)

	fun cardsDrawable7(): List<Int> {
		var cardsDrawableDoubleDeck = listOf<Int>()
		for (i in cardsDrawablePartner7) {
			cardsDrawableDoubleDeck = cardsDrawableDoubleDeck + i + i
		}; return cardsDrawableDoubleDeck
	}

	val cardsIndexSortedPartner7 = listOf(1, 12, 24, 36, 48, 11, 23, 35, 47, 10, 22, 34, 46, 9, 21, 33, 45, 8, 20, 32, 44, 7, 19, 31, 43, 6, 18, 30, 42, 5, 17, 29, 41, 4, 16, 28, 40, 3, 15, 27, 39, 2, 14, 26, 38, 13, 25, 37, 0)

	val cardsIndexSortedPartner4 = listOf(1, 12, 25, 38, 51, 11, 24, 37, 50, 10, 23, 36, 49, 9, 22, 35, 48, 8, 21, 34, 47, 7, 20, 33, 46, 6, 19, 32, 45, 5, 18, 31, 44, 4, 17, 30, 43, 3, 16, 29, 42, 2, 15, 28, 41, 1, 14, 27, 40, 0, 13, 26, 39)

	//    endregion
	//  region Card Points
	val cardsPointsPartner7 = listOf(
        0, 30, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20, // starts with 2 of Spade
            0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20, // starts with 3 of Heart
            0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20, // starts with 3 of Club
            0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20) // starts with 3 of Diamond

	val cardsPoints4 = listOf(
        0, 30, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20,
        0,  0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20,
        0,  0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20,
        0,  0, 0, 5, 0, 0, 0, 0, 10, 15, 15, 15, 20, 0, 0)  // add 2 extra zeros for card index 52 and 53


	fun cardsPoints7(): List<Int> {
		var cardPointsDoubleDeck = listOf<Int>()
		for (i in cardsPointsPartner7) {
			cardPointsDoubleDeck = cardPointsDoubleDeck + i + i
		}
		return cardPointsDoubleDeck + 0 + 0 // add 2 extra zeros for card index 98 and 99
	}

	// endregion Card Points
	//     region Card Suit
	private val cardSuit7 = listOf("S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D")

	val cardSuit4 = listOf("S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D", "D")

	fun cardsSuit7(): List<String> {
		var cardSuitDoubleDeck = listOf<String>()
		for (i in cardSuit7) {
			cardSuitDoubleDeck = cardSuitDoubleDeck + i + i
		}
		return cardSuitDoubleDeck
	}


	// endregion Card Suit


}