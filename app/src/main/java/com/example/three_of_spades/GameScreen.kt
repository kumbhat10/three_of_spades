package com.example.three_of_spades

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pl.droidsonroids.gif.GifImageView
import kotlin.math.round

class GameScreen : AppCompatActivity() {
    //    region Initialization
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var soundShuffle: MediaPlayer
    private lateinit var soundChat: MediaPlayer
    private lateinit var soundCollectCards: MediaPlayer

    private lateinit var soundTimerFinish: MediaPlayer
    private lateinit var refIDMappedTextView: List<Int>
    private lateinit var refIDMappedImageView: List<Int>
    private lateinit var refIDMappedTableImageView: List<Int>
    private lateinit var refIDMappedTableAnim: List<Int>
    private lateinit var refIDMappedTableWinnerAnim: List<Int>
    private lateinit var refIDValesTextViewScore: List<Int>

    private val cardsDrawableDoubleDeck = PlayingCards().cardsDrawableDoubleDeck()
    private val cardsDrawableSingleDeck = PlayingCards().cardsDrawableSingleDeck
    private val cardPointsDoubleDeck    = PlayingCards().cardPointsDoubleDeck()
    private val cardSuitDoubleDeck = PlayingCards().cardSuitDoubleDeck()
    private val highOrderSortedCards = PlayingCards().highOrderSortedCards
    private val cardPointsSingleDeck = PlayingCards().cardPointsSingleDeck

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private lateinit var refGameData: DatabaseReference
    private lateinit var refRooms: DatabaseReference
    private lateinit var gameStateListener:ValueEventListener
    private lateinit var roomListener: ValueEventListener
    private lateinit var chatListener: ValueEventListener
    private lateinit var countDownBidding: CountDownTimer
    private lateinit var countDownPlayCard: CountDownTimer
    private lateinit var bidingTurnListener:ValueEventListener
    private lateinit var cardsOnTableListener1:ValueEventListener
    private lateinit var cardsOnTableListener2:ValueEventListener
    private lateinit var cardsOnTableListener3:ValueEventListener
    private lateinit var cardsOnTableListener4:ValueEventListener
    private lateinit var cardsOnTableListener5:ValueEventListener
    private lateinit var cardsOnTableListener6:ValueEventListener
    private lateinit var cardsOnTableListener7:ValueEventListener
    private lateinit var pointsListener1:ValueEventListener
    private lateinit var pointsListener2:ValueEventListener
    private lateinit var pointsListener3:ValueEventListener
    private lateinit var pointsListener4:ValueEventListener
    private lateinit var pointsListener5:ValueEventListener
    private lateinit var pointsListener6:ValueEventListener
    private lateinit var pointsListener7:ValueEventListener
    private lateinit var onlineStatusListener1: ValueEventListener
    private lateinit var onlineStatusListener2: ValueEventListener
    private lateinit var onlineStatusListener3: ValueEventListener
    private lateinit var onlineStatusListener4: ValueEventListener
    private lateinit var onlineStatusListener5: ValueEventListener
    private lateinit var onlineStatusListener6: ValueEventListener
    private lateinit var onlineStatusListener7: ValueEventListener
    private lateinit var roundNumberListener: ValueEventListener

    private var ct1: Int? = null
    private var ct2: Int? = null
    private var ct3: Int? = null
    private var ct4: Int? = null
    private var ct5: Int? = null
    private var ct6: Int? = null
    private var ct7: Int? = null
    private var pt1 = 0
    private var pt2 = 0
    private var pt3 = 0
    private var pt4 = 0
    private var pt5 = 0
    private var pt6 = 0
    private var pt7 = 0

    private lateinit var roundListener:ValueEventListener
    private lateinit var cardsInHandListener:ValueEventListener
    private lateinit var onClickListener: View.OnClickListener
    private var gameState: Int? = null
    private var playerTurn: Int? = null
    private var playerTurnLast: Int? = null
    private var bider: Int? = null
    private lateinit var trump: String
    private lateinit var trumpStart: String
    private var gameTurn = 0
    private lateinit var cardsInHand: MutableList<Any>
    private var bidValue: Int? = null
    private var bidingStarted = false   /// biding happened before
    private var roundStarted = false
    private lateinit var cardsOnTable: List<Int>
    private lateinit var p1: String
    private lateinit var p2: String
    private lateinit var p3: String
    private lateinit var p4: String
    private lateinit var p5: String
    private lateinit var p6: String
    private lateinit var p7: String
    private var counterChat = 0
    private var counterPartnerSelection = 0
    private var bu1 = 0
    private var bu1Flag = 0
    private var bu2 = 0
    private var bu2Flag = 0
    private var onlineP1 = 1
    private var onlineP2 = 1
    private var onlineP3 = 1
    private var onlineP4 = 1
    private var onlineP5 = 1
    private var onlineP6 = 1
    private var onlineP7 = 1
   private var timeCountdown = 5000L
    private var lastChat = ""
    private lateinit var toast: Toast
    private var roundWinner = 0
    private var roundNumber = 1
    private var scoreNotUpdated = true
    // endregion
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // keep screen in landscape mode always
        roomID   = intent.getStringExtra("roomID")!!.toString()    //Get roomID and display    selfName = intent.getStringExtra("selfName") //Get Username first  - selfName ,roomID available
        from     = intent.getStringExtra("from")!!.toString()    //check if user has joined room or created one and display Toast
        selfName = intent.getStringExtra("selfName")!!.toString()
        refIDMappedTextView = PlayersReference().refIDMappedTextView(from)
        refIDMappedImageView = PlayersReference().refIDMappedImageView(from)
        refIDMappedTableImageView =  PlayersReference().refIDMappedTableImageView(from)
        refIDMappedTableAnim =  PlayersReference().refIDMappedTableAnim(from)
        refIDMappedTableWinnerAnim =  PlayersReference().refIDMappedTableWinnerAnim(from)
        refIDValesTextViewScore = PlayersReference().refIDValesTextViewScore

        refGameData = Firebase.database.getReference("GameData/$roomID")
        refRooms =  Firebase.database.getReference("Rooms/$roomID")
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundSuccess= MediaPlayer.create(applicationContext,R.raw.player_success_chime)
        soundShuffle = MediaPlayer.create(applicationContext,R.raw.cards_shuffle)
        soundChat = MediaPlayer.create(applicationContext,R.raw.chat_new1)
        soundTimerFinish = MediaPlayer.create(applicationContext,R.raw.timer_over)
        soundCollectCards = MediaPlayer.create(applicationContext,R.raw.collect_cards)

