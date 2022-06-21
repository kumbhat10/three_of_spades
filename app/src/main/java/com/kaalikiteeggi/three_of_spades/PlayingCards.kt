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
        R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
        R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9,
        R.drawable.s10, R.drawable.s11, R.drawable.s12, R.drawable.s13,
        R.drawable.s14, R.drawable.h3, R.drawable.h4, R.drawable.h5,
        R.drawable.h6, R.drawable.h7, R.drawable.h8, R.drawable.h9,
        R.drawable.h10, R.drawable.h11, R.drawable.h12, R.drawable.h13,
        R.drawable.h14, R.drawable.c3, R.drawable.c4, R.drawable.c5,
        R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9,
        R.drawable.c10, R.drawable.c11, R.drawable.c12, R.drawable.c13,
        R.drawable.c14, R.drawable.d3, R.drawable.d4, R.drawable.d5,
        R.drawable.d6, R.drawable.d7, R.drawable.d8, R.drawable.d9,
        R.drawable.d10, R.drawable.d11, R.drawable.d12, R.drawable.d13,
        R.drawable.d14)

	val cardsDrawable4 = listOf(
		R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
		R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9,
		R.drawable.s10, R.drawable.s11, R.drawable.s12, R.drawable.s13,
		R.drawable.s14, R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6,
		R.drawable.h7, R.drawable.h8, R.drawable.h9, R.drawable.h10, R.drawable.h11,
		R.drawable.h12, R.drawable.h13, R.drawable.h14, R.drawable.c2, R.drawable.c3,
		R.drawable.c4, R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9,
		R.drawable.c10, R.drawable.c11, R.drawable.c12, R.drawable.c13, R.drawable.c14, R.drawable.d2,
		R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6, R.drawable.d7, R.drawable.d8, R.drawable.d9,
		R.drawable.d10, R.drawable.d11, R.drawable.d12, R.drawable.d13, R.drawable.d14)

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