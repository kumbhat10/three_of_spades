@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "PLUGIN_WARNING", "ImplicitThis", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.properties.Delegates
import kotlin.random.Random

@SuppressLint("SetTextI18n")
class GameScreen : AppCompatActivity() {
    //    region Initialization

    private lateinit var textToSpeech: TextToSpeech
    private var typedValue = TypedValue()
    private var rated = false
    private var reviewRequested = false
    private var ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        .toInt()
    private var requestRatingAfterDays = 2 //dummy
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var refIDMappedTextView: List<Int>
    private lateinit var refIDMappedImageView: List<Int>
    private lateinit var refIDMappedHighlightView: List<Int>
    private lateinit var refIDMappedTableImageView: List<Int>
    private lateinit var refIDMappedTableAnim: List<Int>
    private lateinit var refIDMappedTableWinnerAnim: List<Int>
    private lateinit var refIDValesTextViewScore: List<Int>
    private lateinit var refIDMappedPartnerIconImageView: List<Int>
    private lateinit var refIDMappedOnlineIconImageView: List<Int>

    private lateinit var cardsDrawable: List<Int>
    private lateinit var cardsPoints: List<Int>
    private lateinit var cardsSuit: List<String>
    private lateinit var cardsDrawablePartner: List<Int>
    private lateinit var cardsIndexSortedPartner: List<Int>
    private lateinit var cardsPointsPartner: List<Int>
    private var cardsIndexLimit = 0
    private var roundNumberLimit = 0
    private var scoreLimit = 0

    private var soundStatus = true
    private var vibrateStatus = true
    private lateinit var vibrator: Vibrator

    private var premiumStatus = false
    private var scoreOpenStatus = false
    private var activityExists = true
    private lateinit var mInterstitialAd: InterstitialAd

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private var fromInt = 0
    private var nPlayers = 0
    private var nPlayers7 = false
    private var nPlayers4 = false
    private val emojiMoney = String(Character.toChars(0x1F4B0))
    private val emojiScore = String(Character.toChars(0x1F3AF))
    private val emojiMessage = String(Character.toChars(0x1F4AC))
    private val emojiGuard = String(Character.toChars(0x1F482))

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var refGameData: DatabaseReference
    private var refRoomFirestore = Firebase.firestore.collection("Rooms")
    private var refUsersData = Firebase.firestore.collection("Users")
    private var uid = ""
    private lateinit var chatRegistration: ListenerRegistration

    private lateinit var gameStateListener: ValueEventListener
    private lateinit var countDownBidding: CountDownTimer
    private lateinit var countDownPlayCard: CountDownTimer
    private lateinit var bidingTurnListener: ValueEventListener
    private lateinit var cardsOnTableListener1: ValueEventListener
    private lateinit var cardsOnTableListener2: ValueEventListener
    private lateinit var cardsOnTableListener3: ValueEventListener
    private lateinit var cardsOnTableListener4: ValueEventListener
    private lateinit var cardsOnTableListener5: ValueEventListener
    private lateinit var cardsOnTableListener6: ValueEventListener
    private lateinit var cardsOnTableListener7: ValueEventListener
    private lateinit var pointsListener1: ValueEventListener
    private lateinit var pointsListener2: ValueEventListener
    private lateinit var pointsListener3: ValueEventListener
    private lateinit var pointsListener4: ValueEventListener
    private lateinit var pointsListener5: ValueEventListener
    private lateinit var pointsListener6: ValueEventListener
    private lateinit var pointsListener7: ValueEventListener
    private lateinit var onlineStatusListener1: ValueEventListener
    private lateinit var onlineStatusListener2: ValueEventListener
    private lateinit var onlineStatusListener3: ValueEventListener
    private lateinit var onlineStatusListener4: ValueEventListener
    private lateinit var onlineStatusListener5: ValueEventListener
    private lateinit var onlineStatusListener6: ValueEventListener
    private lateinit var onlineStatusListener7: ValueEventListener
    private lateinit var roundNumberListener: ValueEventListener
    private lateinit var roundListener: ValueEventListener
    private lateinit var partnerListener1: ValueEventListener
    private lateinit var partnerListener2: ValueEventListener

    private lateinit var trump: String
    private lateinit var trumpStart: String
    private lateinit var playerInfo: ArrayList<String>
    private lateinit var playerInfoCoins: ArrayList<Int>
    private var nGamesPlayed = 0
    private var nGamesWon = 0
    private var nGamesBided = 0

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
    private var timeCountdownPlayCard = 15000L
    private var timeCountdownBid = 15000L
    private var lastChat = ""
    private var scoreSheetNotUpdated = true

