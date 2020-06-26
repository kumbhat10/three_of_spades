@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "PLUGIN_WARNING")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.*
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_game_screen.*
import pl.droidsonroids.gif.GifImageView
import java.util.*
import kotlin.math.round
import kotlin.properties.Delegates
import kotlin.random.Random

class GameScreen : AppCompatActivity() {
    //    region Initialization
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var soundShuffle: MediaPlayer
    private lateinit var soundChat: MediaPlayer
    private lateinit var soundCardPlayed: MediaPlayer

    private lateinit var soundCollectCards: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var soundTimerFinish: MediaPlayer
    private lateinit var refIDMappedTextView: List<Int>
    private lateinit var refIDMappedImageView: List<Int>
    private lateinit var refIDMappedTableImageView: List<Int>
    private lateinit var refIDMappedTableAnim: List<Int>
    private lateinit var refIDMappedTableWinnerAnim: List<Int>
    private lateinit var refIDValesTextViewScore: List<Int>
    private lateinit var refIDMappedPartnerIconImageView: List<Int>
    private lateinit var refIDMappedOnlineIconImageView: List<Int>

    private var musicStatus = true
    private var soundStatus = true
    private var vibrateStatus = true
    private lateinit var vibrator:Vibrator

    private var premiumStatus = false
    private var scoreOpenStatus = false
    private var versionStatus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private lateinit var mInterstitialAd: InterstitialAd

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private var nPlayers = 0
    private var nPlayers7 = false
    private var nPlayers4 = false
    private val emoji = String(Character.toChars(0x1F4B0))
    private val emojiScore = String(Character.toChars(0x1F3AF))
    private val emojiMessage = String(Character.toChars(0x1F4AC))
    private val emojiGuard = String(Character.toChars(0x1F482))

    private lateinit var refGameData: DatabaseReference
    private var refRoomFirestore = Firebase.firestore.collection("Rooms")
    private var refUsersData = Firebase.firestore.collection("Users")
    private lateinit var chatRegistration: ListenerRegistration

    private lateinit var gameStateListener:ValueEventListener
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
    private lateinit var roundListener:ValueEventListener
    private lateinit var partnerListener1:ValueEventListener
    private lateinit var partnerListener2:ValueEventListener

    private lateinit var trump: String
    private lateinit var trumpStart: String
    private lateinit var playerInfo: ArrayList<String>
    private lateinit var playerInfoCoins: ArrayList<Int>

    private lateinit var p1: String
    private lateinit var p2: String
    private lateinit var p3: String
    private lateinit var p4: String
    private lateinit var p5: String
    private lateinit var p6: String
    private lateinit var p7: String
    private var p1Coins by Delegates.notNull<Int>()
    private var p2Coins by Delegates.notNull<Int>()
    private var p3Coins by Delegates.notNull<Int>()
    private var p4Coins by Delegates.notNull<Int>()
    private var p5Coins by Delegates.notNull<Int>()
    private var p6Coins by Delegates.notNull<Int>()
    private var p7Coins by Delegates.notNull<Int>()
    private var p1Gain = 0
    private var p2Gain = 0
    private var p3Gain = 0
    private var p4Gain = 0
    private var p5Gain = 0
    private var p6Gain = 0
    private var p7Gain = 0
    private lateinit var toast: Toast
    private lateinit var cardsInHand: MutableList<Long>

    private val cardsDrawableDoubleDeck = PlayingCards().cardsDrawableDoubleDeck()
    private val cardsDrawableSingleDeck = PlayingCards().cardsDrawableSingleDeck
    private val cardPointsDoubleDeck    = PlayingCards().cardPointsDoubleDeck()
    private val cardSuitDoubleDeck = PlayingCards().cardSuitDoubleDeck()
    private val highOrderSortedCards = PlayingCards().highOrderSortedCards
    private val cardPointsSingleDeck = PlayingCards().cardPointsSingleDeck
    private var gameState: Int = 0
    private var gameNumber: Int = 1
    private var counterChat = 0
    private var onlineP1 = 0
    private var onlineP2 = 0
    private var onlineP3 = 0
    private var onlineP4 = 0
    private var onlineP5 = 0
    private var onlineP6 = 0
    private var onlineP7 = 0
    private var timeCountdownPlayCard = 10000L
    private var timeCountdownBid = 15000L
    private var lastChat = ""
    private var scoreSheetNotUpdated = true

    private var played = false
    private var ct1: Int = 99
    private var ct2: Int = 99
    private var ct3: Int = 99
    private var ct4: Int = 99
    private var ct5: Int = 99
    private var ct6: Int = 99
    private var ct7: Int = 99
    private var pt1 = 0
    private var pt2 = 0
    private var pt3 = 0
    private var pt4 = 0
    private var pt5 = 0
    private var pt6 = 0
    private var pt7 = 0
    private var ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
    private var bidTeamScore = 0
    private var scoreList = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
    private var tablePoints = 0
    private var playerTurn: Int = 0
    private var bidder: Int = 0
    private var gameTurn = 0
    private var bidValue: Int = 0
    private var bidingStarted = false   /// biding happened before
    private var roundStarted = false
    private var counterPartnerSelection = 0
    private var bu1 = 0
    private var bu1Flag = 0
    private var bu2 = 0
    private var bu2Flag = 0
    private var buPlayer1 = 0
    private var buFound1 = 0
    private var buPlayer2 = 0
    private var buFound2 = 0
    private var roundWinner = 0
    private var roundNumber = 1
    private var newGameStatus = true
    // endregion
    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable._s_icon_3shadow_bug1) //default: bug image
            .restartActivity(MainHomeScreen::class.java)
            .apply()
        setContentView(R.layout.activity_game_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // keep screen in landscape mode always
        roomID   = intent.getStringExtra("roomID")!!.toString()    //Get roomID and display    selfName = intent.getStringExtra("selfName") //Get Username first  - selfName ,roomID available
        from     = intent.getStringExtra("from")!!.toString()    //check if user has joined room or created one and display Toast
        selfName = intent.getStringExtra("selfName")!!.toString()
        playerInfo = intent.getStringArrayListExtra("playerInfo") as ArrayList<String>
        playerInfoCoins = intent.getStringArrayListExtra("playerInfoCoins") as ArrayList<Int>
        nPlayers = intent.getIntExtra("nPlayers", 4)
        if(nPlayers==7) nPlayers7 = true
        if(nPlayers==4) nPlayers4 = true
        updateGameScreen()
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundSuccess= MediaPlayer.create(applicationContext,R.raw.player_success_chime)
        soundShuffle = MediaPlayer.create(applicationContext,R.raw.cards_shuffle)
        soundChat = MediaPlayer.create(applicationContext,R.raw.chat_new1)
        soundCardPlayed = MediaPlayer.create(applicationContext,R.raw.card_moved)
        soundTimerFinish = MediaPlayer.create(applicationContext,R.raw.timer_over)
        soundCollectCards = MediaPlayer.create(applicationContext,R.raw.collect_cards)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        refIDMappedTextView = PlayersReference().refIDMappedTextView(from)
        refIDMappedImageView = PlayersReference().refIDMappedImageView(from)
        refIDMappedTableImageView =  PlayersReference().refIDMappedTableImageView(from)
        refIDMappedTableAnim =  PlayersReference().refIDMappedTableAnim(from)
        refIDMappedTableWinnerAnim =  PlayersReference().refIDMappedTableWinnerAnim(from)
        refIDValesTextViewScore = PlayersReference().refIDValuesTextViewScoreSheet
        refIDMappedPartnerIconImageView =  PlayersReference().refIDMappedPartnerIconImageView(from)
        refIDMappedOnlineIconImageView =  PlayersReference().refIDMappedOnlineIconImageView(from)

        refGameData = Firebase.database.getReference("GameData/$roomID")

        toast = Toast.makeText(applicationContext,"dd",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER,0,20)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.cardsBackgroundDark))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 14F

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()
        if(from == "p1"){
            editor.putString("Room", roomID)
         // write room ID in storage - to delete later / dummy redundant as not required
            editor.apply()
        }
        //region Player Info Update
        updatePlayerInfo()
