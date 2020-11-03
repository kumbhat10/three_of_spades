package com.kaalikiteeggi.three_of_spades

class PlayersReference {

      private val refIDTextView7 = listOf(R.id.textView1,R.id.textView2,R.id.textView3,
        R.id.textView4,R.id.textView5,R.id.textView6,R.id.textView7)

    private val refIDTextView4 = listOf(R.id.textView1_4,R.id.textView2_4,R.id.textView3_4,R.id.textView7)
    private val refIDTextView4a = listOf(R.id.textView1_4a,R.id.textView2_4a,R.id.textView3_4a,R.id.textView7a)

    val refIDTextViewScoreSheet7 = listOf(R.id.gameNumber,R.id.player1,R.id.player2,R.id.player3,R.id.player4, // size 8 - sheet of score
        R.id.player5,R.id.player6,R.id.player7)

    val refIDTextViewScoreSheet4 = listOf(R.id.gameNumber_4,R.id.player1_4,R.id.player2_4,R.id.player3_4,R.id.player4_4)

     private val refIDImageView7 = listOf(R.id.playerView1,R.id.playerView2,R.id.playerView3,
        R.id.playerView4,R.id.playerView5,R.id.playerView6,R.id.playerView7)

     private val refIDHighlightView7 = listOf(R.id.highlight1,R.id.highlight2,R.id.highlight3,
        R.id.highlight4,R.id.highlight5,R.id.highlight6,R.id.highlight7)

    private val refIDImageView4 = listOf(R.id.playerView1_4,R.id.playerView2_4,R.id.playerView3_4, R.id.playerView7)
    private val refIDHighlightView4 = listOf(R.id.highlight1_4,R.id.highlight2_4,R.id.highlight3_4, R.id.highlight7)

    private val refIDPartnerIconImageView7  = listOf(R.id.partnerIcon1,R.id.partnerIcon2,R.id.partnerIcon3,R.id.partnerIcon4,
        R.id.partnerIcon5,R.id.partnerIcon6,R.id.partnerIcon7)

    private val refIDPartnerIconImageView4  = listOf(R.id.partnerIcon1_4,R.id.partnerIcon2_4,R.id.partnerIcon3_4,R.id.partnerIcon7)

    private val refIDOnlineIconImageView7  = listOf(R.id.onlinep1,R.id.onlinep2,R.id.onlinep3,R.id.onlinep4,
        R.id.onlinep5,R.id.onlinep6,R.id.onlinep7)

    private val refIDOnlineIconImageView4  = listOf(R.id.onlinep1_4,R.id.onlinep2_4,R.id.onlinep3_4, R.id.onlinep7)

    private val refIDTableImageView7  = listOf(R.id.imageViewTable1,R.id.imageViewTable2,R.id.imageViewTable3,R.id.imageViewTable4,
        R.id.imageViewTable5,R.id.imageViewTable6,R.id.imageViewSelf)
    private val refIDTableImageView4  = listOf(R.id.imageViewTable1_4,R.id.imageViewTable2_4,R.id.imageViewTable3_4,R.id.imageViewSelf_4)

    private val refIDTableAnim7 = listOf(R.anim.anim_table_card_1,R.anim.anim_table_card_2,R.anim.anim_table_card_3
        ,R.anim.anim_table_card_4,R.anim.anim_table_card_5,R.anim.anim_table_card_6,R.anim.anim_table_card_self)
    private val refIDTableAnim4 = listOf(R.anim.anim_table_card_1,R.anim.anim_table_card_2_4,R.anim.anim_table_card_6,R.anim.anim_table_card_self)

    private val refIDTableWinnerAnim7 = listOf(R.anim.anim_table_card_winner_1,R.anim.anim_table_card_winner_2,R.anim.anim_table_card_winner_3
        ,R.anim.anim_table_card_winner_4,R.anim.anim_table_card_winner_5,R.anim.anim_table_card_winner_6,R.anim.anim_table_card_winner_self)
    private val refIDTableWinnerAnim4 = listOf(R.anim.anim_table_card_winner_1,R.anim.anim_table_card_winner_2_4,R.anim.anim_table_card_winner_6,R.anim.anim_table_card_winner_self)

    fun refIDMappedOnlineIconImageView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDOnlineIconImageView7)
        else createRefID4(player, refIDOnlineIconImageView4)
    }
    fun refIDScoreLayout(nPlayer: Int): Int {
        return if(nPlayer==7) R.layout.score_board_table_7
        else R.layout.score_board_table_4
    }

    fun refIDMappedPartnerIconImageView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDPartnerIconImageView7)
        else createRefID4(player, refIDPartnerIconImageView4)
    }
    fun refIDMappedTextView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDTextView7)
        else createRefID4(player, refIDTextView4)
    }
    fun refIDMappedTextViewA(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDTextView7)
        else createRefID4(player, refIDTextView4a)
    }
    fun refIDMappedImageView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDImageView7)
        else createRefID4(player, refIDImageView4)
    }
    fun refIDMappedHighlightView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDHighlightView7)
        else createRefID4(player, refIDHighlightView4)
    }


    fun refIDMappedTableImageView(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDTableImageView7)
        else createRefID4(player, refIDTableImageView4)
    }
    fun refIDMappedTableAnim(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDTableAnim7)
        else createRefID4(player, refIDTableAnim4)
    }
    fun refIDMappedTableWinnerAnim(player: String, nPlayer: Int): List<Int>{
        return if(nPlayer==7) createRefID7(player, refIDTableWinnerAnim7)
        else createRefID4(player, refIDTableWinnerAnim4)
    }

    private fun createRefID7(player: String, listToExtract: List<Int> ): List<Int>{
        var listExtracted = listOf<Int>()
        when(player){
            "p7"-> listExtracted = extractRefID(listOf(0,1,2,3,4,5,6),listToExtract)
            "p6"-> listExtracted = extractRefID(listOf(1,2,3,4,5,6,0),listToExtract)
            "p5"-> listExtracted = extractRefID(listOf(2,3,4,5,6,0,1),listToExtract)
            "p4"-> listExtracted = extractRefID(listOf(3,4,5,6,0,1,2),listToExtract)
            "p3"-> listExtracted = extractRefID(listOf(4,5,6,0,1,2,3),listToExtract)
            "p2"-> listExtracted = extractRefID(listOf(5,6,0,1,2,3,4),listToExtract)
            "p1"-> listExtracted = extractRefID(listOf(6,0,1,2,3,4,5),listToExtract)
        }
        return listExtracted
    }

    private fun createRefID4(player: String, listToExtract: List<Int> ): List<Int>{
        var listExtracted = listOf<Int>()
        when(player){
            "p4"-> listExtracted = extractRefID(listOf(0,1,2,3),listToExtract)
            "p3"-> listExtracted = extractRefID(listOf(1,2,3,0),listToExtract)
            "p2"-> listExtracted = extractRefID(listOf(2,3,0,1),listToExtract)
            "p1"-> listExtracted = extractRefID(listOf(3,0,1,2),listToExtract)
        }
        return listExtracted
    }

    private fun extractRefID(x: List<Int>, listToExtract: List<Int>) : List<Int>{
        var refIDMapped = listOf<Int>()
        for(i in x){
            refIDMapped = refIDMapped + listToExtract[i]
        }
        return refIDMapped
    }
}