        toast = Toast.makeText(applicationContext,"dd",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER,0,20)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 14F
    }

    override fun onStart() {
        super.onStart()
        write("OL/$from",1)
        getCardsAndDisplay(display=false)
        animateElements() 
//region Room Listener
        roomListener = object : ValueEventListener {override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(dataLoad: DataSnapshot) {
                updatePlayerInfo(dataLoad)  }  }
// endregion
// region table card listener
        cardsOnTableListener1 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct1 = p0.value.toString().toInt()
                if(ct1!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[0]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[0]))
                    findViewById<ImageView>(refIDMappedTableImageView[0]).setImageResource(cardsDrawableDoubleDeck[ct1!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[0]).clearAnimation()
                }
           }
}
        cardsOnTableListener2 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct2 = p0.value.toString().toInt()
                if(ct2!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[1]))
                    findViewById<ImageView>(refIDMappedTableImageView[1]).setImageResource(cardsDrawableDoubleDeck[ct2!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[1]).clearAnimation()
                }
            }
        }
        cardsOnTableListener3 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct3 = p0.value.toString().toInt()
                if(ct3!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[2]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[2]))
                    findViewById<ImageView>(refIDMappedTableImageView[2]).setImageResource(cardsDrawableDoubleDeck[ct3!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[2]).clearAnimation()
                }
            }
        }
        cardsOnTableListener4 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct4 = p0.value.toString().toInt()
                if(ct4!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[3]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[3]))
                    findViewById<ImageView>(refIDMappedTableImageView[3]).setImageResource(cardsDrawableDoubleDeck[ct4!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[3]).clearAnimation()
                }
            }
        }
        cardsOnTableListener5 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct5 = p0.value.toString().toInt()
                if(ct5!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[4]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[4]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[4]))
                    findViewById<ImageView>(refIDMappedTableImageView[4]).setImageResource(cardsDrawableDoubleDeck[ct5!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[4]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[4]).clearAnimation()
                }
            }
        }
        cardsOnTableListener6 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct6 = p0.value.toString().toInt()
                if(ct6!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[5]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[5]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[5]))
                    findViewById<ImageView>(refIDMappedTableImageView[5]).setImageResource(cardsDrawableDoubleDeck[ct6!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[5]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[5]).clearAnimation()
                }
            }
        }
        cardsOnTableListener7 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct7 = p0.value.toString().toInt()
                if(ct7!=99){
                    soundUpdate.start()
                    findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[6]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[6]))
                    findViewById<ImageView>(refIDMappedTableImageView[6]).setImageResource(cardsDrawableDoubleDeck[ct7!!])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[6]).clearAnimation()
                }
            }
        }
// endregion
// region Chat Listener
        chatListener = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(data.value.toString().isNotEmpty() && lastChat != data.value.toString()) { // if chat is not empty
                    findViewById<TextView>(R.id.textViewChatDisplay).text = findViewById<TextView>(R.id.textViewChatDisplay).text.toString() + "\n" + data.value.toString()
                    findViewById<TextView>(R.id.textViewChatDisplay).requestLayout()
                    lastChat = data.value.toString()
                    if (findViewById<LinearLayout>(R.id.chatLinearLayout).visibility == View.INVISIBLE){
                        soundChat.start()
                    counterChat += 1 // increase counter by 1 is chat display is off
                    findViewById<TextView>(R.id.textViewChatNo).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textViewChatNo).text = counterChat.toString()
                    findViewById<TextView>(R.id.textViewChatNo).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
                }
                }
            }
        }
        // endregion
//region Online Status Listener
        onlineStatusListener1 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
            if(onlineP1 != data.value.toString().toInt()){
                onlineP1 = data.value.toString().toInt()
                if(onlineP1==0 && from!="p1") toastCenter("${playerName(1)} is Offline !")
                if(onlineP1==1 && from!="p1") toastCenter("${playerName(1)} is Online !")
                if(onlineP1==2 && from!="p1") toastCenter("${playerName(1)} has left the room !")}


            }
        }
        onlineStatusListener2 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP2 != data.value.toString().toInt()){
                    onlineP2 = data.value.toString().toInt()
                    if(onlineP2==0 && from!="p2") toastCenter("${playerName(2)} is Offline !")
                    if(onlineP2==1 && from!="p2") toastCenter("${playerName(2)} is Online !")
                    if(onlineP2==2 && from!="p2") toastCenter("${playerName(2)} has left the room !")}
            }
        }
        onlineStatusListener3 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP3 != data.value.toString().toInt()){
                    onlineP3 = data.value.toString().toInt()
                    if(onlineP3==0 && from!="p3") toastCenter("${playerName(3)} is Offline !")
                    if(onlineP3==1 && from!="p3") toastCenter("${playerName(3)} is Online !")
                    if(onlineP3==2 && from!="p3") toastCenter("${playerName(3)} has left the room !")}
            }
        }
        onlineStatusListener4 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP4 != data.value.toString().toInt()){
                    onlineP4 = data.value.toString().toInt()
                    if(onlineP4==0 && from!="p4") toastCenter("${playerName(4)} is Offline !")
                    if(onlineP4==1 && from!="p4") toastCenter("${playerName(4)} is Online !")
                    if(onlineP4==2 && from!="p4") toastCenter("${playerName(4)} has left the room !")}
            }
        }
        onlineStatusListener5 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP5 != data.value.toString().toInt()){
                    onlineP5 = data.value.toString().toInt()
                    if(onlineP5==0 && from!="p5") toastCenter("${playerName(5)} is Offline !")
                    if(onlineP5==1 && from!="p5") toastCenter("${playerName(5)} is Online !")
                    if(onlineP5==2 && from!="p5") toastCenter("${playerName(5)} has left the room !")}
            }
        }
        onlineStatusListener6 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP6 != data.value.toString().toInt()){
                    onlineP6 = data.value.toString().toInt()
                    if(onlineP6==1 && from!="p6") toastCenter("${playerName(6)} is Offline !")
                    if(onlineP6==1 && from!="p6") toastCenter("${playerName(6)} is Online !")
                    if(onlineP6==2 && from!="p6") toastCenter("${playerName(6)} has left the room !")}
            }
        }
        onlineStatusListener7 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP7 != data.value.toString().toInt()){
                    onlineP7 = data.value.toString().toInt()
                    if(onlineP7==0 && from!="p7") toastCenter("${playerName(7)} is Offline !")
                    if(onlineP7==1 && from!="p7") toastCenter("${playerName(7)} is Online !")
                    if(onlineP7==2 && from!="p7") toastCenter("${playerName(7)} has left the room !")}
            }
        }
        // endregion