//        createTargetPicasso()
// endregion
        getSharedPrefs()
        if(getString(R.string.test).contains('n')) initializeAds()
    }

    private fun updateGameScreen() {

        if(nPlayers7) {
            findViewById<TextView>(R.id.textView1).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep1).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView1).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView2).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep2).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView2).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView3).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep3).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView3).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView4).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView5).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep5).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView5).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView6).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep6).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView6).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView7).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView7).visibility = View.VISIBLE

            findViewById<TextView>(R.id.trumpText2).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.trumpImage2).visibility = View.VISIBLE
        }
        if(nPlayers4){

            findViewById<TextView>(R.id.textView1_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep1_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView1_4).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView2_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep2_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView2_4).visibility = View.VISIBLE

            findViewById<TextView>(R.id.textView3_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.onlinep3_4).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.playerView3_4).visibility = View.VISIBLE

        }



    }

    override fun onStart() {
        super.onStart()
        try{
//            toastCenter("Screen width : ${resources.configuration.screenWidthDp.toString()}  Screen Height : ${resources.configuration.screenHeightDp.toString()}")
            write("OL/$from",1)
            getCardsAndDisplay(display = false)
//            animateElements()

// region table card listener
        cardsOnTableListener1 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                ct1 = p0.value.toString().toInt()
                tablePointsCalculator()
                if(ct1 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[0]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[0]))
                    findViewById<ImageView>(refIDMappedTableImageView[0]).setImageResource(cardsDrawableDoubleDeck[ct1])
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
                tablePointsCalculator()
                if(ct2 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[1]))
                    findViewById<ImageView>(refIDMappedTableImageView[1]).setImageResource(cardsDrawableDoubleDeck[ct2])
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
                tablePointsCalculator()
                if(ct3 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[2]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[2]))
                    findViewById<ImageView>(refIDMappedTableImageView[2]).setImageResource(cardsDrawableDoubleDeck[ct3])
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
                tablePointsCalculator()
                if(ct4 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[3]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[3]))
                    findViewById<ImageView>(refIDMappedTableImageView[3]).setImageResource(cardsDrawableDoubleDeck[ct4])
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
                tablePointsCalculator()
                if(ct5 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[4]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[4]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[4]))
                    findViewById<ImageView>(refIDMappedTableImageView[4]).setImageResource(cardsDrawableDoubleDeck[ct5])
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
                tablePointsCalculator()
                if(ct6 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[5]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[5]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[5]))
                    findViewById<ImageView>(refIDMappedTableImageView[5]).setImageResource(cardsDrawableDoubleDeck[ct6])
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
                tablePointsCalculator()
                if(ct7 <=97){
                    soundCardPlayed.start()
                    findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.VISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[6]).startAnimation(AnimationUtils.loadAnimation(applicationContext,refIDMappedTableAnim[6]))
                    findViewById<ImageView>(refIDMappedTableImageView[6]).setImageResource(cardsDrawableDoubleDeck[ct7])
                }else{
                    findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.INVISIBLE
                    findViewById<ImageView>(refIDMappedTableImageView[6]).clearAnimation()
                }
            }
        }
// endregion
// region Chat Listener
            chatRegistration = refRoomFirestore.document(roomID+"_chat").addSnapshotListener{ dataSnapshot, error ->
                if (dataSnapshot != null && dataSnapshot.exists() && error == null) {
                    val data = (dataSnapshot.data as Map<String, String>)["M"].toString()
                    if(data.isNotEmpty() && lastChat != data) { // if chat is not empty
                        soundChat.start()
                        findViewById<TextView>(R.id.textViewChatDisplay).text = findViewById<TextView>(R.id.textViewChatDisplay).text.toString() + "\n$emojiGuard " + data
                        findViewById<TextView>(R.id.textViewChatDisplay).requestLayout()
                        lastChat = data
                        if (findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility != View.VISIBLE){
                            counterChat += 1 // increase counter by 1 is chat display is off
                            findViewById<TextView>(R.id.textViewChatNo).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.textViewChatNo).text = "$counterChat $emojiMessage"
//                            findViewById<TextView>(R.id.textViewChatNo).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
                        }
                    }
                }else if (dataSnapshot != null && !dataSnapshot.exists()) { // dummy
//                    soundError.start()
//                    toastCenter("Sorry $selfName \n$p1 has left the room. \nYou can create your own room or join other")
                } else if(error != null){
                    toastCenter(error.localizedMessage!!.toString()) // dummy
                }
            }
        // endregion
//region Online Status Listener
        onlineStatusListener1 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
            if(onlineP1 != data.value.toString().toInt()){
                onlineP1 = data.value.toString().toInt()
                if(onlineP1==0 && from!="p1") {
                    toastCenter("${playerName(1)} is Offline !")
                    findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_offline)
                }
                if(onlineP1==1 && from!="p1") {
                    toastCenter("${playerName(1)} is Online !")
                    findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_online)
                }
                if(onlineP1==2 && from!="p1") {
                    toastCenter("${playerName(1)} has left the room !")
                    findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_offline)
                }}
            }
        }
        onlineStatusListener2 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP2 != data.value.toString().toInt()){
                    onlineP2 = data.value.toString().toInt()
                    if(onlineP2==0 && from!="p2"){
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(2)} is Offline !")
                    }
                    if(onlineP2==1 && from!="p2") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(2)} is Online !")
                    }
                    if(onlineP2==2 && from!="p2") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(2)} has left the room !")
                    }}
            }
        }
        onlineStatusListener3 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP3 != data.value.toString().toInt()){
                    onlineP3 = data.value.toString().toInt()
                    if(onlineP3==0 && from!="p3") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(3)} is Offline !")
                    }
                    if(onlineP3==1 && from!="p3"){
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(3)} is Online !")
                    }
                    if(onlineP3==2 && from!="p3") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(3)} has left the room !")
                    }}
            }
        }
        onlineStatusListener4 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP4 != data.value.toString().toInt()){
                    onlineP4 = data.value.toString().toInt()
                    if(onlineP4==0 && from!="p4") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(4)} is Offline !")
                    }
                    if(onlineP4==1 && from!="p4") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(4)} is Online !")
                    }
                    if(onlineP4==2 && from!="p4") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(4)} has left the room !")
                    }}
            }
        }
        onlineStatusListener5 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP5 != data.value.toString().toInt()){
                    onlineP5 = data.value.toString().toInt()
                    if(onlineP5==0 && from!="p5"){
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(5)} is Offline !")
                    }
                    if(onlineP5==1 && from!="p5") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(5)} is Online !")
                    }
                    if(onlineP5==2 && from!="p5") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(5)} has left the room !")
                    }}
            }
        }
        onlineStatusListener6 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP6 != data.value.toString().toInt()){
                    onlineP6 = data.value.toString().toInt()
                    if(onlineP6==0 && from!="p6") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(6)} is Offline !")
                    }
                    if(onlineP6==1 && from!="p6") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(6)} is Online !")
                    }
                    if(onlineP6==2 && from!="p6"){
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(6)} has left the room !")
                    }}
            }
        }
        onlineStatusListener7 = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if(onlineP7 != data.value.toString().toInt()){
                    onlineP7 = data.value.toString().toInt()
                    if(onlineP7==0 && from!="p7") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(7)} is Offline !")
                    }
                    if(onlineP7==1 && from!="p7") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_online)
                        toastCenter("${playerName(7)} is Online !")
                    }
                    if(onlineP7==2 && from!="p7") {
                        findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_offline)
                        toastCenter("${playerName(7)} has left the room !")
                    }}
            }
        }
        // endregion