    private var played = false
    private var bidded = false
    private var ct1: Int = cardsIndexLimit
    private var ct2: Int = cardsIndexLimit
    private var ct3: Int = cardsIndexLimit
    private var ct4: Int = cardsIndexLimit
    private var ct5: Int = cardsIndexLimit
    private var ct6: Int = cardsIndexLimit
    private var ct7: Int = cardsIndexLimit
    private var pt1 = 0
    private var pt2 = 0
    private var pt3 = 0
    private var pt4 = 0
    private var pt5 = 0
    private var pt6 = 0
    private var pt7 = 0
    private lateinit var ptAll: List<Int>
    private var bidTeamScore = 0
    private lateinit var scoreList: List<Int>
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
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable._s_icon_bug) //default: bug image
            .restartActivity(MainHomeScreen::class.java).apply()
        setContentView(R.layout.activity_game_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE // keep screen in landscape mode always
        roomID = intent.getStringExtra("roomID")!!
            .toString()    //Get roomID and display    selfName = intent.getStringExtra("selfName") //Get Username first  - selfName ,roomID available
        from = intent.getStringExtra("from")!!
            .toString()    //check if user has joined room or created one and display Toast
        fromInt = from.split("")[2].toInt()
        selfName = intent.getStringExtra("selfName")!!.toString()
        playerInfo = intent.getStringArrayListExtra("playerInfo") as ArrayList<String>
        val userStats = intent.getIntegerArrayListExtra("userStats")!!
        nGamesPlayed = userStats[0]
        nGamesWon = userStats[1]
        nGamesBided = userStats[2]
        playerInfoCoins = intent.getStringArrayListExtra("playerInfoCoins") as ArrayList<Int>
        nPlayers = intent.getIntExtra("nPlayers", 0)
        if (nPlayers == 7) nPlayers7 = true
        if (nPlayers == 4) nPlayers4 = true
        setupGame4or7()

        refIDMappedTextView = PlayersReference().refIDMappedTextView(from, nPlayers)
        refIDMappedImageView = PlayersReference().refIDMappedImageView(from, nPlayers)
        refIDMappedHighlightView = PlayersReference().refIDMappedHighlightView(from, nPlayers)
        refIDMappedPartnerIconImageView = PlayersReference().refIDMappedPartnerIconImageView(from, nPlayers)
        refIDMappedOnlineIconImageView = PlayersReference().refIDMappedOnlineIconImageView(from, nPlayers)
        refIDMappedTableAnim = PlayersReference().refIDMappedTableAnim(from, nPlayers)
        refIDMappedTableWinnerAnim = PlayersReference().refIDMappedTableWinnerAnim(from, nPlayers)
        refIDMappedTableImageView = PlayersReference().refIDMappedTableImageView(from, nPlayers)

        refGameData = Firebase.database.getReference("GameData/$roomID")

        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 20)
        //        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.cardsBackgroundDark))
        //        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
        //        toast.view.findViewById<TextView>(android.R.id.message).textSize = 14F
        //region Other Thread - player info update
        Handler(Looper.getMainLooper()).post {
            SoundManager.initialize(applicationContext)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            updatePlayerInfo()
            initializeSpeechEngine()
            getSharedPrefs()
            firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
            logFirebaseEvent("game_screen", nPlayers, "start")
            uid = FirebaseAuth.getInstance().uid.toString()
            refUsersData.document(uid)
                .set(hashMapOf("LPD" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    .toInt()), SetOptions.merge())
        } // endregion
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
    }

    private fun logFirebaseEvent(event: String, int: Int, key: String) {
        val params = Bundle()
        params.putInt(key, int)
        firebaseAnalytics.logEvent(event, params)
    }

    private fun setupGame4or7() {

        if (nPlayers7) {
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

            refIDValesTextViewScore = PlayersReference().refIDTextViewScoreSheet7
            cardsDrawable = PlayingCards().cardsDrawable7()
            cardsPoints = PlayingCards().cardsPoints7()
            cardsSuit = PlayingCards().cardsSuit7()
            cardsIndexSortedPartner = PlayingCards().cardsIndexSortedPartner7
            cardsDrawablePartner = PlayingCards().cardsDrawablePartner7
            cardsPointsPartner = PlayingCards().cardsPointsPartner7
            cardsIndexLimit = 99
            roundNumberLimit = 14
            scoreLimit = 705
            scoreList = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
            ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
        }
        if (nPlayers4) {
            refIDValesTextViewScore = PlayersReference().refIDTextViewScoreSheet4
            cardsDrawable = PlayingCards().cardsDrawable4
            cardsPoints = PlayingCards().cardsPoints4
            cardsSuit = PlayingCards().cardSuit4
            cardsIndexSortedPartner = PlayingCards().cardsIndexSortedPartner4
            cardsDrawablePartner = PlayingCards().cardsDrawable4
            cardsPointsPartner = PlayingCards().cardsPoints4
            cardsIndexLimit = 53
            roundNumberLimit = 13
            scoreLimit = 355
            scoreList = listOf(pt1, pt2, pt3, pt4)
            ptAll = listOf(pt1, pt2, pt3, pt4)

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
        if (fromInt != 1) checkRoomExists() // dont check for host
        getCardsAndDisplay(display = false)

        // region table card listener
        cardsOnTableListener1 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    ct1 = p0.value.toString().toInt()
                    tablePointsCalculator()
                    if (ct1 <= (cardsIndexLimit - 2)) {
                        if (soundStatus) SoundManager.getInstance()
                            .playCardPlayedSound() // soundCardPlayed.start()
                        findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.VISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[0]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[0]))
                        findViewById<ImageView>(refIDMappedTableImageView[0]).setImageResource(cardsDrawable[ct1])
                    } else {
                        findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.INVISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[0]).clearAnimation()
                    }
                }
            }
        }
        cardsOnTableListener2 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    ct2 = p0.value.toString().toInt()
                    tablePointsCalculator()
                    if (ct2 <= (cardsIndexLimit - 2)) {
                        if (soundStatus) SoundManager.getInstance()
                            .playCardPlayedSound() // soundCardPlayed.start()
                        findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.VISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[1]))
                        findViewById<ImageView>(refIDMappedTableImageView[1]).setImageResource(cardsDrawable[ct2])
                    } else {
                        findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.INVISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[1]).clearAnimation()
                    }
                }
            }
        }
        cardsOnTableListener3 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    ct3 = p0.value.toString().toInt()
                    tablePointsCalculator()
                    if (ct3 <= (cardsIndexLimit - 2)) {
                        if (soundStatus) SoundManager.getInstance()
                            .playCardPlayedSound() // soundCardPlayed.start()
                        findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.VISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[2]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[2]))
                        findViewById<ImageView>(refIDMappedTableImageView[2]).setImageResource(cardsDrawable[ct3])
                    } else {
                        findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.INVISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[2]).clearAnimation()
                    }
                }
            }
        }
        cardsOnTableListener4 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    ct4 = p0.value.toString().toInt()
                    tablePointsCalculator()
                    if (ct4 <= (cardsIndexLimit - 2)) {
                        if (soundStatus) SoundManager.getInstance()
                            .playCardPlayedSound() // soundCardPlayed.start()
                        findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.VISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[3]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[3]))
                        findViewById<ImageView>(refIDMappedTableImageView[3]).setImageResource(cardsDrawable[ct4])
                    } else {
                        findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.INVISIBLE
                        findViewById<ImageView>(refIDMappedTableImageView[3]).clearAnimation()
                    }
                }
            }
        }
        if (nPlayers7) {
            cardsOnTableListener5 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        ct5 = p0.value.toString().toInt()
                        tablePointsCalculator()
                        if (ct5 <= (cardsIndexLimit - 2)) {
                            if (soundStatus) SoundManager.getInstance()
                                .playCardPlayedSound() // soundCardPlayed.start()
                            findViewById<ImageView>(refIDMappedTableImageView[4]).visibility = View.VISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[4]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[4]))
                            findViewById<ImageView>(refIDMappedTableImageView[4]).setImageResource(cardsDrawable[ct5])
                        } else {
                            findViewById<ImageView>(refIDMappedTableImageView[4]).visibility = View.INVISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[4]).clearAnimation()
                        }
                    }
                }
            }
            cardsOnTableListener6 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        ct6 = p0.value.toString().toInt()
                        tablePointsCalculator()
                        if (ct6 <= (cardsIndexLimit - 2)) {
                            if (soundStatus) SoundManager.getInstance()
                                .playCardPlayedSound() // soundCardPlayed.start()
                            findViewById<ImageView>(refIDMappedTableImageView[5]).visibility = View.VISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[5]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[5]))
                            findViewById<ImageView>(refIDMappedTableImageView[5]).setImageResource(cardsDrawable[ct6])
                        } else {
                            findViewById<ImageView>(refIDMappedTableImageView[5]).visibility = View.INVISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[5]).clearAnimation()
                        }
                    }
                }
            }
            cardsOnTableListener7 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        ct7 = p0.value.toString().toInt()
                        tablePointsCalculator()
                        if (ct7 <= (cardsIndexLimit - 2)) {
                            if (soundStatus) SoundManager.getInstance()
                                .playCardPlayedSound() // soundCardPlayed.start()
                            findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.VISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[6]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[6]))
                            findViewById<ImageView>(refIDMappedTableImageView[6]).setImageResource(cardsDrawable[ct7])
                        } else {
                            findViewById<ImageView>(refIDMappedTableImageView[6]).visibility = View.INVISIBLE
                            findViewById<ImageView>(refIDMappedTableImageView[6]).clearAnimation()
                        }
                    }
                }
            }
        }
        // endregion
        // region Chat Listener
        chatRegistration = refRoomFirestore.document(roomID + "_chat")
            .addSnapshotListener { dataSnapshot, error ->
                if (dataSnapshot != null && dataSnapshot.exists() && error == null) {
                    val data = (dataSnapshot.data as Map<String, String>)["M"].toString()
                    if (data.isNotEmpty() && lastChat != data) { // if chat is not empty
                        if (soundStatus) SoundManager.getInstance()
                            .playChatSound() // soundChat.start()
                        findViewById<TextView>(R.id.textViewChatDisplay).text = findViewById<TextView>(R.id.textViewChatDisplay).text.toString() + "\n$emojiGuard " + data
                        findViewById<TextView>(R.id.textViewChatDisplay).requestLayout()
                        lastChat = data
                        if (findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility != View.VISIBLE) {
                            counterChat += 1 // increase counter by 1 is chat display is off
                            findViewById<TextView>(R.id.textViewChatNo).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.textViewChatNo).text = "$counterChat $emojiMessage"
                            //                            findViewById<TextView>(R.id.textViewChatNo).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
                        }
                    }
                } else if (dataSnapshot != null && !dataSnapshot.exists()) { // dummy
                    //                    if(soundStatus) soundError.start()
                    //                    toastCenter("Sorry $selfName \n$p1 has left the room. \nYou can create your own room or join other")
                } else if (error != null) {
                    toastCenter(error.localizedMessage!!.toString()) // dummy
                }
            }
        // endregion
        //region Online Status Listener
        onlineStatusListener1 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCenter("empty host")
            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {
                    if (onlineP1 != data.value.toString().toInt()) {
                        onlineP1 = data.value.toString().toInt()
                        if (onlineP1 == 0 && from != "p1") {
                            toastCenter("${playerName(1)} is Offline !")
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_offline)
                        }
                        if (onlineP1 == 1 && from != "p1") {
                            toastCenter("${playerName(1)} is Online !")
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_online)
                        }
                        if (onlineP1 == 2 && from != "p1") {
                            activityExists = false
                            countDownBidding.cancel()
                            countDownPlayCard.cancel()
                            toastCenter("Ooppps ! ${playerName(1)} has closed the room")
                            speak("Shit.   ${playerName(1)} has left the room. leaving room now", speed = 1.15f)
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_offline)
                            val view = View(applicationContext)
                            view.tag = "notClicked"
                            Handler(Looper.getMainLooper()).postDelayed({ closeGameRoom(view) }, 2500)
                        }
                    }
                }
            }
        }
        onlineStatusListener2 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {

                    if (onlineP2 != data.value.toString().toInt()) {
                        onlineP2 = data.value.toString().toInt()
                        if (onlineP2 == 0 && from != "p2") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_offline)
                            toastCenter("${playerName(2)} is Offline !")
                        }
                        if (onlineP2 == 1 && from != "p2") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_online)
                            toastCenter("${playerName(2)} is Online !")
                        }
                        if (onlineP2 == 2 && from != "p2") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[1]).setImageResource(R.drawable.status_offline)
                            toastCenter("${playerName(2)} has left the room !")
                            speak("${playerName(2)} has left the room!", speed = 1.1f)
                        }
                    }
                }
            }
        }
        onlineStatusListener3 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {
                    if (onlineP3 != data.value.toString().toInt()) {
                        onlineP3 = data.value.toString().toInt()
                        if (onlineP3 == 0 && from != "p3") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_offline)
                            toastCenter("${playerName(3)} is Offline !")
                        }
                        if (onlineP3 == 1 && from != "p3") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_online)
                            toastCenter("${playerName(3)} is Online !")
                        }
                        if (onlineP3 == 2 && from != "p3") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[2]).setImageResource(R.drawable.status_offline)
                            speak("${playerName(3)} has left the room!", speed = 1.1f)
                            toastCenter("${playerName(3)} has left the room !")
                        }
                    }
                }
            }
        }
        onlineStatusListener4 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {
                    if (onlineP4 != data.value.toString().toInt()) {
                        onlineP4 = data.value.toString().toInt()
                        if (onlineP4 == 0 && from != "p4") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_offline)
                            toastCenter("${playerName(4)} is Offline !")
                        }
                        if (onlineP4 == 1 && from != "p4") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_online)
                            toastCenter("${playerName(4)} is Online !")
                        }
                        if (onlineP4 == 2 && from != "p4") {
                            findViewById<ImageView>(refIDMappedOnlineIconImageView[3]).setImageResource(R.drawable.status_offline)
                            speak("${playerName(4)} has left the room!", speed = 1.1f)
                            toastCenter("${playerName(4)} has left the room !")
                        }
                    }
                }
            }
        }
        if (nPlayers7) {
            onlineStatusListener5 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.value != null && activityExists) {
                        if (onlineP5 != data.value.toString().toInt()) {
                            onlineP5 = data.value.toString().toInt()
                            if (onlineP5 == 0 && from != "p5") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_offline)
                                toastCenter("${playerName(5)} is Offline !")
                            }
                            if (onlineP5 == 1 && from != "p5") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_online)
                                toastCenter("${playerName(5)} is Online !")
                            }
                            if (onlineP5 == 2 && from != "p5") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[4]).setImageResource(R.drawable.status_offline)
                                speak("${playerName(5)} has left the room!", speed = 1.1f)
                                toastCenter("${playerName(5)} has left the room !")
                            }
                        }
                    }
                }
            }
            onlineStatusListener6 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.value != null && activityExists) {
                        if (onlineP6 != data.value.toString().toInt()) {
                            onlineP6 = data.value.toString().toInt()
                            if (onlineP6 == 0 && from != "p6") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_offline)
                                toastCenter("${playerName(6)} is Offline !")
                            }
                            if (onlineP6 == 1 && from != "p6") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_online)
                                toastCenter("${playerName(6)} is Online !")
                            }
                            if (onlineP6 == 2 && from != "p6") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[5]).setImageResource(R.drawable.status_offline)
                                speak("${playerName(6)} has left the room!", speed = 1.1f)
                                toastCenter("${playerName(6)} has left the room !")
                            }
                        }
                    }
                }
            }
            onlineStatusListener7 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.value != null && activityExists) {
                        if (onlineP7 != data.value.toString().toInt()) {
                            onlineP7 = data.value.toString().toInt()
                            if (onlineP7 == 0 && from != "p7") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_offline)
                                toastCenter("${playerName(7)} is Offline !")
                            }
                            if (onlineP7 == 1 && from != "p7") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_online)
                                toastCenter("${playerName(7)} is Online !")
                            }
                            if (onlineP7 == 2 && from != "p7") {
                                findViewById<ImageView>(refIDMappedOnlineIconImageView[6]).setImageResource(R.drawable.status_offline)
                                speak("${playerName(7)} has left the room!", speed = 1.1f)
                                toastCenter("${playerName(7)} has left the room !")
                            }
                        }
                    }
                }
            }
        }
        // endregion
        // region Game State Listener
        gameStateListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(GameState: DataSnapshot) {
                if (GameState.value != null && activityExists) {

                    gameState = (GameState.value as Long).toInt()
                    if (gameState == 1) {
                        getCardsAndDisplay(display = false)
                        if (!bidingStarted) {
                            animateElements()
                            resetVariables()
                            findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
                            findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
                            updatePlayerNames()
                            shufflingWindow(gameStateChange = true) // gameStateChange = change game state to 2 after shuffling
                        } else if (activityExists) startBidding()
                    }
                    //                    if (gameState == 2) { // bidding moved to state 1 after shuffling and displaying own cards
                    //                        if (soundStatus) SoundManager.getInstance().playUpdateSound()//soundUpdate.start()
                    //                    }
                    if (gameState == 3) {
                        if (soundStatus) SoundManager.getInstance()
                            .playSuccessSound() //soundSuccess.start()
                        refGameData.child("Bid").removeEventListener(bidingTurnListener)
                        bidingStarted = false
                        finishBackgroundAnimationBidding() // also highlight bidder winner & removed automatically at game state 5
                        startTrumpSelection()
                    }
                    if (gameState == 4) {
                        //                        getCardsAndDisplay() // dummy - check if really required to read cards again from firebase - looks like waste
                        if (soundStatus) SoundManager.getInstance()
                            .playSuccessSound() // soundSuccess.start()
                        getTrumpStartPartnerSelection()
                    }
                    if (gameState == 5) {
                        newGameStatus = true
                        getBuddyAndDisplay()
                        if (!roundStarted) {
                            finishPassOverlay()
                            if (soundStatus) SoundManager.getInstance()
                                .playSuccessSound() // soundSuccess.start()
                            if (bidder > 0) updatePlayerScoreInfo(ptAll)
                            getCardsAndDisplay(animation = false)
                            Handler(Looper.getMainLooper()).postDelayed({ startPlayingRound() }, 3000)
                            if (playerTurn != fromInt) {
                                centralText("${playerName(bidder)} will play first \n You get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
                                speak("${playerName(bidder)} will play first \n You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
                            }
                            if (playerTurn == fromInt) {
                                centralText("You will have ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
                                speak("You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
                            }
                        } else {
                            roundStarted = true
                            getCardsAndDisplay()
                            startPlayingRound()
                        }
                    }
                    if (gameState == 6) {
                        gameMode6()
                    }
                }
            }
        }
        // endregion
        // region Round Number Listener - dummy - check if can be merged with RO - round data
        roundNumberListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    roundNumber = p0.value.toString().toInt()
                    if (roundNumber > 10 && !premiumStatus) mInterstitialAd.loadAd(AdRequest.Builder()
                        .build()) // load the ad again
                }
            }
        }
        // endregion
        // region Individual Points listener - dummy - check if need all 7 or just single and merge all
        pointsListener1 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {

                    if (pt1 != p0.value.toString().toInt()) {
                        pt1 = p0.value.toString().toInt()
                        if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                        else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                        if (gameState == 5) updatePlayerScoreInfo(ptAll)
                    }
                }
            }
        }
        pointsListener2 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    if (pt2 != p0.value.toString().toInt()) {
                        pt2 = p0.value.toString().toInt()
                        if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                        else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                        if (gameState == 5) updatePlayerScoreInfo(ptAll)
                    }
                }
            }
        }
        pointsListener3 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    if (pt3 != p0.value.toString().toInt()) {
                        pt3 = p0.value.toString().toInt()
                        if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                        else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                        if (gameState == 5) updatePlayerScoreInfo(ptAll)
                    }
                }
            }
        }
        pointsListener4 = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    if (pt4 != p0.value.toString().toInt()) {
                        pt4 = p0.value.toString().toInt()
                        if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                        else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                        if (gameState == 5) updatePlayerScoreInfo(ptAll)
                    }
                }
            }
        }
        if (nPlayers7) {
            pointsListener5 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        if (pt5 != p0.value.toString().toInt()) {
                            pt5 = p0.value.toString().toInt()
                            if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                            else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                            if (gameState == 5) updatePlayerScoreInfo(ptAll)
                        }
                    }
                }
            }
            pointsListener6 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        if (pt6 != p0.value.toString().toInt()) {
                            pt6 = p0.value.toString().toInt()
                            if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                            else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                            if (gameState == 5) updatePlayerScoreInfo(ptAll)
                        }
                    }
                }
            }
            pointsListener7 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null && activityExists) {
                        if (pt7 != p0.value.toString().toInt()) {
                            pt7 = p0.value.toString().toInt()
                            if (nPlayers4) ptAll = listOf(pt1, pt2, pt3, pt4)
                            else ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                            if (gameState == 5) updatePlayerScoreInfo(ptAll)
                        }
                    }
                }
            }
        }

        //        endregion
        //            region Partner listener 1
        partnerListener1 = object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {
                    if (buFound1 != data.child("s1").value.toString().toInt() && buFound1 != 1) {
                        if (soundStatus) SoundManager.getInstance()
                            .playSuccessSound() // soundSuccess.start()
                        if (vibrateStatus) vibrationStart()
                        if (data.child("s1").value.toString().toInt() == 1) {
                            speak("${
                                playerName(data.child("b1").value.toString().toInt())
                            } .  is partner now")
                        } else {
                            speak("New partner found")
                        }
                    }
                    buPlayer1 = data.child("b1").value.toString().toInt()
                    buFound1 = data.child("s1").value.toString().toInt()
                    if (buPlayer1 != 0 && buFound1 == 1) findViewById<TextView>(R.id.trumpText1).text = playerName(buPlayer1)
                    displayPartnerIcon()
                }
            }
        }
        //            endregion
        //            region Partner listener 2
        if (nPlayers7) partnerListener2 = object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.value != null && activityExists) {
                    if (buFound2 != data.child("s2").value.toString().toInt() && buFound2 != 1) {
                        if (soundStatus) SoundManager.getInstance()
                            .playSuccessSound() // soundSuccess.start()
                        if (vibrateStatus) vibrationStart()
                        if (data.child("s2").value.toString().toInt() == 1) {
                            speak("${
                                playerName(data.child("b2").value.toString().toInt())
                            } .  is partner now")
                        } else {
                            speak("New partner found")
                        }
                    }
                    buPlayer2 = data.child("b2").value.toString().toInt()
                    buFound2 = data.child("s2").value.toString().toInt()
                    if (buPlayer2 != 0 && buFound2 == 1) findViewById<TextView>(R.id.trumpText2).text = playerName(buPlayer2)
                    displayPartnerIcon()
                }
            }
        }
        //            endregion
        findViewById<EditText>(R.id.editTextChatInput).setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    sendChat(v)
                    hideKeyboard()
                    false
                }
                else -> false
            }
        }  // close keyboard after sending chat

        // region Attach Listener
        if (activityExists) {
            refGameData.child("BU1").addValueEventListener(partnerListener1)
            refGameData.child("SC/p1").addValueEventListener(pointsListener1)
            refGameData.child("SC/p2").addValueEventListener(pointsListener2)
            refGameData.child("SC/p3").addValueEventListener(pointsListener3)
            refGameData.child("SC/p4").addValueEventListener(pointsListener4)
            refGameData.child("R").addValueEventListener(roundNumberListener)
            refGameData.child("GS")
                .addValueEventListener(gameStateListener) // attach the created game data listener
            refGameData.child("CT/p1")
                .addValueEventListener(cardsOnTableListener1) // player 1 cards on table listener
            refGameData.child("CT/p2")
                .addValueEventListener(cardsOnTableListener2) // player 2 cards on table listener
            refGameData.child("CT/p3")
                .addValueEventListener(cardsOnTableListener3) // player 3 cards on table listener
            refGameData.child("CT/p4")
                .addValueEventListener(cardsOnTableListener4) // player 4 cards on table listener
            refGameData.child("OL/p1").addValueEventListener(onlineStatusListener1)
            refGameData.child("OL/p2").addValueEventListener(onlineStatusListener2)
            refGameData.child("OL/p3").addValueEventListener(onlineStatusListener3)
            refGameData.child("OL/p4").addValueEventListener(onlineStatusListener4)

            if (nPlayers7) {
                refGameData.child("BU2").addValueEventListener(partnerListener2)
                refGameData.child("SC/p5").addValueEventListener(pointsListener5)
                refGameData.child("SC/p6").addValueEventListener(pointsListener6)
                refGameData.child("SC/p7").addValueEventListener(pointsListener7)
                refGameData.child("CT/p5")
                    .addValueEventListener(cardsOnTableListener5) // player 1 cards on table listener
                refGameData.child("CT/p6")
                    .addValueEventListener(cardsOnTableListener6) // player 1 cards on table listener
                refGameData.child("CT/p7")
                    .addValueEventListener(cardsOnTableListener7) // player 1 cards on table listener
                refGameData.child("OL/p5").addValueEventListener(onlineStatusListener5)
                refGameData.child("OL/p6").addValueEventListener(onlineStatusListener6)
                refGameData.child("OL/p7").addValueEventListener(onlineStatusListener7)
            }
            //        endregion

            // region       Countdown PlayCard
            countDownPlayCard = object : CountDownTimer(timeCountdownPlayCard, 50) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    //                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished * 10000 / timeCountdownPlayCard).toInt()   //10000 because max progress is 10000
                    findViewById<ProgressBar>(R.id.progressbarTimer).secondaryProgress = ((timeCountdownPlayCard - millisUntilFinished) * 10000 / timeCountdownPlayCard).toInt()
                    findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished / 1000).toDouble() + 1).toInt()
                        .toString() + "s"
                }

                override fun onFinish() {
                    autoPlayCard()
                    if (soundStatus) SoundManager.getInstance()
                        .playTimerSound() //soundTimerFinish.start()
                    if (vibrateStatus) vibrationStart()
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                    findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                    findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                }
            }
            //        endregion
            // region       Countdown Biding
            countDownBidding = object : CountDownTimer(timeCountdownBid, 50) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    //                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished * 10000 / timeCountdownBid).toInt()
                    findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished / 1000).toDouble() + 1).toInt()
                        .toString() + "s"
                }

                override fun onFinish() {
                    if (!bidded) {
                        bidded = true
                        if (vibrateStatus) vibrationStart()
                        if (soundStatus) SoundManager.getInstance()
                            .playTimerSound() //soundTimerFinish.start()
                        write("Bid/BS/p$fromInt", 0) // pass the bid if times up
                        write("Bid/BT", nextTurn(fromInt))
                        findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                        findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                        findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                        findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                        findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                        findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                        findViewById<FrameLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                        Handler(Looper.getMainLooper()).postDelayed({
                            findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
                            findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
                        }, 220)
                        bidButtonsAnimation("clear")
                        centralText("    Time's Up !!  \nYou cannot bid anymore", 2500)
                        speak("Time's Up. ${playerName(fromInt)} You cannot bid anymore", speed = 1.1f)
                    }
                }
            }
            //        endregion
        }
    }

    private fun checkRoomExists() {
        refGameData.child("OL/p1").  // display the host info in joining room screen
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(data: DataSnapshot) {
                if (data.exists() && data.value.toString().toInt() != 2) {
                    write("OL/$from", 1)
                } else {
                    activityExists = false
                    toastCenter("Ooppps ! ${playerName(1)} has closed the room")
                    speak("Shit.   ${playerName(1)} has left the room. leaving room now", speed = 1.15f)
                    findViewById<ImageView>(refIDMappedOnlineIconImageView[0]).setImageResource(R.drawable.status_offline)
                    val view = View(applicationContext)
                    view.tag = "notClicked"
                    Handler(Looper.getMainLooper()).postDelayed({ closeGameRoom(view) }, 2500)
                }
            }
        })
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            view?.let { v ->
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    private fun gameMode6() {
        if (fromInt == 1) logFirebaseEvent("game_screen", nPlayers, "played")
        findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.GONE
        countDownTimer("PlayCard", purpose = "cancel")
        if (vibrateStatus) vibrationStart()
        if (soundStatus) SoundManager.getInstance().playShuffleSound() //
        displayShufflingCards(distribute = false)
        scoreOpenStatus = true
        refGameData.child("S").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (scoreList != p0.value as List<Int> || newGameStatus) { // dummy - newGameStatus not needed as Scorelist has game index which is unique
                    newGameStatus = false
                    scoreList = p0.value as List<Int>
                    updateWholeScoreBoard()
                    gameNumber += 1
                }
            }
        })
        Handler(Looper.getMainLooper()).postDelayed({
            if (!BuildConfig.DEBUG) {
                if (!premiumStatus && mInterstitialAd.isLoaded) mInterstitialAd.show()
            }
            if (!rated && !reviewRequested && (nGamesPlayed > 10 || gameNumber > 2)) {  // Ask only once per game
                //                toastCenter("Review asking")
                inAppReview()
                reviewRequested = true
            } else if (!premiumStatus) {
                if (mInterstitialAd.isLoaded) mInterstitialAd.show()
                else mInterstitialAd.loadAd(AdRequest.Builder()
                    .build()) // load the AD again after loading first time
            }
            if (fromInt == 1) {
                findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
                findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.VISIBLE
                findViewById<AppCompatButton>(R.id.startNextRoundButton).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            }
        }, 3500)
    }

    private fun inAppReview() {
        val manager = ReviewManagerFactory.create(applicationContext)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request1 ->
            if (request1.isSuccessful) {
                val reviewInfo = request1.result
                //                toastCenter("Successful")
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        toastCenter("success result review")
                        speak("Thanks $selfName for the review")
                        rated = true
                        editor = sharedPreferences.edit()
                        editor.putBoolean("rated", rated)
                        editor.apply()
                        logFirebaseEvent("rate_us", 1, "rated")
                        refUsersData.document(uid).set(hashMapOf("rated" to 1), SetOptions.merge())
                    } else toastCenter("failed")
                }
            } else {
                toastCenter("Not successful - In app review")
            }
        }
    }

    private fun updateWholeScoreBoard() {
        p1Gain += scoreList[1].toString().toInt()
        p2Gain += scoreList[2].toString().toInt()
        p3Gain += scoreList[3].toString().toInt()
        p4Gain += scoreList[4].toString().toInt()

        p1Coins += scoreList[1].toString().toInt()
        p2Coins += scoreList[2].toString().toInt()
        p3Coins += scoreList[3].toString().toInt()
        p4Coins += scoreList[4].toString().toInt()

        if (nPlayers7) {
            p5Gain += scoreList[5].toString().toInt()
            p6Gain += scoreList[6].toString().toInt()
            p7Gain += scoreList[7].toString().toInt()

            p5Coins += scoreList[5].toString().toInt()
            p6Coins += scoreList[6].toString().toInt()
            p7Coins += scoreList[7].toString().toInt()
        }
        scoreBoardTable(display = false, data = createScoreTableHeader(), upDateHeader = true)
        scoreBoardTable(display = false, data = createScoreTableTotal(), upDateTotal = true)
        scoreBoardTable(data = scoreList)

        if (bidder == fromInt) {
            nGamesBided += 1
        }
        if (scoreList[fromInt] > 0) {
            nGamesWon += 1
        }
        nGamesPlayed += 1
        refUsersData.document(uid)
            .set(hashMapOf("sc" to playerCoins(from), "w" to nGamesWon, "b" to nGamesBided, "p" to nGamesPlayed), SetOptions.merge())
    }

    private fun createScoreTableHeader(): List<String> {
        return if (nPlayers == 7) listOf("Player\n$emojiMoney", p1 + "\n$emojiMoney${String.format("%,d", p1Coins)}", p2 + "\n$emojiMoney${String.format("%,d", p2Coins)}", p3 + "\n$emojiMoney${String.format("%,d", p3Coins)}", p4 + "\n$emojiMoney${String.format("%,d", p4Coins)}", p5 + "\n$emojiMoney${String.format("%,d", p5Coins)}", p6 + "\n$emojiMoney${String.format("%,d", p6Coins)}", p7 + "\n$emojiMoney${String.format("%,d", p7Coins)}")
        else listOf("Player\n$emojiMoney", p1 + "\n$emojiMoney${String.format("%,d", p1Coins)}", p2 + "\n$emojiMoney${String.format("%,d", p2Coins)}", p3 + "\n$emojiMoney${String.format("%,d", p3Coins)}", p4 + "\n$emojiMoney${String.format("%,d", p4Coins)}")
    }

    private fun createScoreTableTotal(): List<Any> {
        return if (nPlayers == 7) listOf("Total", p1Gain, p2Gain, p3Gain, p4Gain, p5Gain, p6Gain, p7Gain)
        else listOf("Total", p1Gain, p2Gain, p3Gain, p4Gain)
    }

    private fun scoreBoardTable(data: List<Any>, display: Boolean = true, upDateHeader: Boolean = false, upDateTotal: Boolean = false) {
        if (display) {
            scoreOpenStatus = true
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
        }
        val inflater = LayoutInflater.from(applicationContext)
        val viewTemp = when {
            upDateHeader -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScoreName), false)
            upDateTotal -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScoreTotal), false)
            else -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScore), false)
        }
        for (i in 0..nPlayers) {
            viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).text = data[i].toString()
            if (!upDateHeader) viewTemp.findViewById<TextView>(refIDValesTextViewScore[i])
                .setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._12ssp))
            if (i > 0 && !upDateHeader && data[i].toString().toInt() < 0) {
                //                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i])
                    .setTextColor(ContextCompat.getColor(applicationContext, R.color.Red))
            } else if (i > 0 && !upDateHeader) {
                //                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i])
                    .setTextColor(ContextCompat.getColor(applicationContext, R.color.borderblueDark1g))
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

    fun closeChatScoreWindow(view: View) {
        findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.GONE
        findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
        findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
    }

    private fun playerCoins(p: String): Int {
        var coins = 0
        when (p) {
            "p1" -> coins = p1Coins
            "p2" -> coins = p2Coins
            "p3" -> coins = p3Coins
            "p4" -> coins = p4Coins
            "p5" -> coins = p5Coins
            "p6" -> coins = p6Coins
            "p7" -> coins = p7Coins
        }
        return coins
    }

    private fun resetVariables() {
        trumpImage1.setImageResource(R.drawable.ic_back_side_red)
        findViewById<ImageView>(R.id.trumpImage2).setImageResource(R.drawable.ic_back_side_blue)
        findViewById<GifImageView>(R.id.trumpImage).setImageResource(R.drawable.trump1)
        textViewBidValue.text = getString(R.string.bidValue1)  //$emojiScore
        bidNowImage.visibility = View.GONE
        findViewById<TextView>(R.id.textViewBider).text = getString(R.string.Bider)
        findViewById<TextView>(R.id.trumpText).text = getString(R.string.Trump)
        for (i in 0 until nPlayers) { // first reset background and animation of all partner icon
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
        }
        bu1 = 0
        bu1Flag = 0
        buPlayer1 = 0
        buFound1 = 0
        bidValue = 0
        bidTeamScore = 0
        bidder = 0
        bidingStarted = false   /// biding happened before
        counterPartnerSelection = 0
        ct1 = cardsIndexLimit
        ct2 = cardsIndexLimit
        ct3 = cardsIndexLimit
        ct4 = cardsIndexLimit
        gameTurn = 0
        played = false
        playerTurn = 0
        pt1 = 0
        pt2 = 0
        pt3 = 0
        pt4 = 0
        roundStarted = false
        roundNumber = 1
        roundWinner = 0
        tablePoints = 0
        if (nPlayers4) {
            findViewById<TextView>(R.id.trumpText1).text = getString(R.string.partner)
            scoreList = listOf(pt1, pt2, pt3, pt4)
            ptAll = listOf(pt1, pt2, pt3, pt4)
        } else {
            findViewById<TextView>(R.id.trumpText1).text = getString(R.string.partner1)
            findViewById<TextView>(R.id.trumpText2).text = getString(R.string.partner2)
            scoreList = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
            ptAll = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
            pt5 = 0
            pt6 = 0
            pt7 = 0
            bu2 = 0
            bu2Flag = 0
            buPlayer2 = 0
            buFound2 = 0
            ct5 = cardsIndexLimit
            ct6 = cardsIndexLimit
            ct7 = cardsIndexLimit
        }
    }

    fun startNextGame(view: View) { // dummy - try to parameterize and write all at once - or read from CreateGameData class
        findViewById<AppCompatButton>(R.id.startNextRoundButton).clearAnimation()
        findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.GONE
        val cardsShuffled = if (nPlayers7) (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
        else (0..51).shuffled()
        val playerTurn = Random.nextInt(1, nPlayers)
        if (nPlayers7) {
            write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit, "p5" to cardsIndexLimit, "p6" to cardsIndexLimit, "p7" to cardsIndexLimit))
            write("CH", mutableMapOf("p1" to cardsShuffled.slice(0..13)
                .sortedBy { it }, "p2" to cardsShuffled.slice(14..27)
                .sortedBy { it }, "p3" to cardsShuffled.slice(28..41)
                .sortedBy { it }, "p4" to cardsShuffled.slice(42..55)
                .sortedBy { it }, "p5" to cardsShuffled.slice(56..69)
                .sortedBy { it }, "p6" to cardsShuffled.slice(70..83)
                .sortedBy { it }, "p7" to cardsShuffled.slice(84..97).sortedBy { it }))
            if (getString(R.string.testGameData).contains('n')) {
                write("Bid", mutableMapOf("BV" to 350, "BT" to playerTurn, "BB" to playerTurn, "BS" to mutableMapOf("p1" to 1, "p2" to 1, "p3" to 1, "p4" to 1, "p5" to 1, "p6" to 1, "p7" to 1)))
            } else {
                write("Bid", mutableMapOf("BV" to 350, "BT" to 1, "BB" to 1, "BS" to mutableMapOf("p1" to 1, "p2" to 1, "p3" to 1, "p4" to 1, "p5" to 1, "p6" to 1, "p7" to 1)))
            }
            write("BU2", mutableMapOf("b2" to 8, "s2" to 0))
            write("BU", mutableMapOf("b1" to "", "b1s" to "", "b2" to "", "b2s" to ""))
            write("SC", mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0, "p5" to 0, "p6" to 0, "p7" to 0))

        } else if (nPlayers4) {
            write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit))
            write("CH", mutableMapOf("p1" to cardsShuffled.slice(0..12)
                .sortedBy { it }, "p2" to cardsShuffled.slice(13..25)
                .sortedBy { it }, "p3" to cardsShuffled.slice(26..38)
                .sortedBy { it }, "p4" to cardsShuffled.slice(39..51).sortedBy { it }))
            if (getString(R.string.testGameData).contains('n')) {
                write("Bid", mutableMapOf("BV" to 175, "BT" to playerTurn, "BB" to playerTurn, "BS" to mutableMapOf("p1" to 1, "p2" to 1, "p3" to 1, "p4" to 1)))
            } else {
                write("Bid", mutableMapOf("BV" to 175, "BT" to 1, "BB" to 1, "BS" to mutableMapOf("p1" to 1, "p2" to 1, "p3" to 1, "p4" to 1)))
            }
            write("BU", mutableMapOf("b1" to "", "b1s" to "", "b2" to "", "b2s" to ""))
            write("SC", mutableMapOf("p1" to 0, "p2" to 0, "p3" to 0, "p4" to 0))
        }
        write("Tr", "")
        write("R", 1)
        write("RO", mutableMapOf("T" to 1, "P" to 0, "R" to ""))
        write("BU1", mutableMapOf("b1" to 8, "s1" to 0))
        write("GS", 1)
    }

    private fun tablePointsCalculator() {
        if (nPlayers7) tablePoints = cardsPoints[ct1] + cardsPoints[ct2] + cardsPoints[ct3] + cardsPoints[ct4] + cardsPoints[ct5] + cardsPoints[ct6] + cardsPoints[ct7]
        else if (nPlayers4) tablePoints = cardsPoints[ct1] + cardsPoints[ct2] + cardsPoints[ct3] + cardsPoints[ct4]
        val textViewCenterPoints = if (nPlayers4) findViewById<TextView>(R.id.textViewCenterPoints_4)
        else findViewById(R.id.textViewCenterPoints)

        if (tablePoints > 0 && gameState == 5) {
            textViewCenterPoints.text = tablePoints.toString()
            textViewCenterPoints.visibility = View.VISIBLE
        } else {
            textViewCenterPoints.visibility = View.GONE
        }
    }

    private fun updatePlayerScoreInfo(pointsList: List<Int>) {
        if (gameState == 5) {
            var t1 = 0
            var t2 = 0
            if (buFound1 != 0) t1 = pointsList[buPlayer1 - 1]
            if (nPlayers7 && buFound2 != 0 && buPlayer1 != buPlayer2) t2 = pointsList[buPlayer2 - 1] // if not same partners then only add other points
            bidTeamScore = pointsList[bidder - 1] + t1 + t2

            for (i in 0 until nPlayers) {
                val j = i + 1
                if (j == bidder || (j == buPlayer1 && buFound1 != 0) || (j == buPlayer2 && buFound2 != 0)) findViewById<TextView>(refIDMappedTextView[i]).text = playerName(j) + "\n$emojiScore  $bidTeamScore /$bidValue"
                else findViewById<TextView>(refIDMappedTextView[i]).text = playerName(j) + "\n$emojiScore  ${pointsList.sum() - bidTeamScore} /${scoreLimit - bidValue}"
            }
            var tt1 = 0
            var tt2 = 0
            if (buFound1 == 1) tt1 = pointsList[buPlayer1 - 1]
            if (nPlayers7 && buFound2 == 1 && buPlayer1 != buPlayer2) tt2 = pointsList[buPlayer2 - 1] // if not same partners then only add other player points
            val bidTeamScoreFinal = pointsList[bidder - 1] + tt1 + tt2 // total score of bid team
            if (nPlayers4) decideGameWinnerTeam4(bidTeamScoreFinal, totalGamePoints = pointsList.sum())
            else decideGameWinnerTeam7(bidTeamScoreFinal, totalGamePoints = pointsList.sum())
        }
    }

    private fun decideGameWinnerTeam7(bidTeamScoreFinal: Int, totalGamePoints: Int) {
        if (bidTeamScoreFinal >= bidValue) { // bidder team won case
            refGameData.child("RO").removeEventListener(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            toastCenter("Game Over: Bidder team Won \n         Defender team Lost")
            centralText("Game Over: Bidder team Won \n         Defender team Lost")
            speak("Game Over    bidder team won the round")
            if ("p$bidder" == from) { // bidder will change game state to 6
                val pointsListTemp = mutableListOf(gameNumber, -bidValue, -bidValue, -bidValue, -bidValue, -bidValue, -bidValue, -bidValue)
                if (buFound1 != 1 && buFound2 != 1) { //No partners found so far
                    pointsListTemp[bidder] = bidValue * 6
                } else if (buFound1 == 1 && buFound2 != 1) { // only partner 1 found
                    pointsListTemp[bidder] = bidValue * 3
                    pointsListTemp[buPlayer1] = bidValue * 2
                } else if (buFound1 != 1 && buFound2 == 1) { // only partner 2 found
                    pointsListTemp[bidder] = bidValue * 3
                    pointsListTemp[buPlayer2] = bidValue * 2
                } else if (buPlayer1 == buPlayer2 && buPlayer1 == 1) { //both partners found and they are same person
                    pointsListTemp[bidder] = bidValue * 3
                    pointsListTemp[buPlayer1] = bidValue * 2
                } else if (buPlayer1 != buPlayer2 && buFound1 == 1 && buFound2 == 1) { //both partners found and they are different person
                    pointsListTemp[bidder] = bidValue * 2
                    pointsListTemp[buPlayer1] = bidValue
                    pointsListTemp[buPlayer2] = bidValue
                }
                write("S", pointsListTemp) // 0-bidder won, 1 - defenders won??
                write("GS", 6) // dummy - check if need success listner from above write to handle sync issues
            }
        } else if (buFound1 == 1 && buFound2 == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - bidValue)) { // if opponent score has reached target value & both partners are disclosed
            refGameData.child("RO").removeEventListener(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            toastCenter("Game Over: Defender team Won \n         Bidder team Lost")
            centralText("Game Over: Defender team Won \n         Bidder team Lost")
            speak("Game Over    Defender team won the round")
            if ("p$bidder" == from) { // winner will change game state to 6
                val pointsListTemp = mutableListOf<Int>(gameNumber, bidValue, bidValue, bidValue, bidValue, bidValue, bidValue, bidValue)
                if (buPlayer1 == buPlayer2) { // either both partners are same person
                    pointsListTemp[bidder] = -1 * bidValue * 2
                    pointsListTemp[buPlayer1] = -bidValue
                } else {                      // both partners are different person
                    pointsListTemp[bidder] = -1 * bidValue * 2
                    pointsListTemp[buPlayer1] = -bidValue
                    pointsListTemp[buPlayer2] = -bidValue
                }
                write("S", pointsListTemp) // 0-bidder won, 1 - defenders won
                write("GS", 6)
            }
        }
    }

    private fun decideGameWinnerTeam4(bidTeamScoreFinal: Int, totalGamePoints: Int) {
        if (bidTeamScoreFinal >= bidValue) { // bidder team won case
            refGameData.child("RO").removeEventListener(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            toastCenter("Game Over: Bidder team Won \n         Defender team Lost")
            centralText("Game Over: Bidder team Won \n         Defender team Lost")

            if (from == "p$bidder" && buFound1 != 1) {
                if (soundStatus) SoundManager.getInstance().playWonSound() //soundWon.start()
                speak("Well done! ! You won")
            } else if ((from == "p$bidder" || from == "p$buPlayer1") && buFound1 == 1) {
                if (soundStatus) SoundManager.getInstance().playWonSound() //soundWon.start()
                speak("Well done!! Your team has won")
            } else {
                if (soundStatus) SoundManager.getInstance().playLostSound() //soundWon.start()
                speak("Sorry Your team has lost")
            }

            if ("p$bidder" == from) { // bidder will change game state to 6
                val pointsListTemp = mutableListOf(gameNumber, -bidValue, -bidValue, -bidValue, -bidValue)
                if (buFound1 != 1) { //No partners found so far
                    pointsListTemp[bidder] = bidValue * 3 // bidder gets 3 times
                } else if (buFound1 == 1) { // partner 1 found
                    pointsListTemp[bidder] = bidValue * 2
                    pointsListTemp[buPlayer1] = bidValue
                }
                write("S", pointsListTemp) // 0-bidder won, 1 - defenders won??
                write("GS", 6) // dummy - check if need success listener from above write to handle sync issues
            }
        } else if (buFound1 == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - bidValue)) { // if opponent score has reached target value & both partners are disclosed
            refGameData.child("RO").removeEventListener(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            toastCenter("Game Over: Defender team Won \n         Bidder team Lost")
            centralText("Game Over: Defender team Won \n         Bidder team Lost")

            if (from == "p$bidder" || from == "p$buPlayer1") {
                if (soundStatus) SoundManager.getInstance().playLostSound() //soundWon.start()
                speak("Sorry Your team has lost")
            } else {
                if (soundStatus) SoundManager.getInstance().playWonSound() //soundWon.start()
                speak("Well done!! Your team has won")
            }

            if ("p$bidder" == from) { // winner will change game state to 6
                val pointsListTemp = mutableListOf(gameNumber, bidValue, bidValue, bidValue, bidValue)
                pointsListTemp[bidder] = -1 * bidValue * 2
                pointsListTemp[buPlayer1] = -bidValue
                write("S", pointsListTemp) // 0-bidder won, 1 - defenders won
                write("GS", 6)
            }
        }
    }

    private fun displayPartnerIcon() {
        for (i in 0 until nPlayers) { // first reset background and animation of partner icon
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
        }
        if (bidder != 0) { // show single person icon next to bidder
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
        }

        if (buFound1 != 0 && buPlayer1 != 8) {
            if (vibrateStatus) vibrationStart()
            if (buPlayer1 > 0) {
                findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
            }
        }
        if (nPlayers7 && buFound2 != 0 && buPlayer2 != 8) {
            if (vibrateStatus) vibrationStart()
            if (buPlayer2 > 0) {
                findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer2 - 1]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer2 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
            }
        }
        if (gameState == 5) updatePlayerScoreInfo(ptAll)
    }

    private fun countDownTimer(task: String, purpose: String = "start") {
        if (purpose == "start") {
            //            timeCountdown = time
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).progress = 100
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).text = "10s"
            //            findViewById<ProgressBar>(R.id.progressbarTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            //            findViewById<TextView>(R.id.textViewTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            if (task == "Bidding") countDownBidding.start()
            if (task == "PlayCard") countDownPlayCard.start()
        } else if (purpose == "cancel") {
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
            findViewById<TextView>(R.id.textViewTimer).clearAnimation()
            if (task == "Bidding") countDownBidding.cancel()
            if (task == "PlayCard") countDownPlayCard.cancel()
        }

    }

    private fun startPlayingRound() {
        if (!roundStarted) speak("Lets Start Playing!", queue = TextToSpeech.QUEUE_ADD)
        roundStarted = true
        findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
        roundListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null && activityExists) {
                    if (gameTurn != p0.child("T").value.toString()
                            .toInt()) { // if the game turn changes then only proceed
                        playerTurn = p0.child("P").value.toString().toInt()
                        gameTurn = p0.child("T").value.toString().toInt()
                        trumpStart = p0.child("R").value.toString() // trump of start game
                        clearAllAnimation()
                        if (gameTurn == 1) played = false // reset for new round
                        if ((nPlayers7 && gameTurn == 8) || (nPlayers4 && gameTurn == 5)) {
                            Handler(Looper.getMainLooper()).postDelayed({ declareRoundWinner() }, 600)
                        } else if (gameTurn != 8 && gameTurn != 0) {
                            animatePlayer(playerTurn)
                            if (playerTurn == fromInt && !played) {
                                centralText("Please play your next card", 0)
                                displaySelfCards(filter = true)
                                countDownTimer(task = "PlayCard") // start countdown timer and run autoPlayCard
                                if (vibrateStatus) vibrationStart()
                            } else {
                                centralText(cancel = true)
                            }
                        }
                    }
                }
            }
        }
        refGameData.child("RO").addValueEventListener(roundListener)
    }

    private fun autoPlayCard() {
        if (!played) {
            if (gameTurn == 1) {  //can play any random card in 1st chance
                val cardSelected = cardsInHand.random()
                startNextTurn(cardSelected)
            } else { // play only same suit card if not 1st chance
                var cardSelectedIndex = cardsSuit.slice(cardsInHand as Iterable<Int>)
                    .lastIndexOf(trumpStart) // play largest card first
                if (cardSelectedIndex == -1) { //not found same suit card
                    cardSelectedIndex = cardsSuit.slice(cardsInHand as Iterable<Int>)
                        .lastIndexOf(trump) // play trump card
                    if (cardSelectedIndex == -1) {
                        val cardSelected = cardsInHand.random() // or play any random card
                        startNextTurn(cardSelected)
                    } else {
                        val cardSelected = cardsInHand[cardSelectedIndex]
                        startNextTurn(cardSelected)
                    }
                } else {
                    val cardSelected = cardsInHand[cardSelectedIndex]
                    startNextTurn(cardSelected)
                }
            }
        }
    }

    private fun validateSelfPlayedCard(view: View) {
        if (gameState == 5 && "p$playerTurn" == from && gameTurn != 8 && gameTurn != 0) { // dummy error chance - why just 8 and not 5 also ? Investigate later
            val cardSelected = view.tag.toString().toInt()
            if (gameTurn == 1 || cardsSuit[cardSelected] == trumpStart || cardsSuit.slice(cardsInHand as Iterable<Int>)
                    .indexOf(trumpStart) == -1) {
                countDownTimer("PlayCard", purpose = "cancel")
                startNextTurn(cardSelected.toLong()) // allow throw if first chance, or same suit as first turn or doesn't have same suit card
            } else {
                if (soundStatus) SoundManager.getInstance()
                    .playErrorSound() // soundCardPlayed.start()
                if (vibrateStatus) vibrationStart()
                toastCenter("${playerName(playerTurn)}, please play ${getSuitName(trumpStart)} card")
                speak("please play a ${getSuitName(trumpStart)} card")
            }
        }
    }

    private fun startNextTurn(cardSelected: Any) {
        if (!played) {
            played = true
            if (playerTurn != bidder) {
                if (nPlayers7) checkIfPartnerAndUpdateServer7(cardSelected, playerTurn)
                if (nPlayers4) checkIfPartnerAndUpdateServer4(cardSelected, playerTurn)
            }
            refGameData.child("CT/$from").setValue(cardSelected).addOnSuccessListener {
                if (gameTurn == 1) write("RO", mutableMapOf("T" to gameTurn + 1, "P" to nextTurn(fromInt), "R" to cardsSuit[cardSelected.toString()
                    .toInt()]))
                else write("RO", mutableMapOf("T" to gameTurn + 1, "P" to nextTurn(playerTurn), "R" to trumpStart))
            }
            //                .addOnFailureListener{ // try again  - dummy - check if any other way could be done
            //                refGameData.child("CT/$from").setValue(cardSelected).addOnSuccessListener{
            //                    if(gameTurn==1) write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to cardsSuit[cardSelected.toString().toInt()] ))
            //                    else write("RO",mutableMapOf("T" to gameTurn+1,"P" to nextTurn(playerTurn),"R" to trumpStart))
            //                }
            //            }
            cardsInHand.remove(cardSelected)
            if (roundNumber != roundNumberLimit) {
                write("CH/$from", cardsInHand)
                getCardsAndDisplay()
            } else {
                write("CH/$from", "")
                findViewById<LinearLayout>(R.id.imageGallery).removeAllViews() // show no self cards after throwing last card
            }
            write("OL/$from", 1) // Turn them online again
        }
    }

    private fun checkIfPartnerAndUpdateServer4(cardSelected: Any, playerTurn: Int?) {
        if (cardSelected.toString().toInt() == bu1 && buFound1 != 1) {
            if (playerTurn != null) write("BU1", mutableMapOf("b1" to playerTurn, "s1" to 1))
        }
    }

    private fun checkIfPartnerAndUpdateServer7(cardSelected: Any, playerTurn: Int?) {
        if ((cardSelected.toString().toInt() == bu1 * 2 || cardSelected.toString()
                .toInt() == bu1 * 2 + 1) && buFound1 != 1) {
            if (playerTurn != null) {
                write("BU1/b1", playerTurn)
            }
            if (bu1Flag >= 1) { // 1 or 2 --> bidder has either one card or has asked both cards --> lock the partner
                write("BU1/s1", 1)  // locking the partner by 1 else it would be 2
            } else if (bu1Flag == 0) {
                if (buFound1 == 2) write("BU1/s1", 1)  // locking the partner by 1
                if (buFound1 == 0) write("BU1/s1", 2)  // 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
            }
        } else if ((cardSelected.toString().toInt() == bu2 * 2 || cardSelected.toString()
                .toInt() == bu2 * 2 + 1) && buFound2 != 1) {
            if (playerTurn != null) { // null surround check
                write("BU2/b2", playerTurn)
            }
            if (bu2Flag >= 1) { // 1 or 2 --> bidder has either one card or has asked both cards --> lock the partner
                write("BU2/s2", 1)  // locking the partner by 1 else it would be 2
            } else if (bu2Flag == 0) {
                if (buFound2 == 2) write("BU2/s2", 1)  // locking the partner by 1 if previously was 2
                if (buFound2 == 0) write("BU2/s2", 2)  // 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
            }
        }
    }

    private fun declareRoundWinner() {
        val roundCards = if (nPlayers7) listOf(ct1, ct2, ct3, ct4, ct5, ct6, ct7)
        else listOf(ct1, ct2, ct3, ct4)
        var winnerCard = roundCards[playerTurn - 1]
        var startTurn = playerTurn
        for (i in 1 until nPlayers) {
            startTurn = nextTurn(startTurn)
            if (roundCards[startTurn - 1] != 53 && winnerCard != 53) winnerCard = compareCardsForWinner(roundCards[startTurn - 1], winnerCard) // dummy check whats causes to declare round winner earlier
        }
        roundWinner = roundCards.indexOf(winnerCard) + 1
        animatePlayer(roundWinner)
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
        Handler(Looper.getMainLooper()).postDelayed({ animateWinner() }, 550)
        Handler(Looper.getMainLooper()).postDelayed({ // start after 1.5 seconds
            if (roundNumber < roundNumberLimit) {
                if (roundWinner == fromInt) { // only winner can start next round
                    startNextRound()
                } else {
                    centralText("Waiting for ${playerName(roundWinner)} to play next card", 3500)
                }
            } else if (roundNumber == roundNumberLimit) {
                try {
                    refGameData.child("RO").removeEventListener(roundListener)
                } finally {
                    clearAllAnimation()
                }
                if ("p$roundWinner" == from) { // winner will change game state to 6
                    //                        write("GS",6)
                    endGameRound() // update points of last round to server by winner
                }
            }
        }, 1500)
    }

    private fun endGameRound() {
        //        if(buFound1 == 2) write("BU1/s1",1)  // locking the partner by 1
        //        if(buFound2 == 2) write("BU2/s2",1)  // locking the partner by 1
        if (nPlayers7) write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit, "p5" to cardsIndexLimit, "p6" to cardsIndexLimit, "p7" to cardsIndexLimit))
        if (nPlayers4) write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit))
        write("SC/p$roundWinner", tablePoints + ptAll[roundWinner - 1])
    }

    private fun startNextRound() {
        if (buFound1 == 2) write("BU1/s1", 1)  // locking the partner found but not confirmed by 1
        if (nPlayers7 && buFound2 == 2) write("BU2/s2", 1)  // locking the partner found but not confirmed by 1
        //        write("CT", mutableMapOf("p1" to cardsIndexLimit,"p2" to cardsIndexLimit,"p3" to cardsIndexLimit,"p4" to cardsIndexLimit,"p5" to cardsIndexLimit,"p6" to cardsIndexLimit,"p7" to cardsIndexLimit))
        if (nPlayers7) write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit, "p5" to cardsIndexLimit, "p6" to cardsIndexLimit, "p7" to cardsIndexLimit))
        if (nPlayers4) write("CT", mutableMapOf("p1" to cardsIndexLimit, "p2" to cardsIndexLimit, "p3" to cardsIndexLimit, "p4" to cardsIndexLimit))
        write("SC/p$roundWinner", tablePoints + ptAll[roundWinner - 1]) // add table points to the round winner player
        //        write("RO/P",roundWinner) // write next player turn to be round player turn
        //        write("RO/R","")
        //        write("RO/T",1)
        write("R", roundNumber + 1) // increment round number by 1
        write("RO", mutableMapOf("T" to 1, "P" to roundWinner, "R" to ""))
        //        centralText("Please play your next card",2000)
    }

    private fun compareCardsForWinner(currentCard: Int, winnerCard: Int): Int {
        var w = winnerCard
        val wSuit = cardsSuit[winnerCard]
        val cSuit = cardsSuit[currentCard]
        if ((cSuit == trump && wSuit != trump) || ((cSuit == wSuit) && ((currentCard - winnerCard) >= 1 || (nPlayers7 && currentCard % 2 == 1 && winnerCard == currentCard - 1) || (nPlayers7 && winnerCard % 2 == 1 && currentCard == winnerCard - 1)))) w = currentCard
        return w
    }

    private fun getSuitName(ini: String): String {
        var suit = ""
        when (ini) {
            "H" -> suit = "Heart"
            "D" -> suit = "Diamond"
            "S" -> suit = "Spade"
            "C" -> suit = "Club"
        }
        return suit
    }

    private fun displaySelfCards(view: View = View(applicationContext), animations: Boolean = false, filter: Boolean = false, bidingRequest: Boolean = false) {
        findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        gallery.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(applicationContext)
        val typedValue = TypedValue()
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
        for (x: Long in cardsInHand) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list, gallery, false)
            if (x == cardsInHand[cardsInHand.size - 1]) {
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                    .setPaddingRelative(0, 0, 0, 0)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).layoutParams.width = resources.getDimensionPixelSize(R.dimen.widthDisplayCardLast)
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                .setImageResource(cardsDrawable[x.toInt()])
            if (filter && gameTurn > 1 && cardsSuit[x.toInt()] != trumpStart && cardsSuit.slice(cardsInHand as Iterable<Int>)
                    .indexOf(trumpStart) != -1) {
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
            } else if (filter && gameTurn > 1 && cardsSuit.slice(cardsInHand as Iterable<Int>)
                    .indexOf(trumpStart) != -1) {
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                    .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite_active_cards))
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            } else viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)

            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).tag = x.toString() // tag the card number to the image
            if (cardsPoints.elementAt(x.toInt()) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).text = "${cardsPoints.elementAt(x.toInt())}"
                if (animations) viewTemp.findViewById<TextView>(R.id.textViewDisplayCard)
                    .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale))
                if (animations) viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                    .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw_self_cards))
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).visibility = View.GONE
            }
            //            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ContextCompat.getDrawable(applicationContext,typedValue.resourceId)
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                .setOnClickListener(View.OnClickListener {
                    validateSelfPlayedCard(it)
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
                        .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
                })
            gallery.addView(viewTemp)
        }
        if (animations) {
            findViewById<RelativeLayout>(R.id.horizontalScrollView).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
        }
        if (bidingRequest && activityExists) {
            Handler(Looper.getMainLooper()).postDelayed({ startBidding() }, 800)
        }
    }

    private fun clearAllAnimation() {
        for (i in 0 until nPlayers) { // first reset background and animation
            //            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
        }
        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
        findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
    }

    private fun animateWinner() {
        if (soundStatus) SoundManager.getInstance()
            .playCardCollectSound() //        soundCollectCards.start()
        if (nPlayers7) {
            findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.VISIBLE
            if (roundWinner > 0) findViewById<ImageView>(R.id.imageViewWinnerCenter).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableWinnerAnim[roundWinner - 1]))
            Handler(Looper.getMainLooper()).postDelayed({ findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.GONE }, 1000)
        } else if (nPlayers4) {
            findViewById<ImageView>(R.id.imageViewWinnerCenter_4).visibility = View.VISIBLE
            if (roundWinner > 0) findViewById<ImageView>(R.id.imageViewWinnerCenter_4).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableWinnerAnim[roundWinner - 1]))
            Handler(Looper.getMainLooper()).postDelayed({ findViewById<ImageView>(R.id.imageViewWinnerCenter_4).visibility = View.GONE }, 1000)
        }
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1]).clearAnimation()
        for (i in 0 until nPlayers) {
            findViewById<ImageView>(refIDMappedTableImageView[i]).visibility = View.INVISIBLE
        }
    }

    private fun getBuddyAndDisplay() {
        refGameData.child("BU").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(BU: DataSnapshot) {
                bu1 = BU.child("b1").value.toString().toInt()
                bu1Flag = BU.child("b1s").value.toString().toInt()
                if (nPlayers7) {
                    bu2 = BU.child("b2").value.toString().toInt()
                    bu2Flag = BU.child("b2s").value.toString().toInt()
                }
                if (vibrateStatus) vibrationStart()
                trumpImage1.setImageResource(cardsDrawablePartner[bu1])
                trumpImage1.clearAnimation()
                findViewById<TextView>(R.id.trumpText1).clearAnimation()
                if (nPlayers7) {
                    findViewById<ImageView>(R.id.trumpImage2).setImageResource(cardsDrawablePartner[bu2])
                    findViewById<ImageView>(R.id.trumpImage2).clearAnimation()
                    findViewById<TextView>(R.id.trumpText2).clearAnimation()
                }
                if (nPlayers7 && bu1 == bu2) {
                    findViewById<TextView>(R.id.trumpText1).text = getString(R.string.bothPartner)
                    findViewById<TextView>(R.id.trumpText2).text = getString(R.string.bothPartner)
                } else {
                    if (bu1Flag == 1 && nPlayers7) findViewById<TextView>(R.id.trumpText1).text = getString(R.string.onlyPartner)
                    if (bu1Flag == 0 && nPlayers7) findViewById<TextView>(R.id.trumpText1).text = getString(R.string.anyPartner)
                    if (nPlayers7 && bu2Flag == 1) findViewById<TextView>(R.id.trumpText2).text = getString(R.string.onlyPartner)
                    if (nPlayers7 && bu2Flag == 0) findViewById<TextView>(R.id.trumpText2).text = getString(R.string.anyPartner)
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
                if (bidder == fromInt) {  // only to bidder
                    findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE // make selection frame visible
                    findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
                    if (nPlayers7) {
                        findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1)
                        speak("Please Choose your first partner card")
                    } //choose 1st buddy text
                    if (nPlayers4) {
                        findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1_4)
                        speak("Please Choose your partner card")
                    } //choose 1st buddy text
                    displayAllCardsForPartnerSelection()  // display all the cards to choose from
                } else {  // to everyone else
                    speak("Waiting for ${playerName(bidder)} to select partner card")
                    if (nPlayers7) {
                        centralText("Waiting for ${playerName(bidder)} \nto select 2 partners card", 0)
                    } else {
                        centralText("Waiting for ${playerName(bidder)} \nto select 1 partner card", 0)
                    }
                }
            }
        })
    }

    private fun partnerSelectClick4(cardSelected: Int) { // assumption is cardsinHand already updated
        if ((cardsInHand as List<*>).contains((cardSelected).toLong())) {
            if (soundStatus) SoundManager.getInstance().playErrorSound() //
            if (vibrateStatus) vibrationStart()
            toastCenter("$selfName, You already have the same card")
            speak("You already have the same card. Please choose other card", speed = 1.1f)
        } else {
            write("BU/b1", cardSelected)
            write("BU/b1s", 1)  // bidder has one of card in his hand
            write("GS", 5) // change game state to next playing round
            findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
            Handler(Looper.getMainLooper()).postDelayed({
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).clearAnimation()
            }, 240)
        }
    }

    private fun partnerSelectClick7(cardSelected: Int) { // assumption is cardsinHand already updated
        if (counterPartnerSelection == 0) {
            when {
                (cardsInHand as List<*>).contains((cardSelected * 2).toLong()) and (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong()) -> {
                    if (soundStatus) SoundManager.getInstance()
                        .playErrorSound() // soundCardPlayed.start()
                    if (vibrateStatus) vibrationStart()
                    toastCenter("$selfName, You already have both of these cards ")
                }
                (cardsInHand as List<*>).contains((cardSelected * 2).toLong()) or (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong()) -> {
                    if (soundStatus) SoundManager.getInstance()
                        .playUpdateSound() // soundCardPlayed.start()
                    write("BU/b1", cardSelected)
                    bu1 = cardSelected
                    bu1Flag = 1
                    trumpImage1.setImageResource(cardsDrawablePartner[cardSelected])
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2) //choose 2nd buddy
                    speak("Please choose your second partner card")
                    counterPartnerSelection = 1
                }
                else -> {
                    if (soundStatus) SoundManager.getInstance()
                        .playUpdateSound() // soundCardPlayed.start()
                    bu1 = cardSelected
                    write("BU/b1", bu1)
                    bu1Flag = 0
                    trumpImage1.setImageResource(cardsDrawablePartner[cardSelected])
                    findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2) //choose 2nd buddy
                    speak("Please choose your second partner card")
                    counterPartnerSelection = 1
                }
            }
        } else if (counterPartnerSelection == 1) {
            if ((cardsInHand as List<*>).contains((cardSelected * 2).toLong()) and (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong())) {
                if (soundStatus) SoundManager.getInstance().playErrorSound() //
                if (vibrateStatus) vibrationStart()
                toastCenter("$selfName, You already have both of same cards")
                speak("Please choose other card")
            } else if ((cardsInHand as List<*>).contains((cardSelected * 2).toLong()) or (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong())) {
                if (bu1 != cardSelected) {
                    write("BU/b2", cardSelected)
                    write("BU/b2s", 1)  // bider has one of card in his hand
                    write("BU/b1s", bu1Flag)
                    write("GS", 5) // change game state to next playing round
                    counterPartnerSelection = 0
                    findViewById<FrameLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                    Handler(Looper.getMainLooper()).postDelayed({
                        findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                        findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).clearAnimation()
                    }, 240)
                } else {
                    if (soundStatus) SoundManager.getInstance().playErrorSound() //
                    if (vibrateStatus) vibrationStart()
                    toastCenter("You already have and choosen same card")
                    speak("Please choose other card")
                }
            } else {
                write("BU/b2", cardSelected)
                if (bu1 == cardSelected) {
                    write("BU/b2s", 2)  // bider has none in his hands and both same selected
                    write("BU/b1s", 2)
                } else {
                    write("BU/b2s", 0)  // bider has none in his hands and is different than 1st
                    write("BU/b1s", bu1Flag) // bider has none in his hands and first one status remains unchanged
                }
                write("GS", 5)
                counterPartnerSelection = 0
                findViewById<LinearLayout>(R.id.partnerSelectionGallery).clearAnimation()
                findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
            }
        }

    }

    private fun displayAllCardsForPartnerSelection(view: View = View(applicationContext)) {
        findViewById<LinearLayout>(R.id.partnerSelectionGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.partnerSelectionGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in cardsIndexSortedPartner) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list_partner, gallery, false)
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner)
                .setImageResource(cardsDrawablePartner[x])
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).tag = x.toString() //set tag to every card of its own value
            viewTemp.findViewById<TextView>(R.id.textViewPartner).text = "D"
            if (cardsPointsPartner.elementAt(x) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).text = "${cardsPointsPartner.elementAt(x)} pts"
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).visibility = View.GONE
            } // make it invisible
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).setOnClickListener {
                if (nPlayers7) partnerSelectClick7(x)
                else if (nPlayers4) partnerSelectClick4(x)
                viewTemp.findViewById<ImageView>(R.id.imageViewPartner)
                    .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
            }
            gallery.addView(viewTemp)
        }
        findViewById<LinearLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right_selection_cards))

    }

    private fun displayTrumpCard() {
        when (trump) {
            "H" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_hearts))
                findViewById<TextView>(R.id.trumpText).text = "Heart"
            }
            "S" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_spades))
                findViewById<TextView>(R.id.trumpText).text = "Spade"

            }
            "D" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_diamonds))
                findViewById<TextView>(R.id.trumpText).text = "Diamond"

            }
            "C" -> {
                findViewById<GifImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_clubs))
                findViewById<TextView>(R.id.trumpText).text = "Club"

            }
        }
        findViewById<GifImageView>(R.id.trumpImage).clearAnimation() // main trump showing view
    } // just displaying trump card

    private fun startTrumpSelection() {
        textViewBidValue.textColor = ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
        findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
        if (bidder != fromInt) {     //  show to everyone except bidder
            toastCenter("${playerName(bidder)} won the bid round")
            speak("${playerName(bidder)} won bid round. Waiting for ${playerName(bidder)} to choose trump")
            centralText("Waiting for ${playerName(bidder)} \n to choose Trump", 0)
        } else { // show to bidder only
            centralText("Well done! ${playerName(bidder)} \n You won the bid round", 0)
            speak("Well done!! Please choose your trump now", speed = 1.1f, queue = TextToSpeech.QUEUE_ADD)
            findViewById<FrameLayout>(R.id.frameTrumpSelection).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
            trumpAnimation("start")
            if (vibrateStatus) vibrationStart()
        }
    }

    fun onTrumpSelectionClick(view: View) {
        if (soundStatus) SoundManager.getInstance().playUpdateSound() //
        when (view.tag) {
            "h" -> write("Tr", "H")
            "s" -> write("Tr", "S")
            "d" -> write("Tr", "D")
            "c" -> write("Tr", "C")
        }
        write("GS", 4)
        findViewById<FrameLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<FrameLayout>(R.id.frameTrumpSelection).visibility = View.GONE
            findViewById<FrameLayout>(R.id.frameTrumpSelection).clearAnimation()
        }, 230)
        trumpAnimation("clear")
    }

    private fun trumpAnimation(task: String) {
        if (task == "start") {
            findViewById<ImageView>(R.id.imageViewTrumpHearts).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpSpades).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<ImageView>(R.id.imageViewTrumpClubs).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
        } else if (task == "clear") {
            findViewById<ImageView>(R.id.imageViewTrumpHearts).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpSpades).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).clearAnimation()
            findViewById<ImageView>(R.id.imageViewTrumpClubs).clearAnimation()
            findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        }
    }

    private fun startBidding() {
        bidingTurnListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataLoad: DataSnapshot) {
                if (dataLoad.value != null && activityExists) {
                    if (playerTurn != (dataLoad.child("BT").value as Long).toInt()) {
                        val bidSpeak = bidValue != (dataLoad.child("BV").value as Long).toInt()
                        bidValue = (dataLoad.child("BV").value as Long).toInt()
                        playerTurn = (dataLoad.child("BT").value as Long).toInt()
                        bidder = (dataLoad.child("BB").value as Long).toInt()
                        val bidStatus = (dataLoad.child("BS/p$playerTurn").value as Long).toInt()
                        if (!bidingStarted) {
                            bidNowImage.visibility = View.VISIBLE
                            centralText("${playerName(playerTurn)} will start bidding", 0) //display message only first time
                            if (playerTurn != fromInt) speak("${playerName(playerTurn)} will start bidding", speed = 1.05f)
                            else speak("${playerName(playerTurn)}, You will start bidding round", speed = 1.05f)
                        } else {
                            centralText("Waiting for ${playerName(playerTurn)} to bid", 0) //display message always
                        }
                        if (bidSpeak && bidingStarted && soundStatus) speak("${playerName(bidder)} has raised bid to $bidValue", speed = 1f)
                        else if (soundStatus) SoundManager.getInstance().playUpdateSound() //

                        textViewBidValue.text = bidValue.toString() //.toString() //show current bid value
                        findViewById<TextView>(R.id.textViewBider).text = playerName(bidder)
                        textViewBidValue.textColor = ContextCompat.getColor(applicationContext, R.color.font_yellow)
                        findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                        findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE //biding frame invisible
                        resetBackgroundAnimationBidding(dataLoad) //set all background to black or red depending on status
                        if (bidder > 0) {
                            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
                            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
                        }
                        if(playerTurn>0) {
                            val tView: ImageView = findViewById(refIDMappedImageView[playerTurn - 1])
                            bidNowImage.animate().x(tView.x).y(tView.y).duration = 450
                        }
                        animatePlayer(playerTurn)  // animate current player
                        if (bidStatus == 1 && playerTurn > 0) {  // highlight current player
                            if (playerTurn > 0) findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[playerTurn - 1]).visibility = View.VISIBLE
                        }
                        if ("p$playerTurn" == from && (bidder != playerTurn || !bidingStarted)) {
                            if (bidStatus == 1) { // show bid frame and ask to bid or pass
                                findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                                findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is ciritical
                                findViewById<FrameLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
                                bidded = false
                                bidButtonsAnimation("start")
                                countDownTimer("Bidding", purpose = "start")
                                if (vibrateStatus) vibrationStart()
                            } else if (bidStatus == 0) {
                                bidded = true
                                //                                toastCenter(" Sorry $selfName \n You cannot bid anymore")
                                write("Bid/BT", nextTurn(fromInt))
                            }
                        }
                        if ("p$playerTurn" == from && bidder == playerTurn && bidingStarted) { // finish bid and move to next game state
                            write("RO/P", bidder)  // write player turn to bidder
                            write("GS", 3) // change game state to 3 as biding is finished
                            centralText("Well done! ${playerName(bidder)} \n You won the bid round", 0)
                        }
                    }
                    bidingStarted = true
                }
            }
        }
        if (activityExists) refGameData.child("Bid").addValueEventListener(bidingTurnListener)
    }

    fun askToBid(view: View) {
        if (!bidded) {
            countDownTimer("Bidding", purpose = "cancel")
            bidded = true
            if (soundStatus) SoundManager.getInstance().playUpdateSound() //
            when (view.tag) {
                "pass" -> {
                    write("Bid/BS/$from", 0)
                    //                    write("Bid", mutableMapOf("BV" to bidValue, "BT" to nextTurn(fromInt),"BB" to fromInt,"BS" to mutableMapOf("p1" to 1,"p2" to 1,"p3" to 1,"p4" to 1)))
                    centralText("You passed the biding chance !!  \n You cannot bid anymore", 2500)
                }
                "5" -> {
                    write("Bid/BV/", bidValue.plus(5))
                    write("Bid/BB", playerTurn)
                }
                "10" -> {
                    write("Bid/BV/", bidValue.plus(10))
                    write("Bid/BB", playerTurn)
                }
                "20" -> {
                    write("Bid/BV/", bidValue.plus(20))
                    write("Bid/BB", playerTurn)
                }
                "50" -> {
                    write("Bid/BV/", bidValue.plus(50))
                    write("Bid/BB", playerTurn)
                }
            }
            write("Bid/BT", nextTurn(fromInt))
            findViewById<FrameLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
            Handler(Looper.getMainLooper()).postDelayed({
                findViewById<FrameLayout>(R.id.frameAskBid).visibility = View.GONE
                findViewById<FrameLayout>(R.id.frameAskBid).clearAnimation()
            }, 230)
            //        bidButtonsAnimation("clear")
        }
    }

    private fun speak(speechText: String, pitch: Float = 0.95f, speed: Float = 1f, queue: Int = TextToSpeech.QUEUE_FLUSH) {
        if (soundStatus) {
            textToSpeech.setPitch(pitch)
            textToSpeech.setSpeechRate(speed)
            textToSpeech.speak(speechText, queue, null, null)
        }
    }

    private fun initializeSpeechEngine() {
        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastCenter("Missing Language data - Text to speech")
                }
            }
        })
    }

    private fun bidButtonsAnimation(task: String) {
        if (task == "start") {
            findViewById<Button>(R.id.bid05button).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid10button).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid20button).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bid50button).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
            findViewById<Button>(R.id.bidpassbutton).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
        } else if (task == "clear") {
            findViewById<Button>(R.id.bid05button).clearAnimation()
            findViewById<Button>(R.id.bid10button).clearAnimation()
            findViewById<Button>(R.id.bid20button).clearAnimation()
            findViewById<Button>(R.id.bid50button).clearAnimation()
            findViewById<Button>(R.id.bidpassbutton).clearAnimation()
        }
    }

    private fun animatePlayer(index: Int) {
        if (index > 0) findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[index - 1]).visibility = View.VISIBLE
    }

    private fun resetBackgroundAnimationBidding(dataLoad: DataSnapshot) {
        for (i in 0 until nPlayers) {
            val iPlayer = i + 1
            val bidStatus = (dataLoad.child("BS/p$iPlayer").value as Long).toInt()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
            //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
            if (bidStatus == 0) {
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer2))
                findViewById<ImageView>(refIDMappedImageView[i]).foreground = ContextCompat.getDrawable(applicationContext, R.drawable.pass)
                if ("p$iPlayer" == from) findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer2))
            } else {
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
                if ("p$iPlayer" == from) findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            }
            if ("p$iPlayer" == from) findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        }
    }

    private fun finishPassOverlay() {
        for (i in 0 until nPlayers) {
            findViewById<ImageView>(refIDMappedImageView[i]).foreground = null
        }
        bidNowImage.visibility = View.GONE
    }

    private fun finishBackgroundAnimationBidding() {  //clear Everything on finish of biding round
        for (i in 0 until nPlayers) {
            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
            //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
        }
        findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
        findViewById<LinearLayout>(R.id.imageGallery).clearAnimation()
        //        findViewById<ImageView>(refIDMappedImageView[bidder -1]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)) // highlight bidder winner
        if (bidder > 0) findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[bidder - 1]).visibility = View.VISIBLE
        //        findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[bidder-1]).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_appeal_large))

