@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "PLUGIN_WARNING", "ImplicitThis", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kaalikiteeggi.three_of_spades.databinding.*
import com.robinhood.ticker.TickerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.math.round

@SuppressLint("SetTextI18n", "CutPasteId")
class GameScreenAutoPlay : AppCompatActivity() {
//    region Initialization

    private lateinit var textToSpeech: TextToSpeech
    private var closeRoom: Boolean = false
    private var typedValue = TypedValue()
    private var rated = false
    private var shuffleOver = false
    private var ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()
    private var requestRatingAfterDays = 2 //dummy
    private val today = CreateUser().todayDate
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var alertDialog: AlertDialog
    private lateinit var snackBar: Snackbar

    private lateinit var refIDMappedTextView: List<Int>
    private lateinit var refIDMappedTextViewA: List<Int>
    private lateinit var refIDMappedImageView: List<Int>
    private lateinit var refIDMappedHighlightView: List<Int>
    private lateinit var refIDMappedTableImageView: List<Int>
    private lateinit var refIDMappedTableAnim: List<Int>
    private lateinit var refIDMappedTableWinnerAnim: List<Int>
    private lateinit var refIDValesTextViewScore: List<Int>
    private lateinit var refIDMappedPartnerIconImageView: List<Int>

    private lateinit var cardsDrawable: List<Int>
    private lateinit var cardsPoints: List<Int>
    private lateinit var cardsSuit: List<String>
    private lateinit var cardsDrawablePartner: List<Int>
    private lateinit var cardsIndexSortedPartner: List<Int>
    private lateinit var cardsPointsPartner: List<Int>
    private var cardsIndexLimit = 0
    private var roundNumberLimit = 0
    private var scoreLimit = 0
    private var coinDur = 1000L
    private var coinSpeed = 5f
    private var coinRate = 150
    private var reviewRequested = false
    private var soundStatus = true
    private var speechStatus = true
    private var vibrateStatus = true
    private lateinit var vibrator: Vibrator

    private var premiumStatus = false
    private var scoreOpenStatus = false
    private var activityExists = true
    private var mInterstitialAd: InterstitialAd? = null
    private var loadInterAdTry = 0

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private var fromInt = 0
    private var nPlayers = 0
    private var totalDailyCoins = 0

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var refUsersData = Firebase.firestore.collection("Users")
    private var uid = ""

    private lateinit var countDownBidding: CountDownTimer
    private lateinit var countDownPlayCard: CountDownTimer

    private lateinit var gameStateListener: Observer<Int>
    private lateinit var bidTurnListener: Observer<Int>
    private lateinit var roundListener: Observer<Int>

    private lateinit var trump: String
    private var trumpInt = -1
    private lateinit var trumpStart: String

    private var nGamesPlayed = 0
    private var nGamesWon = 0
    private var nGamesBid = 0

    private var nGamesPlayedDaily = 0
    private var nGamesWonDaily = 0
    private var nGamesBidDaily = 0

    private lateinit var playerInfo: ArrayList<String>
    private lateinit var p1: String
    private lateinit var p2: String
    private lateinit var p3: String
    private lateinit var p4: String

    private var p1Coins = 0
    private var p2Coins = 0 // by Delegates.notNull<Int>()
    private var p3Coins = 0
    private var p4Coins = 0

    private var p1Gain = 0
    private var p2Gain = 0
    private var p3Gain = 0
    private var p4Gain = 0

    private lateinit var cardsInHand: MutableList<Int>
    private lateinit var cardsInHand2: MutableList<Int>
    private lateinit var cardsInHand3: MutableList<Int>
    private lateinit var cardsInHand4: MutableList<Int>

    private lateinit var gameState: MutableLiveData<Int>
    private lateinit var playerTurn: MutableLiveData<Int>
    private lateinit var gameTurn: MutableLiveData<Int>
    private lateinit var ct1: MutableLiveData<Int>
    private lateinit var ct2: MutableLiveData<Int>
    private lateinit var ct3: MutableLiveData<Int>
    private lateinit var ct4: MutableLiveData<Int>
    private var maskWinner = MutableLiveData(false)

    private var gameNumber: Int = 1
    private var gameLimitNoAds: Int = 2

    private var delayWaitGameMode6 = 5500L
    private var delayDeclareWinner = 1000L
    private var timeCountdownPlayCard = 20000L
    private var timeAutoPlayCard = listOf<Long>(700, 850, 600, 700, 1000, 600, 1000)
    private var timeCountdownBid = 20000L
    private var timeAutoBid = listOf<Long>(1650, 1400, 1500, 1800)
    private var speedAutoBid = 1.1f
    private var timeAutoTrumpAndPartner = listOf<Long>(1700, 2000, 1700)
    private var maxAutoBidLimit = listOf(220, 230, 235)

    private var scoreSheetNotUpdated = true
    private var played = false

    private var pt1 = 0
    private var pt2 = 0
    private var pt3 = 0
    private var pt4 = 0

    private lateinit var ptAll: List<Int>
    private var bidTeamScore = 0
    private lateinit var scoreList: List<Int>
    private var tablePoints = 0

    private var bidder: Int = 0
    private var bidValue: Int = 0
    private var maxBidValue: Int = 350
    private lateinit var bidStatus: MutableList<Int>
    private var bidValuePrev: Int = 0
    private var bidingStarted = false   /// biding happened before
    private var roundStarted = false
    private var partnerCardSelected = false
    private var trumpSelected = false
    private var bidDone = true
    private var counterPartnerSelection = 0
    private var bu1 = 0
    private var bu1Flag = 0

    private var buPlayer1 = 0
    private var buFound1 = 0
    private var roundWinner = 0
    private var roundNumber = 1
    private var newGameStatus = true
    private lateinit var binding: ActivityGameScreenBinding

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
            .errorDrawable(R.drawable.bug_icon) //default: bug image
            .restartActivity(MainHomeScreen::class.java).apply()
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            controller!!.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        roomID = intent.getStringExtra("roomID")!!.toString()    //Get roomID and display    selfName = intent.getStringExtra("selfName") //Get Username first  - selfName ,roomID available
        from = intent.getStringExtra("from")!!.toString()    //check if user has joined room or created one and display Toast
        fromInt = from.split("")[2].toInt()
        selfName = intent.getStringExtra("selfName")!!.toString()
        totalDailyCoins = intent.getIntExtra("totalDailyCoins", 0)
        val userStats = intent.getIntegerArrayListExtra("userStats")!!
        val userStatsDaily = intent.getIntegerArrayListExtra("userStatsDaily")!!
        nGamesPlayed = userStats[0]
        nGamesWon = userStats[1]
        nGamesBid = userStats[2]
        nGamesPlayedDaily = userStatsDaily[0]
        nGamesWonDaily = userStatsDaily[1]
        nGamesBidDaily = userStatsDaily[2]
        updateTimeAutoPlay()
        nPlayers = 4 //intent.getIntExtra("nPlayers", 0)
        setupGame()
        refIDMappedTextView = PlayersReference().refIDMappedTextView(from, nPlayers)
        refIDMappedTextViewA = PlayersReference().refIDMappedTextViewA(from, nPlayers)
        refIDMappedImageView = PlayersReference().refIDMappedImageView(from, nPlayers)
        refIDMappedHighlightView = PlayersReference().refIDMappedHighlightView(from, nPlayers)
        refIDMappedPartnerIconImageView = PlayersReference().refIDMappedPartnerIconImageView(from, nPlayers)
        refIDMappedTableAnim = PlayersReference().refIDMappedTableAnim(from, nPlayers)
        refIDMappedTableWinnerAnim = PlayersReference().refIDMappedTableWinnerAnim(from, nPlayers)
        refIDMappedTableImageView = PlayersReference().refIDMappedTableImageView(from, nPlayers)