// region Game State Listener
        gameStateListener   = object :ValueEventListener{override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(GameState: DataSnapshot) {
                gameState = (GameState.value as Long).toInt()
                if (gameState == 1) {
                    getCardsAndDisplay(display = false)
                    animateElements()
                    resetVariables()
                   findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
                    updatePlayerNames()
                    shufflingWindow(gameStateChange = true) // gameStateChange = change game state to 2 after shuffling
                }
                if (gameState == 2) { // bidding moved to state 1 after shuffling and displaying own cards
                    soundUpdate.start()
//                    getCardsAndDisplay(animation = true)
//                    displaySelfCards(animations = true)
//                    startBidding()
                }
                if (gameState == 3) {
                soundSuccess.start()
                refGameData.child("Bid").removeEventListener(bidingTurnListener)
                bidingStarted = false
                finishBackgroundAnimationBidding() // also highlight bidder winner & removed automatically at game state 5
                startTrumpSelection()
            }
                if (gameState == 4) {
                getCardsAndDisplay() // dummy - check if really required to read cards again from firebase - looks like waste
                soundSuccess.start()
                getTrumpStartPartnerSelection()
            }
                if (gameState == 5) {
                    newGameStatus = true
                getBuddyandDisplay()
                if(!roundStarted){
                    soundSuccess.start()
                    updatePlayerScoreInfo(ptAll)
                    getCardsAndDisplay(animation = true)
                    Handler().postDelayed( { startPlayingRound() },3000)
                    if("p$playerTurn"!=from)  centralText("${playerName(bidder)} will play first \n You get ${(timeCountdownPlayCard/1000).toInt()} seconds to play card")
                    if("p$playerTurn"==from) centralText("${playerName(bidder)}, \n You will have ${(timeCountdownPlayCard/1000).toInt()} seconds to play card")
                }else{
                    getCardsAndDisplay()
                    startPlayingRound()
                }
            }
                if (gameState == 6) {
                    gameMode6()
            }
        }}
// endregion
// region Round Number Listener - dummy - check if can be merged with RO - round data
        roundNumberListener = object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {    }
            override fun onDataChange(p0: DataSnapshot) {
                roundNumber = p0.value.toString().toInt()
    }
}
        // endregion
// region Individual Points listener - dummy - check if need all 7 or just single and merge all
        pointsListener1 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt1!=p0.value.toString().toInt()){
                    pt1 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener2 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt2!=p0.value.toString().toInt()){
                    pt2 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener3 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt3!=p0.value.toString().toInt()){
                    pt3 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener4 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt4!=p0.value.toString().toInt()){
                    pt4 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener5 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt5!=p0.value.toString().toInt()){
                    pt5 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener6 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt6!=p0.value.toString().toInt()){
                    pt6 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }
        pointsListener7 = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(pt7!=p0.value.toString().toInt()){
                    pt7 = p0.value.toString().toInt()
                    ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                    if(gameState==5) updatePlayerScoreInfo(ptAll)
                }
            }
        }

//        endregion
//            region Partner listener 1
            partnerListener1 = object : ValueEventListener {
                override fun onCancelled(errorDataLoad: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.value != null) {
                        if (buFound1 != data.child("s1").value.toString()
                                .toInt() && buFound1 != 1
                        ) {
                            soundSuccess.start()
                            if (vibrateStatus) vibrationStart()
                        }
                        buPlayer1 = data.child("b1").value.toString().toInt()
                        buFound1 = data.child("s1").value.toString().toInt()
                        if (buPlayer1 != 0 && buFound1 == 1) findViewById<TextView>(R.id.trumpText1).text =
                            playerName(buPlayer1)
                        displayPartnerIcon()
                    }
                }
            }
//            endregion
//            region Partner listener 2
            partnerListener2 = object : ValueEventListener {
                override fun onCancelled(errorDataLoad: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.value != null) {
                        if (buFound2 != data.child("s2").value.toString()
                                .toInt() && buFound2 != 1
                        ) soundSuccess.start()
                        buPlayer2 = data.child("b2").value.toString().toInt()
                        buFound2 = data.child("s2").value.toString().toInt()
                        if (buPlayer2 != 0 && buFound2 == 1) findViewById<TextView>(R.id.trumpText2).text =
                            playerName(buPlayer2)
                        displayPartnerIcon()
                    }
                }
            }
//            endregion
findViewById<EditText>(R.id.editTextChatInput).setOnEditorActionListener { v, actionId, _ ->
    return@setOnEditorActionListener when (actionId) {
        EditorInfo.IME_ACTION_SEND -> {
            sendChat(v)
            false
        }
        else -> false
    }
}  // close keyboard after sending chat
//        region Attach Listener
//            refGameData.child("M").addValueEventListener(chatListener) //attach chat listener
            refGameData.child("BU1").addValueEventListener(partnerListener1)
            refGameData.child("BU2").addValueEventListener(partnerListener2)
            refGameData.child("SC/p1").addValueEventListener(pointsListener1)
            refGameData.child("SC/p2").addValueEventListener(pointsListener2)
            refGameData.child("SC/p3").addValueEventListener(pointsListener3)
            refGameData.child("SC/p4").addValueEventListener(pointsListener4)
            refGameData.child("SC/p5").addValueEventListener(pointsListener5)
            refGameData.child("SC/p6").addValueEventListener(pointsListener6)
            refGameData.child("SC/p7").addValueEventListener(pointsListener7)
            refGameData.child("R").addValueEventListener(roundNumberListener)
            refGameData.child("GS").addValueEventListener(gameStateListener) // attach the created game data listener        refGameData.child("M").addValueEventListener(chatListener) //attach chat listener
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
        countDownPlayCard = object: CountDownTimer(timeCountdownPlayCard,100){
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished*100/timeCountdownPlayCard).toInt()
                findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished/1000).toDouble() +1).toInt().toString() + "s"
            }
            override fun onFinish() {
                autoPlayCard()
                soundTimerFinish.start()
                if(vibrateStatus) vibrationStart()
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                findViewById<TextView>(R.id.textViewTimer).clearAnimation()
            }
        }
//        endregion
        // region       Countdown Biding
        countDownBidding = object: CountDownTimer(timeCountdownBid,100){
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished*100/timeCountdownBid).toInt()
                findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished/1000).toDouble()).toInt().toString() + "s"
            }
            override fun onFinish() {
                if(vibrateStatus) vibrationStart()
                soundTimerFinish.start()
                write("Bid/BS/p$playerTurn",0) // pass the bid if times up
                write("Bid/BT",nextTurn(playerTurn))
                findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.textViewTimer).visibility = View.INVISIBLE
                findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
                findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
                bidButtonsAnimation("clear")
                centralText("    Time's Up !!  \n You cannot bid anymore",2500)
            }
        }
