@file:Suppress("UNUSED_PARAMETER", "ImplicitThis", "DEPRECATION")

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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var refSelfCardTable: ImageView
    private lateinit var cardsDrawable: List<Int>
    private lateinit var cardsPoints: List<Int>
    private lateinit var cardsSuit: List<String>
    private lateinit var cardsDrawablePartner: List<Int>
    private lateinit var cardsIndexSortedPartner: List<Int>
    private lateinit var cardsPointsPartner: List<Int>
    private var cardsIndexLimit = 0
    private var roundNumberLimit = 0
    private var scoreLimit = 0
    private var reviewRequested = false
    private var soundStatus = true
    private var speechStatus = true
    private var vibrateStatus = true
    private lateinit var vibrator: Vibrator
    private var interpolator = AccelerateDecelerateInterpolator()

    private var premiumStatus = false
    private var activityExists = true
    private var mInterstitialAd: InterstitialAd? = null
    private var loadInterAdTry = 0

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private var fromInt = 0
    private var nPlayers = 4
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

    private var scoreWindowOpen = MutableLiveData(false)
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

    private var delayWaitGameMode6 = 4500L
    private var delayDeclareWinner = 2500L
    private var moveViewDuration = 350L
    private var timeCountdownPlayCard = if (BuildConfig.DEBUG) 6000L else 20000L
    private var timeAutoPlayCard = if (BuildConfig.DEBUG) listOf<Long>(100, 150, 200, 175, 125, 225) else listOf<Long>(500, 700, 850, 600, 700, 600, 1000)
    private var timeCountdownBid = if (BuildConfig.DEBUG) 2000L else 20000L
    private var timeAutoBid = if (BuildConfig.DEBUG) listOf<Long>(800, 1200, 900) else listOf<Long>(1650, 1400, 1500, 1800)
    private var speedAutoBid = 1.1f
    private var timeAutoTrumpAndPartner = if (BuildConfig.DEBUG) listOf<Long>(300) else listOf<Long>(1700, 2000, 1700)
    private var maxAutoBidLimit = listOf(220, 225, 230, 235, 245, 250)

    private var played = false

    private var pt1 = 0
    private var pt2 = 0
    private var pt3 = 0
    private var pt4 = 0

    private lateinit var ptAll: List<Int>
    private var bidTeamScore = 0
    private lateinit var scoreList: List<Int>
    private var scoreBodyArrayList = arrayListOf<Int>()
    private var scoreHeaderArrayList = arrayListOf<PlayerScoreItemDescription>()
    private lateinit var adapterScoreHeader: ScoreHeaderAdapter
    private lateinit var adapterScoreBody: ScoreBodyAdapter
    private var selfCardsArrayList = arrayListOf<PlayingCardDescription>()
    private lateinit var adapterSelfCards: SelfCardListAdapter
    private val adapterShuffleCards = ShuffleCardsAdapter()

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
    private var buddyImage1X = 0F
    private var buddyImage1Y = 0F
    private var trumpImageX = 0F
    private var trumpImageY = 0F
    private var bidCoinX = 0F
    private var bidCoinY = 0F


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
        binding.buddyImage1.doOnLayout {
            buddyImage1X = binding.buddyImage1.x
            buddyImage1Y = binding.buddyImage1.y  }
        binding.trumpImage.doOnLayout {
            trumpImageX = binding.trumpImage.x
            trumpImageY = binding.trumpImage.y  }
        binding.bidCoin.doOnLayout {
            bidCoinX = binding.bidCoin.x
            bidCoinY = binding.bidCoin.y  }


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
        refIDMappedTableWinnerAnim = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline) && resources.getBoolean(R.bool.enable_super_fast_test_offline)) listOf(R.anim.anim_table_card_winner_1_sf, R.anim.anim_table_card_winner_2_4_sf, R.anim.anim_table_card_winner_6_sf, R.anim.anim_table_card_winner_self_sf)
        else PlayersReference().refIDMappedTableWinnerAnim(from, nPlayers)
        refIDMappedTableImageView = PlayersReference().refIDMappedTableImageView(from, nPlayers)
        refSelfCardTable = binding.imageViewSelf4
        //region Other Thread tasks
        Handler(Looper.getMainLooper()).post {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            updatePlayerInfo()
            initializeSpeechEngine()
            getSharedPrefs()
            scoreWindowOpen.observe(this) {
                if (it) {
                    binding.closeGameRoomIcon.visibility = View.GONE
                    binding.scoreViewLayout.visibility = View.VISIBLE
                    binding.scoreViewLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
                } else {
                    binding.closeGameRoomIcon.visibility = View.VISIBLE
                    binding.scoreViewLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_scoretable_close))
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.scoreViewLayout.visibility = View.GONE
                    }, 180)
                }
            }
            // region       Countdown PlayCard
            countDownPlayCard = object : CountDownTimer(timeCountdownPlayCard, 20) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = (millisUntilFinished * 10000 / timeCountdownPlayCard).toInt()   //10000 because max progress is 10000
                    findViewById<TextView>(R.id.textViewTimer).text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString() + "s"
                }

                override fun onFinish() {
                    autoPlayCard(cardsInHand, forcePlay = false)
                    if (soundStatus && !(BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline))) SoundManager.instance?.playTimerSound()
                    if (vibrateStatus) vibrationStart()
                    findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                    binding.closeGameRoomIcon.visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                    findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                    findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                    findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                }
            } //        endregion
            // region       Countdown Biding
            countDownBidding = object : CountDownTimer(timeCountdownBid, 20) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    binding.progressbarTimer.progress = (millisUntilFinished * 10000 / timeCountdownBid).toInt()
                    binding.textViewTimer.text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString() + "s"
                }

                override fun onFinish() {
                    if (!bidDone) {
                        bidDone = true
                        if (vibrateStatus) vibrationStart()
                        if (soundStatus) SoundManager.instance?.playTimerSound()
                        bidStatus[playerTurn.value!! - 1] = 0
                        bidingStarted = true
                        playerTurn.value = nextBidderTurn(fromInt)
                        findViewById<ProgressBar>(R.id.progressbarTimer).progress = 0
                        binding.closeGameRoomIcon.visibility = View.VISIBLE
                        findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.GONE
                        findViewById<TextView>(R.id.textViewTimer).visibility = View.GONE
                        findViewById<ProgressBar>(R.id.progressbarTimer).clearAnimation()
                        findViewById<TextView>(R.id.textViewTimer).clearAnimation()
                        findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
                        findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
                        centralText("    Time's Up !!  \n You cannot bid anymore", 2500)
                        speak("Time's Up ${playerName(fromInt)}. You can't bid now", speed = 1.05f)
                    }
                }
            } //        endregion
            // region Game State Listener
            gameStateListener = Observer {
                if (gameState.value!! == 1) {
                    binding.horizontalScrollView1.foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.transparent))
                    if (!roundStarted) {
                        resetVariables()
                        if (scoreWindowOpen.value!!) scoreWindowOpen.value = false
                        if (this::p1.isInitialized) updatePlayerNames()
                        roundStarted = true
                        if (BuildConfig.DEBUG) shufflingWindow(time = 500L, gameStateChange = true) // gameStateChange = change game state to 2 after shuffling
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
                        if (nGamesPlayed < 10) Handler(Looper.getMainLooper()).postDelayed({ startPlayingRound() }, 2000)
                        else Handler(Looper.getMainLooper()).postDelayed({ startPlayingRound() }, 1000)

                        if (playerTurn.value!! != fromInt) {
                            if (nGamesPlayed < 10) speak("${playerName(bidder)} will play first \n You get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1.1f)
                            else speak("${playerName(bidder)} will play first ", speed = 1f)
                        }
                        if (playerTurn.value!! == fromInt) {
                            if (nGamesPlayed < 10) speak("You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1f)
                        }
                    } else {
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
            FirebaseCrashlytics.getInstance().setCustomKey("UID", "https://console.firebase.google.com/u/0/project/kaali-ki-teegi/firestore/data/~2FUsers~2F$uid?consoleUI=FIREBASE")
            refUsersData.document(uid).set(hashMapOf("LPD_bot" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()), SetOptions.merge())
        } // endregion
        // region table card listener
        ct1 = MutableLiveData()
        ct1.value = cardsIndexLimit
        ct1.observe(this) { cardSelected ->
            tablePointsCalculator()
            if (cardSelected <= (cardsIndexLimit - 2)) {
                if (soundStatus) SoundManager.instance?.playCardPlayedSound()
                findViewById<ImageView>(refIDMappedTableImageView[0]).visibility = View.VISIBLE
                findViewById<ImageView>(refIDMappedTableImageView[0]).setImageResource(cardsDrawable[cardSelected])
                val cardViewHolder = binding.selfCards.layoutManager?.findViewByPosition(cardsInHand.indexOf(cardSelected))
                if (cardViewHolder != null) {
                    moveView(refSelfCardTable, toX = refSelfCardTable.x, toY = refSelfCardTable.y, fromX = binding.selfCards.x + cardViewHolder.x, fromY = binding.selfCards.y - cardViewHolder.height)
                }
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
                binding.konfettiLottie.visibility = View.VISIBLE
                binding.winnerLottie.visibility = View.VISIBLE
                binding.winnerLottie.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
                binding.winnerLottie.playAnimation()
                binding.konfettiLottie.playAnimation()
            } else {
                binding.startNextRoundButton.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.5F }
                binding.gridLoser.visibility = View.GONE
                binding.gridWinner.visibility = View.GONE
                binding.textWinner.visibility = View.GONE
                binding.textLoser.visibility = View.GONE
                binding.textResult.visibility = View.GONE
                binding.konfettiLottie.visibility = View.GONE
                binding.winnerLottie.visibility = View.GONE
                binding.winnerLottie.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
                binding.winnerLottie.cancelAnimation()
                binding.konfettiLottie.cancelAnimation()
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
        startNextGame(binding.startNextRoundButton)
    }

    private fun logFirebaseEvent(event: String = "game_screen", int: Int = 1, key: String) {
        val params = Bundle()
        params.putInt(key, int)
        firebaseAnalytics.logEvent(event, params)
    }

    private fun setupGame() {
        delayWaitGameMode6 = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 500L else 4500L
        delayDeclareWinner = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 300L else 2500L
        moveViewDuration = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 30L else 350L
        timeCountdownBid = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 80L else if (BuildConfig.DEBUG) 2000L else 20000L
        timeCountdownPlayCard = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 80L else if (BuildConfig.DEBUG) 5000L else 20000L
        timeAutoPlayCard = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) listOf(120L) else timeAutoPlayCard
        timeAutoBid = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) listOf(100L) else if (BuildConfig.DEBUG) listOf<Long>(800, 1200, 900) else listOf<Long>(1650, 1400, 1500, 1800)
        timeAutoTrumpAndPartner = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) listOf(200L) else if (BuildConfig.DEBUG) listOf<Long>(300) else listOf<Long>(1700, 2000, 1700)

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
        scoreList = listOf(1, pt1, pt2, pt3, pt4)

        binding.selfCards.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.HORIZONTAL }
        binding.gridScoreHeader.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.HORIZONTAL }
        binding.gridScoreBody.layoutManager = GridLayoutManager(this, nPlayers + 1, LinearLayoutManager.VERTICAL, false)
        adapterSelfCards = SelfCardListAdapter(cardsArray = selfCardsArrayList) { cardSelected ->
            validateSelfPlayedCard(cardSelected)
        }

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
            BuildConfig.DEBUG -> timeAutoPlayCard
            nGamesPlayed < 5 -> listOf(1000, 900, 1100, 1200, 800)
            nGamesPlayed < 10 -> listOf(600, 900, 650, 800, 700, 900)
            nGamesPlayed < 15 -> listOf(500, 550, 600, 700, 800)
            nGamesPlayed < 30 -> listOf(400, 350, 400, 300)
            else -> listOf(350, 250, 300, 300, 200)
        }
        timeAutoBid = when {
            BuildConfig.DEBUG -> timeAutoBid
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
        displayShufflingCards(distribute = false)
        if (newGameStatus) { // dummy - newGameStatus not needed as score list has game index which is unique
            newGameStatus = false
//            updateWholeScoreBoard()
            maskWinner.value = true
            gameNumber += 1
        }
        if (mInterstitialAd == null && !premiumStatus) loadInterstitialAd()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!scoreWindowOpen.value!!) scoreWindowOpen.value = true
            binding.startNextRoundButton.visibility = View.VISIBLE
            maskWinner.value = false
            if (!rated && !reviewRequested && (nGamesPlayed > 10 || gameNumber > 3)) {  // Ask only once per game
                inAppReview()
                reviewRequested = true
            } else if (!premiumStatus && ((gameNumber - 1) % gameLimitNoAds == 0) && !(BuildConfig.DEBUG && resources.getBoolean(R.bool.disable_ads_game_screen_offline))) {
                if (mInterstitialAd != null) {
                    showInterstitialAd()
                }
            } else if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) {
                startNextGame(View(this))
                binding.startNextRoundButton.visibility = View.GONE
            }
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
        binding.textResult.visibility = View.VISIBLE
        binding.winnerLottie.visibility = View.VISIBLE
        binding.winnerLottie.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.tint_background_lottie))
        binding.winnerLottie.playAnimation()

        p1Gain += scoreList[1]
        p2Gain += scoreList[2]
        p3Gain += scoreList[3]
        p4Gain += scoreList[4]

        totalDailyCoins += scoreList[1]
        p1Coins += scoreList[1]
        p2Coins += scoreList[2]
        p3Coins += scoreList[3]
        p4Coins += scoreList[4]

        val arrayListWinner = ArrayList<PlayerScoreItemDescription>()
        val arrayListLoser = ArrayList<PlayerScoreItemDescription>()
        val scoredList = listOf(pt1, pt2, pt3, pt4)
        for (i in 1..nPlayers) {
            val target = if (i == bidder || i == buPlayer1) bidValue else scoreLimit - bidValue
            if (scoreList[i] > 0) arrayListWinner.add(PlayerScoreItemDescription(scored = scoredList[i - 1], target = target, points = scoreList[i], playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1]))
            else arrayListLoser.add(PlayerScoreItemDescription(scored = scoredList[i - 1], target = target, points = scoreList[i], playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1]))
        }
        binding.gridWinner.adapter = PlayerWinnerGridAdapter(arrayList = arrayListWinner, winner = true)
        binding.gridLoser.adapter = PlayerWinnerGridAdapter(arrayList = arrayListLoser, winner = false)