//        textViewBidValue.clearAnimation()
        findViewById<TextView>(R.id.textViewBider).clearAnimation()
        if (bidder > 0) {
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
        }
    }

    private fun centralText(message: String = "", displayTime: Int = 3000, cancel: Boolean = false) {
        if (cancel) {
            textViewShuffling.clearAnimation()
            textViewShuffling.text = ""
            textViewShuffling.visibility = View.GONE
        } else {
            textViewShuffling.visibility = View.VISIBLE
            textViewShuffling.text = message
            textViewShuffling.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_infinite_700ms))
            if (displayTime != 0) Handler(Looper.getMainLooper()).postDelayed({
                textViewShuffling.clearAnimation()
                textViewShuffling.visibility = View.GONE
            }, displayTime.toLong())
        }
    }

    private fun animateElements() {
        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
        findViewById<TextView>(R.id.textViewBider).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_infinite_700ms))
//        textViewBidValue.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_infinite_700ms))
        trumpImage1.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_hanging))
        findViewById<ImageView>(R.id.trumpImage2).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_hanging))
        findViewById<GifImageView>(R.id.trumpImage).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_hanging))
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
        if (nPlayers == 7) {
            p5 = playerInfo[4]
            p6 = playerInfo[5]
            p7 = playerInfo[6]
            p5Coins = playerInfoCoins[4]
            p6Coins = playerInfoCoins[5]
            p7Coins = playerInfoCoins[6]
        }
        updatePlayerNames()
        for (i in 0 until nPlayers) {
            val j = i + nPlayers
            if (playerInfo[j].isNotEmpty()) {
                Picasso.get().load(playerInfo[j]).resize(300, 300).centerCrop().error(R.drawable.s3)
                    .into(findViewById<ImageView>(refIDMappedImageView[i]))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlayerNames() {
        val totalCoins = if (nPlayers7) listOf(p1Coins, p2Coins, p3Coins, p4Coins, p5Coins, p6Coins, p7Coins)
        else listOf(p1Coins, p2Coins, p3Coins, p4Coins)
        for (i in 0 until nPlayers) {
            findViewById<TextView>(refIDMappedTextView[i]).text = playerName(i + 1) + "\n${emojiMoney}${String.format("%,d", totalCoins[i])}"
        }
    }

    private fun shufflingWindow(time: Long = 4900, fadeOffTime: Long = 500, gameStateChange: Boolean = false) {
        if (soundStatus) Handler(Looper.getMainLooper()).postDelayed({
            SoundManager.getInstance().playShuffleSound() // soundShuffle.start()
        }, 400) //delayed sound play of shuffling
        displayShufflingCards() //show suits cards and animate
        centralText(getString(R.string.shufflingcards), 5200)
        speak("Please wait while i Shuffle cards")
        Handler(Looper.getMainLooper()).postDelayed({
            if (nPlayers4) {
                findViewById<ImageView>(R.id.imageViewWinnerCenter_4).animation = null
                findViewById<ImageView>(R.id.imageViewWinnerCenter_4).clearAnimation()
                findViewById<ImageView>(R.id.imageViewWinnerCenter_4).visibility = View.GONE
            } else if (nPlayers7) {
                findViewById<ImageView>(R.id.imageViewWinnerCenter).animation = null
                findViewById<ImageView>(R.id.imageViewWinnerCenter).clearAnimation()
                findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.GONE
            }
            findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.GONE
            findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_out))
            Handler(Looper.getMainLooper()).postDelayed({
                //                if(fromInt == 1 && gameStateChange) write("GS",2) // Update Game State to start Biding round by Host only
                displaySelfCards(animations = true, bidingRequest = true)
            }, fadeOffTime)
        }, time)
    }

    private fun displayShufflingCards(view: View = View(applicationContext), sets: Int = 5, distribute: Boolean = true) {
        if (distribute) shufflingDistribute()
        val gallery = findViewById<LinearLayout>(R.id.imageGallery)
        gallery.removeAllViews()
        val inflater = LayoutInflater.from(applicationContext)
        for (xx: Int in 0 until sets) {
            for (x: Int in 0..3) {
                val viewTemp = inflater.inflate(R.layout.cards_item_list_suits, gallery, false)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
                    .setImageResource(PlayingCards().suitsDrawable[x])
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
                    .startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw))
                if (x % 2 != 0) {
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
                        .setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.cardsBackgroundDark))
                } else {
                    viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
                        .setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.cardsBackgroundLight))
                }
                //                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1).setOnClickListener {
                //                    viewTemp.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.clockwise_ccw))
                //                }
                gallery.addView(viewTemp)
            }
        }
        gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right))
    }

    private fun shufflingDistribute() {
        findViewById<RelativeLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
        if (nPlayers7) {
            findViewById<ImageView>(R.id.imageViewWinnerCenter).visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_shuffle_7)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    findViewById<ImageView>(R.id.imageViewWinnerCenter).startAnimation(anim)
                }

                override fun onAnimationStart(animation: Animation?) {}
            })
            findViewById<ImageView>(R.id.imageViewWinnerCenter).startAnimation(anim)
        } else if (nPlayers4) {
            findViewById<ImageView>(R.id.imageViewWinnerCenter_4).visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_shuffle_4)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    findViewById<ImageView>(R.id.imageViewWinnerCenter_4).startAnimation(anim)
                }

                override fun onAnimationStart(animation: Animation?) {}
            })
            findViewById<ImageView>(R.id.imageViewWinnerCenter_4).startAnimation(anim)
        }
    }

    private fun write(path: String, value: Any) {
        if (activityExists) refGameData.child(path).setValue(value)
    }

    private fun getCardsAndDisplay(player: String = from, display: Boolean = true, animation: Boolean = false) {
        refGameData.child("CH/$player").  // display the host info in joining room screen
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(errorDataLoad: DatabaseError) {}
            override fun onDataChange(dataLoad: DataSnapshot) {
                if (dataLoad.value.toString().isNotEmpty() && dataLoad.value.toString() != "null") {
                    cardsInHand = (dataLoad.value as MutableList<*>).filterNotNull() as MutableList<Long>
                    if (display) displaySelfCards(animations = animation)
                }
            }
        })
    }

    private fun toastCenter(message: String) {
        toast.setText(message)
        toast.show()
    }

    private fun playerName(index: Int): String {
        var name = ""
        when (index) {
            1 -> name = p1
            2 -> name = p2
            3 -> name = p3
            4 -> name = p4
            5 -> name = p5
            6 -> name = p6
            7 -> name = p7
        }
        return name
    }

    private fun nextTurn(current: Int): Int {
        var next = 0
        if ((current != 7 && nPlayers7) || (current != 4 && nPlayers4)) {
            next = current + 1
        } else if ((current == 7 && nPlayers7) || (current == 4 && nPlayers4)) next = 1
        return next
    }

    fun openCloseChatWindow(view: View) {
        if (findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility == View.VISIBLE) { //close chat display
            hideKeyboard()
            findViewById<RelativeLayout>(R.id.chatLinearLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_chat_close))
            Handler(Looper.getMainLooper()).postDelayed({
                findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.GONE
            }, 140)
        } else { //open chat display
            counterChat = 0 // reset chat counter to 0
            findViewById<TextView>(R.id.textViewChatNo).visibility = View.GONE // make counter invisible
            findViewById<TextView>(R.id.textViewChatNo).clearAnimation() // clear counter animation
            findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.chatLinearLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_chat_open))
        }
    }

    fun openCloseScoreSheet(view: View) {
        if (findViewById<ScrollView>(R.id.scrollViewScore).visibility == View.VISIBLE) {
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE

            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_scoretable_close))
            Handler(Looper.getMainLooper()).postDelayed({
                findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.GONE
            }, 140)
            scoreOpenStatus = false
        } else {
            scoreOpenStatus = true
            if (scoreSheetNotUpdated) {
                scoreBoardTable(display = false, data = createScoreTableHeader(), upDateHeader = true)
                scoreBoardTable(display = false, data = createScoreTableTotal(), upDateTotal = true)
            }
            scoreSheetNotUpdated = false
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
        }
    }

    fun sendChat(view: View) {
        //        val uni = 0x1F60A
        ////        val emoji = String(Character.toChars(uni))
        ////        write("M", "$selfName : $emoji  ")
        if (findViewById<EditText>(R.id.editTextChatInput).text.toString().isNotEmpty()) {
            findViewById<EditText>(R.id.editTextChatInput).text
            refRoomFirestore.document(roomID + "_chat")
                .set(hashMapOf("M" to "$selfName : ${findViewById<EditText>(R.id.editTextChatInput).text}  "))
            findViewById<EditText>(R.id.editTextChatInput).setText("")
        }
    }

    private fun getSharedPrefs() {
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
            if (!premiumStatus) initializeAds()
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
        }
        if (sharedPreferences.contains("rated")) {
            rated = sharedPreferences.getBoolean("rated", false)
        } else {
            editor.putBoolean("rated", rated)
            editor.apply()
        }
        if (!sharedPreferences.contains("ratingRequestDate")) {
            ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                .toInt() + requestRatingAfterDays
            editor.putInt("ratingRequestDate", ratingRequestDate)
            editor.apply()
        } else {
            ratingRequestDate = sharedPreferences.getInt("ratingRequestDate", 0)
        }
    }

    @SuppressLint("NewApi")
    private fun vibrationStart(duration: Long = 200) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    private fun initializeAds() {
        if (!premiumStatus) {
            findViewById<AdView>(R.id.addViewGameScreenBanner).visibility = View.VISIBLE
            findViewById<AdView>(R.id.addViewGameScreenBanner).loadAd(AdRequest.Builder().build())
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).visibility = View.VISIBLE
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).loadAd(AdRequest.Builder()
                .build())
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = resources.getString(R.string.interstitial)
            mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() { // dummy - check if at some other places ads is shown-conflict with ads closed  - no start next game button needs to be added here
                    logFirebaseEvent("game_screen", 1, "watched_ad")
                    mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the ad again
                    if (fromInt == 1) {
                        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
                        findViewById<AppCompatButton>(R.id.startNextRoundButton).visibility = View.VISIBLE
                        findViewById<AppCompatButton>(R.id.startNextRoundButton).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
                    }
                    mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD again after loading first time
                }
            }
        } else {
            findViewById<AdView>(R.id.addViewGameScreenBanner).visibility = View.GONE
            findViewById<AdView>(R.id.addViewChatGameScreenBanner).visibility = View.GONE
        }
    }

    fun closeGameRoom(view: View) {
        if (activityExists && (fromInt == 1 || view.tag == "clicked")) refGameData.child("OL/$from")
            .setValue(2) // only host says offline if(fromInt == 1)  write("OL/$from",2) // only host says offline
        activityExists = false
        countDownBidding.cancel()
        countDownPlayCard.cancel()
        finish()
        startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) })
        overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
    }

    override fun onStop() {
        super.onStop()
        refGameData.child("GS").removeEventListener(gameStateListener)
        try {
            refGameData.child("RO").removeEventListener(roundListener)
        } catch (me: Exception) {
            //            toastCenter(me.toString()) // dummy think to implement a good way
        }
        try {
            refGameData.child("Bid").removeEventListener(bidingTurnListener)
        } catch (me: Exception) {
            //            toastCenter(me.toString()) // dummy think to implement a good way
        }
        refGameData.child("BU1").removeEventListener(partnerListener1)
        refGameData.child("R").removeEventListener(roundNumberListener)
        chatRegistration.remove()
        refGameData.child("SC/p1").removeEventListener(pointsListener1)
        refGameData.child("SC/p2").removeEventListener(pointsListener2)
        refGameData.child("SC/p3").removeEventListener(pointsListener3)
        refGameData.child("SC/p4").removeEventListener(pointsListener4)

        refGameData.child("CT/p1")
            .removeEventListener(cardsOnTableListener1) // player 1 cards on table listener
        refGameData.child("CT/p2")
            .removeEventListener(cardsOnTableListener2) // player 1 cards on table listener
        refGameData.child("CT/p3")
            .removeEventListener(cardsOnTableListener3) // player 1 cards on table listener
        refGameData.child("CT/p4")
            .removeEventListener(cardsOnTableListener4) // player 1 cards on table listener

        refGameData.child("OL/p1").removeEventListener(onlineStatusListener1)
        refGameData.child("OL/p2").removeEventListener(onlineStatusListener2)
        refGameData.child("OL/p3").removeEventListener(onlineStatusListener3)
        refGameData.child("OL/p4").removeEventListener(onlineStatusListener4)

        if (nPlayers7) {
            refGameData.child("BU2").removeEventListener(partnerListener2)
            refGameData.child("SC/p5").removeEventListener(pointsListener5)
            refGameData.child("SC/p6").removeEventListener(pointsListener6)
            refGameData.child("SC/p7").removeEventListener(pointsListener7)
            refGameData.child("CT/p5")
                .removeEventListener(cardsOnTableListener5) // player 1 cards on table listener
            refGameData.child("CT/p6")
                .removeEventListener(cardsOnTableListener6) // player 1 cards on table listener
            refGameData.child("CT/p7")
                .removeEventListener(cardsOnTableListener7) // player 1 cards on table listener
            refGameData.child("OL/p5").removeEventListener(onlineStatusListener5)
            refGameData.child("OL/p6").removeEventListener(onlineStatusListener6)
            refGameData.child("OL/p7").removeEventListener(onlineStatusListener7)
        }
    }

    override fun onPause() {
        super.onPause()
        if (activityExists) write("OL/$from", 0)
    }  // is offline

    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if (!scoreOpenStatus) {
            //            if(vibrateStatus) vibrationStart()
            toastCenter("App is minimized")
            this.moveTaskToBack(true)
        } else {
            openCloseScoreSheet(View(applicationContext))
        }
    } // is offline

    override fun onDestroy() {
        try {
            textToSpeech.shutdown()
        } catch (me: java.lang.Exception) {
        }
        super.onDestroy()
        //        toastCenter("Destroyed")
        //        deleteAllRoomdata()
    } // has left the room
}