//        endregion

    }catch (error: Exception){
            if(vibrateStatus) vibrationStart()
            toastCenter(error.toString()) // dummy
    }
    }

    private fun gameMode6() {
        findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.GONE
        countDownTimer("PlayCard",purpose = "cancel")
        if(vibrateStatus) vibrationStart()
        soundShuffle.start()
        displayShufflingCards()
        scoreOpenStatus = true
//        if(scoreSheetNotUpdated) {
//            scoreBoardTable(display = false,data = createScoreTableHeader(), upDateHeader = true)
//            scoreBoardTable(display = false,data = createScoreTableTotal(), upDateTotal = true)
//        }
//        scoreSheetNotUpdated = false
        refGameData.child("S").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(scoreList != p0.value as List<Int> || newGameStatus){// dummy - newGameStatus not needed as Scorelist has game index which is unique
                    newGameStatus = false
                    scoreList  = p0.value as List<Int>
                    updateWholeScoreBoard()
                    gameNumber += 1
                }
            }
        })
        Handler().postDelayed({
            if(getString(R.string.test).contains('n')) {
                if (!premiumStatus && mInterstitialAd.isLoaded) mInterstitialAd.show()
            }
            if(from == "p1") {
                findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.inActiveCard))
                findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.VISIBLE
                findViewById<AppCompatButton>(R.id.startNextRoundButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            }
        },3000)    }
    private fun updateWholeScoreBoard(){
        p1Gain += scoreList[1].toString().toInt()
        p2Gain += scoreList[2].toString().toInt()
        p3Gain += scoreList[3].toString().toInt()
        p4Gain += scoreList[4].toString().toInt()

        p1Coins += scoreList[1].toString().toInt()
        p2Coins += scoreList[2].toString().toInt()
        p3Coins += scoreList[3].toString().toInt()
        p4Coins += scoreList[4].toString().toInt()

        if(nPlayers7) {
            p5Gain += scoreList[5].toString().toInt()
            p6Gain += scoreList[6].toString().toInt()
            p7Gain += scoreList[7].toString().toInt()

            p5Coins += scoreList[5].toString().toInt()
            p6Coins += scoreList[6].toString().toInt()
            p7Coins += scoreList[7].toString().toInt()
        }
        scoreBoardTable(display = false,data = createScoreTableHeader(), upDateHeader = true)
        scoreBoardTable(display = false,data = createScoreTableTotal(), upDateTotal = true)
        scoreBoardTable(data = scoreList)

        val uid = FirebaseAuth.getInstance().uid.toString()
        refUsersData.document(uid).set(hashMapOf("sc" to playerCoins(from) ), SetOptions.merge())
    }
    private fun createScoreTableHeader(): List<String>{

        return if(nPlayers ==7) listOf("Player\n$emoji",
            p1 + "\n$emoji${String.format("%,d", p1Coins)}",
            p2 + "\n$emoji${String.format("%,d", p2Coins)}",
            p3 + "\n$emoji${String.format("%,d", p3Coins)}",
            p4 + "\n$emoji${String.format("%,d", p4Coins)}",
            p5 + "\n$emoji${String.format("%,d", p5Coins)}",
            p6 + "\n$emoji${String.format("%,d", p6Coins)}",
            p7 + "\n$emoji${String.format("%,d", p7Coins)}")

        else listOf("Player\n$emoji",
            p1 + "\n$emoji${String.format("%,d", p1Coins)}",
            p2 + "\n$emoji${String.format("%,d", p2Coins)}",
            p3 + "\n$emoji${String.format("%,d", p3Coins)}",
            p4 + "\n$emoji${String.format("%,d", p4Coins)}")
    }
    private fun createScoreTableTotal(): List<Any> {
        return if(nPlayers ==7) listOf("Gain/Loss", p1Gain , p2Gain , p3Gain , p4Gain , p5Gain , p6Gain , p7Gain )
        else listOf("Gain/Loss", p1Gain , p2Gain , p3Gain , p4Gain )
    }
    private fun scoreBoardTable(data: List<Any>,display: Boolean = true, upDateHeader: Boolean = false, upDateTotal:Boolean = false) {
        if(display) {
            scoreOpenStatus = true
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.zoomin_scoretable_open))
        }
        val inflater = LayoutInflater.from(applicationContext)
        val viewTemp = when {
            upDateHeader -> inflater.inflate(R.layout.score_board_table_7, findViewById<LinearLayout>(R.id.imageGalleryScoreName), false)
            upDateTotal -> inflater.inflate(R.layout.score_board_table_7, findViewById<LinearLayout>(R.id.imageGalleryScoreTotal), false)
            else -> inflater.inflate(R.layout.score_board_table_7, findViewById<LinearLayout>(R.id.imageGalleryScore), false)
        }
        for(i in 0..nPlayers){
            viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).text = data[i].toString()
            if(!upDateHeader)  viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTextSize(TypedValue.COMPLEX_UNIT_PX,resources.getDimension(R.dimen._12ssp))
            if(i>0 && !upDateHeader && data[i].toString().toInt() <0){
//                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTextColor(ContextCompat.getColor(applicationContext, R.color.Red))
            }else if(i>0 && !upDateHeader){
//                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTextColor(ContextCompat.getColor(applicationContext, R.color.borderblueDark1g))
            }
        }
        when {
            upDateHeader -> {
                findViewById<LinearLayout>(R.id.imageGalleryScoreName).removeAllViews()
                findViewById<LinearLayout>(R.id.imageGalleryScoreName).addView(viewTemp)
            }
            upDateTotal -> {
                findViewById<LinearLayout>(R.id.imageGalleryScoreTotal).removeAllViews()
                findViewById<LinearLayout>(R.id.imageGalleryScoreTotal).addView(viewTemp)
            }
            else -> findViewById<LinearLayout>(R.id.imageGalleryScore).addView(viewTemp)
        }
    }
    fun closeChatScoreWindow (view: View){
        findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.GONE
        findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
        findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
    }
    private fun playerCoins(p: String): Int {
        var coins = 0
        when(p){
            "p1"-> coins = p1Coins
            "p2"-> coins = p2Coins
            "p3"-> coins = p3Coins
            "p4"-> coins = p4Coins
            "p5"-> coins = p5Coins
            "p6"-> coins = p6Coins
            "p7"-> coins = p7Coins
        }
        return coins
    }

    private fun resetVariables(){
        findViewById<ImageView>(R.id.trumpImage1).setImageResource(R.drawable.ic_back_side_red)
        findViewById<ImageView>(R.id.trumpImage2).setImageResource(R.drawable.ic_back_side_blue)
        findViewById<GifImageView>(R.id.trumpImage).setImageResource(R.drawable.trump1)
        findViewById<TextView>(R.id.textViewBidValue).text = "$emojiScore ${getString(R.string.bidValue1)}"
        findViewById<TextView>(R.id.textViewBider).text = getString(R.string.Bider)
        findViewById<TextView>(R.id.trumpText2).text = getString(R.string.partner2)
        findViewById<TextView>(R.id.trumpText1).text = getString(R.string.partner1)
        findViewById<TextView>(R.id.trumpText).text = getString(R.string.Trump)
        for (i in 0..6) { // first reset background and animation of all partner icon
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
        }
        bu1 = 0
        bu1Flag = 0
        bu2 = 0
        bu2Flag = 0
        buPlayer1 = 0
        buFound1 = 0
        buPlayer2 = 0
        buFound2 = 0
        bidValue = 0
        bidTeamScore = 0
        bidder = 0
        bidingStarted = false   /// biding happened before
        counterPartnerSelection = 0
        ct1= 99
        ct2= 99
        ct3= 99
        ct4= 99
        ct5= 99
        ct6= 99
        ct7= 99
        gameTurn = 0
        played = false
        playerTurn = 0
        pt1 = 0
        pt2 = 0
        pt3 = 0
        pt4 = 0
        pt5 = 0
        pt6 = 0
        pt7 = 0
        ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
        roundStarted = false
        roundNumber = 0
        roundWinner = 0
        scoreList = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
        tablePoints = 0
    }
    fun startNextGame(view: View) {
        findViewById<AppCompatButton>(R.id.startNextRoundButton).clearAnimation()
        findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.INVISIBLE

        val cardsShuffled =  (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
        val playerTurn = Random.nextInt(1, 7)
        write("CT" , mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99))
        write("CH" , mutableMapOf<String ,List<Int>>("p1" to cardsShuffled.slice(0..13).sortedBy {it},"p2" to cardsShuffled.slice(14..27).sortedBy {it}
        ,"p3" to cardsShuffled.slice(28..41).sortedBy {it},"p4" to cardsShuffled.slice(42..55).sortedBy {it},"p5" to cardsShuffled.slice(56..69).sortedBy {it}
        ,"p6" to cardsShuffled.slice(70..83).sortedBy {it},"p7" to cardsShuffled.slice(84..97).sortedBy {it}))
        write("Bid" , mutableMapOf("BV" to 350, "BT" to 1,"BB" to 3,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1,"p5" to 1,"p6" to 1,"p7" to 1)))
        write("SC" , mutableMapOf("p1" to 0,"p2" to 0,"p3" to 0,"p4" to 0,"p5" to 0,"p6" to 0,"p7" to 0))
        write( "Tr" , "")
        write(   "R" , 1)
        write("RO" , mutableMapOf("T" to 1,"P" to 0,"R" to ""))
        write("BU2" , mutableMapOf("b2" to 8,"s2" to 0))
        write( "BU1" ,mutableMapOf("b1" to 8,"s1" to 0))
        write("BU" , mutableMapOf("b1" to "","b1s" to "","b2" to "","b2s" to ""))
        write("GS" , 1)
    }
    private fun tablePointsCalculator(){
        tablePoints = cardPointsDoubleDeck[ct1] + cardPointsDoubleDeck[ct2] +cardPointsDoubleDeck[ct3] +
                cardPointsDoubleDeck[ct4] + cardPointsDoubleDeck[ct5] + cardPointsDoubleDeck[ct6] + cardPointsDoubleDeck[ct7]
        if(tablePoints > 0 && gameState == 5){
            textViewCenterPoints.text = tablePoints.toString()
            textViewCenterPoints.visibility = View.VISIBLE
//            textViewCenterPoints.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        }
        else{
//            textViewCenterPoints.clearAnimation()
            textViewCenterPoints.visibility = View.GONE
        }
    }
    private fun updatePlayerScoreInfo(pointsList: List<Int>) {
        if(gameState==5){
            var t1 = 0
            var t2 = 0
            if(buFound1!=0)                          t1 = pointsList[buPlayer1-1]
            if(buFound2!=0 && buPlayer1!=buPlayer2)  t2 = pointsList[buPlayer2-1] // if not same partners then only add other points
            bidTeamScore   = pointsList[bidder-1] + t1 + t2

            for(i in 0..6) {
                val j = i+1
                if(j == bidder || (j == buPlayer1 && buFound1!=0) || (j == buPlayer2 && buFound2!=0))
                    findViewById<TextView>(refIDMappedTextView[i]).text = playerName(j) + "\n$emojiScore  $bidTeamScore /$bidValue"
                else findViewById<TextView>(refIDMappedTextView[i]).text = playerName(j) + "\n$emojiScore  ${pointsList.sum()-bidTeamScore} /${705 - bidValue}"
            }
            var tt1 = 0
            var tt2 = 0
            if(buFound1==1)                         tt1 = pointsList[buPlayer1-1]
            if(buFound2==1 && buPlayer1!=buPlayer2) tt2 = pointsList[buPlayer2-1] // if not same partners then only add other player points
            val bidTeamScoreFinal   = pointsList[bidder-1] + tt1 + tt2 // total score of bid team
            if(bidTeamScoreFinal >= bidValue) { // bidder team won case
                refGameData.child("RO").removeEventListener(roundListener)
                clearAllAnimation()
                if(vibrateStatus) vibrationStart()
                toastCenter("Game Over: Bidder team Won \n         Defender team Lost")
                centralText("Game Over: Bidder team Won \n         Defender team Lost")
                if ("p$bidder" == from) {// bidder will change game state to 6
                    val pointsListTemp = mutableListOf<Int>(gameNumber,-bidValue,-bidValue,-bidValue,-bidValue,-bidValue,-bidValue,-bidValue)
                    if(buFound1!= 1 && buFound2!=1 ){ //No partners found so far
                        pointsListTemp[bidder] = bidValue*6
                    }
                    else if(buFound1==1 && buFound2 != 1){ // only partner 1 found
                        pointsListTemp[bidder] = bidValue*3
                        pointsListTemp[buPlayer1] = bidValue*2
                    }
                    else if(buFound1!=1 && buFound2 == 1){ // only partner 2 found
                        pointsListTemp[bidder] = bidValue*3
                        pointsListTemp[buPlayer2] = bidValue*2
                    }
                    else if(buPlayer1==buPlayer2 && buPlayer1==1){ //both partners found and they are same person
                        pointsListTemp[bidder] = bidValue*3
                        pointsListTemp[buPlayer1] = bidValue*2
                    }
                    else if(buPlayer1!= buPlayer2 && buFound1==1 && buFound2==1){//both partners found and they are different person
                        pointsListTemp[bidder] = bidValue*2
                        pointsListTemp[buPlayer1] = bidValue
                        pointsListTemp[buPlayer2] = bidValue
                    }
                    write("S",pointsListTemp) // 0-bidder won, 1 - defenders won??
                    write("GS", 6)// dummy - check if need success listner from above write to handle sync issues
                }
            }
            else if (buFound1==1 && buFound2==1 && (pointsList.sum()-bidTeamScore) >= (705 - bidValue) ){ // if opponent score has reached target value & both partners are disclosed
                refGameData.child("RO").removeEventListener(roundListener)
                clearAllAnimation()
                if(vibrateStatus) vibrationStart()
                toastCenter("Game Over: Defender team Won \n         Bidder team Lost")
                centralText("Game Over: Defender team Won \n         Bidder team Lost")
                if ("p$bidder" == from) {// winner will change game state to 6
                    val pointsListTemp = mutableListOf<Int>(gameNumber,bidValue,bidValue,bidValue,bidValue,bidValue,bidValue,bidValue)
                    if(buPlayer1==buPlayer2){ // either both partners are same person
                        pointsListTemp[bidder] = -1*bidValue*2
                        pointsListTemp[buPlayer1] = -bidValue
                    }
                    else{                      // both partners are different person
                        pointsListTemp[bidder] = -1*bidValue*2
                        pointsListTemp[buPlayer1] = -bidValue
                        pointsListTemp[buPlayer2] = -bidValue
                    }
                    write("S",pointsListTemp) // 0-bidder won, 1 - defenders won
                    write("GS", 6)
                }
            }
        }
    }

    private fun displayPartnerIcon() {
        for (i in 0..6) { // first reset background and animation of partner icon
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
        }
        if(bidder!=0) { // show single person icon next to bidder
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder-1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder-1]).setImageResource(R.drawable.biddericon)
        }

        if(buFound1!=0 && buPlayer1 !=8 ) {
            if(vibrateStatus) vibrationStart()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1-1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1-1])
                .startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_big_fast))
        }
        if(buFound2!=0 && buPlayer2 !=8 ) {
            if(vibrateStatus) vibrationStart()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer2-1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer2-1])
                .startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_big_fast))
        }
        if(gameState==5) updatePlayerScoreInfo(ptAll)
    }
    private fun countDownTimer(task: String, purpose: String = "start") {
        if(purpose=="start"){
//            timeCountdown = time
            findViewById<ProgressBar>(R.id.progressbarTimer).progress = 100
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).text = "10s"
            findViewById<ProgressBar>(R.id.progressbarTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            findViewById<TextView>(R.id.textViewTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            if (task=="Bidding") countDownBidding.start()
            if (task=="PlayCard") countDownPlayCard.start()
        }else if(purpose== "cancel"){
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
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
                    if(gameTurn==1) played = false
                    if(gameTurn!=8 && gameTurn != 0){
                        animatePlayerPlayingRound(playerTurn)
                        if("p$playerTurn"==from && !played){
                            displaySelfCards(filter = true)
                            countDownTimer(task = "PlayCard") // start countdown timer and run autoPlayCard
                            if(vibrateStatus) vibrationStart()
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
                startNextTurn(cardSelected.toLong()) // allow throw if first chance, or same suit as first turn or doesn't have same suit card
            }
                else{
                soundError.start()
                if(vibrateStatus) vibrationStart()
                toastCenter("${playerName(playerTurn)}, please play ${getSuitName(trumpStart)} card")
            }
        }
    }
    private fun startNextTurn(cardSelected: Any){
        if(!played){
            played = true
            if(playerTurn != bidder) checkIfPartnerandUpdateServer(cardSelected, playerTurn)
            refGameData.child("CT/$from").setValue(cardSelected).addOnSuccessListener{
//                write("RO/T",gameTurn+1)
                if(gameTurn==1) write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to cardSuitDoubleDeck[cardSelected.toString().toInt()] ))
               else write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to trumpStart))
            }.addOnFailureListener{ // try again  - dummy - check if any other way could be done
                refGameData.child("CT/$from").setValue(cardSelected).addOnSuccessListener{
//                write("RO/T",gameTurn+1)
                    if(gameTurn==1) write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to cardSuitDoubleDeck[cardSelected.toString().toInt()] ))
                    else write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to trumpStart))
                }
            }
            cardsInHand.remove(cardSelected)
            if (roundNumber !=14) {
                write("CH/$from",cardsInHand)
                getCardsAndDisplay()
            }
            else{
                write("CH/$from","")
                findViewById<LinearLayout>(R.id.imageGallery).removeAllViews() // show no self cards
            }
        }
    }
    private fun checkIfPartnerandUpdateServer(cardSelected: Any, playerTurn: Int?){
        if((cardSelected.toString().toInt() == bu1*2 || cardSelected.toString().toInt() == bu1*2+1 ) && buFound1 != 1){
            if (playerTurn != null) {
                write("BU1/b1", playerTurn)
            }
            if(bu1Flag >= 1) { // 1 or 2 --> bidder has either one card or has asked both cards --> lock the partner
              write("BU1/s1",1)  // locking the partner by 1 else it would be 2
          }else if(bu1Flag == 0){
              if(buFound1 == 2) write("BU1/s1",1)  // locking the partner by 1
              if(buFound1 == 0) write("BU1/s1",2)  // 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
          }
        } else if((cardSelected.toString().toInt() == bu2*2 || cardSelected.toString().toInt() == bu2*2+1 ) && buFound2 != 1){
            if (playerTurn != null) { // null surround check
                write("BU2/b2", playerTurn)
            }
            if(bu2Flag >= 1) { // 1 or 2 --> bidder has either one card or has asked both cards --> lock the partner
                write("BU2/s2",1)  // locking the partner by 1 else it would be 2
            }else if(bu2Flag == 0){
                if(buFound2 == 2) write("BU2/s2",1)  // locking the partner by 1 if previously was 2
                if(buFound2 == 0) write("BU2/s2",2)  // 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
            }
        }
    }
    private fun declareRoundWinner(){
        val roundCards =  listOf(ct1, ct2, ct3, ct4, ct5, ct6, ct7)
        var winnerCard = roundCards[playerTurn-1]
        var startTurn = playerTurn
        var points = 0
        for(k in 0..6){
            points += cardPointsDoubleDeck[roundCards[k]]
        }
        for(i in 1..6){
            startTurn = nextTurn(startTurn)
            winnerCard = compareCardsforWinner(roundCards[startTurn -1], winnerCard)
        }
        roundWinner = roundCards.indexOf(winnerCard) + 1
        animatePlayerPlayingRound(roundWinner)
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_big_fast))
        Handler().postDelayed( { animateWinner() },300)
            Handler().postDelayed( {
                if(roundNumber < 14){
                    if("p$roundWinner" == from){// only winner can start next round
                        startNextRound()
                    }else{
                        centralText("Waiting for ${playerName(roundWinner)} to play next card",3500)
                    }
                }else if(roundNumber == 14){
                    try{
                        refGameData.child("RO").removeEventListener(roundListener)
                    }finally {
                        clearAllAnimation()
                    }
                    if("p$roundWinner" == from){// winner will change game state to 6
//                        write("GS",6)
                        endGameRound() // update points of last round to server by winner
                    }
                }
            },1500)
    }
    private fun endGameRound(){
//        if(buFound1 == 2) write("BU1/s1",1)  // locking the partner by 1
//        if(buFound2 == 2) write("BU2/s2",1)  // locking the partner by 1
        write("CT", mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99))
        write("SC/p$roundWinner",tablePoints + ptAll[roundWinner-1])
    }
    private fun startNextRound(){
        if(buFound1 == 2) write("BU1/s1",1)  // locking the partner by 1
        if(buFound2 == 2) write("BU2/s2",1)  // locking the partner by 1
        write("CT", mutableMapOf("p1" to 99,"p2" to 99,"p3" to 99,"p4" to 99,"p5" to 99,"p6" to 99,"p7" to 99))
        write("SC/p$roundWinner",tablePoints + ptAll[roundWinner-1]) // add table points to the round winner player
        write("RO/P",roundWinner) // write next player turn to be round player turn
        write("RO/R","")
        write("RO/T",1)
        write("R",roundNumber+1)
        centralText("Please play your next card",2000)
    }


    private fun compareCardsforWinner(currentCard: Int, winnerCard: Int): Int{
        var w = winnerCard
        val wSuit = cardSuitDoubleDeck[winnerCard]
        val cSuit = cardSuitDoubleDeck[currentCard]
        if( (cSuit == trump && wSuit != trump) || ((cSuit == wSuit) && ((currentCard - winnerCard ) >1 || (currentCard%2==1 && winnerCard==currentCard-1)  || (winnerCard%2==1 && currentCard==winnerCard-1))))
            w = currentCard
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
    @SuppressLint("NewApi")
    private fun displaySelfCards(view: View = View(applicationContext), animations: Boolean = false, filter: Boolean = false, bidingRequest: Boolean = false) {
        findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        gallery.visibility= View.VISIBLE
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Long in cardsInHand) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list, gallery, false)
            if(x== cardsInHand[cardsInHand.size-1]){
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setPaddingRelative(0,0,0,0)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).layoutParams.width = resources.getDimensionPixelSize(R.dimen.widthDisplayCardLast)
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setImageResource(cardsDrawableDoubleDeck[x.toInt()])
            if(filter && gameTurn>1 && cardSuitDoubleDeck[x.toInt()] != trumpStart  && cardSuitDoubleDeck.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) != -1){
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.inActiveCard))
            }else if(filter && gameTurn>1 && cardSuitDoubleDeck.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) != -1) {
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_active_cards))
                }

            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).tag = x.toString() // tag the card number to the image
            if (cardPointsDoubleDeck.elementAt(x.toInt()) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).text =
                    "${cardPointsDoubleDeck.elementAt(x.toInt())}"
                if (animations) viewTemp.findViewById<TextView>(R.id.textViewDisplayCard)
                    .startAnimation(
                        AnimationUtils
                            .loadAnimation(applicationContext, R.anim.blink_and_scale)
                    )
                if (animations) viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                    .startAnimation(
                        AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw_self_cards)
                    )
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).visibility = View.GONE
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setOnClickListener(View.OnClickListener {
                validateSelfPlayedCard(it)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
            })
            gallery.addView(viewTemp)
        }
        if (animations) {
            findViewById<RelativeLayout>(R.id.horizontalScrollView).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
        }
        if(bidingRequest) startBidding()
    }
    private fun animatePlayerPlayingRound(index: Int){
        findViewById<ImageView>(refIDMappedImageView[index-1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
        findViewById<ImageView>(refIDMappedImageView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
        if("p$index"==from){
//        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
//            if(gameTurn==1) findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_active_cards))
//            if(gameTurn==1) findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
        }
    }
    private fun clearAllAnimation(){
        for (i in 0..6) { // first reset background and animation
            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            findViewById<ImageView>(refIDMappedImageView[i]).clearAnimation()
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

        Handler().postDelayed( {
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
            if(vibrateStatus) vibrationStart()
            findViewById<ImageView>(R.id.trumpImage1).setImageResource(cardsDrawableSingleDeck[bu1])
            findViewById<ImageView>(R.id.trumpImage2).setImageResource(cardsDrawableSingleDeck[bu2])
            findViewById<ImageView>(R.id.trumpImage1).clearAnimation()
            findViewById<ImageView>(R.id.trumpImage2).clearAnimation()
            findViewById<TextView>(R.id.trumpText1).clearAnimation()
            findViewById<TextView>(R.id.trumpText2).clearAnimation()
            if(bu1==bu2){
                findViewById<TextView>(R.id.trumpText1).text = getString(R.string.bothPartner)
                findViewById<TextView>(R.id.trumpText2).text = getString(R.string.bothPartner)
            }else {
                if (bu1Flag == 1) findViewById<TextView>(R.id.trumpText1).text = getString(R.string.onlyPartner)
                if (bu1Flag == 0) findViewById<TextView>(R.id.trumpText1).text = getString(R.string.anyPartner)
                if (bu2Flag == 1) findViewById<TextView>(R.id.trumpText2).text = getString(R.string.onlyPartner)
                if (bu2Flag == 0) findViewById<TextView>(R.id.trumpText2).text = getString(R.string.anyPartner)
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
                if("p$bidder" == from) {  // only to bidder
                    findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE // make selection frame visible
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1)//choose 1st buddy text
                    displayAllCardsForPartnerSelection()  // display all the cards to choose from
                }else{  // to everyone else
                    centralText("Waiting for ${playerName(bidder)} \nto select 2 buddies(partners)", 0)
                }
            } })
    }
    private fun partnerSelectClick(cardSelected: Int){ // assumption is cardsinHand already updated
        if( counterPartnerSelection == 0){
            when {
                (cardsInHand as List<*>).contains((cardSelected*2).toLong()) and (cardsInHand as List<*>).contains((cardSelected*2+1).toLong()) -> {
                    soundError.start()
                    if(vibrateStatus) vibrationStart()
                    toastCenter("$selfName, You already have both of these cards ")
                }
                (cardsInHand as List<*>).contains((cardSelected*2).toLong()) or (cardsInHand as List<*>).contains((cardSelected*2+1).toLong()) -> {
                    soundUpdate.start()
                    write("BU/b1",cardSelected)
                    bu1 = cardSelected
                    bu1Flag = 1
                    findViewById<ImageView>(R.id.trumpImage1).setImageResource(cardsDrawableSingleDeck[cardSelected])
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2)//choose 2nd buddy
                    counterPartnerSelection =1
                }
                else -> {
                    soundUpdate.start()
                    bu1 = cardSelected
                    write("BU/b1",bu1)
                    bu1Flag = 0
                    findViewById<ImageView>(R.id.trumpImage1).setImageResource(cardsDrawableSingleDeck[cardSelected])
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2)//choose 2nd buddy
                    counterPartnerSelection =1
                }
            }
        }else if( counterPartnerSelection == 1){
            if((cardsInHand as List<*>).contains((cardSelected*2).toLong()) and (cardsInHand as List<*>).contains((cardSelected*2+1).toLong())) {
                soundError.start()
                if(vibrateStatus) vibrationStart()
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
                    if(vibrateStatus) vibrationStart()
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
                findViewById<LinearLayout>(R.id.partnerSelectionGallery).clearAnimation()
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
            }
        }

    }
    private fun displayAllCardsForPartnerSelection(view: View = View(applicationContext)){
        findViewById<LinearLayout>(R.id.partnerSelectionGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.partnerSelectionGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in highOrderSortedCards) {
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

            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).setOnClickListener {
                partnerSelectClick(x)
            }
            gallery.addView(viewTemp)
        }
        findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right_selection_cards))

    }
    private fun displayTrumpCard() {
        when (trump) {
            "H" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_hearts
                    )
                )
                findViewById<TextView>(R.id.trumpText).text = "Heart"
            }
            "S" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_spades
                    )
                )
                findViewById<TextView>(R.id.trumpText).text = "Spade"

            }
            "D" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_diamonds
                    )
                )
                findViewById<TextView>(R.id.trumpText).text = "Diamond"

            }
            "C" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_clubs
                    )
                )
                findViewById<TextView>(R.id.trumpText).text = "Club"

            }
        }
        findViewById<GifImageView>(R.id.trumpImage).clearAnimation() // main trump showing view
    } // just displaying trump card

    private fun startTrumpSelection() {
        findViewById<TextView>(R.id.textViewBidValue).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
        findViewById<TextView>(R.id.textViewBider).setTextColor(   ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
        if("p$bidder" != from){     //  show to everyone except bidder
            toastCenter("${playerName(bidder)} won the bid round")
            centralText("Waiting for ${playerName(bidder)} \n to choose Trump", 0)
             }
            else{ // show to bidder only
            centralText("Congratulations ${playerName(bidder)} \n You won the bid round", 0)

            findViewById<FrameLayout>(R.id.frameTrumpSelection).visibility= View.VISIBLE
            trumpAnimation("start")
            if(vibrateStatus) vibrationStart()
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
            findViewById<ImageView>(R.id.imageViewTrumpHearts).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpSpades).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpClubs).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))

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
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataLoad: DataSnapshot) {
                    if (playerTurn!=(dataLoad.child("BT").value as Long).toInt()) {
                        bidValue = (dataLoad.child("BV").value as Long).toInt()
                        playerTurn = (dataLoad.child("BT").value as Long).toInt()
                        bidder = (dataLoad.child("BB").value as Long).toInt()
                        val bidStatus = (dataLoad.child("BS/p$playerTurn").value as Long).toInt()
                        if (!bidingStarted) {
                            centralText("${playerName(playerTurn)} will start bidding", 0) //display message only first time
                        }else{
                            centralText("Waiting for ${playerName(playerTurn)} to bid", 0) //display message only first time
                        }
                        soundUpdate.start()
                        findViewById<TextView>(R.id.textViewBidValue).text = "$emojiScore$bidValue" //.toString() //show current bid value
                        findViewById<TextView>(R.id.textViewBider).text = playerName(bidder)
                        findViewById<TextView>(R.id.textViewBidValue).setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                        findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                        findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.INVISIBLE //biding frame invisible
                        resetBackgroundAnimationBidding(dataLoad) //set all background to black or red depending on status
                        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
                        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
                        animatePlayer(playerTurn)  // animate current player
                        if (bidStatus == 1) {  // highlight current player
                            findViewById<ImageView>(refIDMappedImageView[playerTurn - 1]).setBackgroundColor(
                                ContextCompat.getColor(applicationContext, R.color.font_yellow))
                        }
                        if ("p$playerTurn" == from && (bidder != playerTurn || !bidingStarted)) {
                            if (bidStatus == 1) { // show bid frame and ask to bid or pass
                                findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                                findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is ciritical
                                bidButtonsAnimation("start")
                                countDownTimer("Bidding", purpose = "start")
                                if (vibrateStatus) vibrationStart()
                            } else if (bidStatus == 0) {
                                if (vibrateStatus) vibrationStart()
                                toastCenter("   Sorry $selfName \n You cannot bid anymore")
                                write("Bid/BT", nextTurn(playerTurn))
                            }
                        }
                        if ("p$playerTurn" == from && bidder == playerTurn && bidingStarted) { // finish bid and move to next game state
                            write("RO/P", bidder)  // write player turn to bidder
                            write("GS", 3) // change game state to 3 as biding is finished
                            centralText("Congratulations ${playerName(bidder)} \n You won the bid round", 0)
                        }
                    }
                bidingStarted = true
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
               centralText("    Time's Up !!  \n You cannot bid anymore",2500)
            }
            "5"->{
                write("Bid/BV/", bidValue.plus(5))
                write("Bid/BB", playerTurn)
            }
            "10"->{
                write("Bid/BV/", bidValue.plus(10))
                write("Bid/BB", playerTurn)
            }
            "20"->{
                write("Bid/BV/", bidValue.plus(20))
                write("Bid/BB", playerTurn)
            }
            "50"->{
                write("Bid/BV/", bidValue.plus(50))
                write("Bid/BB", playerTurn)
            }
        }
        write("Bid/BT",nextTurn(playerTurn))
        findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
        findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