// region Game State Listener
        gameStateListener   = object :ValueEventListener{override fun onCancelled(p0: DatabaseError) {};
            override fun onDataChange(GameState: DataSnapshot) {
                gameState = (GameState.value as Long).toInt()
                if (gameState == 1) {
//                    write("GS",6)
                    shufflingWindow(gameStateChange = true)
                }
                if (gameState == 2) {
                soundUpdate.start()
                getCardsAndDisplay()
                startBidding()
            }
                if (gameState == 3) {
                soundSuccess.start()
                refGameData.child("Bid").removeEventListener(bidingTurnListener)
                bidingStarted = false
                finishBackgroundAnimationBidding()
                startTrumpSelection()
            }
                if (gameState == 4) {
                getCardsAndDisplay()
                soundSuccess.start()
                getTrumpStartPartnerSelection()
            }
                if (gameState == 5) {
                getCardsAndDisplay()
                getBuddyandDisplay()
                if(!roundStarted){
                    soundSuccess.start()
                    Handler().postDelayed(Runnable { startPlayingRound() },4000)
                    if("p$playerTurn"!=from)  centralText("${bider?.let { playerName(it) }} will play first \n You get 10 seconds to play card")
                    if("p$playerTurn"==from) centralText("${bider?.let { playerName(it) }}, \n You will have 10 seconds to play card")
                }else{
                    startPlayingRound()
                }

            }
                if (gameState == 6) {
                    soundShuffle.start()
                    displayShufflingCards()
                    if(scoreNotUpdated) {
                        scoreBoardTable(display = false,data = listOf("#",p1,p2,p3,p4,p5,p6,p7))
                        scoreNotUpdated = false
                    }
                    scoreBoardTable(data = listOf(1,350,0,350,0,700,0,0))

                }
        }}
// endregion
// region Round Number Listener
        roundNumberListener = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {    }
            override fun onDataChange(p0: DataSnapshot) {
                roundNumber = p0.value.toString().toInt()
    }
}
        // endregion
// region points listener
        pointsListener1 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt1!=p0.value.toString().toInt()){
                    pt1 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt1.toString())
                }
            }
        }
        pointsListener2 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt2!=p0.value.toString().toInt()){
                    pt2 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt2.toString())
                }
            }
        }
        pointsListener3 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt3!=p0.value.toString().toInt()){
                    pt3 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt3.toString())
                }
            }
        }
        pointsListener4 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt4!=p0.value.toString().toInt()){
                    pt4 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt4.toString())
                }
            }
        }
        pointsListener5 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt5!=p0.value.toString().toInt()){
                    pt5 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt5.toString())
                }
            }
        }
        pointsListener6 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt6!=p0.value.toString().toInt()){
                    pt6 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt6.toString())
                }
            }
        }
        pointsListener7 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt7!=p0.value.toString().toInt()){
                    pt7 = p0.value.toString().toInt()
//                    soundUpdate.start()
                    toastCenter(pt7.toString())
                }
            }
        }

//        endregion
findViewById<EditText>(R.id.editTextChatInput).setOnEditorActionListener { v, actionId, event ->
    return@setOnEditorActionListener when (actionId) {
        EditorInfo.IME_ACTION_SEND -> {
            sendChat(v)
            false
        }
        else -> false
    }
}
//        region Attach Listener
        refRooms.addValueEventListener(roomListener)
        refGameData.child("SC/p1").addValueEventListener(pointsListener1)
        refGameData.child("SC/p2").addValueEventListener(pointsListener2)
        refGameData.child("SC/p3").addValueEventListener(pointsListener3)
        refGameData.child("SC/p4").addValueEventListener(pointsListener4)
        refGameData.child("SC/p5").addValueEventListener(pointsListener5)
        refGameData.child("SC/p6").addValueEventListener(pointsListener6)
        refGameData.child("SC/p7").addValueEventListener(pointsListener7)

        refGameData.child("R").addValueEventListener(roundNumberListener)
        refGameData.child("GS").addValueEventListener(gameStateListener) // attach the created game data listener
        refGameData.child("M").addValueEventListener(chatListener) //attach chat listener
        refGameData.child("CT/p1").addValueEventListener(cardsOnTableListener1) // player 1 cards on table listener
        refGameData.child("CT/p2").addValueEventListener(cardsOnTableListener2) // player 1 cards on table listener
        refGameData.child("CT/p3").addValueEventListener(cardsOnTableListener3) // player 1 cards on table listener
        refGameData.child("CT/p4").addValueEventListener(cardsOnTableListener4) // player 1 cards on table listener
        refGameData.child("CT/p5").addValueEventListener(cardsOnTableListener5) // player 1 cards on table listener
        refGameData.child("CT/p6").addValueEventListener(cardsOnTableListener6) // player 1 cards on table listener
        refGameData.child("CT/p7").addValueEventListener(cardsOnTableListener7) // player 1 cards on table listener
        refGameData.child("OL/p1").addValueEventListener(onlineStatusListener1)
        refGameData.child("OL/p2").addValueEventListener(onlineStatusListener2)
        refGameData.child("OL/p3").addValueEventListener(onlineStatusListener3)
        refGameData.child("OL/p4").addValueEventListener(onlineStatusListener4)
        refGameData.child("OL/p5").addValueEventListener(onlineStatusListener5)
        refGameData.child("OL/p6").addValueEventListener(onlineStatusListener6)
        refGameData.child("OL/p7").addValueEventListener(onlineStatusListener7)
//        endregion
        // region       Countdown PlayCard
        countDownPlayCard = object: CountDownTimer(timeCountdown,50){
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished*100/timeCountdown).toInt()
                findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished/1000).toDouble() +1).toInt().toString() + "s"
            }
            override fun onFinish() {
                soundTimerFinish.start()
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                autoPlayCard()
            }
        }
//        endregion
        // region       Countdown Biding
        countDownBidding = object: CountDownTimer(timeCountdown,50){
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished*100/timeCountdown).toInt()
                findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished/1000).toDouble()).toInt().toString() + "s"
            }
            override fun onFinish() {
                soundTimerFinish.start()
                write("Bid/BS/p$playerTurn",0) // pass the bid if times up
                write("Bid/BT",nextTurn(playerTurn!!))
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
                findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
                bidButtonsAnimation("clear")
                centralText("    Time's Up !!  \n You cannot bid anymore",5000)
            }
        }