        //region Other Thread tasks
        Handler(Looper.getMainLooper()).post {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            updatePlayerInfo()
            initializeSpeechEngine()
            getSharedPrefs() // region       Countdown PlayCard
            countDownPlayCard = object : CountDownTimer(timeCountdownPlayCard, 20) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished * 10000 / timeCountdownPlayCard).toInt()   //10000 because max progress is 10000
                    findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString() + "s"
                }

                override fun onFinish() {
                    autoPlayCard(cardsInHand, forcePlay = false)
                    if (soundStatus) SoundManager.instance?.playTimerSound()
                    if (vibrateStatus) vibrationStart()
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                    findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                    findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                }
            } //        endregion
            // region       Countdown Biding
            countDownBidding = object : CountDownTimer(timeCountdownBid, 20) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) { //                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
                    binding.progressbarTimer.progress = (millisUntilFinished * 10000 / timeCountdownBid).toInt()
                    binding.textViewTimer.text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString() + "s"
                }

                override fun onFinish() {
                    if (vibrateStatus) vibrationStart()
                    if (soundStatus) SoundManager.instance?.playTimerSound()
                    bidStatus[playerTurn.value!! - 1] = 0
                    bidingStarted = true
                    playerTurn.value = nextBidderTurn(fromInt)
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                    findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                    findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                    findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
                    centralText("    Time's Up !!  \n You cannot bid anymore", 2500)
                    speak("Time's Up ${playerName(fromInt)}. You can't bid now", speed = 1.05f)
                }
            } //        endregion
            // region Game State Listener
            gameStateListener = Observer {
                if (gameState.value!! == 1) {
                    binding.horizontalScrollView1.foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.transparent))
                    if (!roundStarted) {
                        resetVariables()
                        binding.scrollViewScore.visibility = View.GONE
                        binding.scoreViewLayout.visibility = View.GONE
                        if (this::p1.isInitialized) updatePlayerNames()
                        roundStarted = true
                        if (BuildConfig.DEBUG) shufflingWindow(time = 2000L, gameStateChange = true) // gameStateChange = change game state to 2 after shuffling
                        else shufflingWindow(gameStateChange = true) // gameStateChange = change game state to 2 after shuffling
                    } else {
                        startBidding()
                    }
                }
                if (gameState.value!! == 3) {
                    if (soundStatus) SoundManager.instance?.playSuccessSound()
                    roundStarted = false
                    bidingStarted = false
                    playerTurn.removeObserver(bidTurnListener)
                    finishBackgroundAnimationBidding() // also highlight bidder winner & removed automatically at game state 5
                    startTrumpSelection()
                }
                if (gameState.value!! == 4) {
                    if (soundStatus) SoundManager.instance?.playSuccessSound() // soundSuccess.start()
                    getTrumpStartPartnerSelection()
                }
                if (gameState.value!! == 5) {
                    newGameStatus = true
                    if (!roundStarted) {
                        finishPassOverlay()
                        if (soundStatus) SoundManager.instance?.playSuccessSound()
                        updatePlayerScoreInfo(listOf(pt1, pt2, pt3, pt4))
                        displaySelfCards(animations = false)
                        Handler(Looper.getMainLooper()).postDelayed({ startPlayingRound() }, 3000)
                        if (playerTurn.value!! != fromInt) {
                            if (nGamesPlayed < 5) speak("${playerName(bidder)} will play first \n You get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1.1f)
                            else speak("${playerName(bidder)} will play first ", speed = 1f)
                        }
                        if (playerTurn.value!! == fromInt) {
                            speak("You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1f)
                        }
                    } else {
                        displaySelfCards(animations = false)
                        startPlayingRound()
                    }
                }
                if (gameState.value!! == 6) {
                    roundStarted = false
                    gameMode6()
                }
            }
            gameState.observe(this, gameStateListener) // endregion
            uid = FirebaseAuth.getInstance().uid.toString()
            FirebaseCrashlytics.getInstance().setUserId(uid)
            refUsersData.document(uid).set(hashMapOf("LPD_bot" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()), SetOptions.merge())
        } // endregion
        // region table card listener
        ct1 = MutableLiveData()
        ct1.value = cardsIndexLimit
        ct1.observe(this) {
            tablePointsCalculator()
            if (ct1.value!! <= (cardsIndexLimit - 2)) {
                if (soundStatus) SoundManager.instance?.playCardPlayedSound()
                findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[0]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[0]))
                findViewById<ImageView>(refIDMappedTableImageView[0]).setImageResource(cardsDrawable[ct1.value!!])
            } else {
                findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.INVISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[0]).clearAnimation()
            }
        }
        ct2 = MutableLiveData()
        ct2.value = cardsIndexLimit
        ct2.observe(this) {
            tablePointsCalculator()
            if (ct2.value!! <= (cardsIndexLimit - 2)) {
                if (soundStatus) SoundManager.instance?.playCardPlayedSound()
                findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[1]))
                findViewById<ImageView>(refIDMappedTableImageView[1]).setImageResource(cardsDrawable[ct2.value!!])
            } else {
                findViewById<ImageView>(refIDMappedTableImageView[1]).visibility = View.INVISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[1]).clearAnimation()
            }
        }
        ct3 = MutableLiveData()
        ct3.value = cardsIndexLimit
        ct3.observe(this) {
            tablePointsCalculator()
            if (ct3.value!! <= (cardsIndexLimit - 2)) {
                if (soundStatus) SoundManager.instance?.playCardPlayedSound()
                findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[2]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[2]))
                findViewById<ImageView>(refIDMappedTableImageView[2]).setImageResource(cardsDrawable[ct3.value!!])
            } else {
                findViewById<ImageView>(refIDMappedTableImageView[2]).visibility = View.INVISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[2]).clearAnimation()
            }
        }
        ct4 = MutableLiveData()
        ct4.value = cardsIndexLimit
        ct4.observe(this) {
            tablePointsCalculator()
            if (ct4.value!! <= (cardsIndexLimit - 2)) {
                if (soundStatus) SoundManager.instance?.playCardPlayedSound()
                findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[3]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[3]))
                findViewById<ImageView>(refIDMappedTableImageView[3]).setImageResource(cardsDrawable[ct4.value!!])
            } else {
                findViewById<ImageView>(refIDMappedTableImageView[3]).visibility = View.INVISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[3]).clearAnimation()
            }
        } // endregion
        maskWinner.observe(this) {
            if (it) {
                binding.startNextRoundButton.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.95F }
                binding.gridLoser.visibility = View.VISIBLE
                binding.gridWinner.visibility = View.VISIBLE
                binding.textWinner.visibility = View.VISIBLE
                binding.textLoser.visibility = View.VISIBLE
                binding.textResult.visibility = View.VISIBLE
                binding.konfettLottie.visibility = View.VISIBLE
                binding.winnerLottie.visibility = View.VISIBLE
                binding.winnerLottie.playAnimation()
                binding.konfettLottie.playAnimation()
            } else {
                binding.startNextRoundButton.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.5F }
                binding.gridLoser.visibility = View.GONE
                binding.gridWinner.visibility = View.GONE
                binding.textWinner.visibility = View.GONE
                binding.textLoser.visibility = View.GONE
                binding.textResult.visibility = View.GONE
                binding.konfettLottie.visibility = View.GONE
                binding.winnerLottie.visibility = View.GONE
                binding.winnerLottie.cancelAnimation()
                binding.konfettLottie.cancelAnimation()
            }
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        logFirebaseEvent(key = "start_offline$nPlayers")
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true) // for click effect on self playing cards
        gameState = MutableLiveData()
        gameState.value = 1
        gameTurn = MutableLiveData() // 0
        playerTurn = MutableLiveData()
        playerTurn.value = 1
        gameTurn.value = 0
        startNextGame(View(applicationContext))
    }

    private fun logFirebaseEvent(event: String = "game_screen", int: Int = 1, key: String) {
        val params = Bundle()
        params.putInt(key, int)
        firebaseAnalytics.logEvent(event, params)
    }

    private fun setupGame() {
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

        binding.textView14.visibility = View.VISIBLE
        binding.textView14a.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.onlinep1_4).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.playerView1_4).visibility = View.VISIBLE
        binding.textView24.visibility = View.VISIBLE
        binding.textView24a.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.onlinep2_4).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.playerView2_4).visibility = View.VISIBLE
        binding.textView34.visibility = View.VISIBLE
        binding.textView34a.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.onlinep3_4).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.playerView3_4).visibility = View.VISIBLE
    }

    private fun updateTimeAutoPlay() {
        timeAutoPlayCard = when {
            nGamesPlayed < 5 -> listOf(1000, 900, 1100, 1200, 800)
            nGamesPlayed < 10 -> listOf(600, 900, 650, 800, 700, 900)
            nGamesPlayed < 15 -> listOf(500, 550, 600, 700, 800)
            nGamesPlayed < 30 -> listOf(400, 350, 400, 300)
            else -> listOf(350, 250, 300, 300, 200)
        }
        timeAutoBid = when {
            nGamesPlayed < 10 -> listOf(1600, 1700, 1800)
            nGamesPlayed < 20 -> listOf(1600, 1400, 1500)
            else -> listOf(1400)
        }
        speedAutoBid = when {
            nGamesPlayed < 4 -> 1.05f
            nGamesPlayed < 10 -> 1.5f
            else -> 1.7f
        }
    }

    private fun inAppReview() {
        val manager = ReviewManagerFactory.create(this) // FakeReviewManager(this)//
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request1 ->
            if (request1.isSuccessful) {
                val reviewInfo = request1.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        rated = true
                        editor.putBoolean("rated", true)
                        editor.apply()
                        logFirebaseEvent("rate_us", key = "rated_gs")
                        refUsersData.document(uid).set(hashMapOf("rated" to 1, "ratedD" to today), SetOptions.merge())
                    } else {
                        openPlayStore()
                    }
                }
            } else {
                logFirebaseEvent(event = "rate_us", key = "ratedFailure_gs")
                openPlayStore()
            }
        }
    }

    private fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.kaalikiteeggi.three_of_spades")
            setPackage("com.android.vending")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    override fun onStart() {
        super.onStart()
        if (gameState.value!! == 5 && playerTurn.value!! == fromInt) countDownTimer("PlayCard", purpose = "start")
        else if (gameState.value!! == 1 && playerTurn.value!! == fromInt && this::countDownBidding.isInitialized) countDownTimer("Bidding", purpose = "start")
    }

    private fun gameMode6() {
        logFirebaseEvent(key = "played_offline")
        binding.relativeLayoutTableCards.visibility = View.GONE
        countDownTimer("PlayCard", purpose = "cancel")
        if (vibrateStatus) vibrationStart()
//        if (soundStatus) SoundManager.instance?.playShuffleSound() //soundShuffle.start()
        displayShufflingCards(distribute = false)
        scoreOpenStatus = true
        if (newGameStatus) { // dummy - newGameStatus not needed as score list has game index which is unique
            newGameStatus = false
            updateWholeScoreBoard()
            gameNumber += 1
        }
        if (mInterstitialAd == null && !premiumStatus) loadInterstitialAd()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!rated && !reviewRequested && (nGamesPlayed > 10 || gameNumber > 3)) {  // Ask only once per game
                inAppReview()
                reviewRequested = true
            } else if (!premiumStatus && mInterstitialAd != null && ((gameNumber - 1) % gameLimitNoAds == 0)) showInterstitialAd()
            binding.startNextRoundButton.visibility = View.VISIBLE