//        maskWinner.value = true

        updateScoreTableHeader()
        updateScoreTableBody()

        if (bidder == fromInt) {
            nGamesBid += 1
            nGamesBidDaily += 1
        }
        nGamesPlayed += 1
        nGamesPlayedDaily += 1
        updateTimeAutoPlay()
        refUsersData.document(uid).set(hashMapOf("LPD_bot" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt(), "sc" to playerCoins(from), "scd" to totalDailyCoins, "w_bot" to nGamesWon, "w_daily" to nGamesWonDaily, "b_bot" to nGamesBid, "b_daily" to nGamesBidDaily, "p_bot" to nGamesPlayed, "p_daily" to nGamesPlayedDaily), SetOptions.merge())

    }

    private fun updateScoreTableBody() {
        val prevSize = scoreBodyArrayList.size
        scoreBodyArrayList.addAll(scoreList)
        if (!this::adapterScoreBody.isInitialized) {
            adapterScoreBody = ScoreBodyAdapter((scoreBodyArrayList), nColumns = nPlayers + 1)
            binding.gridScoreBody.adapter = adapterScoreBody
        }
        adapterScoreBody.notifyItemRangeInserted(prevSize, scoreList.size)
        binding.gridScoreBody.scrollToPosition(scoreBodyArrayList.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateScoreTableHeader(showRank: Boolean = true) {
        scoreHeaderArrayList.clear() // = ArrayList<PlayerScoreItemDescription>()
        scoreHeaderArrayList.add(PlayerScoreItemDescription(playerName = "Player"))
        val totalList = listOf(p1Gain, p2Gain, p3Gain, p4Gain)
        val sortedArray = totalList.distinct().sortedDescending()
        for (i in 1..nPlayers) {
            scoreHeaderArrayList.add(PlayerScoreItemDescription(playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1], points = totalList[i - 1], rank = rankStringFromInt(1 + sortedArray.indexOf(totalList[i - 1])), showRank = showRank))
        }
        adapterScoreHeader.notifyDataSetChanged()
//        for (i in 1..3) { //dummy for 7 players testing
//            scoreHeaderArrayList.add(PlayerScoreItemDescription(playerName = playerName(i), imageUrl = playerInfo[nPlayers + i - 1], points = totalList[i-1]))
//        }
//            adapterScoreHeader = ScoreHeaderAdapter(scoreHeaderArrayList)
//            binding.gridScoreHeader.adapter = adapterScoreHeader
    }

    fun closeChatAndScoreWindow(view: View) {
        if (scoreWindowOpen.value!!) scoreWindowOpen.value = false
    }

    fun openCloseScoreSheet(view: View) {
        maskWinner.value = false
        scoreWindowOpen.value = !scoreWindowOpen.value!!
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
        binding.trumpImage.setImageResource(R.drawable.trump)
        binding.textViewBidValue.text = getString(R.string.bidValue2)  //$emojiScore
        binding.bidNowImage.visibility = View.GONE // redundant not required really
        binding.textViewBider.text = getString(R.string.Bider)
        binding.trumpText.text = getString(R.string.Trump)
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
        scoreList = listOf(gameNumber, pt1, pt2, pt3, pt4)
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
        if (scoreWindowOpen.value!!) scoreWindowOpen.value = false
        val cardsShuffled = (0..51).shuffled()
        cardsInHand = (cardsShuffled.slice(0..12).sortedBy { it }).toMutableList()  //  (mutableListOf<Int>(1,11,12,24,25,37,38,50,51, 49,23,36,5).sortedBy { it }).toMutableList()   // //
        createCardsArray()
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
                if (j == bidder || (j == buPlayer1 && buFound1 != 0)) findViewById<TickerView>(refIDMappedTextViewA[i]).text = "$bidTeamScore /$bidValue"
                else findViewById<TickerView>(refIDMappedTextViewA[i]).text = "${pointsList.sum() - bidTeamScore} /${scoreLimit - bidValue}"
            }
            var tt1 = 0
            if (buFound1 == 1) tt1 = pointsList[buPlayer1 - 1]
            val bidTeamScoreFinal = pointsList[bidder - 1] + tt1 // total score of bid team
            decideGameWinnerTeam4(bidTeamScoreFinal, totalGamePoints = pointsList.sum())
        }
    }

    private fun decideGameWinnerTeam4(bidTeamScoreFinal: Int, totalGamePoints: Int) {
        if (bidTeamScoreFinal >= bidValue) { // bidder team won case
            gameTurn.removeObserver(roundListener)

            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            centralText("Game Over")

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
            } else { // partner 1 found
                pointsListTemp[bidder] = bidValue * 2
                pointsListTemp[buPlayer1] = bidValue
            }
            scoreList = pointsListTemp
            updateWholeScoreBoard()
            Handler(Looper.getMainLooper()).postDelayed({ gameState.value = 6 }, delayDeclareWinner)
        } else if (buFound1 == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - bidValue)) { // if opponent score has reached target value & both partners are disclosed
            gameTurn.removeObserver(roundListener)
            clearAllAnimation()
            if (vibrateStatus) vibrationStart()
            centralText("Game Over")

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
            updateWholeScoreBoard()
            Handler(Looper.getMainLooper()).postDelayed({ gameState.value = 6 }, delayDeclareWinner)
        }
    }

    private fun displayPartnerIcon() {
        if (buFound1 == 1 && buPlayer1 != 8) {
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).setImageResource(R.drawable.partnericon)
            findViewById<ImageView>(refIDMappedPartnerIconImageView[buPlayer1 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
        }
//        moveView(binding.buddyText1, findViewById(refIDMappedTextView[buPlayer1 - 1]), duration = if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 200L else 500L)
        if (gameState.value!! == 5) updatePlayerScoreInfo(listOf(pt1, pt2, pt3, pt4))
    }

    private fun countDownTimer(task: String, purpose: String = "start") {
        if (purpose == "start") {
            binding.closeGameRoomIcon.visibility = View.GONE
            findViewById<ProgressBar>(R.id.progressbarTimer).progress = 100
            findViewById<ProgressBar>(R.id.progressbarTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewTimer).text = "10s" //            findViewById<ProgressBar>(R.id.progressbarTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            //            findViewById<TextView>(R.id.textViewTimer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
            if (task == "Bidding" && this::countDownBidding.isInitialized) countDownBidding.start()
            if (task == "PlayCard" && this::countDownPlayCard.isInitialized) countDownPlayCard.start()
        } else if (purpose == "cancel") {
            binding.closeGameRoomIcon.visibility = View.VISIBLE
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
                Handler(Looper.getMainLooper()).postDelayed({ declareRoundWinner() }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 0L else 700L)
            } else if (gameTurn.value!! != 5 && gameTurn.value!! != 0) {
                animatePlayer(playerTurn.value!!)
                if (playerTurn.value!! == fromInt && !played) {
                    displaySelfCards()
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

    private fun validateSelfPlayedCard(cardSelected: Int) {
        if (gameState.value!! == 5 && playerTurn.value!! == fromInt && gameTurn.value!! != 8 && gameTurn.value!! != 0) {
            Log.d("SelfCard", "1 selfCardsArrayList->${selfCardsArrayList.size}  cardsInHand->${cardsInHand.size}")
//            val cardSelected = selfCardsArrayList[position].cardInt
            if (gameTurn.value!! == 1 || cardsSuit[cardSelected] == trumpStart || cardsSuit.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) == -1) {
                countDownTimer("PlayCard", purpose = "cancel")
                startNextTurn(cardSelected, forcePlay = false) // allow throw if first chance, or same suit as first turn or doesn't have same suit card
            } else {
                binding.selfCards.layoutManager?.findViewByPosition(cardsInHand.indexOf(cardSelected))?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_highlight))
                if (soundStatus) SoundManager.instance?.playErrorSound() // soundError.start()
                if (vibrateStatus) vibrationStart()
                speak("Play ${getSuitName(trumpStart)} card", speed = 1.1f)
            }
        } else {
            binding.selfCards.layoutManager?.findViewByPosition(cardsInHand.indexOf(cardSelected))?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_highlight))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun startNextTurn(cardSelected: Int, forcePlay: Boolean) {
        if (!played || forcePlay) {
            if (!forcePlay) played = true // make it true only if human played not bot
            if (playerTurn.value!! != bidder) {
                checkIfPartnerAndUpdateServer4(cardSelected, playerTurn.value!!)
            }
            val pt = playerTurn.value!!
            val gt = gameTurn.value!!
            if (gt == 1) trumpStart = cardsSuit[cardSelected]

            if (roundNumber <= roundNumberLimit) {
                when (pt) {
                    1 -> {
                        ct1.value = cardSelected
                        val position = cardsInHand.indexOf(cardSelected)
                        selfCardsArrayList.removeAt(position)
                        adapterSelfCards.notifyItemRemoved(position)
                        cardsInHand.remove(cardSelected)
                        val rangeList = mutableListOf<Int>()
                        selfCardsArrayList.forEachIndexed { index, card ->
                            val notify = card.filter || if (index == selfCardsArrayList.size - 1) !card.expandCard else card.expandCard
                            if (notify) rangeList.add(index)
                            card.filter = false
                            card.expandCard = index == selfCardsArrayList.size - 1
                        }
                        if (rangeList.isNotEmpty()) {
                            adapterSelfCards.notifyItemRangeChanged(rangeList[0], rangeList.size)
                        }
                    }

                    2 -> {
                        ct2.value = cardSelected
                        cardsInHand2.remove(cardSelected)
                    }

                    3 -> {
                        ct3.value = cardSelected
                        cardsInHand3.remove(cardSelected)
                    }

                    4 -> {
                        ct4.value = cardSelected
                        cardsInHand4.remove(cardSelected)
                    }
                }
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
        Handler(Looper.getMainLooper()).postDelayed({ animateWinner() }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 0L else 550)
        Handler(Looper.getMainLooper()).postDelayed({ // start after 1.5 seconds
            if (roundNumber < roundNumberLimit) {
                endGameRound()
                startNextRound()
            } else if (roundNumber == roundNumberLimit) {
                try {
                    gameTurn.removeObserver(roundListener)
                } finally {
                    clearAllAnimation()
                }
                endGameRound() // update points of last round to server by winner
            }
        }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 0L else 1500)
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

    private fun startNextRound() {
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

    private fun createCardsArray() {
        selfCardsArrayList.clear()
        for (x: Int in cardsInHand) {
            val card = PlayingCardDescription(cardInt = x, cardDrawable = cardsDrawable[x], points = cardsPoints.elementAt(x), expandCard = x == cardsInHand.last())
            selfCardsArrayList.add(card)
        }
    }

    private fun displaySelfCards() { // Only called when Its self turn to play cards
        val cardsPresentCheck = cardsSuit.slice(cardsInHand as Iterable<Int>).indexOf(trumpStart) != -1
        if (cardsPresentCheck) {
            for (i in 0 until selfCardsArrayList.size) {
                selfCardsArrayList[i].filter = gameTurn.value!! > 1 && cardsSuit[selfCardsArrayList[i].cardInt] != trumpStart
                selfCardsArrayList[i].expandCard = if (i == selfCardsArrayList.size - 1) true else !selfCardsArrayList[i].filter // Expand only last card or not filtered cards
                val notify = if (i == selfCardsArrayList.size - 1) selfCardsArrayList[i].filter else selfCardsArrayList[i].filter || selfCardsArrayList[i].expandCard
                if (notify) adapterSelfCards.notifyItemChanged(i)
            }
        }
    }

    private fun clearAllAnimation() {
        for (i in 0 until nPlayers) { // first reset background and animation
            //            findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
            //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE
        }
        binding.selfCards.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
        binding.selfCards.clearAnimation()
    }

    private fun animateWinner() {
        if (soundStatus) SoundManager.instance?.playCardCollectSound()
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.imageViewWinnerCenter4).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableWinnerAnim[roundWinner - 1]))
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.GONE
        }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 50L else 1000L)
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
        } else if (!partnerCardSelected) {
            partnerCardSelected = true
            textToSpeech.stop()
            bu1 = cardSelected
            bu1Flag = 1 // bidder has one of card in his hand
            if (vibrateStatus) vibrationStart()
            binding.buddyImage1.setImageResource(cardsDrawablePartner[bu1])
            moveView(viewToMove = binding.buddyImage1, toX = buddyImage1X, toY = buddyImage1Y , fromView = findViewById(refIDMappedImageView[bidder - 1]))
            gameState.value = 5  // change game state to next playing round
            if (bidder == fromInt) { // at this point bidder is fixed and more reliable than player turn
                findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                Handler(Looper.getMainLooper()).postDelayed({
                    findViewById<ConstraintLayout>(R.id.linearLayoutPartnerSelection).visibility = View.GONE
                }, 180)
            }
        }
    }

    private fun displayTrumpCard() {
        when (trump) {
            "H" -> {
                binding.trumpImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.nh0))
                findViewById<TickerView>(R.id.trumpText).text = "Heart"
            }

            "S" -> {
                binding.trumpImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ns0))
                findViewById<TickerView>(R.id.trumpText).text = "Spade"

            }

            "D" -> {
                binding.trumpImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.nd0))
                findViewById<TickerView>(R.id.trumpText).text = "Diamond"

            }

            "C" -> {
                binding.trumpImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.nc0))
                findViewById<TickerView>(R.id.trumpText).text = "Club"

            }
        }
        moveView(viewToMove = binding.trumpImage, toX = trumpImageX, toY = trumpImageY , fromView =  findViewById(refIDMappedImageView[bidder - 1]))
    } // just displaying trump card

    private fun startTrumpSelection() {
        if (bidder != fromInt) {     //  show to everyone except bidder
            autoTrumpSelect()
            speak("${playerName(bidder)} won bid. Waiting to choose trump", speed = 1.10f)
            centralText(cancel = true)
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
                moveView(viewToMove = binding.bidCoin, toX = bidCoinX, toY = bidCoinY , fromView = findViewById(refIDMappedImageView[bidder - 1]))
            }
            binding.textViewBidValue.text = "$bidValue"  //show current bid value
            findViewById<TextView>(R.id.textViewBider).text = getString(R.string.Bider) + playerName(bidder)
            findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE //biding frame invisible
            resetBackgroundAnimationBidding() //set all background to black or red depending on status
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).visibility = View.VISIBLE
            findViewById<ImageView>(refIDMappedPartnerIconImageView[bidder - 1]).setImageResource(R.drawable.biddericon)
            animatePlayer(it)  // animate current player
            val tView: ImageView = findViewById(refIDMappedImageView[it - 1])
            binding.bidNowImage.animate().x(tView.x).y(tView.y).duration = 450
            if (bidStatus[it - 1] == 1) {  // highlight current player
                findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[it - 1]).visibility = View.VISIBLE
            }
            if (it == fromInt && (bidder != it || !bidingStarted)) {
                if (bidStatus[it - 1] == 1) { // show bid frame and ask to bid or pass
                    bidDone = false
                    binding.selfCards.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
                    findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is critical
                    findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
                    countDownTimer("Bidding", purpose = "start")
                    if (vibrateStatus) vibrationStart()
                } else if (bidStatus[it - 1] == 0) {
                    playerTurn.value = nextBidderTurn(it)
                }
            }
            if (bidder == it && bidingStarted) { // finish bid and move to next game state  // dummy playerTurn.value!! == fromInt &&
                playerTurn.removeObserver(bidTurnListener)
                gameState.value = 3 //// change game state to 3 as biding is finished
            } else if (it != 1) {
                autoBid()
            }
        }
        playerTurn.observe(this, bidTurnListener)
    }

    private fun moveView(viewToMove: View, fromView: View? = null, toX: Float = 0F, toY: Float = 0F, fromX: Float = 0F, fromY: Float = 0F, duration: Long = moveViewDuration) {
        if (fromView != null) {
            viewToMove.x = fromView.x
            viewToMove.y = fromView.y
        } else {
            viewToMove.x = fromX
            viewToMove.y = fromY
        }
        viewToMove.animate().x(toX).y(toY).setDuration(duration).interpolator = interpolator
    }

    fun nextBidderTurn(currentTurn: Int): Int {
        var nBT = currentTurn
        var found = false
        for (i in 1 until nPlayers) {
            nBT = nextTurn(nBT)
            if (bidStatus[nBT - 1] == 1) {
                found = true
                break
            }
        }
        return if (found) nBT else bidder
    }

    private fun autoBid() {
        val tempView = View(applicationContext)
        if (bidStatus[playerTurn.value!! - 1] == 1) {
            if (maxAutoBidLimit.random() > bidValue) tempView.tag = listOf("5", "10", "20", "10", "pass").random() //check if bid value has not reached maximum allowed bid value
            else tempView.tag = "pass"
            countDownBidding.cancel() // dummy
            Handler(Looper.getMainLooper()).postDelayed({
                bidDone = false
                askToBid(tempView)
            }, timeAutoBid.random())
        } else {
            playerTurn.value = nextBidderTurn(playerTurn.value!!)
        }
    }

    fun askToBid(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (this::countDownBidding.isInitialized) countDownBidding.cancel()
        countDownTimer("Bidding", purpose = "cancel")
        if (!bidDone) {
            bidDone = true
            if (soundStatus) SoundManager.instance?.playUpdateSound() // soundUpdate.start()
            when (view.tag) {
                "pass" -> {
                    bidStatus[playerTurn.value!! - 1] = 0
                    speak("${playerName(playerTurn.value!!)} passed", speed = 1.15f)
                    if (playerTurn.value!! == fromInt) centralText("You passed \nYou cannot bid anymore", 2500)
                }

                "5" -> {
                    bidValue = min(bidValue + 5, maxBidValue)
                    bidStatus[playerTurn.value!! - 1] = 1
                    bidder = playerTurn.value!!
                }

                "10" -> {
                    bidValue = min(bidValue + 10, maxBidValue)
                    bidStatus[playerTurn.value!! - 1] = 1
                    bidder = playerTurn.value!!
                }

                "20" -> {
                    bidValue = min(bidValue + 20, maxBidValue)
                    bidStatus[playerTurn.value!! - 1] = 1
                    bidder = playerTurn.value!!
                }

                "50" -> {
                    bidValue = min(bidValue + 50, maxBidValue)
                    bidStatus[playerTurn.value!! - 1] = 1
                    bidder = playerTurn.value!!
                }

                "75" -> {
                    bidValue = min(bidValue + 75, maxBidValue)
                    bidStatus[playerTurn.value!! - 1] = 1
                    bidder = playerTurn.value!!
                }
            }
            if (playerTurn.value!! == fromInt) {
                findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
                Handler(Looper.getMainLooper()).postDelayed({
                    findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
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
                    if (BuildConfig.DEBUG) toastCenter("Missing Language data - Text to speech")
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
                if (iPlayer == fromInt) binding.selfCards.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer2))
            } else {
                findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))

                if (iPlayer == fromInt) binding.selfCards.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
            }
            if (iPlayer == fromInt) binding.selfCards.clearAnimation()
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
        binding.selfCards.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.transparent))
        binding.selfCards.clearAnimation()
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
            }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 1000L else displayTime.toLong())
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun updatePlayerInfo() {
        playerInfo = intent.getStringArrayListExtra("playerInfo") as ArrayList<String>
        val playerInfoCoins = intent.getIntegerArrayListExtra("playerInfoCoins") as ArrayList<Int>

        p1 = playerInfo[0]
        p2 = playerInfo[1]
        p3 = playerInfo[2]
        p4 = playerInfo[3]
        p1Coins = playerInfoCoins[0]
        p2Coins = playerInfoCoins[1]
        p3Coins = playerInfoCoins[2]
        p4Coins = playerInfoCoins[3]

        updatePlayerNames()
        adapterScoreHeader = ScoreHeaderAdapter(scoreHeaderArrayList)
        binding.gridScoreHeader.adapter = adapterScoreHeader
        updateScoreTableHeader(showRank = false)
        for (i in 0 until nPlayers) {
            val j = i + nPlayers
            if (playerInfo[j].isNotEmpty()) {
                Picasso.get().load(playerInfo[j]).resize(300, 300).centerCrop().transform(CircleTransform()).error(R.drawable.user_photo).into(findViewById<ImageView>(refIDMappedImageView[i]))
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

    private fun shufflingWindow(time: Long = 4900, fadeOffTime: Long = 600, gameStateChange: Boolean = false) {
        shuffleOver = false
        if (soundStatus) Handler(Looper.getMainLooper()).postDelayed({
            SoundManager.instance?.playShuffleSound()
        }, 400) //delayed sound play of shuffling
        displayShufflingCards() //show suits cards and animate
        speak("Shuffling cards Please wait", speed = 1f)
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).animation = null
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).clearAnimation()
            findViewById<ImageView>(R.id.imageViewWinnerCenter4).visibility = View.GONE
            binding.relativeLayoutTableCards.visibility = View.GONE
            binding.selfCards.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_out))
            Handler(Looper.getMainLooper()).postDelayed({
                shuffleOver = true
                binding.selfCards.adapter = adapterSelfCards
                binding.selfCards.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
                Handler(Looper.getMainLooper()).postDelayed({ if (activityExists) startBidding() }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 200L else if (BuildConfig.DEBUG) 500L else 1500L)
            }, fadeOffTime)
        }, if (BuildConfig.DEBUG && resources.getBoolean(R.bool.enable_super_fast_test_offline) && resources.getBoolean(R.bool.enable_auto_mode_game_screen_offline)) 1000L else time)
    }

    private fun displayShufflingCards(view: View = View(this), sets: Int = 5, distribute: Boolean = true) {
        if (distribute) shufflingDistribute()
        binding.selfCards.adapter = adapterShuffleCards
        binding.selfCards.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right))
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
        snackBar.show()
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
        return if (current < 4) current + 1 else 1
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
                Log.d("Inter", "onInitializationComplete ${it.toString()}")