//        endregion
    }

    private fun countDownTimer(task: String, time: Long = 10000, purpose: String = "start") {
        if(purpose=="start"){
//            timeCountdown = time
            findViewById<ProgressBar>(R.id.progressbarTimer).progress = 100
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).text = "10s"
            findViewById<ProgressBar>(R.id.progressbarTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<TextView>(R.id.textViewTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            if (task=="Bidding") countDownBidding.start()
            if (task=="PlayCard") countDownPlayCard.start()
        }else if(purpose== "cancel"){
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
            findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
            findViewById<TextView>(R.id.textViewTimer).clearAnimation()
            if (task=="Bidding") countDownBidding.cancel()
            if (task=="PlayCard") countDownPlayCard.cancel()
        }

    }
    private fun startPlayingRound(){
        roundStarted = true
        findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
        roundListener = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {  }
            override fun onDataChange(p0: DataSnapshot) {
                if(gameTurn != p0.child("T").value.toString().toInt()){ // if the game turn changes then only proceed
                    playerTurn = p0.child("P").value.toString().toInt()
                    gameTurn = p0.child("T").value.toString().toInt()
                    trumpStart = p0.child("R").value.toString() // trump of start game
                    clearAllAnimation()
                    if(gameTurn!=8 && gameTurn != 0){
                        animatePlayerPlayingRound(playerTurn!!)
                        if("p$playerTurn"==from){
                            countDownTimer(task = "PlayCard",time = 6000L) // start countdown timer and run autoPlayCard
                        }
                    }else if(gameTurn==8){
                        declareRoundWinner()
                    }
                }}        }
    refGameData.child("RO").addValueEventListener(roundListener)
    }

    private fun autoPlayCard(){
        if(gameTurn==1){  //can play any random card
            val cardSelected = cardsInHand.random()
            startNextTurn(cardSelected)
        }
        else{ // play only same suit card
            var cardSelectedIndex = cardSuitDoubleDeck.slice(cardsInHand as Iterable<Int>).lastIndexOf(trumpStart)// play largest card first
            if(cardSelectedIndex ==-1){ //not found
                cardSelectedIndex = cardSuitDoubleDeck.slice(cardsInHand as Iterable<Int>).lastIndexOf(trump)// play trump card
                if(cardSelectedIndex ==-1){
                    val cardSelected = cardsInHand.random()// or play any random card
                    startNextTurn(cardSelected)
                }else{
                    val cardSelected = cardsInHand[cardSelectedIndex]
                    startNextTurn(cardSelected)
                }
            }else{
                val cardSelected = cardsInHand[cardSelectedIndex]
                startNextTurn(cardSelected)
            }
        }
    }
    private fun validateSelfPlayedCard(view: View) {
        if(gameState==5 && "p$playerTurn" == from && gameTurn != 8 && gameTurn !=0 ){
            val cardSelected = view.tag.toString().toInt()
            if(gameTurn==1 || cardSuitDoubleDeck[cardSelected] == trumpStart  || cardSuitDoubleDeck.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) == -1){
                countDownTimer("PlayCard",purpose = "cancel")
//                if(gameTurn==1) write("RO/R",cardSuitDoubleDeck[cardSelected])
                startNextTurn(cardSelected.toLong()) // allow throw if first chance, or same suit as first turn or doesnt have same suit card
            }
                else{
                soundError.start()
                toastCenter("${playerName(playerTurn!!)}, please play ${getSuitName(trumpStart)} card")
            }
        }
    }
    private fun startNextTurn(cardSelected: Any){
        write("CT/$from",cardSelected)
        write("RO/P",nextTurn(playerTurn!!))
        if(gameTurn==1) write("RO/R",cardSuitDoubleDeck[cardSelected.toString().toInt()])

        write("RO/T",gameTurn+1)
        cardsInHand.remove(cardSelected)

        if (roundNumber !=14) {
            write("CH/$from",cardsInHand)
            getCardsAndDisplay()
        }
        else{
            write("CH/$from","")
            findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
        }
    }
    private fun declareRoundWinner(){
        val roundCards =  listOf<Int>(ct1!!.toInt(),ct2!!.toInt(),ct3!!.toInt(),  ct4!!.toInt(),ct5!!.toInt(),ct6!!.toInt(),ct7!!.toInt())
        var winnerCard = roundCards[playerTurn!!-1]
        var start = playerTurn!!
        var points = 0
        for(k in 0..6){
            points += cardPointsDoubleDeck[roundCards[k]]
        }

        for(i in 1..6){
            start = nextTurn(start)
            winnerCard = compareCardsforWinner(roundCards[start -1], winnerCard)
        }
        roundWinner = roundCards.indexOf(winnerCard) + 1
        animatePlayerPlayingRound(roundWinner)
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_big_fast))
        Handler().postDelayed(Runnable {
            animateWinner()
        },600)

            Handler().postDelayed(Runnable {
                if(roundNumber != 14){
                    if("p$roundWinner" == from){// only winner can start next round
                        startNextRound()
                    }else{
                        toastCenter("Waiting for ${playerName(roundWinner)} to play next card")
                    }
                }else{
                    refGameData.child("RO").removeEventListener(roundListener)
                    clearAllAnimation()
                    if("p$roundWinner" == from){// winner set gamestate to 6
                        write("GS",6)
                    }
                }
            },2000)

    }
    private fun startNextRound(){
        write("CT/p1",99)
        write("CT/p2",99)
        write("CT/p3",99)
        write("CT/p4",99)
        write("CT/p5",99)
        write("CT/p6",99)
        write("CT/p7",99)
        write("RO/P",roundWinner)
        write("RO/R","")
        toastCenter("Please play next card")
        write("RO/T",1)
        write("R",roundNumber+1)
    }
    private fun scoreBoardTable(view: View = View(applicationContext), data: List<Any>,display: Boolean = true) {
        if(display) findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.VISIBLE
        val gallery = findViewById<LinearLayout>(R.id.imageGalleryScore)
        val inflater = LayoutInflater.from(applicationContext)
        val viewTemp = inflater.inflate(R.layout.score_board_table, gallery, false)
        for(i in 0..7){
            viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).text = data[i].toString()
        }
        gallery.addView(viewTemp)
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up_in_score_table))
    }

    private fun compareCardsforWinner(currentCard: Int, winnerCard: Int): Int{
        var w = winnerCard
        val wSuit = cardSuitDoubleDeck[winnerCard]
        val cSuit = cardSuitDoubleDeck[currentCard]
        if((cSuit != trump && cSuit != trumpStart) || (cSuit != trump && wSuit == trump)) // c lost
        else if(cSuit == wSuit){
            if((currentCard - winnerCard ) >1 ){ w = currentCard}
            else if((winnerCard - currentCard)>1) {w = winnerCard}
            else{
                if((currentCard%2==1 && winnerCard==currentCard-1)  || (winnerCard%2==1 && currentCard==winnerCard-1)) w = currentCard
            }
        }else if(cSuit == trump && wSuit != trump){   // c won
            w = currentCard
        }
        return w
    }
    private fun getSuitName(ini: String): String{
        var suit = ""
        when(ini){
            "H"->suit = "Heart"
            "D"->suit = "Diamond"
            "S"->suit = "Spade"
            "C"->suit = "Club"
        }
        return suit
    }
    private fun displaySelfCards(view: View = View(applicationContext),  animation: Boolean = false) {
        findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in cardsInHand as List<Int>) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list, gallery, false)
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setImageResource(
                cardsDrawableDoubleDeck[x]
            )
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).tag = x.toString()
            if (cardPointsDoubleDeck.elementAt(x) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).text =
                    "${cardPointsDoubleDeck.elementAt(x)}"
                if (animation) viewTemp.findViewById<TextView>(R.id.textViewDisplayCard)
                    .startAnimation(
                        AnimationUtils
                            .loadAnimation(applicationContext, R.anim.blink_and_scale)
                    )
                if (animation) viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                    .startAnimation(
                        AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw)
                    )
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).visibility = View.GONE
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setOnClickListener(View.OnClickListener {
                validateSelfPlayedCard(it)
                viewTemp.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
            })
            gallery.addView(viewTemp)
            if (animation) { gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
                gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right))
            }
        }
    }
    private fun animatePlayerPlayingRound(index: Int){
        findViewById<GifImageView>(refIDMappedImageView[index-1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
        findViewById<GifImageView>(refIDMappedImageView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<TextView>(refIDMappedTextView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        if("p$index"==from){
            findViewById<LinearLayout>(R.id.imageGallery).
            startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_linearlayout))
//        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
        }
    }
    private fun clearAllAnimation(){
        for (i in 0..6) { // first reset background and animation
            findViewById<GifImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            findViewById<GifImageView>(refIDMappedImageView[i]).clearAnimation()
            findViewById<TextView>(refIDMappedTextView[i]).clearAnimation()
        }
        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
        findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
    }
    private fun animateWinner(){
        soundCollectCards.start()
        findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.imageViewWinnerCenter).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableWinnerAnim[roundWinner-1]))
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner-1]).clearAnimation()

        Handler().postDelayed(Runnable {
            findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.INVISIBLE
        },1500)

        for(i in 0..6){
            findViewById<ImageView>(refIDMappedTableImageView[i]).visibility = View.INVISIBLE
        }
    }

    private fun getBuddyandDisplay(){
    refGameData.child("BU").addListenerForSingleValueEvent(object :ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {        }
        override fun onDataChange(BU: DataSnapshot) {
          bu1 = BU.child("b1").value.toString().toInt()
            bu2 = BU.child("b2").value.toString().toInt()
            bu1Flag = BU.child("b1s").value.toString().toInt()
            bu2Flag = BU.child("b2s").value.toString().toInt()
            findViewById<ImageView>(R.id.imageViewP1).setImageResource(cardsDrawableSingleDeck[bu1])
            findViewById<ImageView>(R.id.imageViewP2).setImageResource(cardsDrawableSingleDeck[bu2])
            findViewById<ImageView>(R.id.imageViewP1).clearAnimation()
            findViewById<ImageView>(R.id.imageViewP2).clearAnimation()
            findViewById<TextView>(R.id.textViewP1).clearAnimation()
            findViewById<TextView>(R.id.textViewP2).clearAnimation()
            if(bu1==bu2){
                findViewById<TextView>(R.id.textViewP1).text = "Both"
                findViewById<TextView>(R.id.textViewP2).text = "Both"
            }else {
                if (bu1Flag == 1) findViewById<TextView>(R.id.textViewP1).text = "Only"
                if (bu1Flag == 0) findViewById<TextView>(R.id.textViewP1).text = "Any"
                if (bu2Flag == 1) findViewById<TextView>(R.id.textViewP2).text = "Only"
                if (bu2Flag == 0) findViewById<TextView>(R.id.textViewP2).text = "Any"
            }
        }

    })
}
    private fun getTrumpStartPartnerSelection() {
        refGameData.child("Tr").  //get the trump
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(dataLoad: DataSnapshot) {
                trump = dataLoad.value.toString()
                displayTrumpCard()
                if("p$bider" == from) {
                    findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE // make selection frame visible
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1)//choose 1st buddy text
                    findViewById<TextView>(R.id.textViewPartnerSelect).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
                    displayAllCardsForPartnerSelection()  // display all the cards to choose from
                }else{
                    centralText("Waitng for ${bider?.let { playerName(it) }} \n to choose 2 buddies",0)
                }
            } })
    }
    private fun partnerSelectClick(cardSelected: Int){ // assumption is cardsinHand already updated
        if( counterPartnerSelection == 0){
            if((cardsInHand as List<*>).contains((cardSelected*2).toLong()) and (cardsInHand as List<*>).contains((cardSelected*2+1).toLong())) {
                soundError.start()
                toastCenter("$selfName, You already have both of these cards ")
            }
            else if((cardsInHand as List<*>).contains((cardSelected*2).toLong()) or (cardsInHand as List<*>).contains((cardSelected*2+1).toLong())){
                soundUpdate.start()
                write("BU/b1",cardSelected)
                bu1 = cardSelected
                bu1Flag = 1
                soundUpdate.start()
                findViewById<ImageView>(R.id.imageViewP1).setImageResource(cardsDrawableSingleDeck[cardSelected])
                findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2)//choose 2nd buddy
                counterPartnerSelection =1
            } else{
                soundUpdate.start()
                bu1 = cardSelected
                write("BU/b1",bu1)
                bu1Flag = 0
                findViewById<ImageView>(R.id.imageViewP1).setImageResource(cardsDrawableSingleDeck[cardSelected])
                findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2)//choose 2nd buddy
                counterPartnerSelection =1
            }
        }else if( counterPartnerSelection == 1){
            if((cardsInHand as List<*>).contains((cardSelected*2).toLong()) and (cardsInHand as List<*>).contains((cardSelected*2+1).toLong())) {
                soundError.start()
                toastCenter("$selfName, You already have 2 same cards")
            }
            else if((cardsInHand as List<*>).contains((cardSelected*2).toLong()) or (cardsInHand as List<*>).contains((cardSelected*2+1).toLong())){
                if(bu1!=cardSelected){
                    write("BU/b2",cardSelected)
                    write("BU/b2s",1)  // bider has one of card in his hand
                    write("BU/b1s",bu1Flag)
                    write("GS",5) // change game state to next playing round
                counterPartnerSelection =0
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.partnerSelectionGallery).clearAnimation()}
                else{
                    soundError.start()
                    toastCenter("You have one and another already choosen")
                }
            } else{
                write("BU/b2",cardSelected)
                if(bu1 == cardSelected){
                    write("BU/b2s",2)  // bider has none in his hands and both same selected
                    write("BU/b1s",2)
                }else{
                    write("BU/b2s",0)  // bider has none in his hands and is different than 1st
                    write("BU/b1s",bu1Flag) // bider has none in his hands and first one status remains unchanged
                }
                write("GS",5)
                counterPartnerSelection =0
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                findViewById<LinearLayout>(R.id.partnerSelectionGallery).clearAnimation()
            }
        }

    }
    private fun displayAllCardsForPartnerSelection(view: View = View(applicationContext)){
        findViewById<LinearLayout>(R.id.partnerSelectionGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.partnerSelectionGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in highOrderSortedCards as List<Int>) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list_partner, gallery, false)
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).setImageResource( cardsDrawableSingleDeck[x]  )
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).tag = x.toString() //set tag to every card of its own value
            viewTemp.findViewById<TextView>(R.id.textViewPartner).text = "D"
            if (cardPointsSingleDeck.elementAt(x) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).text =
                    "${cardPointsSingleDeck.elementAt(x)} pts"
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).visibility = View.GONE
            } // make it invisible

            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).setOnClickListener(View.OnClickListener {
                partnerSelectClick(x)
            })
            gallery.addView(viewTemp)
        }
        findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right_selection_cards))


    }
    private fun displayTrumpCard() {
        when (trump) {
            "H" -> findViewById<GifImageView>(R.id.imageViewTrump).setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_hearts
                )
            )
            "S" -> findViewById<GifImageView>(R.id.imageViewTrump).setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_spades
                )
            )
            "D" -> findViewById<GifImageView>(R.id.imageViewTrump).setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_diamonds
                )
            )
            "C" -> findViewById<GifImageView>(R.id.imageViewTrump).setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_clubs
                )
            )
        }
        findViewById<GifImageView>(R.id.imageViewTrump).clearAnimation() // main trump showing view
    } // just displaying trump card

    private fun startTrumpSelection() {
        findViewById<TextView>(R.id.textViewBidValue).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
        findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
        if("p$bider" != from){     //  show to everyone except bider
            toastCenter("${bider?.let { playerName(it) }} won the bid round")
            centralText("Waiting for ${bider?.let { playerName(it) }} \n to choose Trump", 0)
             }
            else{ // show to bider only
            findViewById<FrameLayout>(R.id.frameTrumpSelection).visibility= View.VISIBLE
            trumpAnimation("start")
        }
    }
    fun onTrumpSelectionClick(view: View) {
        soundUpdate.start()
        when(view.tag){
            "h"-> write("Tr","H")
            "s"-> write("Tr","S")
            "d"-> write("Tr","D")
            "c"-> write("Tr","C")
        }
        write("GS",4)
        findViewById<FrameLayout>(R.id.frameTrumpSelection).visibility = View.GONE
        trumpAnimation("clear")
    }
    private fun trumpAnimation(task: String){
        if(task=="start"){
            findViewById<ImageView>(R.id.imageViewTrumpHearts).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<ImageView>(R.id.imageViewTrumpSpades).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<ImageView>(R.id.imageViewTrumpClubs).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        }else if (task == "clear"){
            findViewById<ImageView>(R.id.imageViewTrumpHearts).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpSpades).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpClubs).clearAnimation()
            findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        }
    }

    private fun startBidding() {
        bidingTurnListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(dataLoad: DataSnapshot) {
                bidValue = (dataLoad.child("BV").value as Long).toInt()
                playerTurn = (dataLoad.child("BT").value as Long).toInt()
                bider = (dataLoad.child("BB").value as Long).toInt()
                val bidStatus = (dataLoad.child("BS/p$playerTurn").value as Long).toInt()
               if(!bidingStarted) {
                   centralText("${playerName(playerTurn!!)} will start bidding",8000) //display message only first time
                   bidingStarted = true
                   playerTurnLast = playerTurn!! -1
               }
                if (playerTurnLast!=playerTurn){
                    soundUpdate.start()
                    findViewById<TextView>(R.id.textViewBidValue).text = bidValue.toString() //.toString() //show current bid value
                    findViewById<TextView>(R.id.textViewBider).text = playerName(bider!!)
                    findViewById<TextView>(R.id.textViewBidValue).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
                    findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
                    findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.INVISIBLE //biding frame invisible
                    resetBackgroundAnimationBidding(dataLoad) //set all background to black or red depending on status
                    animatePlayer(playerTurn!!)  // animate current player
                    if(bidStatus==1){  // highlight current player
                        findViewById<GifImageView>(refIDMappedImageView[playerTurn!!-1]).
                        setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
                    }
                    if("p$playerTurn"==from && bider !=playerTurn){
                        if (bidStatus==1)  { // show bid frame and ask to bid or pass
                            findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
                            findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is ciritical
                            bidButtonsAnimation("start")
                            countDownTimer("Bidding",purpose = "start")
                        }
                        else if (bidStatus==0 ){
                            toastCenter("   Sorry $selfName \n You cannot bid anymore")
                            write("Bid/BT",nextTurn(playerTurn!!))
                        }
                    }
                    if("p$playerTurn"==from && bider ==playerTurn) { // finish bid and move to next game state
                        bider?.let { write("RO/P", it) }  // write player turn to bider
                        write("GS", 3) // change gamestate to 3 as biding is finished
                    }
                }
                playerTurnLast = playerTurn
            }
        }
        refGameData.child("Bid").addValueEventListener(bidingTurnListener)
    }
    fun askToBid(view: View) {
        countDownTimer("Bidding", purpose = "cancel")
        soundUpdate.start()
        when(view.tag){
            "pass"-> {
                write("Bid/BS/$from", 0)
                centralText("    Time's Up !!  \n You cannot bid anymore",5000)
            }
            "5"->{bidValue?.plus(5)?.let { write("Bid/BV/", it) }
                playerTurn?.let { write("Bid/BB", it) }            }
            "10"->{bidValue?.plus(10)?.let { write("Bid/BV/", it) }
                playerTurn?.let { write("Bid/BB", it) }            }
            "20"->{bidValue?.plus(20)?.let { write("Bid/BV/", it) }
                playerTurn?.let { write("Bid/BB", it) }            }
            "50"->{bidValue?.plus(50)?.let { write("Bid/BV/", it) }
                playerTurn?.let { write("Bid/BB", it) }            }
        }
        write("Bid/BT",nextTurn(playerTurn!!))
        findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
        findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
        bidButtonsAnimation("clear")
    }
    private fun bidButtonsAnimation(task: String){
        if(task=="start"){
            findViewById<Button>(R.id.bid05button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<Button>(R.id.bid10button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<Button>(R.id.bid20button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<Button>(R.id.bid50button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<Button>(R.id.bidpassbutton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        }else if (task == "clear"){
            findViewById<Button>(R.id.bid05button).clearAnimation()
            findViewById<Button>(R.id.bid10button).clearAnimation()
            findViewById<Button>(R.id.bid20button).clearAnimation()
            findViewById<Button>(R.id.bid50button).clearAnimation()
            findViewById<Button>(R.id.bidpassbutton).clearAnimation()
        }
  }
    private fun resetBackgroundAnimationBidding(dataLoad: DataSnapshot) {
        for (i in 0..6) {
            val iPlayer = i+1
            val bidStatus = (dataLoad.child("BS/p$iPlayer").value as Long).toInt()
            if(bidStatus==0){
                findViewById<GifImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer2))
                if("p$iPlayer"==from)
                    findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer2))
            }
            else{
                findViewById<GifImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
                if("p$iPlayer"==from)
                    findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.layoutBackground))
            }
            findViewById<GifImageView>(refIDMappedImageView[i]).clearAnimation()
            findViewById<TextView>(refIDMappedTextView[i]).clearAnimation()
            if("p$iPlayer"==from) findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        }
    }
    private fun finishBackgroundAnimationBidding(){  //clear Everything on finish of biding round
        for (i in 0..6) {
            findViewById<GifImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            findViewById<GifImageView>(refIDMappedImageView[i]).clearAnimation()
            findViewById<TextView>(refIDMappedTextView[i]).clearAnimation()
        }
        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.layoutBackground))
        findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        findViewById<GifImageView>(refIDMappedImageView[this!!.bider!!-1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
    }

    private fun centralText(message: String, displayTime: Int=5000){
        findViewById<TextView>(R.id.textViewShuffling).visibility= View.VISIBLE
        findViewById<TextView>(R.id.textViewShuffling).text = message
        findViewById<TextView>(R.id.textViewShuffling).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
       if(displayTime != 0)
        Handler().postDelayed(Runnable {
            findViewById<TextView>(R.id.textViewShuffling).clearAnimation()
            findViewById<TextView>(R.id.textViewShuffling).visibility = View.INVISIBLE },displayTime.toLong() )
    }
    private fun animateElements() {
//        findViewById<TextView>(R.id.textViewBidValue).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        findViewById<TextView>(R.id.textViewBider).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        findViewById<TextView>(R.id.textViewBidValue).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        findViewById<ImageView>(R.id.imageViewP1).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<ImageView>(R.id.imageViewP2).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<GifImageView>(R.id.imageViewTrump).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<Button>(R.id.bid05button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<Button>(R.id.bid10button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<Button>(R.id.bid20button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<Button>(R.id.bid50button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<Button>(R.id.bidpassbutton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<GifImageView>(R.id.imageViewChat).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<GifImageView>(R.id.imageViewChat).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
        findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
        findViewById<TextView>(R.id.textViewTimer).clearAnimation()
    }
    private fun updatePlayerInfo(dataLoad: DataSnapshot) {
        p1 = dataLoad.child("Players").child("p1").value.toString()
        p2 = dataLoad.child("Players").child("p2").value.toString()
        p3 = dataLoad.child("Players").child("p3").value.toString()
        p4 = dataLoad.child("Players").child("p4").value.toString()
        p5 = dataLoad.child("Players").child("p5").value.toString()
        p6 = dataLoad.child("Players").child("p6").value.toString()
        p7 = dataLoad.child("Players").child("p7").value.toString()
        for(i in 0..6) {
            val j = i+1
            if ((dataLoad.child("Sex").child("p$j").value as Long).toInt() == 0)
                findViewById<GifImageView>(refIDMappedImageView[i]).setImageResource(R.drawable.man1)
            else findViewById<GifImageView>(refIDMappedImageView[i]).setImageResource(R.drawable.lady1)
            findViewById<TextView>(refIDMappedTextView[i]).text = "$j ${playerName(j)}"

        }
//        findViewById<TextView>(refIDMappedTextView[0]).text = p1
//        findViewById<TextView>(refIDMappedTextView[1]).text = p2
//        findViewById<TextView>(refIDMappedTextView[2]).text = p3
//        findViewById<TextView>(refIDMappedTextView[3]).text = p4
//        findViewById<TextView>(refIDMappedTextView[4]).text = p5
//        findViewById<TextView>(refIDMappedTextView[5]).text = p6
//        findViewById<TextView>(refIDMappedTextView[6]).text = p7
    }
    private fun shufflingWindow(time: Long = 4900,fadeOffTime: Long = 800, gameStateChange: Boolean = false){
        Handler().postDelayed(Runnable {soundShuffle.start()},600)//delayed sound play of shuffling
        displayShufflingCards() //show suits cards and animate
        findViewById<TextView>(R.id.textViewShuffling).visibility= View.VISIBLE
        findViewById<TextView>(R.id.textViewShuffling).text = "Shuffling Cards"
        findViewById<TextView>(R.id.textViewShuffling).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        Handler().postDelayed(Runnable {
            findViewById<TextView>(R.id.textViewShuffling).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fade_shuffle_gif))
            findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_down_out))
            Handler().postDelayed(Runnable {
                findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
                findViewById<TextView>(R.id.textViewShuffling).visibility= View.INVISIBLE
//                getCardsAndDisplay(animation = true)
                if(from=="p1" && gameStateChange) write("GS",2) // Update Game State to start Bidng round by Host only
//                displaySelfCards(animation = true)
            },fadeOffTime)
        },time)
    }
    private fun displayShufflingCards(view: View = View(applicationContext), sets:Int = 5){
        findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for(xx:Int in 0 until sets) {
            for (x: Int in 0..3) {
                val viewTemp = inflater.inflate(R.layout.cards_item_list_suits, gallery, false)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
                    .setImageResource(PlayingCards().suitsDrawable[x])
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).startAnimation(
                    AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw))
                if(x%2!=0){
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).setBackgroundColor(
                        ContextCompat.getColor(applicationContext,R.color.cardsBackgroundDark))
                }else{
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).setBackgroundColor(
                        ContextCompat.getColor(applicationContext,R.color.cardsBackgroundLight))
                }
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).setOnClickListener { viewTemp.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.clockwise_ccw)) }
                gallery.addView(viewTemp)
                gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_down_in))
                gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_left_right))
            }
        }
    }
    private fun write(path: String, value: Any) {
        refGameData.child(path).setValue(value)
    }
    private fun getCardsAndDisplay(player: String = from, display: Boolean = true, animation: Boolean=false){
        refGameData.child("CH/$player").  // display the host info in joining room screen
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(dataLoad: DataSnapshot) {
//                cardsInHand = (dataLoad.value as Iterable<Int>).filterNotNull()
                if (dataLoad.value.toString().isNotEmpty() && dataLoad.value.toString()!= "null"){
                    cardsInHand = (dataLoad.value as MutableList<Any>).filterNotNull() as MutableList<Any>
                    if(display) displaySelfCards(animation=animation)
                }else{
                    findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
                }

                } })
        }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }
    private fun playerName(index: Int): String{
        var name = ""
        when(index){
            1-> name = p1
            2-> name = p2
            3-> name = p3
            4-> name = p4
            5-> name = p5
            6-> name = p6
            7-> name = p7
        }
        return name
    }
    private fun nextTurn(current: Int): Int {
        var next = 0
        if (current != 7) {
            next = current + 1
        } else if(current==7) next = 1
        return next
    }
    private fun animatePlayer(index: Int){
        findViewById<GifImageView>(refIDMappedImageView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<TextView>(refIDMappedTextView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))

        if("p$index"==from)
            findViewById<LinearLayout>(R.id.imageGallery).
            startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_linearlayout))
    }
    fun openCloseChatWindow(view: View){
        if (findViewById<LinearLayout>(R.id.chatLinearLayout).visibility == View.VISIBLE){ //close chat display
            findViewById<LinearLayout>(R.id.chatLinearLayout).visibility = View.INVISIBLE
            findViewById<Button>(R.id.chatSendButton).clearAnimation()
        }
        else { //open chat display
            counterChat = 0 // reset chat counter to 0
            findViewById<TextView>(R.id.textViewChatNo).visibility = View.INVISIBLE // make counter invisible
            findViewById<TextView>(R.id.textViewChatNo).clearAnimation() // clear counter animation
            findViewById<LinearLayout>(R.id.chatLinearLayout).visibility = View.VISIBLE
            findViewById<Button>(R.id.chatSendButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        }
    }
    fun closeChatWindow (view: View){
        findViewById<LinearLayout>(R.id.chatLinearLayout).visibility = View.INVISIBLE
        findViewById<Button>(R.id.chatSendButton).clearAnimation()
//        if(findViewById<EditText>(R.id.editTextChatInput).text.toString().isNotEmpty()){
//            write("M",selfName +" : " + findViewById<EditText>(R.id.editTextChatInput).text.toString())
            findViewById<EditText>(R.id.editTextChatInput).setText("")
//        }
    }
    fun sendChat(view: View) {
//        soundUpdate.start()
        val cc2 = "2202"
        val text2 = String(Character.toChars(Integer.parseInt(cc2,16)))
        val text3 = Integer.parseInt("d83d", 16).toString() + Integer.parseInt("de04", 16).toString()

        val uni = 0x1F60A
        val emoji = String(Character.toChars(uni))

        write("M", "$selfName : $emoji  ")
//        if(findViewById<EditText>(R.id.editTextChatInput).text.toString().isNotEmpty()){
//            val b = findViewById<EditText>(R.id.editTextChatInput).text
//            write("M", "$selfName : ${findViewById<EditText>(R.id.editTextChatInput).text}  ")
//            findViewById<EditText>(R.id.editTextChatInput).setText("")
//        }
    }
    override fun onStop() {
        super.onStop()
        refGameData.child("GS").removeEventListener(gameStateListener)
//        refGameData.child("RO").removeEventListener(roundListener)

        refRooms.removeEventListener(roomListener)
        refGameData.child("R").removeEventListener(roundNumberListener)
        refGameData.child("M").removeEventListener(chatListener) //attach chat listener
        refGameData.child("SC/p1").removeEventListener(pointsListener1)
        refGameData.child("SC/p2").removeEventListener(pointsListener2)
        refGameData.child("SC/p3").removeEventListener(pointsListener3)
        refGameData.child("SC/p4").removeEventListener(pointsListener4)
        refGameData.child("SC/p5").removeEventListener(pointsListener5)
        refGameData.child("SC/p6").removeEventListener(pointsListener6)
        refGameData.child("SC/p7").removeEventListener(pointsListener7)
        refGameData.child("CT/p1").removeEventListener(cardsOnTableListener1) // player 1 cards on table listener
        refGameData.child("CT/p2").removeEventListener(cardsOnTableListener2) // player 1 cards on table listener
        refGameData.child("CT/p3").removeEventListener(cardsOnTableListener3) // player 1 cards on table listener
        refGameData.child("CT/p4").removeEventListener(cardsOnTableListener4) // player 1 cards on table listener
        refGameData.child("CT/p5").removeEventListener(cardsOnTableListener5) // player 1 cards on table listener
        refGameData.child("CT/p6").removeEventListener(cardsOnTableListener6) // player 1 cards on table listener
        refGameData.child("CT/p7").removeEventListener(cardsOnTableListener7) // player 1 cards on table listener
        refGameData.child("OL/p1").removeEventListener(onlineStatusListener1)
        refGameData.child("OL/p2").removeEventListener(onlineStatusListener2)
        refGameData.child("OL/p3").removeEventListener(onlineStatusListener3)
        refGameData.child("OL/p4").removeEventListener(onlineStatusListener4)
        refGameData.child("OL/p5").removeEventListener(onlineStatusListener5)
        refGameData.child("OL/p6").removeEventListener(onlineStatusListener6)
        refGameData.child("OL/p7").removeEventListener(onlineStatusListener7)
    }
    override fun onPause() {
        super.onPause()
        write("OL/$from",0)
    }
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        this.moveTaskToBack(true)
    }
}