//            binding.startNextRoundButton.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
//            maskWinner.value = false
        }, delayWaitGameMode6)
    }

    private fun updateWholeScoreBoard() {
        if (scoreList[fromInt] > 0) {
            binding.winnerLottie.setAnimation(R.raw.trophy_lottie)
            binding.textResult.text = getString(R.string.resultWon)
            nGamesWon += 1
            nGamesWonDaily += 1
        } else {
            binding.winnerLottie.setAnimation(R.raw.sad_lottie)
            binding.textResult.text = getString(R.string.resultLost)

        }
        p1Gain += scoreList[1]
        p2Gain += scoreList[2]
        p3Gain += scoreList[3]
        p4Gain += scoreList[4]

        totalDailyCoins += scoreList[1]
        p1Coins += scoreList[1]
        p2Coins += scoreList[2]
        p3Coins += scoreList[3]
        p4Coins += scoreList[4]

        val arrayListWinner = ArrayList<WinnerItemDescription>()
        val arrayListLoser = ArrayList<WinnerItemDescription>()
        val scoredList = listOf(pt1, pt2, pt3, pt4)
        for (i in 1..nPlayers) {
            val target = if (i == bidder || i == buPlayer1) bidValue else scoreLimit - bidValue
            if (scoreList[i] > 0) arrayListWinner.add(WinnerItemDescription(scored = scoredList[i - 1], target = target, points = scoreList[i], playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1]))
            else arrayListLoser.add(WinnerItemDescription(scored = scoredList[i - 1], target = target, points = scoreList[i], playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1]))
        }
        binding.gridWinner.adapter = PlayerWinnerGridAdapter(arrayList = arrayListWinner, winner = true)
        binding.gridLoser.adapter = PlayerWinnerGridAdapter(arrayList = arrayListLoser, winner = false)
        maskWinner.value = true

        scoreBoardTable(display = false, data = createScoreTableHeader(), upDateHeader = true)
        scoreBoardTable(display = false, data = listOf("Total", p1Gain, p2Gain, p3Gain, p4Gain), upDateTotal = true)   // createScoreTableTotal
        scoreBoardTable(display = false, data = scoreList)

        if (bidder == fromInt) {
            nGamesBid += 1
            nGamesBidDaily += 1
        }
        nGamesPlayed += 1
        nGamesPlayedDaily += 1
        updateTimeAutoPlay()
        refUsersData.document(uid).set(hashMapOf("sc" to playerCoins(from), "scd" to totalDailyCoins, "w_bot" to nGamesWon, "w_daily" to nGamesWonDaily, "b_bot" to nGamesBid, "b_daily" to nGamesBidDaily, "p_bot" to nGamesPlayed, "p_daily" to nGamesPlayedDaily), SetOptions.merge())
    }

    private fun createScoreTableHeader(): List<String> {
        return listOf("Player\n${Emoji().money}", p1 + "\n${Emoji().money}${String.format("%,d", p1Coins)}", p2 + "\n${Emoji().money}${String.format("%,d", p2Coins)}", p3 + "\n${Emoji().money}${String.format("%,d", p3Coins)}", p4 + "\n${Emoji().money}${String.format("%,d", p4Coins)}")
    }

    private fun scoreBoardTable(data: List<Any>, display: Boolean = true, upDateHeader: Boolean = false, upDateTotal: Boolean = false) {
        if (display) {
            scoreOpenStatus = true
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            binding.scrollViewScore.visibility = View.VISIBLE
            binding.scoreViewLayout.visibility = View.VISIBLE
            binding.scoreViewLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
        }
        val inflater = LayoutInflater.from(applicationContext)
        val viewTemp = when {
            upDateHeader -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScoreName), false)
            upDateTotal -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScoreTotal), false)
            else -> inflater.inflate(PlayersReference().refIDScoreLayout(nPlayers), findViewById<LinearLayout>(R.id.imageGalleryScore), false)
        }
        for (i in 0..nPlayers) {
            viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).text = data[i].toString()
            if (!upDateHeader) viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._12ssp))
            if (i > 0 && !upDateHeader && data[i].toString().toInt() < 0) {
                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTextColor(ContextCompat.getColor(applicationContext, R.color.Red))
            } else if (i > 0 && !upDateHeader) {
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
            else -> {
                viewTemp.layoutParams.height = resources.getDimensionPixelSize(R.dimen._22sdp)
//				viewTemp.background = ContextCompat.getDrawable(this, R.drawable.blackrectangle)
                findViewById<LinearLayout>(R.id.imageGalleryScore).addView(viewTemp)
            }
        }
        if (display) binding.scrollViewScore.post {
            binding.scrollViewScore.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun closeChatScoreWindow(view: View) {
        //        findViewById<RelativeLayout>(R.id.chatLinearLayout).visibility = View.GONE
        binding.scrollViewScore.visibility = View.GONE
        binding.scoreViewLayout.visibility = View.GONE
        findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
    }

    fun openCloseScoreSheet(view: View) {
        maskWinner.value = false
        if (binding.scrollViewScore.visibility == View.VISIBLE) {
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
            binding.scoreViewLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_scoretable_close))
            Handler(Looper.getMainLooper()).postDelayed({
                binding.scrollViewScore.visibility = View.GONE
                binding.scoreViewLayout.visibility = View.GONE
            }, 140)
            scoreOpenStatus = false
        } else {
            scoreOpenStatus = true
            if (scoreSheetNotUpdated) {
                scoreBoardTable(display = false, data = createScoreTableHeader(), upDateHeader = true)
                scoreBoardTable(display = false, data = listOf("Total", p1Gain, p2Gain, p3Gain, p4Gain), upDateTotal = true)
            }
            scoreSheetNotUpdated = false
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            binding.scrollViewScore.visibility = View.VISIBLE
            binding.scoreViewLayout.visibility = View.VISIBLE
            binding.scoreViewLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
            binding.scrollViewScore.post {
                binding.scrollViewScore.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun playerCoins(p: String): Int {
        var coins = 0
        when (p) {
            "p1" -> coins = p1Coins
            "p2" -> coins = p2Coins
            "p3" -> coins = p3Coins
            "p4" -> coins = p4Coins
        }
        return coins
    }

    private fun resetVariables() {
        if (gameNumber > 1) binding.gameBkgd.setImageResource(GameScreenData().tableBackground.random())// change background only from 2nd game
        binding.buddyImage1.setImageResource(R.drawable.ic_back_side_blue)
        findViewById<ImageView>(R.id.trumpImage).setImageResource(R.drawable.trump)
        binding.textViewBidValue.text = getString(R.string.bidValue2)  //$emojiScore
        binding.bidNowImage.visibility = View.GONE // redundant not required really
        findViewById<TextView>(R.id.textViewBider).text = getString(R.string.Bider)
        findViewById<TickerView>(R.id.trumpText).text = getString(R.string.Trump)
        for (i in 0 until nPlayers) { // first reset background and animation of all partner icon
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
        }
        bu1 = 0
        bu1Flag = 0
        buPlayer1 = 0
        buFound1 = 0
        bidValue = 175
        bidValuePrev = 0
        bidStatus = mutableListOf(1, 1, 1, 1)
        bidTeamScore = 0 //        bidder = 0
        bidingStarted = false   /// biding happened before
        counterPartnerSelection = 0
        ct1.value = cardsIndexLimit
        ct2.value = cardsIndexLimit
        ct3.value = cardsIndexLimit
        ct4.value = cardsIndexLimit
        playerTurn.value = 1
        gameTurn.value = 0
        played = false //        playerTurn = 0 //AP
        bidder = playerTurn.value!! // bidder is same as player turn

        pt1 = 0
        pt2 = 0
        pt3 = 0
        pt4 = 0
        scoreList = listOf(pt1, pt2, pt3, pt4)
        ptAll = listOf(pt1, pt2, pt3, pt4)
        partnerCardSelected = false
        roundStarted = false
        roundNumber = 1
        roundWinner = 0
        tablePoints = 0
        trumpSelected = false
        bidDone = false
        findViewById<TickerView>(R.id.buddyText1).text = getString(R.string.partner)
        binding.startNextRoundButton.clearAnimation()
        binding.startNextRoundButton.visibility = View.GONE
    }

    fun startNextGame(view: View) {
        maskWinner.value = false
        binding.startNextRoundButton.clearAnimation()
        binding.startNextRoundButton.visibility = View.GONE
        binding.scrollViewScore.visibility = View.GONE
        binding.scoreViewLayout.visibility = View.GONE

        val cardsShuffled = (0..51).shuffled()
        cardsInHand = (cardsShuffled.slice(0..12).sortedBy { it }).toMutableList()  //  (mutableListOf<Int>(1,11,12,24,25,37,38,50,51, 49,23,36,5).sortedBy { it }).toMutableList()   // //
        cardsInHand2 = (cardsShuffled.slice(13..25).sortedBy { it }).toMutableList()
        cardsInHand3 = (cardsShuffled.slice(26..38).sortedBy { it }).toMutableList()
        cardsInHand4 = (cardsShuffled.slice(39..51).sortedBy { it }).toMutableList()
        gameState.value = 1
    }

    private fun tablePointsCalculator() {
        tablePoints = cardsPoints[ct1.value!!] + cardsPoints[ct2.value!!] + cardsPoints[ct3.value!!] + cardsPoints[ct4.value!!]
        if (tablePoints > 0 && gameState.value!! == 5) {
            binding.textViewCenterPoints4.text = tablePoints.toString()
            binding.textViewCenterPoints4.visibility = View.VISIBLE
        } else {
            binding.textViewCenterPoints4.visibility = View.GONE
        }
    }

    private fun updatePlayerScoreInfo(pointsList: List<Int>) {
        if (gameState.value!! == 5) {
            var t1 = 0
            if (buFound1 != 0) t1 = pointsList[buPlayer1 - 1]
            bidTeamScore = pointsList[bidder - 1] + t1

            for (i in 0 until nPlayers) {
                val j = i + 1
                if (j == bidder || (j == buPlayer1 && buFound1 != 0)) {
//                    findViewById<MaterialTextView>(refIDMappedTextView[i]).text = playerName(j)
                    findViewById<TickerView>(refIDMappedTextViewA[i]).text = "$bidTeamScore /$bidValue"
                } else {
//                    findViewById<MaterialTextView>(refIDMappedTextView[i]).text = playerName(j)
                    findViewById<TickerView>(refIDMappedTextViewA[i]).text = "${pointsList.sum() - bidTeamScore} /${scoreLimit - bidValue}"
                }
            }
            var tt1 = 0
            if (buFound1 == 1) tt1 = pointsList[buPlayer1 - 1]
            val bidTeamScoreFinal = pointsList[bidder - 1] + tt1 // total score of bid team
            if (roundNumber != roundNumberLimit) Handler(Looper.getMainLooper()).postDelayed({
                decideGameWinnerTeam4(bidTeamScoreFinal, totalGamePoints = pointsList.sum())
            }, delayDeclareWinner)
            else decideGameWinnerTeam4(bidTeamScoreFinal, totalGamePoints = pointsList.sum())
        }
    }

    private fun decideGameWinnerTeam4(bidTeamScoreFinal: Int, totalGamePoints: Int) {
        if (bidTeamScoreFinal >= bidValue) { // bidder team won case
            gameTurn.removeObserver(roundListener)

            clearAllAnimation()
            if (vibrateStatus) vibrationStart() //            toastCenter("Game Over: Bidder team Won \n         Defender team Lost")
            centralText("Game Over: Bidder team Won \n         Defender team Lost")

            if (fromInt == bidder && buFound1 != 1) {
                if (soundStatus) {
                    SoundManager.instance?.playWonSound()
                    SoundManager.instance?.playDholSound()
                }
                speak("Well done! You won")
            } else if ((fromInt == bidder || from == "p$buPlayer1") && buFound1 == 1) {
                if (soundStatus) {
                    SoundManager.instance?.playDholSound()
                    SoundManager.instance?.playWonSound()
                }
                speak("Well done! Your team won")
            } else {
                if (soundStatus) SoundManager.instance?.playLostSound() //soundLost.start()
                speak("Sorry Your team lost")
            }

            val pointsListTemp = mutableListOf(gameNumber, -bidValue, -bidValue, -bidValue, -bidValue)
            if (buFound1 != 1) { //No partners found so far
                pointsListTemp[bidder] = bidValue * 3 // bidder gets 3 times
            } else if (buFound1 == 1) { // partner 1 found
                pointsListTemp[bidder] = bidValue * 2
                pointsListTemp[buPlayer1] = bidValue
            }
            scoreList = pointsListTemp
            gameState.value = 6
        } else if (buFound1 == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - bidValue)) { // if opponent score has reached target value & both partners are disclosed
            gameTurn.removeObserver(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart() //            toastCenter("Game Over: Defender team Won \n         Bidder team Lost")
            centralText("Game Over: Defender team Won \n         Bidder team Lost")

            if (fromInt == bidder || fromInt == buPlayer1) {
                if (soundStatus) SoundManager.instance?.playLostSound()
                speak("Sorry Your team lost")
            } else {
                if (soundStatus) {
                    SoundManager.instance?.playDholSound()
                    SoundManager.instance?.playWonSound()
                }
                speak("Well done! Your team won")
            }

            val pointsListTemp = mutableListOf(gameNumber, bidValue, bidValue, bidValue, bidValue)
            pointsListTemp[bidder] = -1 * bidValue * 2
            pointsListTemp[buPlayer1] = -bidValue
            scoreList = pointsListTemp
            gameState.value = 6
        }
    }

    private fun displayPartnerIcon() {
        if (buFound1 == 1 && buPlayer1 != 8) {
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).setImageResource(R.drawable.partnericon)
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
        }
        moveView(binding.buddyText1, findViewById(refIDMappedTextView[buPlayer1 - 1]), duration = 500L)
        if (gameState.value!! == 5) updatePlayerScoreInfo(listOf(pt1, pt2, pt3, pt4))
    }

    private fun countDownTimer(task: String, purpose: String = "start") {
        if (purpose == "start") {
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).progress = 100
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).text = "10s" //            findViewById<ProgressBar>(R.id.progressbarTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            //            findViewById<TextView>(R.id.textViewTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            if (task == "Bidding" && this::countDownBidding.isInitialized) countDownBidding.start()
            if (task == "PlayCard" && this::countDownPlayCard.isInitialized) countDownPlayCard.start()
        } else if (purpose == "cancel") {
            findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
            findViewById<TextView>(R.id.textViewTimer).clearAnimation()
            if (task == "Bidding" && this::countDownBidding.isInitialized) countDownBidding.cancel()
            if (task == "PlayCard" && this::countDownPlayCard.isInitialized) countDownPlayCard.cancel()
        }
    }

    private fun startPlayingRound() {
        if (!roundStarted) {
            playerTurn.value = bidder
            gameTurn.value = 1
            speak("Lets Start ", queue = TextToSpeech.QUEUE_ADD)
        }
        roundStarted = true
        binding.relativeLayoutTableCards.visibility = View.VISIBLE

        roundListener = Observer<Int> {
            clearAllAnimation()
            if (gameTurn.value!! == 1) {
                played = false
                trumpStart = ""
            } // reset for new round
            if (gameTurn.value!! == 5) {
                Handler(Looper.getMainLooper()).postDelayed({ declareRoundWinner() }, 700)
            } else if (gameTurn.value!! != 5 && gameTurn.value!! != 0) {
                animatePlayer(playerTurn.value!!)
                if (playerTurn.value!! == fromInt && !played) {
                    displaySelfCards(filter = true)
                    countDownTimer(task = "PlayCard") // start countdown timer and run autoPlayCard
                    if (vibrateStatus) vibrationStart()
                } else if (playerTurn.value!! != fromInt) {
                    if (gameTurn.value!! != 1) centralText(cancel = true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        when (playerTurn.value!!) {
                            2 -> autoPlayCard(cardsInHand2, forcePlay = true)
                            3 -> autoPlayCard(cardsInHand3, forcePlay = true)
                            4 -> autoPlayCard(cardsInHand4, forcePlay = true)
                        }
                    }, timeAutoPlayCard.random())
                } else {
                    centralText(cancel = true)
                }
            }
        }
        gameTurn.observe(this, roundListener)
    }

    private fun autoPlayCard(cardsAvailable: MutableList<Int>, forcePlay: Boolean) {
        if (!played || forcePlay) {
            if (gameTurn.value!! == 1) {  //can play any random card in 1st chance
                val cardSelected = cardsAvailable.random()
                startNextTurn(cardSelected, forcePlay)
            } else { // play only same suit card if not 1st chance
                var cardSelectedIndex = cardsSuit.slice(cardsAvailable as Iterable<Int>).lastIndexOf(trumpStart) // play largest card first
                if (cardSelectedIndex == -1) { //not found same suit card
                    cardSelectedIndex = cardsSuit.slice(cardsAvailable as Iterable<Int>).lastIndexOf(trump) // play trump card
                    if (cardSelectedIndex == -1) {
                        val cardSelected = cardsAvailable.random() // or play any random card
                        startNextTurn(cardSelected, forcePlay)
                    } else {
                        val cardSelected = cardsAvailable[cardSelectedIndex]
                        startNextTurn(cardSelected, forcePlay)
                    }
                } else {
                    val cardSelected = cardsAvailable[cardSelectedIndex]
                    startNextTurn(cardSelected, forcePlay)
                }
            }
        }
    }

    private fun validateSelfPlayedCard(view: View) {
        if (gameState.value!! == 5 && playerTurn.value!! == fromInt && gameTurn.value!! != 8 && gameTurn.value!! != 0) {
            val cardSelected = view.tag.toString().toInt()
            if (gameTurn.value!! == 1 || cardsSuit[cardSelected] == trumpStart || cardsSuit.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) == -1) {
                countDownTimer("PlayCard", purpose = "cancel")
                startNextTurn(cardSelected, forcePlay = false) // allow throw if first chance, or same suit as first turn or doesn't have same suit card
            } else {
                if (soundStatus) SoundManager.instance?.playErrorSound() // soundError.start()
                if (vibrateStatus) vibrationStart()
//				toastCenter("${playerName(playerTurn.value!!)}, Please play ${getSuitName(trumpStart)} card")
                speak("Please play ${getSuitName(trumpStart)} card", speed = 1.1f)
            }
        }
    }

    private fun startNextTurn(cardSelected: Int, forcePlay: Boolean) {
        if (!played || forcePlay) {
            if (!forcePlay) played = true // make it true only if human played not bot
            if (playerTurn.value!! != bidder) {
                checkIfPartnerAndUpdateServer4(cardSelected, playerTurn.value!!)
            }
            val pt = playerTurn.value!!
            val gt = gameTurn.value!!
            if (gt == 1) trumpStart = cardsSuit[cardSelected.toString().toInt()]

            if (roundNumber <= roundNumberLimit) {
                when (pt) {
                    1 -> {
                        ct1.value = cardSelected.toString().toInt()
                        cardsInHand.remove(cardSelected)
                        displaySelfCards(animations = false) // refresh cards in hand only after first player turn not always
                    }
                    2 -> {
                        ct2.value = cardSelected.toString().toInt()
                        cardsInHand2.remove(cardSelected)
                    }
                    3 -> {
                        ct3.value = cardSelected.toString().toInt()
                        cardsInHand3.remove(cardSelected)
                    }
                    4 -> {
                        ct4.value = cardSelected.toString().toInt()
                        cardsInHand4.remove(cardSelected)
                    }
                }
            }
            if (pt == 1 && roundNumber == roundNumberLimit) {
                binding.imageGallery.removeAllViews() // show no self cards after throwing last card
            }
            playerTurn.value = nextTurn(pt)
            gameTurn.value = gt + 1
        }
    }

    private fun checkIfPartnerAndUpdateServer4(cardSelected: Any, playerTurnInt: Int?) {
        if (cardSelected.toString().toInt() == bu1 && buFound1 != 1) {
            buPlayer1 = playerTurnInt!!
            buFound1 = 1
            findViewById<TickerView>(R.id.buddyText1).text = playerName(buPlayer1)
            displayPartnerIcon()
            if (soundStatus) SoundManager.instance?.playSuccessSound() //soundSuccess.start()
            if (vibrateStatus) vibrationStart()
            speak("${playerName(playerTurnInt)} is partner now")
        }
    }

    private fun declareRoundWinner() {
        val roundCards = listOf(ct1.value!!, ct2.value!!, ct3.value!!, ct4.value!!)
        var winnerCard = roundCards[playerTurn.value!! - 1]
        var startTurn = playerTurn.value!!
        for (i in 1 until nPlayers) {
            startTurn = nextTurn(startTurn)
            winnerCard = compareCardsForWinner(roundCards[startTurn - 1], winnerCard)
        }
        roundWinner = roundCards.indexOf(winnerCard) + 1
        animatePlayer(roundWinner)
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
        Handler(Looper.getMainLooper()).postDelayed({ animateWinner() }, 550)
        Handler(Looper.getMainLooper()).postDelayed({ // start after 1.5 seconds
            if (roundNumber < roundNumberLimit) {
                startNextRound()
            } else if (roundNumber == roundNumberLimit) {
                try {
                    gameTurn.removeObserver(roundListener)
                } finally {
                    clearAllAnimation()
                }
                endGameRound() // update points of last round to server by winner
            }
        }, 1500)
    }

    private fun endGameRound() {
        when (roundWinner) {
            1 -> pt1 += tablePoints
            2 -> pt2 += tablePoints
            3 -> pt3 += tablePoints
            4 -> pt4 += tablePoints
        }
        updatePlayerScoreInfo(listOf(pt1, pt2, pt3, pt4))
        ct1.value = cardsIndexLimit
        ct2.value = cardsIndexLimit
        ct3.value = cardsIndexLimit
        ct4.value = cardsIndexLimit
    }

    private fun startNextRound() { //        if(roundNumber==12) toastCenter("Round number $roundNumber")
        when (roundWinner) {
            1 -> pt1 += tablePoints
            2 -> pt2 += tablePoints
            3 -> pt3 += tablePoints
            4 -> pt4 += tablePoints
        }
        updatePlayerScoreInfo(listOf(pt1, pt2, pt3, pt4))
        ct1.value = cardsIndexLimit
        ct2.value = cardsIndexLimit
        ct3.value = cardsIndexLimit
        ct4.value = cardsIndexLimit
        playerTurn.value = roundWinner
        trumpStart = ""
        roundNumber += 1
        gameTurn.value = 1 // reset game turn when the round ends
    }

    private fun compareCardsForWinner(currentCard: Int, winnerCard: Int): Int {
        var w = winnerCard
        val wSuit = cardsSuit[winnerCard]
        val cSuit = cardsSuit[currentCard]
        if ((cSuit == trump && wSuit != trump) || ((cSuit == wSuit) && ((currentCard - winnerCard) >= 1))) w = currentCard
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

    @SuppressLint("NewApi", "CutPasteId")
    private fun displaySelfCards(view: View = View(applicationContext), animations: Boolean = false, filter: Boolean = false, bidingRequest: Boolean = false) {
        binding.imageGallery.removeAllViews()
        binding.imageGallery.visibility = View.VISIBLE
        val typedValue = TypedValue()
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
        for (x: Int in cardsInHand) {
            val viewTemp = CardsItemListBinding.inflate(layoutInflater, binding.imageGallery, false)
            if (x == cardsInHand[cardsInHand.size - 1]) {
                viewTemp.imageViewDisplayCard.setPaddingRelative(0, 0, 0, 0)
                viewTemp.imageViewDisplayCard.layoutParams.width = resources.getDimensionPixelSize(R.dimen.widthDisplayCardLast)
            }
            viewTemp.imageViewDisplayCard.setImageResource(cardsDrawable[x])
            if (filter && gameTurn.value!! > 1 && cardsSuit[x] != trumpStart && cardsSuit.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) != -1) {
                viewTemp.imageViewDisplayCard.foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
            } else if (filter && gameTurn.value!! > 1 && cardsSuit.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) != -1) {
                viewTemp.imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite_active_cards))
                viewTemp.imageViewDisplayCard.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            } else viewTemp.imageViewDisplayCard.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            viewTemp.imageViewDisplayCard.tag = x.toString() // tag the card number to the image
            if (cardsPoints.elementAt(x) != 0) {
                viewTemp.textViewDisplayCard.text = "${cardsPoints.elementAt(x)}"
                if (animations) viewTemp.textViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale)) //                if (animations) viewTemp.imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw_self_cards))
            } else {
                viewTemp.textViewDisplayCard.visibility = View.GONE
            }
            viewTemp.imageViewDisplayCard.setOnClickListener {
                validateSelfPlayedCard(it)
                viewTemp.imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
            }
            binding.imageGallery.addView(viewTemp.root)
        }
        if (animations) {
            findViewById<ConstraintLayout>(R.id.playerCards).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
        }
        if (bidingRequest && activityExists) {
            startBidding()
        }
    }

    private fun clearAllAnimation() {
        for (i in 0 until nPlayers) { // first reset background and animation
            //            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
        }
        binding.imageGallery.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
        binding.imageGallery.clearAnimation()
    }

    private fun animateWinner() {
        if (soundStatus) SoundManager.instance?.playCardCollectSound()
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableWinnerAnim[roundWinner - 1]))
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.GONE
        }, 1000)
        findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1]).clearAnimation()
        for (i in 0 until nPlayers) { // don't GONE self card is it is primary reference view
            findViewById<ImageView>(refIDMappedTableImageView[i]).visibility = View.INVISIBLE
        }
    }

    private fun getTrumpStartPartnerSelection() {
        displayTrumpCard()
        if (bidder == fromInt) {  // only to bidder
            findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).visibility = View.VISIBLE // make selection frame visible
            findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
            findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1_4)
            speak("Choose a partner card") //choose 1st buddy text