//        bidButtonsAnimation("clear")
    }
    private fun bidButtonsAnimation(task: String){
        if(task=="start"){
            findViewById<Button>(R.id.bid05button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid10button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid20button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid50button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bidpassbutton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
        }else if (task == "clear"){
            findViewById<Button>(R.id.bid05button).clearAnimation()
            findViewById<Button>(R.id.bid10button).clearAnimation()
            findViewById<Button>(R.id.bid20button).clearAnimation()
            findViewById<Button>(R.id.bid50button).clearAnimation()
            findViewById<Button>(R.id.bidpassbutton).clearAnimation()
        }
  }
    private fun animatePlayer(index: Int){
        findViewById<ImageView>(refIDMappedImageView[index-1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
        findViewById<ImageView>(refIDMappedImageView[index-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
    }
    private fun resetBackgroundAnimationBidding(dataLoad: DataSnapshot) {
        for (i in 0..6) {
            val iPlayer = i+1
            val bidStatus = (dataLoad.child("BS/p$iPlayer").value as Long).toInt()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            if(bidStatus==0){
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer2))
                if("p$iPlayer"==from)
                    findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer2))
            }
            else{
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
                if("p$iPlayer"==from)
                    findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.layoutBackground))
            }
            findViewById<ImageView>(refIDMappedImageView[i]).clearAnimation()
            findViewById<TextView>(refIDMappedTextView[i]).clearAnimation()
            if("p$iPlayer"==from) findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        }
    }
    private fun finishBackgroundAnimationBidding(){  //clear Everything on finish of biding round
        for (i in 0..6) {
            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            findViewById<ImageView>(refIDMappedImageView[i]).clearAnimation()
            findViewById<TextView>(refIDMappedTextView[i]).clearAnimation()
        }
        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.layoutBackground))
        findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        findViewById<ImageView>(refIDMappedImageView[bidder -1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)) // highlight bidder winner
        findViewById<TextView>(R.id.textViewBidValue).clearAnimation()
        findViewById<TextView>(R.id.textViewBider).clearAnimation()
        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder-1]).visibility = View.VISIBLE
        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder-1]).setImageResource(R.drawable.biddericon)
    }

    private fun centralText(message: String, displayTime: Int=3000, cancel: Boolean = false){
        if(cancel){
            findViewById<TextView>(R.id.textViewShuffling).clearAnimation()
            findViewById<TextView>(R.id.textViewShuffling).text = ""
            findViewById<TextView>(R.id.textViewShuffling).visibility = View.GONE
        }else {
            findViewById<TextView>(R.id.textViewShuffling).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewShuffling).text = message
            findViewById<TextView>(R.id.textViewShuffling).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_infinite_700ms))
            if (displayTime != 0)
                Handler().postDelayed({
                    findViewById<TextView>(R.id.textViewShuffling).clearAnimation()
                    findViewById<TextView>(R.id.textViewShuffling).visibility = View.GONE
                }, displayTime.toLong())
        }
    }
    private fun animateElements() {
        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.layoutBackground))
        findViewById<TextView>(R.id.textViewBider).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        findViewById<TextView>(R.id.textViewBidValue).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_700ms))
        findViewById<ImageView>(R.id.trumpImage1).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<ImageView>(R.id.trumpImage2).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
        findViewById<GifImageView>(R.id.trumpImage).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
