package com.example.three_of_spades

//fun main(): Unit {
//////    println(PlayersReference().refIDValuesTextView)
//////    println(PlayersReference().createRefID("p1"))
//////    println(PlayersReference().createRefID("p2"))
//////    println(PlayersReference().createRefID("p3"))
//////    println(PlayersReference().createRefID("p4"))
//////    println(PlayersReference().createRefID("p5"))
//////    println(PlayersReference().createRefID("p6"))
//////    println(PlayersReference().createRefID("p7"))
////}
class PlayersReference {

      private val refIDValuesTextView = listOf(R.id.textView1,R.id.textView2,R.id.textView3,
        R.id.textView4,R.id.textView5,R.id.textView6,R.id.textView7)

    val refIDValesTextViewScore = listOf(R.id.gameNumber,R.id.player1,R.id.player2,R.id.player3,R.id.player4,
        R.id.player5,R.id.player6,R.id.player7)

     private val refIDValuesImageView = listOf<Int>(R.id.playerView1,R.id.playerView2,R.id.playerView3,
        R.id.playerView4,R.id.playerView5,R.id.playerView6,R.id.playerView7)

    private val refIDValuesTableImageView  = listOf<Int>(R.id.imageViewTable1,R.id.imageViewTable2,R.id.imageViewTable3,R.id.imageViewTable4,
        R.id.imageViewTable5,R.id.imageViewTable6,R.id.imageViewSelf)

    private val refIDValuesTableAnim = listOf<Int>(R.anim.anim_table_card_1,R.anim.anim_table_card_2,R.anim.anim_table_card_3
        ,R.anim.anim_table_card_4,R.anim.anim_table_card_5,R.anim.anim_table_card_6,R.anim.anim_table_card_self)

    private val refIDValuesTableWinnerAnim = listOf<Int>(R.anim.anim_table_card_winner_1,R.anim.anim_table_card_winner_2,R.anim.anim_table_card_winner_3
        ,R.anim.anim_table_card_winner_4,R.anim.anim_table_card_winner_5,R.anim.anim_table_card_winner_6,R.anim.anim_table_card_winner_self)


    fun refIDMappedTextView(player: String): List<Int>{
       return createRefID(player,refIDValuesTextView)
    }
    fun refIDMappedImageView(player: String): List<Int>{
        return createRefID(player,refIDValuesImageView)
    }
    fun refIDMappedTableImageView(player: String): List<Int>{
        return createRefID(player,refIDValuesTableImageView)
    }

    fun refIDMappedTableAnim(player: String): List<Int>{
        return createRefID(player,refIDValuesTableAnim)
    }
    fun refIDMappedTableWinnerAnim(player: String): List<Int>{
        return createRefID(player,refIDValuesTableWinnerAnim)
    }

    fun createRefID(player: String, listToExtract: List<Int> ): List<Int>{
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
    private fun extractRefID(x: List<Int>, listToExtract: List<Int>) : List<Int>{
        var refIDMapped = listOf<Int>()
        for(i in x){
            refIDMapped = refIDMapped + listToExtract[i]
        };
        return refIDMapped
    }
}