//			displayAllCardsForPartnerSelection()  // inflate all the cards to choose from
            binding.partnerSelectRV.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            binding.partnerSelectRV.adapter = PartnerCardListAdapter(nPlayers = 4) { output ->
                partnerSelectClick4(output)
            }
        } else {  // to everyone else
            speak("Waiting to select partner card", speed = 1.05f)
            Handler(Looper.getMainLooper()).postDelayed({ autoPartnerSelect() }, timeAutoTrumpAndPartner.random()) // auto select for robot bidder
        }
    }

    private fun autoPartnerSelect() {
        val cardsInHandTemp = when (bidder) {
            2 -> cardsInHand2
            3 -> cardsInHand3
            else -> cardsInHand4
        } //        cardsInHandTemp = cardsInHand // dummy remove it
        val partnerCardSelected: Int = if (!cardsInHandTemp.contains(13 * trumpInt - 1)) 13 * trumpInt - 1 //check Ace of trump
        else if (!cardsInHandTemp.contains(13 * trumpInt - 2)) 13 * trumpInt - 2                   // check king of trump
        else if (!cardsInHandTemp.contains(12)) 12      // check ace of spade in hand
        else if (!cardsInHandTemp.contains(1)) 1      // check 3 of spade in hand
        else if (!cardsInHandTemp.contains(11)) 11      // check king of spade in hand
        else if (!cardsInHandTemp.contains(13 * trumpInt - 3)) 13 * trumpInt - 3                   // check Queen of trump
        else if (!cardsInHandTemp.contains(25)) 25      // check ace of heart in hand
        else if (!cardsInHandTemp.contains(38)) 38      // check ace of club in hand
        else if (!cardsInHandTemp.contains(51)) 51      // check ace of diamond in hand
        else if (!cardsInHandTemp.contains(13 * trumpInt - 4)) 13 * trumpInt - 4                   // check Jack of trump
        else if (!cardsInHandTemp.contains(24)) 24      // check king of heart in hand
        else if (!cardsInHandTemp.contains(37)) 37      // check king of club in hand
        else if (!cardsInHandTemp.contains(50)) 50      // check king of diamond in hand
        else if (!cardsInHandTemp.contains(10)) 10      // check queen of spade in hand
        else if (!cardsInHandTemp.contains(23)) 23      // check queen of heart in hand
        else if (!cardsInHandTemp.contains(36)) 36      // check queen of club in hand
        else if (!cardsInHandTemp.contains(49)) 49      // check queen of diamond in hand
        else 10                                        // 10 of spade
        partnerSelectClick4(partnerCardSelected)
    }

    private fun partnerSelectClick4(cardSelected: Int) { // assumption is cards in Hand already updated
        if (!partnerCardSelected && bidder == fromInt && cardsInHand.contains(cardSelected)) {
            if (soundStatus) SoundManager.instance?.playErrorSound()
            if (vibrateStatus) vibrationStart()
            speak("Choose any other card", speed = 1.05f)
            toastCenter("You already have same card. Choose other card")
        } else if (!partnerCardSelected) {
            partnerCardSelected = true
            textToSpeech.stop()
            bu1 = cardSelected
            bu1Flag = 1 // bidder has one of card in his hand
            if (vibrateStatus) vibrationStart()
            binding.buddyImage1.setImageResource(cardsDrawablePartner[bu1])
            moveView(binding.buddyImage1, findViewById(refIDMappedImageView[bidder - 1]))
            binding.buddyImage1.clearAnimation()
            findViewById<TickerView>(R.id.buddyText1).clearAnimation()
            gameState.value = 5  // change game state to next playing round
            if (bidder == fromInt) { // at this point bidder is fixed and more reliable than player turn
                findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                Handler(Looper.getMainLooper()).postDelayed({
                    findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).clearAnimation()
                }, 180)
            }
        }
    }

    private fun displayTrumpCard() {
        when (trump) {
            "H" -> {
                findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_hearts))
                findViewById<TickerView>(R.id.trumpText).text = "Heart"
            }
            "S" -> {
                findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_spades))
                findViewById<TickerView>(R.id.trumpText).text = "Spade"

            }
            "D" -> {
                findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_diamonds))
                findViewById<TickerView>(R.id.trumpText).text = "Diamond"

            }
            "C" -> {
                findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_clubs))
                findViewById<TickerView>(R.id.trumpText).text = "Club"

            }
        }
        findViewById<ImageView>(R.id.trumpImage).clearAnimation() // main trump showing view
        moveView(binding.trumpImage, findViewById(refIDMappedImageView[bidder - 1]))
    } // just displaying trump card

    private fun startTrumpSelection() { //        textViewBidValue.textColor = ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
        //        findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer44))
        if (bidder != fromInt) {     //  show to everyone except bidder
            autoTrumpSelect()
            speak("${playerName(bidder)} won bid. Waiting to choose trump", speed = 1.10f)
        } else { // show to bidder only
            binding.bidNowImage.visibility = View.GONE // redundant not required really
            speak("You won bid. Choose your trump now", speed = 1.10f, queue = TextToSpeech.QUEUE_ADD)
            findViewById<ConstraintLayout>(R.id.frameTrumpSelection).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
            if (vibrateStatus) vibrationStart()
        }
    }

    private fun autoTrumpSelect() {
        val tempView = View(applicationContext)
        tempView.tag = listOf("d", "h", "c", "s").random()
        Handler(Looper.getMainLooper()).postDelayed({ onTrumpSelectionClick(tempView) }, timeAutoTrumpAndPartner.random())
    }

    fun onTrumpSelectionClick(view: View) {
        if (!trumpSelected) {
            trumpSelected = true
            if (soundStatus) SoundManager.instance?.playUpdateSound() //soundUpdate.start()
            when (view.tag) {
                "h" -> {
                    trump = "H"
                    trumpInt = 2
                }
                "s" -> {
                    trump = "S"
                    trumpInt = 1
                }
                "d" -> {
                    trump = "D"
                    trumpInt = 4
                }
                "c" -> {
                    trump = "C"
                    trumpInt = 3
                }
            }
            gameState.value = 4
            if (bidder == fromInt) { // at this point bidder is fixed and more reliable than player turn
                findViewById<ConstraintLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                Handler(Looper.getMainLooper()).postDelayed({
                    findViewById<ConstraintLayout>(R.id.frameTrumpSelection).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.frameTrumpSelection).clearAnimation()
                }, 170)
            }
        }
    }

    private fun startBidding() {
        bidTurnListener = Observer<Int> {
            val bidSpeak = bidValue != bidValuePrev
            bidValuePrev = bidValue
            if (!bidingStarted) {
                binding.bidNowImage.visibility = View.VISIBLE
                speak("You will start bidding", speed = 1f)
            }
            if (bidSpeak && bidingStarted && soundStatus) {
                speak("${playerName(bidder)} bid $bidValue", speed = 1.3f)
                GameScreenAutoPlay().moveView(binding.bidCoin, findViewById(refIDMappedImageView[bidder - 1]))
            } //            else if (soundStatus) SoundManager.instance?.playUpdateSound()//soundUpdate.start()
            binding.textViewBidValue.text = "$bidValue" //.toString() //show current bid value $emojiScore
            findViewById<TextView>(R.id.textViewBider).text = getString(R.string.Bider) + playerName(bidder) //            textViewBidValue.textColor = ContextCompat.getColor(applicationContext, R.color.font_yellow)
            //            findViewById<TextView>(R.id.textViewBider).setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
            findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE //biding frame invisible
            resetBackgroundAnimationBidding() //set all background to black or red depending on status
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
            animatePlayer(playerTurn.value!!)  // animate current player
            val tView: ImageView = findViewById(refIDMappedImageView[playerTurn.value!! - 1])
            binding.bidNowImage.animate().x(tView.x).y(tView.y).duration = 450
            if (bidStatus[playerTurn.value!! - 1] == 1) {  // highlight current player
                findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[playerTurn.value!! - 1]).visibility = View.VISIBLE
            }
            if (playerTurn.value!! == fromInt && (bidder != playerTurn.value!! || !bidingStarted)) {
                if (bidStatus[playerTurn.value!! - 1] == 1) { // show bid frame and ask to bid or pass
                    bidDone = false
                    binding.imageGallery.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                    findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is critical
                    findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
                    countDownTimer("Bidding", purpose = "start")
                    if (vibrateStatus) vibrationStart()
                } else if (bidStatus[playerTurn.value!! - 1] == 0) {
                    playerTurn.value = nextBidderTurn(playerTurn.value!!)
                }
            }
            if (bidder == playerTurn.value!! && bidingStarted) { // finish bid and move to next game state  // dummy playerTurn.value!! == fromInt &&
                playerTurn.removeObserver(bidTurnListener)
                gameState.value = 3 //// change game state to 3 as biding is finished
            } else if (playerTurn.value!! != 1 && (bidder != playerTurn.value!! || !bidingStarted)) {
                autoBid()
            } //            bidingStarted = true
        }
        playerTurn.observe(this, bidTurnListener)
    }

    private fun moveView(viewToMove: View, fromView: View, duration: Long = 350) {
        val xViewToMove = viewToMove.x
        val yViewToMove = viewToMove.y
        viewToMove.x = fromView.x
        viewToMove.y = fromView.y
        viewToMove.animate().x(xViewToMove).y(yViewToMove).duration = duration
    }

    fun nextBidderTurn(currentTurn: Int): Int {
        var nBT = currentTurn
        while (true) {
            nBT = nextTurn(nBT)
            if (bidStatus[nBT - 1] == 1) break
        }
        return nBT
    }

    private fun autoBid() {
        val tempView = View(applicationContext)
        if (bidStatus[playerTurn.value!! - 1] == 1) {
            if (maxAutoBidLimit.random() > bidValue) tempView.tag = listOf("5", "10", "20", "pass").random() //check if bid value has not reached maximum allowed bid value
            else tempView.tag = "pass"
            countDownBidding.cancel() // dummy
            Handler(Looper.getMainLooper()).postDelayed({
                bidDone = false
                askToBid(tempView)
            }, timeAutoBid.random()) // timeDelayAutoPlay.random())
        } else {
            playerTurn.value = nextBidderTurn(playerTurn.value!!)
        }
    }

    fun askToBid(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        countDownTimer("Bidding", purpose = "cancel")
        if (!bidDone) {
            bidDone = true
            if (soundStatus) SoundManager.instance?.playUpdateSound() // soundUpdate.start()
            when (view.tag) {
                "pass" -> {
                    bidStatus[playerTurn.value!! - 1] = 0
                    speak("${playerName(playerTurn.value!!)} passed", speed = 1.15f)
                    if (playerTurn.value!! == fromInt) centralText("    Time's Up !!  \n You cannot bid anymore", 2500)
                }
                "5" -> {
                    bidValue = min(bidValue + 5, maxBidValue)
                    bidder = playerTurn.value!!
                }
                "10" -> {
                    bidValue = min(bidValue + 10, maxBidValue)
                    bidder = playerTurn.value!!
                }
                "20" -> {
                    bidValue = min(bidValue + 20, maxBidValue)
                    bidder = playerTurn.value!!
                }
                "50" -> {
                    bidValue = min(bidValue + 50, maxBidValue)
                    bidder = playerTurn.value!!
                }
                "75" -> {
                    bidValue = min(bidValue + 75, maxBidValue)
                    bidder = playerTurn.value!!
                }
            }
            if (playerTurn.value!! == fromInt) {
                findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                Handler(Looper.getMainLooper()).postDelayed({
                    findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
                    findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
                }, 200)
            }
            bidingStarted = true
            playerTurn.value = nextBidderTurn(playerTurn.value!!)
        }
    }

    private fun speak(speechText: String, speed: Float = 1f, queue: Int = TextToSpeech.QUEUE_FLUSH, forceSpeak: Boolean = false) {
        if (speechStatus && this::textToSpeech.isInitialized && (forceSpeak || !closeRoom)) {
            textToSpeech.setPitch(1f)
            textToSpeech.setSpeechRate(speed)
            textToSpeech.speak(speechText, queue, bundleOf(Pair(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.15f)), null)
        }
    }

    private fun initializeSpeechEngine() {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastCenter("Missing Language data - Text to speech")
                } else if (!shuffleOver) {
                    speak("Shuffling cards Please wait", speed = 1f)
                }
            }
        }
    }

    private fun animatePlayer(index: Int) {
        findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[index - 1]).visibility = View.VISIBLE
    }

    private fun resetBackgroundAnimationBidding() {
        for (i in 0 until nPlayers) {
            val iPlayer = i + 1
            findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
            if (bidStatus[i] == 0) {
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer2))
                findViewById<ImageView>(refIDMappedImageView[i]).foreground = ContextCompat.getDrawable(applicationContext, R.drawable.pass)
                if (iPlayer == fromInt) binding.imageGallery.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer2))
            } else {
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))

                if (iPlayer == fromInt) binding.imageGallery.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
            }
            if (iPlayer == fromInt) binding.imageGallery.clearAnimation()
        }
    }

    private fun finishPassOverlay() {
        for (i in 0 until nPlayers) {
            findViewById<ImageView>(refIDMappedImageView[i]).foreground = null
        }
        binding.bidNowImage.visibility = View.GONE
    }

    private fun finishBackgroundAnimationBidding() {  //clear Everything on finish of biding round
        for (i in 0 until nPlayers) {
            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
        }
        binding.imageGallery.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
        binding.imageGallery.clearAnimation()
        findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[bidder - 1]).visibility = View.VISIBLE

        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
        findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
    }

    private fun centralText(message: String = "", displayTime: Int = 3000, cancel: Boolean = false) {
        if (cancel) {
            findViewById<TextView>(R.id.textViewShuffling).clearAnimation()
            findViewById<TextView>(R.id.textViewShuffling).text = ""
            findViewById<TextView>(R.id.textViewShuffling).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.textViewShuffling).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewShuffling).text = message
            findViewById<TextView>(R.id.textViewShuffling).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_infinite_700ms))
            if (displayTime != 0) Handler(Looper.getMainLooper()).postDelayed({
                findViewById<TextView>(R.id.textViewShuffling).clearAnimation()
                findViewById<TextView>(R.id.textViewShuffling).visibility = View.GONE
            }, displayTime.toLong())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlayerInfo() {
        playerInfo = intent.getStringArrayListExtra("playerInfo") as ArrayList<String>
        val playerInfoCoins = intent.getStringArrayListExtra("playerInfoCoins") as ArrayList<Int>

        p1 = playerInfo[0]
        p2 = playerInfo[1]
        p3 = playerInfo[2]
        p4 = playerInfo[3]
        p1Coins = playerInfoCoins[0]
        p2Coins = playerInfoCoins[1]
        p3Coins = playerInfoCoins[2]
        p4Coins = playerInfoCoins[3]

        updatePlayerNames()
        for (i in 0 until nPlayers) {
            val j = i + nPlayers
            if (playerInfo[j].isNotEmpty()) {
                Picasso.get().load(playerInfo[j]).resize(300, 300).centerCrop().error(R.drawable.s3).into(findViewById<ImageView>(refIDMappedImageView[i]))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlayerNames() {
        val totalCoins = listOf(p1Coins, p2Coins, p3Coins, p4Coins)
        for (i in 0 until nPlayers) {
            findViewById<MaterialTextView>(refIDMappedTextView[i]).text = playerName(i + 1)
            findViewById<TickerView>(refIDMappedTextViewA[i]).text = "$${String.format("%,d", totalCoins[i])}"
        }
    }

    private fun shufflingWindow(time: Long = 4900, fadeOffTime: Long = 700, gameStateChange: Boolean = false) {
        shuffleOver = false
        if (soundStatus) Handler(Looper.getMainLooper()).postDelayed({
            SoundManager.instance?.playShuffleSound() // soundShuffle.start()
        }, 400) //delayed sound play of shuffling
        displayShufflingCards() //show suits cards and animate
        speak("Shuffling cards Please wait", speed = 1f)
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).animation = null
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).clearAnimation()
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.GONE
            binding.relativeLayoutTableCards.visibility = View.GONE
            binding.imageGallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_out))
            Handler(Looper.getMainLooper()).postDelayed({
                shuffleOver = true
                displaySelfCards(animations = true, bidingRequest = true)
            }, fadeOffTime)
        }, time)
    }

    private fun displayShufflingCards(view: View = View(this), sets: Int = 5, distribute: Boolean = true) {
        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.transparent))
        if (distribute) shufflingDistribute()
        binding.imageGallery.removeAllViews()
        for (xx: Int in 0 until sets) {
            for (x: Int in 0..3) {
                val viewTemp = CardsItemListSuitsBinding.inflate(layoutInflater, binding.imageGallery, false)  //inflater.inflate(R.layout.cards_item_list_suits, gallery, false)
                viewTemp.imageViewDisplayCard1.setImageResource(PlayingCards().suitsDrawable[x])
                viewTemp.imageViewDisplayCard1.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw))
                if (x % 2 != 0) {
                    viewTemp.imageViewDisplayCard1.setBackgroundColor(ContextCompat.getColor(this, R.color.cardsBackgroundDark))
                } else {
                    viewTemp.imageViewDisplayCard1.setBackgroundColor(ContextCompat.getColor(this, R.color.cardsBackgroundLight))
                }
                binding.imageGallery.addView(viewTemp.root)
            }
        }
        findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.transparent))
        binding.imageGallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right))
    }

    private fun shufflingDistribute() {
        binding.relativeLayoutTableCards.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_shuffle_4)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                findViewById<ImageView>(R.id.imageViewWinnerCenter4).startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).startAnimation(anim)

    }

    private fun toastCenter(message: String) {
        if (!this::snackBar.isInitialized) {
            snackBar = Snackbar.make(findViewById(R.id.gameScreen1), message, Snackbar.LENGTH_LONG).setAction("Dismiss") { snackBar.dismiss() }
            snackBar.setActionTextColor(getColor(R.color.borderblue))
            snackBar.view.setOnClickListener { snackBar.dismiss() }
        } else snackBar.setText(message)
        snackBar.show() //        toast.setText(message)
        //        toast.show()
    }

    private fun playerName(index: Int): String {
        var name = ""
        when (index) {
            1 -> name = p1
            2 -> name = p2
            3 -> name = p3
            4 -> name = p4
        }
        return name
    }

    private fun nextTurn(current: Int): Int {
        var next = 0
        if (current != 4) {
            next = current + 1
        } else if (current == 4) next = 1
        return next
    }

    private fun getSharedPrefs() {
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
        }
        initializeAds()
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
        }
        if (sharedPreferences.contains("speechStatus")) {
            speechStatus = sharedPreferences.getBoolean("speechStatus", true)
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
            ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt() + requestRatingAfterDays
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
            MobileAds.initialize(this) {
                Log.d("Inter", "onInitializationComplete")
                binding.addViewChatGameScreenBanner.loadAd(AdRequest.Builder().build())  //load ad to banner view Admob
                binding.addViewGameScreenBanner.loadAd(AdRequest.Builder().build())  //load ad to banner view Admob
                binding.addViewChatGameScreenBanner.visibility = View.VISIBLE
                binding.addViewGameScreenBanner.visibility = View.VISIBLE
                loadInterstitialAd()
            }
        } else {
            binding.addViewGameScreenBanner.visibility = View.GONE
            binding.addViewChatGameScreenBanner.visibility = View.GONE
        }
    }

    private fun loadInterstitialAd(showAd: Boolean = false) {
        loadInterAdTry += 1 // try 5 times
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.inter_admob), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("InterstitialAd", "onAdFailedToLoad")
                mInterstitialAd = null
                if (loadInterAdTry <= 2) loadInterstitialAd()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("InterstitialAd", "onAdLoaded")
                loadInterAdTry = 0
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.d("InterstitialAd", "onAdFailedToShowFullScreenContent")
                    mInterstitialAd = null
                    loadInterstitialAd()
                }
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAd", "onAdDismissedFullScreenContent")
                    mInterstitialAd = null
                    logFirebaseEvent(key = "watched_ad")
                    if (gameState.value!! == 6) {
                        binding.startNextRoundButton.visibility = View.VISIBLE
                    }
                }
                override fun onAdShowedFullScreenContent() {}
                override fun onAdImpression() {}
            }