//        findViewById<Button>(R.id.bid05button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<Button>(R.id.bid10button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<Button>(R.id.bid20button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<Button>(R.id.bid50button).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<Button>(R.id.bidpassbutton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
////        findViewById<GifImageView>(R.id.imageViewChat).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_hanging))
//        findViewById<GifImageView>(R.id.imageViewChat).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<ImageView>(R.id.scoreSheetButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        findViewById<ImageView>(R.id.shine).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide))
//        findViewById<ImageView>(R.id.gifViewGameScreen).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_slow_green_table))
    }
    @SuppressLint("SetTextI18n")
    private fun updatePlayerInfo() {
        p1 = playerInfo[0]
        p2 = playerInfo[1]
        p3 = playerInfo[2]
        p4 = playerInfo[3]
        p1Coins = playerInfoCoins[0]
        p2Coins = playerInfoCoins[1]
        p3Coins = playerInfoCoins[2]
        p4Coins = playerInfoCoins[3]
       if(nPlayers==7){
           p5 = playerInfo[4]
           p6 = playerInfo[5]
           p7 = playerInfo[6]
           p5Coins = playerInfoCoins[4]
           p6Coins = playerInfoCoins[5]
           p7Coins = playerInfoCoins[6]
       }
        updatePlayerNames()
        for(i in 0 until nPlayers) {
            val j = i+nPlayers
            if(playerInfo[j].isNotEmpty()) {
                Picasso.get().load(playerInfo[j]).resize(300, 300).centerCrop().error(R.drawable.s3)
                    .into(findViewById<ImageView>(refIDMappedImageView[i]))
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updatePlayerNames(){
        for(i in 0 until nPlayers) {
            findViewById<TextView>(refIDMappedTextView[i]).text = playerName(i+1) + "\n${emoji}${String.format("%,d", playerInfoCoins[i])}"
        }
    }
    private fun shufflingWindow(time: Long = 4900,fadeOffTime: Long = 700, gameStateChange: Boolean = false){
        Handler().postDelayed({soundShuffle.start()},400)//delayed sound play of shuffling
        displayShufflingCards() //show suits cards and animate
        centralText(getString(R.string.shufflingcards),5200)
        Handler().postDelayed({
            findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_down_out))
            Handler().postDelayed({
//                findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
//                findViewById<LinearLayout>(R.id.imageGallery).visibility= View.INVISIBLE
//                if(from=="p1" && gameStateChange) write("GS",2) // Update Game State to start Biding round by Host only
                displaySelfCards(animations = true, bidingRequest = true)
            },fadeOffTime)
        },time)
    }
    private fun displayShufflingCards(view: View = View(applicationContext), sets:Int = 5){
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        gallery.removeAllViews()
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
//                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).setOnClickListener {
//                    viewTemp.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.clockwise_ccw))
//                }
                gallery.addView(viewTemp)
            }
        }
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_left_right))
    }
    private fun write(path: String, value: Any) {
        refGameData.child(path).setValue(value)
    }
    private fun getCardsAndDisplay(player: String = from, display: Boolean = true, animation: Boolean=false){
        refGameData.child("CH/$player").  // display the host info in joining room screen
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(dataLoad: DataSnapshot) {
                if (dataLoad.value.toString().isNotEmpty() && dataLoad.value.toString()!= "null"){
                    cardsInHand = (dataLoad.value as MutableList<*>).filterNotNull() as MutableList<Long>
                    if(display) displaySelfCards(animations = animation)
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

    fun openCloseChatWindow(view: View){
        if (findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility == View.VISIBLE){ //close chat display
            findViewById<RelativeLayout>(R.id.chatLinearLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.zoomout_chat_close))
            Handler().postDelayed({
                findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.GONE
            }, 140)
        }
        else { //open chat display
            counterChat = 0 // reset chat counter to 0
            findViewById<TextView>(R.id.textViewChatNo).visibility = View.GONE // make counter invisible
            findViewById<TextView>(R.id.textViewChatNo).clearAnimation() // clear counter animation
            findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.chatLinearLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.zoomin_chat_open))
        }
    }
    fun openCloseScoreSheet(view: View){
        if (findViewById<ScrollView>(R.id.scrollViewScore).visibility == View.VISIBLE){
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE

            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.zoomout_scoretable_close))
            Handler().postDelayed({
                findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
            }, 140)
            scoreOpenStatus = false
        }
        else {
            scoreOpenStatus = true
            if(scoreSheetNotUpdated) {
                scoreBoardTable(display = false,data = createScoreTableHeader(), upDateHeader = true)
                scoreBoardTable(display = false,data = createScoreTableTotal(), upDateTotal = true)
            }
            scoreSheetNotUpdated = false
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.zoomin_scoretable_open))}
    }

    fun sendChat(view: View) {
//        val uni = 0x1F60A
////        val emoji = String(Character.toChars(uni))
////        write("M", "$selfName : $emoji  ")
        if(findViewById<EditText>(R.id.editTextChatInput).text.toString().isNotEmpty()){
            findViewById<EditText>(R.id.editTextChatInput).text
            refRoomFirestore.document(roomID+"_chat").set(hashMapOf( "M" to "$selfName : ${findViewById<EditText>(R.id.editTextChatInput).text}  "))
            findViewById<EditText>(R.id.editTextChatInput).setText("")
        }
    }
    private fun getSharedPrefs(){
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
        }
    }
    @SuppressLint("NewApi")
    private fun vibrationStart(duration: Long = 200){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else{
            vibrator.vibrate(duration)
        }
    }
    private fun initializeAds(){
        if(!premiumStatus){
            findViewById<AdView>(R.id.addViewGameScreenBanner).visibility = View.VISIBLE
            findViewById<AdView>(R.id.addViewGameScreenBanner).loadAd(AdRequest.Builder().build())
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).visibility = View.VISIBLE
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).loadAd(AdRequest.Builder().build())
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = resources.getString(R.string.interstitial)
            mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() { // dummy - check if some other places ads is shown  - no start next game button needs to be added here
                    if(from == "p1") {
                        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.inActiveCard))
                        findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.VISIBLE
                        findViewById<AppCompatButton>(R.id.startNextRoundButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal))
                    }
                }
            }
        }
        else  {
            findViewById<AdView>(R.id.addViewGameScreenBanner).visibility = View.GONE
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).visibility = View.GONE
        }
    }
    fun closeGameRoom(view: View){
        finish()
        startActivity(
            Intent(this, MainHomeScreen::class.java)
            .apply {putExtra("newUser",false)})
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
    }
    override fun onStop() {
        super.onStop()
        refGameData.child("GS").removeEventListener(gameStateListener)
        try{
            refGameData.child("RO").removeEventListener(roundListener)
        }catch(me: Exception){
//            toastCenter(me.toString()) // dummy think to implement a good way
        }
        try{
            refGameData.child("Bid").removeEventListener(bidingTurnListener)
        }catch(me: Exception){
//            toastCenter(me.toString()) // dummy think to implement a good way
        }
//        refRooms.removeEventListener(roomListener)
        refGameData.child("BU1").removeEventListener(partnerListener1)
        refGameData.child("BU2").removeEventListener(partnerListener2)
        refGameData.child("R").removeEventListener(roundNumberListener)
        chatRegistration.remove()
//        refGameData.child("M").removeEventListener(chatListener) //attach chat listener
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
    private fun deleteAllRoomdata(){
        write("OL/$from",2)
        if(from=="p1") {
            refRoomFirestore.document(roomID + "_chat").delete()
            refRoomFirestore.document(roomID).delete()
            refGameData.removeValue()
        }
    }
    override fun onPause() {
        super.onPause()
        write("OL/$from",0)
    }  // is offline
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if(!scoreOpenStatus) {
//            if(vibrateStatus) vibrationStart()
            toastCenter("App is minimized")
            this.moveTaskToBack(true)
        }else{
            openCloseScoreSheet(View(applicationContext))
        }
    } // is offline
    override fun onDestroy() {
        super.onDestroy()
//        toastCenter("Destroyed")
//        deleteAllRoomdata()
    } // has left the room

}