//                binding.addViewScoreBanner.adUnitId = if (BuildConfig.DEBUG) getString(R.string.banner_admob_test)
//                else getString(R.string.banner_admob)
                binding.addViewScoreBanner.visibility = View.VISIBLE
                binding.addViewScoreBanner.loadAd(AdRequest.Builder().build())  //load ad to banner view Admob
                loadInterstitialAd()
            }
        } else {
            binding.addViewScoreBanner.visibility = View.GONE
        }
    }

    private fun loadInterstitialAd() {
        loadInterAdTry += 1 // try 5 times
        InterstitialAd.load(this, getString(R.string.inter_admob), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
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
                    Log.d("InterstitialAd", "onAdFailedToShowFullScreenContent - ${p0.message}")
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
            bodyTextView.dialogueBody.text = getString(R.string.leave_room_confirm1)
            builder.setView(bodyTextView.root)

            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
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
        refUsersData.document(uid).set(hashMapOf("LPD_bot" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()), SetOptions.merge())
        startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) }.apply { putExtra("returnFromGameScreen", true) }.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
        finishAndRemoveTask()
    }

    override fun onStop() {
        super.onStop()
        countDownTimer("PlayCard", purpose = "cancel")
        countDownTimer("Bidding", purpose = "cancel")
    }

    override fun onPause() {
        super.onPause()
        binding.addViewScoreBanner.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.addViewScoreBanner.resume()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if (!scoreWindowOpen.value!!) {
            this.moveTaskToBack(true)
        } else {
            scoreWindowOpen.value = false
        }
    }

    override fun onDestroy() {
        try {
            mInterstitialAd!!.fullScreenContentCallback = null
            mInterstitialAd = null
        } catch (_: java.lang.Exception) {
        }
        binding.addViewScoreBanner.destroy()
        try {
            if (this::textToSpeech.isInitialized) {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
        } catch (_: java.lang.Exception) {
        }
        binding.addViewScoreBanner.destroy()
        super.onDestroy()

    }
}