//            mInterstitialAd!!.setImmersiveMode(true)
            mInterstitialAd!!.show(this)
        } else {
            Log.d("Inter", "InterstitialAd is Null")
            loadInterstitialAd()
        }
    }

    fun showDialogue(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        closeRoom = true
        speak("Are you sure want to leave the game", speed = 0.95f, forceSpeak = true)
        if (!this::alertDialog.isInitialized) {
            val builder = AlertDialog.Builder(this)

            val titleTextView = DialogueTitleBinding.inflate(LayoutInflater.from(this))
            titleTextView.dialogueTitle.text = "Exit Game"
            builder.setCustomTitle(titleTextView.root)

            val bodyTextView = DialogueBodyBinding.inflate(LayoutInflater.from(this))
            bodyTextView.dialogueBody.text = getString(R.string.leave_room_confirm)
            builder.setView(bodyTextView.root)

            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                toastCenter("Leaving game now")
                speak("Leaving game", forceSpeak = true)
                Handler(Looper.getMainLooper()).postDelayed({ closeGameRoom() }, 250)
            }
            builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
                closeRoom = false
            }
            builder.setOnDismissListener {
                closeRoom = false
            }
            alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shine_dialogue_background))
        }
        alertDialog.show()
    }

    private fun closeGameRoom() {
        activityExists = false
        countDownBidding.cancel()
        countDownPlayCard.cancel()
        startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) }.apply { putExtra("returnFromGameScreen", true) }.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
        finishAndRemoveTask()
    }

    override fun onStop() {
        super.onStop() //        gameState.removeObserver(gameStateListener)
        //        try {
        //            gameTurn.removeObserver(roundListener)
        //        } catch (me: Exception) {
        ////            toastCenter(me.toString()) // dummy think to implement a good way
        //        }
        //        soundShuffle.stop()
        try { //            playerTurn.removeObserver(bidTurnListener)
        } catch (me: Exception) {
            toastCenter(me.toString())
        }
        countDownTimer("PlayCard", purpose = "cancel")
        countDownTimer("Bidding", purpose = "cancel")
    }

    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if (!scoreOpenStatus) { //            if(vibrateStatus) vibrationStart()
            toastCenter("App is minimized")
            this.moveTaskToBack(true)
        } else {
            openCloseScoreSheet(View(applicationContext))
        }
    } // is offline

    override fun onDestroy() {
        try {
            mInterstitialAd!!.fullScreenContentCallback = null
            mInterstitialAd = null
        } catch (_: java.lang.Exception) {
        }
        binding.addViewGameScreenBanner.destroy()
        binding.addViewChatGameScreenBanner.destroy()
        try {
            if (this::textToSpeech.isInitialized) {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
        } catch (me: java.lang.Exception) {
        }
        super.onDestroy()

    }
}


