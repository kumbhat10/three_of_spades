@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "PLUGIN_WARNING", "ImplicitThis", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mopub.common.MoPub
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import com.robinhood.ticker.TickerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_join_room_screen.*
import kotlinx.android.synthetic.main.activity_game_screen.*
import kotlinx.android.synthetic.main.cards_item_list_partner.view.*
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.math.round
import kotlin.properties.Delegates
import kotlin.random.Random

@SuppressLint("SetTextI18n")
class GameScreen : AppCompatActivity() { //    region Initialization

	private lateinit var textToSpeech: TextToSpeech
	private var closeRoom: Boolean = false
	private var typedValue = TypedValue()
	private var rated = false

	private var shuffleOver = false
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
	private var coinDur = 1000L
	private var coinSpeed = 5f
	private var coinRate = 70

	private var soundStatus = true
	private var vibrateStatus = true
	private lateinit var vibrator: Vibrator

	private var premiumStatus = false
	private var scoreOpenStatus = false
	private var chatOpenStatus = false
	private var activityExists = true
	private lateinit var mInterstitialAdMP: MoPubInterstitial
	private var loadInterAdTry = 0

	private lateinit var roomID: String
	private lateinit var selfName: String
	private lateinit var from: String
	private var fromInt = 0
	private var nPlayers = 0
	private var totalDailyCoins = 0
	private var nPlayers7 = false
	private var nPlayers4 = false
	private lateinit var textViewCenterPoint: TextView
	private lateinit var firebaseAnalytics: FirebaseAnalytics
	private lateinit var refRoomDatabase: DatabaseReference
	private lateinit var refGameDatabase: DatabaseReference
	private lateinit var refRoomFirestore: CollectionReference
	private var refUsersData = Firebase.firestore.collection("Users")
	private var uid = ""
	private lateinit var chatRegistration: ListenerRegistration
	private lateinit var chatAdapter: ChatRecyclerAdapter
	private var chatArray = ArrayList<String>()

	private lateinit var countDownBidding: CountDownTimer
	private lateinit var countDownPlayCard: CountDownTimer
	private lateinit var onlineStatusListener: ValueEventListener
	private lateinit var gameDataListener: ValueEventListener

	private var trump = MutableLiveData<String>()
	private var currentBidder = MutableLiveData<Int>()
	private var bidValue = MutableLiveData<Int>()
	private var partner1Card = MutableLiveData<Int>()
	private var partner1CardText = MutableLiveData<Int>()
	private var partner2Card = MutableLiveData<Int>()
	private var partner2CardText = MutableLiveData<Int>()

	private lateinit var playerInfo: ArrayList<String>
	private lateinit var playerInfoCoins: ArrayList<Int>
	private var nGamesPlayed = 0
	private var nGamesWon = 0
	private var nGamesBid = 0
	private var nGamesPlayedDaily = 0
	private var nGamesWonDaily = 0
	private var nGamesBidDaily = 0

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
	private lateinit var cardsInHand: MutableList<Long>

	private lateinit var gameData: GameData
	private var gameLimitNoAds: Int = 2

	private var counterChat = 0
	private lateinit var onlineStatus: MutableList<Int>
	private lateinit var allCardsReset: MutableList<Int>
	private var timeCountdownPlayCard = 15000L
	private var timeCountdownBid = 15000L
	private var delayGameOver = 4000L

	private var lastChat = ""
	private var scoreSheetNotUpdated = true

	private var played = false
	private var bidded = false
	private lateinit var allCards: MutableList<Int>
	private lateinit var ptAll: MutableList<Int>
	private var bidTeamScore = 0
	private lateinit var scoreList: List<Int>
	private var tablePoints = 0
	private var playerTurn: Int = 0
	private var nextValidBidder: Int = 0
	private var gameTurn = 0
//	private var bidValue: Int = 0
	private var maxBidValue: Int = 350
	private var bidingStarted = false   /// biding happened before
	private var gameState1 = false   /// biding happened before
	private var gameState4 = false   /// biding happened before
	private var roundStarted = false
	private var gameState6 = false
	private var counterPartnerSelection = 0
	private var pc1 = 0
	private var pc1s = 0
	private var pc2 = 0
	private var pc2s = 0
	private var p1s = 0
	private var p2s = 0
	private var roundWinner = 0
	private var roundNumber = 1
	private var newGameStatus = true

	// endregion
	@SuppressLint("ShowToast", "MissingPermission")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		CaocConfig.Builder.create()
			.backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
			.enabled(true) //default: true
			.showErrorDetails(true) //default: true
			.showRestartButton(true) //default: true
			.logErrorOnRestart(false) //default: true
			.trackActivities(false) //default: false
			.errorDrawable(R.drawable.bug_icon) //default: bug image
			.restartActivity(MainHomeScreen::class.java).apply()
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_game_screen)
		roomID = intent.getStringExtra("roomID")!!.toString()
		from = intent.getStringExtra("from")!!.toString()
		fromInt = from.split("")[2].toInt()
		selfName = intent.getStringExtra("selfName")!!.toString()
		totalDailyCoins = intent.getIntExtra("totalDailyCoins", 0)
		playerInfo = intent.getStringArrayListExtra("playerInfo") as ArrayList<String>
		val userStats = intent.getIntegerArrayListExtra("userStats")!!
		val userStatsDaily = intent.getIntegerArrayListExtra("userStatsDaily")!!
		nGamesPlayed = userStats[0]
		nGamesWon = userStats[1]
		nGamesBid = userStats[2]
		nGamesPlayedDaily = userStatsDaily[0]
		nGamesWonDaily = userStatsDaily[1]
		nGamesBidDaily = userStatsDaily[2]
		playerInfoCoins = intent.getStringArrayListExtra("playerInfoCoins") as ArrayList<Int>
		nPlayers = intent.getIntExtra("nPlayers", 0)
		nPlayers7 = nPlayers == 7
		nPlayers4 = nPlayers == 4
		setupGame4or7()

		refRoomDatabase = Firebase.database.getReference("GameData/$roomID")
		refGameDatabase = Firebase.database.getReference("GameData/$roomID/G")
		refRoomFirestore = Firebase.firestore.collection(getString(R.string.pathRoom))
		//region Other Thread - player info update
		uid = FirebaseAuth.getInstance().uid.toString()
		FirebaseCrashlytics.getInstance().setUserId(uid)
		Handler(Looper.getMainLooper()).post {
			vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
			updatePlayerInfo()
			initializeSpeechEngine()
			getSharedPrefs()
			firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
			logFirebaseEvent(key = "start$nPlayers")
			refUsersData.document(uid)
				.set(hashMapOf("LPD" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
					.toInt()), SetOptions.merge())
			inflateEmoji()  // for chat screen
			// region Chat Setup
			val layoutManager = LinearLayoutManager(this)
			layoutManager.stackFromEnd = true
			chatRecyclerView.layoutManager = layoutManager
			chatAdapter = ChatRecyclerAdapter(chatArray)
			chatRecyclerView.adapter = chatAdapter		// endregion
		} // endregion
		// region       Countdown PlayCard
		countDownPlayCard = object : CountDownTimer(timeCountdownPlayCard, 50) {
			@SuppressLint("SetTextI18n")
			override fun onTick(millisUntilFinished: Long) {
				progressbarTimer.progress = (millisUntilFinished * 10000 / timeCountdownPlayCard).toInt()   //10000 because max progress is 10000
				textViewTimer.text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString()
			}
			override fun onFinish() {
				autoPlayCard()
				if (soundStatus) SoundManager.instance?.playTimerSound()
				if (vibrateStatus) vibrationStart()
				progressbarTimer.progress = 0
				findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
				progressbarTimer.visibility = View.GONE
				textViewTimer.visibility = View.GONE
				progressbarTimer.clearAnimation()
				textViewTimer.clearAnimation()
			}
		} //        endregion
		// region       Countdown Biding
		countDownBidding = object : CountDownTimer(timeCountdownBid, 50) {
			@SuppressLint("SetTextI18n")
			override fun onTick(millisUntilFinished: Long) { //                    findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
				progressbarTimer.progress = (millisUntilFinished * 10000 / timeCountdownBid).toInt()
				textViewTimer.text = round((millisUntilFinished / 1000).toDouble() + 1).toInt().toString()
			}
			override fun onFinish() {
				if (!bidded) {
					bidded = true
					if (vibrateStatus) vibrationStart()
					if (soundStatus) SoundManager.instance?.playTimerSound()
					gameData.bs?.set(fromInt-1, 0)
					writeChild(data = mutableMapOf("pt" to nextValidBidder, "bs" to gameData.bs!!, "bvo" to gameData.bv!!))
					progressbarTimer.progress = 0
					findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
					progressbarTimer.visibility = View.GONE
					textViewTimer.visibility = View.GONE
					progressbarTimer.clearAnimation()
					textViewTimer.clearAnimation()
					findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
					Handler(Looper.getMainLooper()).postDelayed({
						findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
						findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
					}, 180)
					centralText("    Time's Up !!  \nYou cannot bid anymore", 2500)
					speak("Time's Up ${playerName(fromInt)}. You can't bid now", speed = 1.05f)
				}
			}
		} //        endregion
		//region Online Status Listener
		onlineStatusListener = object : ValueEventListener {
			override fun onCancelled(p0: DatabaseError) {}
			override fun onDataChange(data: DataSnapshot) {
				if(data.value!= null && activityExists){
					for(iIndex in 0 until nPlayers){
						updateOnlineStatus(iIndex, newValue = data.child("p${iIndex+1}").value.toString().toInt())
					}
				}
			}
		}
		//endregion
		trump.observe(this, {
			when(it){
				"H" -> {
					findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_hearts))
					findViewById<TickerView>(R.id.trumpText).text = "Heart"
				}
				"S" -> {
					findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_spades))
					findViewById<TickerView>(R.id.trumpText).text = "Spade"
				}
				"D" -> {
					findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_diamonds))
					findViewById<TickerView>(R.id.trumpText).text = "Diamond"
				}
				"C" -> {
					findViewById<ImageView>(R.id.trumpImage).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_clubs))
					findViewById<TickerView>(R.id.trumpText).text = "Club"
				}
				else->{
					findViewById<ImageView>(R.id.trumpImage).setImageResource(R.drawable.trump)
					findViewById<TickerView>(R.id.trumpText).text = getString(R.string.Trump)
				}
			}
		})
		currentBidder.value = 0
		currentBidder.observe(this, {
			if(it==0) textViewBider.text = getString(R.string.Bider)
			else textViewBider.text = "Bid: " + playerName(currentBidder.value!!)
		})
		bidValue.value = 0
		bidValue.observe(this, {
			textViewBidValue.text = if(it == 0) getString(R.string.bidValue1) else it.toString()
		})
		partner1Card.value = cardsIndexLimit
		partner1Card.observe(this,{
			if(it==cardsIndexLimit){
				buddyImage1.setImageResource(R.drawable.ic_back_side_blue)
			}else{
				buddyImage1.setImageResource(cardsDrawablePartner[it])
			}
		})
		partner1CardText.value = 13
		partner1CardText.observe(this, {
			when (it) {
				12 -> buddyText1.text = getString(R.string.bothPartner)
				11 -> buddyText1.text = getString(R.string.onlyPartner)
				10 -> buddyText1.text = getString(R.string.anyPartner)
				in 1..nPlayers -> buddyText1.text = playerName(it)
				else -> buddyText1.text = if(nPlayers4) getString(R.string.partner) else getString(R.string.partner1)
			}
		})

		if(nPlayers7){
			partner2Card.value = cardsIndexLimit
			partner2Card.observe(this,{
				if(it==cardsIndexLimit){
					buddyImage2.setImageResource(R.drawable.ic_back_side_blue)
				}else{
					buddyImage2.setImageResource(cardsDrawablePartner[it])
				}
			})

			partner2CardText.value = 13
			partner2CardText.observe(this, {
				when (it) {
					12 -> buddyText2.text = getString(R.string.bothPartner)
					11 -> buddyText2.text = getString(R.string.onlyPartner)
					10 -> buddyText2.text = getString(R.string.anyPartner)
					in 1..nPlayers -> buddyText2.text = playerName(it)
					else -> buddyText2.text = getString(R.string.partner1)
				}
			})
		}

		//region Game Data Listener
		gameDataListener = object : ValueEventListener {
			override fun onCancelled(p0: DatabaseError) {}
			override fun onDataChange(data: DataSnapshot) {
				if (data.value != null && activityExists) {
					gameData = data.getValue<GameData>()!!
					//region Table Cards & Table Points - Independent of everything else
					if (gameData.gs == 5) {
						showTableCard()
					} else showTableCard(resetCards = true)
					tablePointsCalculator()
					//endregion
					trump.value = gameData.tr
					currentBidder.value = gameData.bb!!
					bidValue.value = gameData.bv!!
					partner1Card.value = gameData.pc1!!
					partner1CardText.value = gameData.pc1s
					if (nPlayers7) {
						partner2CardText.value = gameData.pc2s
						partner2Card.value = gameData.pc2!!
					}
					when (gameData.gs) {
						1->gameState1()
						3->gameState3()
						4->gameState4()
						5->gameState5()
						6->gameState6()
					}
				}
			}
		}
		//endregion
		applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
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
	}

	private fun gameState1(){
		findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
		getCardsAndDisplay(display = false)
		if (!gameState1) {
			gameState1 = true
			resetVariables()
			findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
			findViewById<ConstraintLayout>(R.id.scoreViewLayout).visibility = View.GONE
			updatePlayerNames()
			shufflingWindow(gameStateChange = true)
		} else if (activityExists) startBidding()
	}
	private fun resetVariables() {
		if (gameData.gn > 1) changeBackground()
		currentBidder.value = 0
		bidNowImage.visibility = View.GONE
		for (i in 0 until nPlayers) { // first reset background and animation of all partner icon
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
		}
		bidValue.value = 0
		partner1Card.value = cardsIndexLimit
		partner1CardText.value = 13
		pc1 = 0
		pc1s = 0
		gameData.p1 = 8
		p1s = 0
		bidTeamScore = 0
		nextValidBidder = 0
		// bidingStarted = false   /// biding happened before check if it is required here
		counterPartnerSelection = 0
		gameTurn = 0
		played = false
		playerTurn = 0
		roundStarted = false
		gameState6 = false
		roundNumber = 1
		roundWinner = 0
		tablePoints = 0
		allCards = allCardsReset.toMutableList()
		if (nPlayers4) {
			ptAll = mutableListOf(0, 0, 0, 0)
			buddyText1.text = getString(R.string.partner)
		} else {
			ptAll = mutableListOf(0, 0, 0, 0, 0, 0, 0)
			partner2CardText.value = 13
			partner2Card.value = cardsIndexLimit
			pc2 = 0
			pc2s = 0
			gameData.p2 = 8
			p2s = 0
		}
		showTableCard(resetCards = true)
	}
	@SuppressLint("SetTextI18n")
	private fun updatePlayerNames() {
		val totalCoins = if (nPlayers7) listOf(p1Coins, p2Coins, p3Coins, p4Coins, p5Coins, p6Coins, p7Coins)
		else listOf(p1Coins, p2Coins, p3Coins, p4Coins)
		for (i in 0 until nPlayers) {
			findViewById<TickerView>(refIDMappedTextView[i]).text = playerName(i + 1) // + " $${String.format("%,d", totalCoins[i])}"
			findViewById<TickerView>(refIDMappedTextViewA[i]).text = "$${String.format("%,d", totalCoins[i])}"
		}
	}
	private fun startBidding() {
		//			if (playerTurn != gameData.pt) {
		val bidSpeak = gameData.bvo!! < gameData.bv!!
		val prevPlayerTurn = playerTurn
		playerTurn = gameData.pt!!
		nextValidBidder = nextBidderTurn(gameData.pt!!, gameData.bs!!)

		if (!bidingStarted) {
			textViewBidValue.text = gameData.bv!!.toString()
			bidNowImage.visibility = View.VISIBLE
			centralText("${playerName(gameData.pt!!)} will start bidding", 0) //display message only first time
			if (gameData.pt!! != fromInt) speak("${playerName(gameData.pt!!)} will start bidding", speed = 1.1f)
			else speak("${playerName(gameData.pt!!)} You will start bidding", speed = 1.1f)
		} else {
			centralText("Waiting for ${playerName(gameData.pt!!)} to bid", 0) //display message always
		}

		if (bidSpeak && bidingStarted && soundStatus) {
			speak("${playerName(gameData.bb!!)} bid ${gameData.bv!!}", speed = 1f)
			moveView(bidCoin, findViewById(refIDMappedImageView[gameData.bb!! - 1]))
		} else if (!bidSpeak && bidingStarted && soundStatus && prevPlayerTurn != gameData.pt!!) speak("${playerName(prevPlayerTurn)} passed", speed = 1f) //                        else if (soundStatus) SoundManager.instance?.playUpdateSound() //

		findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE //biding frame invisible
		resetBackgroundAnimationBidding() //set pass label on photo if passed
		if (gameData.bb!! > 0) {
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).visibility = View.VISIBLE
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).setImageResource(R.drawable.biddericon)
		}
		if (gameData.pt!! > 0) {
			val tView: ImageView = findViewById(refIDMappedImageView[gameData.pt!! - 1])
			bidNowImage.animate().x(tView.x).y(tView.y).duration = 450
		}
		animatePlayer(gameData.pt!!)  // animate current player
		if (gameData.bs?.get(fromInt-1) == 1 && gameData.pt!! > 0) {  // highlight current player
			findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[gameData.pt!! - 1]).visibility = View.VISIBLE
		}
		if (gameData.pt!! == fromInt && (gameData.bb!! != gameData.pt!! || !bidingStarted)) {
			if (gameData.bs?.get(fromInt-1) == 1) { // show bid frame and ask to bid or pass
				findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
				findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.VISIBLE // this path is critical
				findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
				bidded = false
				countDownTimer("Bidding", purpose = "start")
				if (vibrateStatus) vibrationStart()
			} else if (gameData.bs?.get(fromInt-1) == 0) {
				bidded = true
				write("G/pt", nextTurn(fromInt))
			}
		}
		if (gameData.pt!! == fromInt && gameData.bb!! == gameData.pt!! && bidingStarted) { // finish bid and move to next game state
			writeChild(data = mutableMapOf("gs" to 3, "pt" to gameData.bb!!))
			centralText("Well done! ${playerName(gameData.bb!!)} \n You won the bid round", 0)
		}
		//			}
		bidingStarted = true
	}
	fun askToBid(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (!bidded) {
			bidded = true
			countDownTimer("Bidding", purpose = "cancel")
			if (soundStatus) SoundManager.instance?.playUpdateSound() //
			when (view.tag) {
				"pass" -> {
					gameData.bs?.set(fromInt-1, 0)
					writeChild(data = mutableMapOf("bs" to gameData.bs!!, "pt" to nextValidBidder))
				}
				"5"  -> writeChild(data = mutableMapOf("bv" to  min(gameData.bv!! + 5 , maxBidValue),"bvo" to gameData.bv!!, "bb" to fromInt, "pt" to nextValidBidder))
				"10" -> writeChild(data = mutableMapOf("bv" to  min(gameData.bv!! + 10, maxBidValue),"bvo" to gameData.bv!!, "bb" to fromInt, "pt" to nextValidBidder))
				"20" -> writeChild(data = mutableMapOf("bv" to  min(gameData.bv!! + 20, maxBidValue),"bvo" to gameData.bv!!, "bb" to fromInt, "pt" to nextValidBidder))
				"50" -> writeChild(data = mutableMapOf("bv" to  min(gameData.bv!! + 50, maxBidValue),"bvo" to gameData.bv!!, "bb" to fromInt, "pt" to nextValidBidder))
				"75" -> writeChild(data = mutableMapOf("bv" to  min(gameData.bv!! + 75, maxBidValue),"bvo" to gameData.bv!!, "bb" to fromInt, "pt" to nextValidBidder))
			}
			findViewById<ConstraintLayout>(R.id.frameAskBid).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
			Handler(Looper.getMainLooper()).postDelayed({
				findViewById<ConstraintLayout>(R.id.frameAskBid).visibility = View.GONE
				findViewById<ConstraintLayout>(R.id.frameAskBid).clearAnimation()
			}, 180)
		}
	}
	private fun moveView(viewToMove: View, fromView: View, duration: Long = 350) {
		val xViewToMove = viewToMove.x
		val yViewToMove = viewToMove.y
		viewToMove.x = fromView.x
		viewToMove.y = fromView.y
		viewToMove.animate().x(xViewToMove).y(yViewToMove).duration = duration
	}

	private fun gameState3(){
		gameState1 = false
		if (soundStatus) SoundManager.instance?.playSuccessSound()
		bidingStarted = false
		finishBackgroundAnimationBidding() // also highlight bidder winner & removed automatically at game state 5
		startTrumpSelection()
	}
	private fun startTrumpSelection() {
		if (gameData.bb!! != fromInt && gameData.bb!! != 0) {     //  show to everyone except bidder
			toastCenter("${playerName(gameData.bb!!)} won the bid round")
			findViewById<ConstraintLayout>(R.id.frameTrumpSelection).visibility = View.GONE
			speak("${playerName(gameData.bb!!)} won bid. Waiting to choose trump")
			centralText("Waiting for ${playerName(gameData.bb!!)} \n to choose Trump", 0)
		} else { // show to gameData.bb!! only
			bidNowImage.visibility = View.GONE // redundant not required really
			if (gameData.bb!! != 0) centralText("Well done! ${playerName(gameData.bb!!)} \n You won the bid round", 0)
			speak("Well done!! Choose trump now", speed = 1f, queue = TextToSpeech.QUEUE_ADD)
			findViewById<ConstraintLayout>(R.id.frameTrumpSelection).visibility = View.VISIBLE
			findViewById<ConstraintLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
			if (vibrateStatus) vibrationStart()
		}
	}
	fun onTrumpSelectionClick(view: View) {
		if (soundStatus) SoundManager.instance?.playUpdateSound() //
		when (view.tag) {
			"h" -> writeChild(data = mutableMapOf("tr" to "H", "gs" to 4)) //write("G/Tr", "H")
			"s" -> writeChild(data = mutableMapOf("tr" to "S", "gs" to 4)) //write("G/Tr", "S")
			"d" -> writeChild(data = mutableMapOf("tr" to "D", "gs" to 4)) //write("G/Tr", "D")
			"c" -> writeChild(data = mutableMapOf("tr" to "C", "gs" to 4)) //write("G/Tr", "C")
		}
		findViewById<ConstraintLayout>(R.id.frameTrumpSelection).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
		Handler(Looper.getMainLooper()).postDelayed({
			findViewById<ConstraintLayout>(R.id.frameTrumpSelection).visibility = View.GONE
			findViewById<ConstraintLayout>(R.id.frameTrumpSelection).clearAnimation()
		}, 180)
	}

	private fun gameState4() {
		gameState1 = false
		if (!gameState4) {
			gameState4 = true
			if (soundStatus) SoundManager.instance?.playSuccessSound()
			if (gameData.bb!! != 0) moveView(trumpImage, findViewById(refIDMappedImageView[gameData.bb!! - 1]))
			startPartnerSelection()
		}
	}
	private fun startPartnerSelection() {
		if (gameData.bb!! == fromInt) {  // only to bidder
			linearLayoutPartnerSelection.visibility = View.VISIBLE // make selection frame visible
			linearLayoutPartnerSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_center))
			if (nPlayers7) {
				findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1)
				speak("Choose your partner card", speed = 1.1f)
			} //choose 1st buddy text
			if (nPlayers4) {
				findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection1_4)
				speak("Choose your partner card", speed = 1.1f)
			}
			partnerSelectRV.layoutManager = LinearLayoutManager(this@GameScreen, RecyclerView.HORIZONTAL, false)
			partnerSelectRV.adapter = PartnerCardListAdapter(nPlayers = nPlayers) { output ->
				if (nPlayers7) partnerSelectClick7(cardSelected = output)
				else if (nPlayers4) partnerSelectClick4(cardSelected = output)
			}
		} else {
			speak("Waiting for ${playerName(gameData.bb!!)} to choose partner card", speed = 1.1f)
			if (nPlayers7) {
				if (gameData.bb!! != 0) centralText("Waiting for ${playerName(gameData.bb!!)} \nto choose partners card", 0)
			} else {
				if (gameData.bb!! != 0) centralText("Waiting for ${playerName(gameData.bb!!)} \nto choose partner card", 0)
			}
		}
	}
	private fun partnerSelectClick4(cardSelected: Int) { // assumption is cardsInHand already updated
		if ((cardsInHand as List<*>).contains((cardSelected).toLong())) {
			if (soundStatus) SoundManager.instance?.playErrorSound() //
			if (vibrateStatus) vibrationStart()
			toastCenter("$selfName, You have this card")
			speak("You have this card. Choose any other card", speed = 1.1f)
		} else {
			writeChild(data = mutableMapOf("pc1" to cardSelected, "pc1s" to 1, "gs" to 5))
			linearLayoutPartnerSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
			Handler(Looper.getMainLooper()).postDelayed({
				linearLayoutPartnerSelection.visibility = View.GONE
				linearLayoutPartnerSelection.clearAnimation()
			}, 180)
		}
	}
	private fun partnerSelectClick7(cardSelected: Int) { // assumption is cardsInHand already updated
		if (counterPartnerSelection == 0) {
			when {
				(cardsInHand as List<*>).contains((cardSelected * 2).toLong()) and (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong()) -> {
					if (soundStatus) SoundManager.instance?.playErrorSound()
					if (vibrateStatus) vibrationStart()
					toastCenter("$selfName, You already have both of selected card")
					speak("You already have both card")
				}
				(cardsInHand as List<*>).contains((cardSelected * 2).toLong()) or (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong()) -> {
					if (soundStatus) SoundManager.instance?.playUpdateSound()
					writeChild(data = mutableMapOf("pc1" to cardSelected, "pc1s" to 11)) // only card
					pc1 = cardSelected
					pc1s = 1
					buddyImage1.setImageResource(cardsDrawablePartner[cardSelected])
					findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2) //choose 2nd buddy
					speak("Choose second partner card")
					counterPartnerSelection = 1
				}
				else -> {
					if (soundStatus) SoundManager.instance?.playUpdateSound()
					pc1 = cardSelected
					writeChild(data = mutableMapOf("pc1" to cardSelected, "pc1s" to 10)) // Any card
					pc1s = 0
					buddyImage1.setImageResource(cardsDrawablePartner[cardSelected])
					findViewById<TextView>(R.id.textViewPartnerSelect).text = getString(R.string.partnerSelection2) //choose 2nd buddy
					speak("Choose second partner card")
					counterPartnerSelection = 1
				}
			}
		} else if (counterPartnerSelection == 1) {
			if ((cardsInHand as List<*>).contains((cardSelected * 2).toLong()) and (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong())) {
				if (soundStatus) SoundManager.instance?.playErrorSound() //
				if (vibrateStatus) vibrationStart()
				toastCenter("$selfName, You already have both of same cards")
				speak("You have both. Choose any other card")
			} else if ((cardsInHand as List<*>).contains((cardSelected * 2).toLong()) or (cardsInHand as List<*>).contains((cardSelected * 2 + 1).toLong())) {
				if (pc1 != cardSelected) {
					writeChild(data = mutableMapOf("pc2" to cardSelected, "pc2s" to 11, "gs" to 5))  // bider has one of card in his hand
					counterPartnerSelection = 0
					linearLayoutPartnerSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
					Handler(Looper.getMainLooper()).postDelayed({
						linearLayoutPartnerSelection.clearAnimation()
						linearLayoutPartnerSelection.visibility = View.GONE
					}, 180)
				} else {
					if (soundStatus) SoundManager.instance?.playErrorSound()
					if (vibrateStatus) vibrationStart()
//					toastCenter("You already have and chosen same card")
					speak("this card is already selected. Choose other card")
				}
			} else {
				if (pc1 == cardSelected) writeChild(data = mutableMapOf("pc2" to cardSelected, "pc2s" to 12, "pc1s" to 12, "gs" to 5))  // bider has none in his hands and both same selected
				 else writeChild(data = mutableMapOf("pc2" to cardSelected, "pc2s" to 10, "gs" to 5)) // bider has none in his hands and is different than 1st
				counterPartnerSelection = 0
				linearLayoutPartnerSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_center))
				Handler(Looper.getMainLooper()).postDelayed({
					linearLayoutPartnerSelection.clearAnimation()
					linearLayoutPartnerSelection.visibility = View.GONE
				}, 180)			}
		}
	}

	private fun gameState5(){
		gameState1 = false
		gameState4 = false
		if (!roundStarted) {
			newGameStatus = true
			roundStarted = true
			if (soundStatus) SoundManager.instance?.playSuccessSound()
			getBuddyAndDisplay()
			finishPassOverlay()
			if (gameData.bb!! > 0) updatePlayerScoreInfo()
			getCardsAndDisplay(animation = false)
			findViewById<ConstraintLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
			Handler(Looper.getMainLooper()).postDelayed({
				startPlayingRound()
				speak("Lets Start!", queue = TextToSpeech.QUEUE_ADD)}, 2000)
			if (playerTurn != fromInt) {
				centralText("${playerName(gameData.pt!!)} will play first \n You get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
				speak("${playerName(gameData.bb!!)} will play first \n You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1.1f)
			} else{
				centralText("You will have ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card")
				speak("You will get ${(timeCountdownPlayCard / 1000).toInt()} seconds to play card", speed = 1.1f)
			}
		} else {
			startPlayingRound()
		}
	}
	private fun getBuddyAndDisplay() {
		pc1 = gameData.pc1!!
		pc1s = gameData.pc1s

		if (vibrateStatus) vibrationStart()
		if (gameData.bb!! != 0) moveView(buddyImage1, findViewById(refIDMappedImageView[gameData.bb!! - 1]))
		if (nPlayers7) {
			pc2 = gameData.pc2!!
			pc2s = gameData.pc2s
			if (gameData.bb!! != 0) moveView(buddyImage2, findViewById(refIDMappedImageView[gameData.bb!! - 1]))
		}
	}
	private fun startPlayingRound() {
//		findViewById<ConstraintLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
		//region Partner Check
		if (p1s != gameData.p1s && p1s != 1) {
			p1s = gameData.p1s
			displayPartnerIcon()
			if (soundStatus) SoundManager.instance?.playSuccessSound()
			if (vibrateStatus) vibrationStart()
			if (gameData.p1s == 1) {
				speak("${playerName(gameData.p1)}.  is partner now", speed = 1.05f)
			} else speak("New partner found", speed = 1.05f)
		}
		if (gameData.p1 != 0 && gameData.p1s == 1) partner1CardText.value = gameData.p1

		if(nPlayers7) {
			if (p2s != gameData.p2s && p2s != 1) {
				p2s = gameData.p2s
				displayPartnerIcon()
				if (soundStatus) SoundManager.instance?.playSuccessSound()
				if (vibrateStatus) vibrationStart()
				if (gameData.p2s == 1) {
					speak("${playerName(gameData.p2)} .  is partner now", speed = 1.1f)
				} else speak("New partner found")
			}
			if (gameData.p2 != 0 && gameData.p2s == 1) partner2CardText.value = gameData.p2
		}// endregion

		if (roundNumber < gameData.rn || gameData.ct?.get(fromInt-1) != cardsIndexLimit || gameData.pt!! != fromInt) { // check for new round or not
			roundNumber = gameData.rn
			played = false
			if ((gameData.rn > 8 || BuildConfig.DEBUG) && !premiumStatus) loadInterstitialAd() // load the ad again
		}
		ptAll = gameData.sc!!
		updatePlayerScoreInfo()
		play()
	}

	private fun play(){
		if (gameTurn != gameData.rt){ // if the game turn changes then only proceed
			gameTurn = gameData.rt  // check later
			clearAllAnimation()
			if (gameTurn == nPlayers+1) {
				Handler(Looper.getMainLooper()).postDelayed({ declareRoundWinner() }, 500)
			} else if (gameTurn != 8 && gameTurn != 0) {
				animatePlayer(gameData.pt!!)
				if (gameData.pt!! == fromInt && !played) {
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
	private fun autoPlayCard() {
		if (!played) {
			if (gameTurn == 1) {  //can play any random card in 1st chance
				val cardSelected = cardsInHand.random()
				startNextTurn(cardSelected)
			} else { // play only same suit card if not 1st chance
				var cardSelectedIndex = cardsSuit.slice(cardsInHand as Iterable<Int>)
						.lastIndexOf(gameData.rtr) // play largest card first
				if (cardSelectedIndex == -1) { //not found same suit card
					cardSelectedIndex = cardsSuit.slice(cardsInHand as Iterable<Int>)
							.lastIndexOf(gameData.tr) // play trump card
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
		if (!played && gameData.gs == 5 && gameData.pt == fromInt && !(gameTurn == 8 && nPlayers7) && !(gameTurn == 5 && nPlayers4) && gameTurn != 0) { // dummy error chance - why just 8 and not 5 also ? Investigate later
			val cardSelected = view.tag.toString().toInt()
			if (gameTurn == 1 || cardsSuit[cardSelected] == gameData.rtr || cardsSuit.slice(cardsInHand as Iterable<Int>)
							.indexOf(gameData.rtr) == -1) {
				countDownTimer("PlayCard", purpose = "cancel")
				startNextTurn(cardSelected.toLong()) // allow throw if first chance, or same suit as first turn or doesn't have same suit card
			} else {
				if (soundStatus) SoundManager.instance?.playErrorSound()
				if (vibrateStatus) vibrationStart()
				speak("Play ${getSuitName(gameData.rtr)} card")
			}
		}
	}
	private fun startNextTurn(cardSelected: Any) {  // always called whenever played any card
		if (!played) {
			played = true
			cardsInHand.remove(cardSelected)
			if (gameData.rn != roundNumberLimit) {
				write("CH/$from", cardsInHand)
				getCardsAndDisplay()
			}
			else {
				write("CH/$from", "")
				findViewById<LinearLayout>(R.id.imageGallery).removeAllViews() // show no self cards after throwing last card
			}

			if (playerTurn != gameData.bb!!) {
				if (nPlayers7) checkIfPartnerAndUpdateServer7(cardSelected, playerTurn)
				else checkIfPartnerAndUpdateServer4(cardSelected, playerTurn)
			}

			gameData.ct?.set(fromInt-1, cardSelected.toString().toInt())

			writeChild(data = mutableMapOf(
				"p1"  to gameData.p1,  "p1s" to gameData.p1s, "pc1s" to gameData.pc1s,
				"p2"  to gameData.p2,  "p2s" to gameData.p2s, "pc2s" to gameData.pc2s,
				"ct"  to gameData.ct!!, "rt" to gameTurn + 1,   "pt" to nextTurn(fromInt),
				"rtr" to if (gameTurn == 1) cardsSuit[cardSelected.toString().toInt()] else gameData.rtr))

			write("OL/$from", 1) // dummy - Turn them online again - check if really required
		}
	}
	private fun startNextRound() {
		gameData.sc!![fromInt-1] = tablePoints + gameData.sc!![fromInt-1]
		writeChild(data = mutableMapOf("ct" to allCardsReset, "sc" to gameData.sc!!,
			"rt" to 1, "pt" to fromInt, "rn" to gameData.rn+1, "rtr" to ""))
	}
	private fun checkIfPartnerAndUpdateServer4(cardSelected: Any, playerTurn: Int?) {
		if (cardSelected.toString().toInt() == pc1 && p1s != 1) {
			if (playerTurn != null) {
				gameData.p1 = playerTurn
				gameData.p1s = 1
			}
		}
	}
	private fun checkIfPartnerAndUpdateServer7(cardSelected: Any, playerTurn: Int?) {
		if ((cardSelected.toString().toInt() == pc1 * 2 || cardSelected.toString().toInt() == pc1 * 2 + 1) && p1s != 1) {
			if (playerTurn != null) {
				gameData.p1 = playerTurn
			}
			if (gameData.pc1s >= 11) { // 11 or 12 --> bidder has either one card(only) or has asked both cards(both) --> lock the partner
				gameData.p1s = 1
				gameData.pc1s = playerTurn!!
			} else if (pc1s == 10) { // any card asked
				if (gameData.p1s == 2 || gameData.rt == nPlayers) {// locking the partner by 1
					gameData.p1s = 1
					gameData.pc1s = playerTurn!!
				}
				if (gameData.p1s == 0) {// 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
					gameData.p1s = 2
				}
			}
		} else if ((cardSelected.toString().toInt() == pc2 * 2 || cardSelected.toString()
						.toInt() == pc2 * 2 + 1) && p2s != 1) {
			if (playerTurn != null) { // null surround check
				gameData.p2 = playerTurn
			}
			if (gameData.pc2s >= 11) { // 11 or 12 --> bidder has either one card or has asked both cards --> lock the partner
				gameData.p2s = 1
				gameData.pc2s = playerTurn!!
			} else if (gameData.pc2s == 10) {//any card asked
				if (gameData.p2s == 2 || gameData.rt == nPlayers) { // locking the partner by 1 if previously was 2
					gameData.p2s = 1
					gameData.pc2s = playerTurn!!
				}
				if (gameData.p2s == 0) {// 1st partner disclosed as previously was 0 .. 0 --> 2 --> 1
					gameData.p2s = 2
				}
			}
		}
	}

	private fun gameState6() {
		if (!gameState6) {
			gameState6 = true
			showTableCard(resetCards = true)
			if (fromInt == 1) logFirebaseEvent(key = "played$nPlayers")
			findViewById<ConstraintLayout>(R.id.relativeLayoutTableCards).visibility = View.GONE
			countDownTimer("PlayCard", purpose = "cancel")
			if (vibrateStatus) vibrationStart()
			if (soundStatus) SoundManager.instance?.playShuffleSound() //
			displayShufflingCards(distribute = false)
			scoreOpenStatus = true
			if (!mInterstitialAdMP.isReady && !premiumStatus) loadInterstitialAd()
			refRoomDatabase.child("S").addListenerForSingleValueEvent(object : ValueEventListener {
				override fun onCancelled(p0: DatabaseError) {}
				override fun onDataChange(p0: DataSnapshot) {
					if (newGameStatus) {
						newGameStatus = false
						scoreList = p0.value as List<Int>
						updateWholeScoreBoard()
					}
				}
			})
			Handler(Looper.getMainLooper()).postDelayed({
				if (!premiumStatus && mInterstitialAdMP.isReady && (gameData.gn % gameLimitNoAds == 0)) mInterstitialAdMP.show()
				if (fromInt == 1) { // only to host
					//				findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
					findViewById<AppCompatTextView>(R.id.startNextRoundButton).visibility = View.VISIBLE
					findViewById<AppCompatTextView>(R.id.startNextRoundButton).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
				}
			}, delayGameOver)
		}
	}

	private fun logFirebaseEvent(event: String = "game_screen", int: Int = 1, key: String) {
		val params = Bundle()
		params.putInt(key, int)
		firebaseAnalytics.logEvent(event, params)
	}

	private fun changeBackground() {
		when (Random.nextInt(0, 6)) {
			0 -> gameBkgd.setImageResource(R.drawable.poker)
			1 -> gameBkgd.setImageResource(R.drawable.pokerblue)
			2 -> gameBkgd.setImageResource(R.drawable.pokercyan)
			3 -> gameBkgd.setImageResource(R.drawable.pokerred)
			4 -> gameBkgd.setImageResource(R.drawable.pokerred1)
			5 -> gameBkgd.setImageResource(R.drawable.poker)
		}
	}

	private fun setupGame4or7() {
		refIDMappedTextView = PlayersReference().refIDMappedTextView(from, nPlayers)
		refIDMappedTextViewA = PlayersReference().refIDMappedTextViewA(from, nPlayers)
		refIDMappedImageView = PlayersReference().refIDMappedImageView(from, nPlayers)
		refIDMappedHighlightView = PlayersReference().refIDMappedHighlightView(from, nPlayers)
		refIDMappedPartnerIconImageView = PlayersReference().refIDMappedPartnerIconImageView(from, nPlayers)
		refIDMappedOnlineIconImageView = PlayersReference().refIDMappedOnlineIconImageView(from, nPlayers)
		refIDMappedTableAnim = PlayersReference().refIDMappedTableAnim(from, nPlayers)
		refIDMappedTableWinnerAnim = PlayersReference().refIDMappedTableWinnerAnim(from, nPlayers)
		refIDMappedTableImageView = PlayersReference().refIDMappedTableImageView(from, nPlayers)
		findViewById<GifImageView>(R.id.imageViewChat).visibility = View.VISIBLE
		textViewCenterPoint = if (nPlayers4) findViewById(R.id.textViewCenterPoints_4)
		else findViewById(R.id.textViewCenterPoints)
		if (nPlayers7) {
			maxBidValue = 700
			findViewById<TickerView>(R.id.textView1).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView1a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep1).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView1).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView2).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView2a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep2).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView2).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView3).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView3a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep3).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView3).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView4a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep4).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView4).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView5).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView5a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep5).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView5).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView6).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView6a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep6).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView6).visibility = View.VISIBLE

			findViewById<TickerView>(R.id.textView7).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView7a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView7).visibility = View.VISIBLE

			buddyText2.visibility = View.VISIBLE
			findViewById<ImageView>(R.id.buddyImage2).visibility = View.VISIBLE

			bidNowImage.layoutParams.width = resources.getDimensionPixelSize(R.dimen.imageWidth)
			bidNowImage.layoutParams.height = resources.getDimensionPixelSize(R.dimen.imageHeight)
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
			onlineStatus = mutableListOf(0, 0, 0, 0, 0, 0, 0)
			allCardsReset = mutableListOf(cardsIndexLimit, cardsIndexLimit, cardsIndexLimit, cardsIndexLimit, cardsIndexLimit, cardsIndexLimit, cardsIndexLimit)
		}
		if (nPlayers4) {
			allCardsReset = mutableListOf(cardsIndexLimit, cardsIndexLimit, cardsIndexLimit, cardsIndexLimit)
			onlineStatus = mutableListOf(0, 0, 0, 0)
			maxBidValue = 350
			refIDValesTextViewScore = PlayersReference().refIDTextViewScoreSheet4
			cardsDrawable = PlayingCards().cardsDrawable4
			cardsPoints = PlayingCards().cardsPoints4
			cardsSuit = PlayingCards().cardSuit4
			cardsIndexSortedPartner = PlayingCards().cardsIndexSortedPartner4
			cardsDrawablePartner = PlayingCards().cardsDrawable4
			cardsPointsPartner = PlayingCards().cardsPoints4
			cardsIndexLimit = 53
			roundNumberLimit = 13
			scoreLimit = 355 //			scoreList = listOf(0,0,0,0,0)
			//			ptAll = listOf(0,0,0,0)
			findViewById<TickerView>(R.id.textView1_4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView1_4a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep1_4).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView1_4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView2_4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView2_4a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep2_4).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView2_4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView3_4).visibility = View.VISIBLE
			findViewById<TickerView>(R.id.textView3_4a).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.onlinep3_4).visibility = View.VISIBLE
			findViewById<ImageView>(R.id.playerView3_4).visibility = View.VISIBLE
		}
		allCards = allCardsReset.toMutableList()
	}

	private fun updateOnlineStatus(index: Int, newValue: Int) {
		if (onlineStatus[index] != newValue) {
			onlineStatus[index] = newValue
			if (onlineStatus[index] == 0 && fromInt != index + 1) {
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).setImageResource(R.drawable.status_offline)
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale))
			}
			if (onlineStatus[index] == 1 && fromInt != index + 1) {
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).setImageResource(R.drawable.status_online)
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale))
			}
			if (onlineStatus[index] == 2 && fromInt != index + 1) {
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).setImageResource(R.drawable.status_offline)
				findViewById<ImageView>(refIDMappedOnlineIconImageView[index]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale))
				speak("${playerName(index + 1)} has left !", speed = 1.1f)
				toastCenter("${playerName(index + 1)} has left the room !")
				if (index == 0) { //if only host leaves the room
					activityExists = false
					countDownBidding.cancel()
					countDownPlayCard.cancel()
					val view = View(applicationContext)
					view.tag = "notClicked"
					Handler(Looper.getMainLooper()).postDelayed({ closeGameRoom(view) }, 2500)
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		if (fromInt != 1) checkRoomExists() // check for all except host
		else write("OL/$from", 1) // Turn only host online
		getCardsAndDisplay(display = false)

		chatRegistration = refRoomFirestore.document(roomID + "_chat").addSnapshotListener { dataSnapshot, error ->
				if (dataSnapshot != null && dataSnapshot.exists() && error == null) {
					val data = (dataSnapshot.data as Map<String, String>)["M"].toString()
					if (data.isNotEmpty() && lastChat != data) { // if chat is not empty
						if (soundStatus) SoundManager.instance?.playChatSound()
						chatArray.add(data)
						chatAdapter.notifyItemInserted(chatArray.size - 1)
						chatRecyclerView.scrollToPosition(chatArray.size - 1)
						lastChat = data
						if (chatLinearLayout.visibility != View.VISIBLE) {
							(imageViewChat.drawable as GifDrawable).start()
							counterChat += 1 // increase counter by 1 is chat display is off
							textViewChatNo.visibility = View.VISIBLE
							textViewChatNo.text = "$counterChat New ${Emoji().message}"
						}
					}
				} else if (error != null) {
					toastCenter(error.localizedMessage!!.toString()) // dummy
				}
			}
		if (activityExists) {
			refRoomDatabase.child("G").removeEventListener(gameDataListener)
			refRoomDatabase.child("G").addValueEventListener(gameDataListener)
			refRoomDatabase.child("OL").addValueEventListener(onlineStatusListener)
		}
	}

	private fun checkRoomExists() {
		refRoomDatabase.child("OL/p1").  // display the host info in joining room screen
		addListenerForSingleValueEvent(object : ValueEventListener {
			override fun onCancelled(errorDataLoad: DatabaseError) {}
			override fun onDataChange(data: DataSnapshot) {
				if (data.exists() && data.value.toString().toInt() != 2) {
					write("OL/$from", 1)
				} else {
					activityExists = false
					toastCenter("${playerName(1)} has left the game")
					speak("${playerName(1)} has left the game", speed = 1.05f)
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

	private fun updateWholeScoreBoard() {
		p1Gain += scoreList[1]
		p2Gain += scoreList[2]
		p3Gain += scoreList[3]
		p4Gain += scoreList[4]

		totalDailyCoins += scoreList[fromInt]
		p1Coins += scoreList[1]
		p2Coins += scoreList[2]
		p3Coins += scoreList[3]
		p4Coins += scoreList[4]

		if (nPlayers7) {
			p5Gain += scoreList[5]
			p6Gain += scoreList[6]
			p7Gain += scoreList[7]

			p5Coins += scoreList[5]
			p6Coins += scoreList[6]
			p7Coins += scoreList[7]
		}
		scoreBoardTable(display = false, data = createScoreTableHeader(), upDateHeader = true)
		scoreBoardTable(display = false, data = createScoreTableTotal(), upDateTotal = true)
		scoreBoardTable(data = scoreList)

		if (gameData.bb!! == fromInt) {
			nGamesBid += 1
			nGamesBidDaily += 1
		}
		if (scoreList[fromInt] > 0) {
			if (soundStatus) {
				SoundManager.instance?.playDholSound()
				SoundManager.instance?.playWonSound()
			}
			speak("Congratulations! Your team won", speed = 1.1f)
			createKonfetti(applicationContext, konfettiGSA, duration = coinDur, konType = KonType.Win, burst = false, speed = coinSpeed, ratePerSec = coinRate)
			nGamesWon += 1
			nGamesWonDaily += 1
		} else {
			if (soundStatus) SoundManager.instance?.playLostSound()
			speak("Sorry! Your team lost", speed = 1.1f)
			createKonfetti(applicationContext, konfettiGSA, duration = coinDur, konType = KonType.Lost, burst = false, speed = coinSpeed, ratePerSec = coinRate)
		}

		nGamesPlayed += 1
		nGamesPlayedDaily += 1
		refUsersData.document(uid)
			.set(hashMapOf("sc" to playerCoins(from), "scd" to totalDailyCoins, "w" to nGamesWon, "w_daily" to nGamesWonDaily, "b" to nGamesBid, "b_daily" to nGamesBidDaily, "p" to nGamesPlayed, "p_daily" to nGamesPlayedDaily), SetOptions.merge())
	}

	private fun createScoreTableHeader(): List<String> {
		return if (nPlayers == 7) listOf("Player\n${Emoji().money}", p1 + "\n${Emoji().money}${String.format("%,d", p1Coins)}", p2 + "\n${Emoji().money}${String.format("%,d", p2Coins)}", p3 + "\n${Emoji().money}${String.format("%,d", p3Coins)}", p4 + "\n${Emoji().money}${String.format("%,d", p4Coins)}", p5 + "\n${Emoji().money}${String.format("%,d", p5Coins)}", p6 + "\n${Emoji().money}${String.format("%,d", p6Coins)}", p7 + "\n${Emoji().money}${String.format("%,d", p7Coins)}")
		else listOf("Player\n${Emoji().money}", p1 + "\n${Emoji().money}${String.format("%,d", p1Coins)}", p2 + "\n${Emoji().money}${String.format("%,d", p2Coins)}", p3 + "\n${Emoji().money}${String.format("%,d", p3Coins)}", p4 + "\n${Emoji().money}${String.format("%,d", p4Coins)}")
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
			findViewById<ConstraintLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
			findViewById<ConstraintLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
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
			if (i > 0 && !upDateHeader && data[i].toString()
					.toInt() < 0) { //                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
				viewTemp.findViewById<TextView>(refIDValesTextViewScore[i])
					.setTextColor(ContextCompat.getColor(applicationContext, R.color.Red))
			} else if (i > 0 && !upDateHeader) { //                viewTemp.findViewById<TextView>(refIDValesTextViewScore[i]).setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD)
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
			else -> {
				viewTemp.layoutParams.height = resources.getDimensionPixelSize(R.dimen._22sdp)
				viewTemp.background = ContextCompat.getDrawable(this, R.drawable.blackrectangle)
				findViewById<LinearLayout>(R.id.imageGalleryScore).addView(viewTemp)
			}
		}
		if (display) scrollViewScore.post {
			scrollViewScore.fullScroll(View.FOCUS_DOWN)
		}
	}

	fun openCloseScoreSheet(view: View) {
		if (findViewById<ScrollView>(R.id.scrollViewScore).visibility == View.VISIBLE) {
			findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE

			findViewById<ConstraintLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_scoretable_close))
			Handler(Looper.getMainLooper()).postDelayed({
				findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
				findViewById<ConstraintLayout>(R.id.scoreViewLayout).visibility = View.GONE
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
			findViewById<ConstraintLayout>(R.id.scoreViewLayout).visibility = View.VISIBLE
			findViewById<ConstraintLayout>(R.id.scoreViewLayout).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_scoretable_open))
			scrollViewScore.post {
				scrollViewScore.fullScroll(View.FOCUS_DOWN)
			}
		}
	}

	fun closeChatScoreWindow(view: View) {
		chatLinearLayout.visibility = View.GONE
		chatOpenStatus = false
		scoreOpenStatus = false
		findViewById<ScrollView>(R.id.scrollViewScore).visibility = View.GONE
		findViewById<ConstraintLayout>(R.id.scoreViewLayout).visibility = View.GONE
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

	fun startNextGame(view: View) { // dummy - try to parameterize and write all at once - or read from CreateGameData class
		findViewById<AppCompatTextView>(R.id.startNextRoundButton).clearAnimation()
		findViewById<AppCompatTextView>(R.id.startNextRoundButton).visibility = View.GONE

		refRoomDatabase.child("G").setValue(
			if (nPlayers4) gameData4(dummy = BuildConfig.DEBUG, gameNumber = gameData.gn + 1)
			else gameData7(dummy = BuildConfig.DEBUG, gameNumber = gameData.gn + 1)
		)
			.addOnFailureListener {
				toastCenter("Failed to start new game. Please Try again")
				speak("Failed to start new game. Please try again")
				findViewById<AppCompatTextView>(R.id.startNextRoundButton).visibility = View.VISIBLE
			}.addOnSuccessListener {
				findViewById<AppCompatTextView>(R.id.startNextRoundButton).visibility = View.GONE
			}
	}

	private fun tablePointsCalculator() {
		tablePoints = 0
		for (iCard in allCards) {
			tablePoints += cardsPoints[iCard]
		}
		if (tablePoints > 0 && gameData.gs == 5) {
			textViewCenterPoint.text = tablePoints.toString()
			textViewCenterPoint.visibility = View.VISIBLE
		} else {
			textViewCenterPoint.visibility = View.GONE
		}
	}

	private fun updatePlayerScoreInfo() {
		if (gameData.gs == 5) {
			val t1 = if (gameData.p1s != 0) gameData.sc!![gameData.p1 - 1] else 0
			val t2 = if (nPlayers7 && p2s != 0 && gameData.p1 != gameData.p2) gameData.sc!![gameData.p2 - 1] else 0 // if not same partners then only add other points

			if (gameData.bb!! > 0) bidTeamScore = gameData.sc!![gameData.bb!! - 1] + t1 + t2

			for (i in 0 until nPlayers) {
				val j = i + 1
				if (j == gameData.bb!! || (j == gameData.p1 && gameData.p1s != 0) || (j == gameData.p2 && gameData.p2s != 0)) {
					findViewById<TickerView>(refIDMappedTextView[i]).text = playerName(j) // + " $bidTeamScore /$bidValue"
					findViewById<TickerView>(refIDMappedTextViewA[i]).text = "$bidTeamScore /${gameData.bv!!}"
				} else {
					findViewById<TickerView>(refIDMappedTextView[i]).text = playerName(j)
					findViewById<TickerView>(refIDMappedTextViewA[i]).text = "${gameData.sc!!.sum() - bidTeamScore} /${scoreLimit - gameData.bv!!}" //                    findViewById<TickerView>(refIDMappedTextView[i]).text = playerName(j) + "\n$emojiScore  ${pointsList.sum() - bidTeamScore} /${scoreLimit - bidValue}"
				}
			}
			val tt1 = if (gameData.p1s == 1) gameData.sc!![gameData.p1 - 1] else 0
			val tt2 = if (nPlayers7 && gameData.p2s == 1 && gameData.p1 != gameData.p2) gameData.sc!![gameData.p2 - 1] else 0 // if not same partners then only add other player points

			val bidTeamScoreFinal = if (gameData.bb!! > 0) gameData.sc!![gameData.bb!! - 1] + tt1 + tt2 else tt1 + tt2 // total score of bid team
			if (nPlayers4) decideGameWinnerTeam4(bidTeamScoreFinal, totalGamePoints = gameData.sc!!.sum())
			else decideGameWinnerTeam7(bidTeamScoreFinal, totalGamePoints = gameData.sc!!.sum())
		}
	}

	private fun decideGameWinnerTeam7(bidTeamScoreFinal: Int, totalGamePoints: Int) {
		if (bidTeamScoreFinal >= gameData.bv!!) { // bidder team won case
			clearAllAnimation()
			if (vibrateStatus) vibrationStart()
			centralText("Game Over: ${playerName(gameData.bb!!-1)}'s team Won")
			speak("Game Over   bidder team won")
			if (fromInt == gameData.bb!!) { // bidder will change game state to 6
				val pointsListTemp = mutableListOf(gameData.gn, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!)
				if (p1s != 1 && p2s != 1) { //No partners found so far
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 6
				} else if (p1s == 1 && p2s != 1) { // only partner 1 found
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 3
					pointsListTemp[gameData.p1] = gameData.bv!! * 2
				} else if (p1s != 1 && p2s == 1) { // only partner 2 found
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 3
					pointsListTemp[gameData.p2] = gameData.bv!! * 2
				} else if (gameData.p1 == gameData.p2 && gameData.p1 == 1) { //both partners found and they are same person
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 3
					pointsListTemp[gameData.p1] = gameData.bv!! * 2
				} else if (gameData.p1 != gameData.p2 && p1s == 1 && p2s == 1) { //both partners found and they are different person
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 2
					pointsListTemp[gameData.p1] = gameData.bv!!
					pointsListTemp[gameData.p2] = gameData.bv!!
				}
				write("S", pointsListTemp) // 0-bidder won, 1 - defenders won??
				writeChild(data = mutableMapOf("gs" to 6))
			}
		} else if (p1s == 1 && p2s == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - gameData.bv!!)) {
			// if opponent score has reached target value & both partners are disclosed
			clearAllAnimation()
			if (vibrateStatus) vibrationStart()
			centralText("Game Over: ${playerName(gameData.bb!!-1)}'s team Lost")
			speak("Game Over  Defender team won")
			if (fromInt == gameData.bb!!) { // winner will change game state to 6
				val pointsListTemp = mutableListOf(gameData.gn, gameData.bv!!, gameData.bv!!, gameData.bv!!, gameData.bv!!, gameData.bv!!, gameData.bv!!, gameData.bv!!)
				if (gameData.p1 == gameData.p2) { // either both partners are same person
					pointsListTemp[gameData.bb!!] = -1 * gameData.bv!! * 2
					pointsListTemp[gameData.p1] = -gameData.bv!!
				} else {                      // both partners are different person
					pointsListTemp[gameData.bb!!] = -1 * gameData.bv!! * 2
					pointsListTemp[gameData.p1] = -gameData.bv!!
					pointsListTemp[gameData.p2] = -gameData.bv!!
				}
				write("S", pointsListTemp) // 0-bidder won, 1 - defenders won
				writeChild(data = mutableMapOf("gs" to 6))
			}
		}
	}

	private fun decideGameWinnerTeam4(bidTeamScoreFinal: Int, totalGamePoints: Int) {
		if (bidTeamScoreFinal >= gameData.bv!!) { // bidder team won case
			clearAllAnimation()
			if (vibrateStatus) vibrationStart()
			centralText("Game Over: ${playerName(gameData.bb!!-1)}'s team Won")

			if (fromInt == gameData.bb!! && p1s != 1) speak("Well done!! You did it")
			 else if ((fromInt == gameData.bb!! || from == "p$gameData.p1") && p1s == 1) {
				speak("Well done!! Your team did it")
			} else speak("Sorry Your team lost")

			if (fromInt == gameData.bb!!) { // bidder will change game state to 6
				val pointsListTemp = mutableListOf(gameData.gn, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!, -gameData.bv!!)
				if (p1s != 1) { //No partners found so far
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 3 // bidder gets 3 times
				} else if (p1s == 1) { // partner 1 found
					pointsListTemp[gameData.bb!!] = gameData.bv!! * 2
					pointsListTemp[gameData.p1] = gameData.bv!!
				}
				write("S", pointsListTemp) // 0-bidder won, 1 - defenders won??
				writeChild(data = mutableMapOf("gs" to 6))
			}
		} else if (p1s == 1 && (totalGamePoints - bidTeamScore) >= (scoreLimit - gameData.bv!!)) { // if opponent score has reached target value & both partners are disclosed
			clearAllAnimation()
			if (vibrateStatus) vibrationStart() //            toastCenter("Game Over: Defender team Won \n         Bidder team Lost")
			centralText("Game Over: ${playerName(gameData.bb!!-1)}'s  team Lost")

			if (fromInt == gameData.bb!! || fromInt == gameData.p1) speak("Sorry Your team lost")
			 else speak("Well done!! Your team did it")

			if (gameData.bb!! == fromInt) { // bidder will change game state to 6
				val pointsListTemp = mutableListOf(gameData.gn, gameData.bv!!, gameData.bv!!, gameData.bv!!, gameData.bv!!)
				pointsListTemp[gameData.bb!!] = -1 * gameData.bv!! * 2
				pointsListTemp[gameData.p1] = -gameData.bv!!
				write("S", pointsListTemp) // 0-bidder won, 1 - defenders won
				writeChild(data = mutableMapOf("gs" to 6))
			}
		}
	}

	private fun displayPartnerIcon() {
		for (i in 0 until nPlayers) { // first reset background and animation of partner icon
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).clearAnimation()
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).setImageResource(R.drawable.partnericon)
		}
		if (gameData.bb!! != 0) { // show single person icon next to bidder
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).visibility = View.VISIBLE
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).setImageResource(R.drawable.biddericon)
		}

		if (p1s != 0 && gameData.p1 != 8) {
			if (vibrateStatus) vibrationStart()
			if (gameData.p1 > 0) {
				findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.p1 - 1]).visibility = View.VISIBLE
				findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.p1 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
			}
		}
		if (nPlayers7 && p2s != 0 && gameData.p2 != 8) {
			if (vibrateStatus) vibrationStart()
			if (gameData.p2 > 0) {
				findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.p2 - 1]).visibility = View.VISIBLE
				findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.p2 - 1]).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
			}
		}
	}

	private fun countDownTimer(task: String, purpose: String = "start") {
		if (purpose == "start") {
			findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
			progressbarTimer.progress = 100
			findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.GONE
			progressbarTimer.visibility = View.VISIBLE
			textViewTimer.visibility = View.VISIBLE
			textViewTimer.text = "10"
			if (task == "Bidding") {
				countDownBidding.cancel()
				countDownBidding.start()
			}
			if (task == "PlayCard") {
				countDownPlayCard.cancel()
				countDownPlayCard.start()
			}
		} else if (purpose == "cancel") {
			findViewById<ImageView>(R.id.closeGameRoomIcon).visibility = View.VISIBLE
			progressbarTimer.visibility = View.GONE
			textViewTimer.visibility = View.GONE
			progressbarTimer.clearAnimation()
			textViewTimer.clearAnimation()
			if (task == "Bidding") countDownBidding.cancel()
			if (task == "PlayCard") countDownPlayCard.cancel()
		}
	}

	private fun showTableCard(resetCards:Boolean = false) {
		for (index in 0 until nPlayers) {
			val card = gameData.ct!![index]
			val newCard = card != allCards[index]
			if (card <= (cardsIndexLimit - 2) && !resetCards) {
				if (soundStatus && newCard) SoundManager.instance?.playCardPlayedSound()
				findViewById<ImageView>(refIDMappedTableImageView[index]).visibility = View.VISIBLE
				if (newCard) findViewById<ImageView>(refIDMappedTableImageView[index]).startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableAnim[index]))
				findViewById<ImageView>(refIDMappedTableImageView[index]).setImageResource(cardsDrawable[card])
			} else {
				findViewById<ImageView>(refIDMappedTableImageView[index]).visibility = View.INVISIBLE
				findViewById<ImageView>(refIDMappedTableImageView[index]).clearAnimation()
			}
			allCards[index] = card
		}
	}

	private fun declareRoundWinner() {
		val roundCards = allCards
		var winnerCard = roundCards[playerTurn - 1]  // -1 due to index starts from 0 and playerTurn is absolute
		var currentCard: Int
		var startTurn = playerTurn
		for (i in 1 until nPlayers) {
			startTurn = nextTurn(startTurn)
			currentCard = roundCards[startTurn - 1]
			if (currentCard != 53 && currentCard != 99 && winnerCard != 53 && winnerCard != 99) winnerCard = compareCardsForWinner(currentCard, winnerCard) // dummy check whats causes to declare round winner earlier
		}
		roundWinner = roundCards.toIntArray().indexOf(winnerCard) + 1
		animatePlayer(roundWinner)
		findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1])
			.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_big_fast))
		Handler(Looper.getMainLooper()).postDelayed({ animateWinner() }, 550)
		Handler(Looper.getMainLooper()).postDelayed({ // start after 1.5 seconds
			if (gameData.rn < roundNumberLimit) {
				if (roundWinner == fromInt) { // only winner can start next round
					startNextRound()
				} else {
					centralText("Waiting for ${playerName(roundWinner)} to play next card", 1500)
				}
			} else if (gameData.rn == roundNumberLimit) {
				clearAllAnimation()
				if (roundWinner == fromInt) { // winner will change game state to 6
					endGameRound() // update points of last round to server by winner
				}
			}
		}, 1500)
	}
	private fun endGameRound() {
		gameData.sc!![fromInt-1] = tablePoints + gameData.sc!![fromInt - 1]
		refRoomDatabase.child("G")
			.setValue(mutableMapOf("p1" to 8, "p1s" to 0, "p2" to 8, "p2s" to 0, "ct" to allCardsReset,
				"sc" to gameData.sc!!, "rt" to 1, "pt" to 0, "rn" to 1, "tr" to "", "rtr" to ""))
			.addOnSuccessListener {
			}.addOnFailureListener {
				logFirebaseEvent(key = "Failed-Next-Turn")
				refRoomDatabase.child("G")
					.setValue(mutableMapOf("p1" to 8, "p1s" to 0, "p2" to 8, "p2s" to 0, "ct" to allCardsReset,
						"sc" to gameData.sc!!, "rt" to 1, "pt" to 0, "rn" to 1, "tr" to "", "rtr" to ""))
			}
	}

	private fun compareCardsForWinner(currentCard: Int, winnerCard: Int): Int {
		var w = winnerCard
		val wSuit = cardsSuit[winnerCard]
		val cSuit = cardsSuit[currentCard]
		if ((cSuit == gameData.tr && wSuit != gameData.tr) || ((cSuit == wSuit) && ((currentCard - winnerCard) >= 1 || (nPlayers7 && currentCard % 2 == 1 && winnerCard == currentCard - 1) // current card is odd & more than winner card
					|| (nPlayers7 && winnerCard % 2 == 1 && currentCard == winnerCard - 1) //winner card is odd & more than current card
					))) w = currentCard
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

	@SuppressLint("CutPasteId")
	private fun displaySelfCards(view: View = View(applicationContext), animations: Boolean = false, filter: Boolean = false, bidingRequest: Boolean = false) {
		findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
		findViewById<LinearLayout>(R.id.imageGallery).removeAllViews()
		val gallery = findViewById<LinearLayout>(R.id.imageGallery)
		gallery.visibility = View.VISIBLE
		val inflater = LayoutInflater.from(applicationContext)
		val typedValue = TypedValue()
		applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
		for (x: Long in cardsInHand) {
			val viewTemp = inflater.inflate(R.layout.cards_item_list, gallery, false)
			val imageViewDisplayCard = viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard)
			if (x == cardsInHand[cardsInHand.size - 1]) {
				imageViewDisplayCard.setPaddingRelative(0, 0, 0, 0)
				imageViewDisplayCard.layoutParams.width = resources.getDimensionPixelSize(R.dimen.widthDisplayCardLast)
			}
			imageViewDisplayCard.setImageResource(cardsDrawable[x.toInt()])
			if (filter && gameTurn > 1 && cardsSuit[x.toInt()] != gameData.rtr && cardsSuit.slice(cardsInHand as Iterable<Int>)
					.indexOf(gameData.rtr) != -1) {
				imageViewDisplayCard.foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
			} else if (filter && gameTurn > 1 && cardsSuit.slice(cardsInHand as Iterable<Int>)
					.indexOf(gameData.rtr) != -1) {
				imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite_active_cards))
				imageViewDisplayCard.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
			} else imageViewDisplayCard.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)

			imageViewDisplayCard.tag = x.toString() // tag the card number to the image
			if (cardsPoints.elementAt(x.toInt()) != 0) {
				viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).text = "${cardsPoints.elementAt(x.toInt())}"
				if (animations) viewTemp.findViewById<TextView>(R.id.textViewDisplayCard)
					.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink_and_scale)) //                if (animations) imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw_self_cards))
			} else {
				viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).visibility = View.GONE
			} //            imageViewDisplayCard.foreground = ContextCompat.getDrawable(applicationContext,typedValue.resourceId)
			imageViewDisplayCard.setOnClickListener {
				validateSelfPlayedCard(it)
				imageViewDisplayCard.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
			}
			gallery.addView(viewTemp)
		}
		if (animations) {
			findViewById<ConstraintLayout>(R.id.playerCards).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_in))
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
		if (soundStatus) SoundManager.instance?.playCardCollectSound() //        soundCollectCards.start()
		if (nPlayers7) {
			imageViewWinnerCenter.visibility = View.VISIBLE
			if (roundWinner > 0) imageViewWinnerCenter.startAnimation(AnimationUtils.loadAnimation(applicationContext, refIDMappedTableWinnerAnim[roundWinner - 1]))
			Handler(Looper.getMainLooper()).postDelayed({ imageViewWinnerCenter.visibility = View.GONE }, 1000)
		} else if (nPlayers4) {
			imageViewWinnerCenter_4.visibility = View.VISIBLE
			if (roundWinner > 0) imageViewWinnerCenter_4.startAnimation(AnimationUtils.loadAnimation(this, refIDMappedTableWinnerAnim[roundWinner - 1]))
			Handler(Looper.getMainLooper()).postDelayed({ imageViewWinnerCenter_4.visibility = View.GONE }, 1000)
		}
		findViewById<ImageView>(refIDMappedTableImageView[roundWinner - 1]).clearAnimation()
		for (i in 0 until nPlayers) {
			findViewById<ImageView>(refIDMappedTableImageView[i]).visibility = View.INVISIBLE
		}
	}
	
	private fun nextBidderTurn(currentTurn: Int, bidStatus: MutableList<Int>): Int {
		var nBT = currentTurn
		while (true) {
			nBT = nextTurn(nBT)
			if (bidStatus[nBT - 1] == 1) break
		}
		return nBT
	}

	private fun speak(speechText: String, pitch: Float = 0.95f, speed: Float = 1f, queue: Int = TextToSpeech.QUEUE_FLUSH, forceSpeak: Boolean = false) {
		if (soundStatus && this::textToSpeech.isInitialized && (forceSpeak || !closeRoom)) {
			textToSpeech.setPitch(pitch)
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
					speak("Shuffling cards Please wait", speed = 1.1f)
				}
			}
		}
	}

	private fun animatePlayer(index: Int) {
		if (index > 0) findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[index - 1]).visibility = View.VISIBLE
	}

	private fun resetBackgroundAnimationBidding() {
		for (i in 0 until nPlayers) {
			val iPlayer = i + 1
			findViewById<ImageView>(refIDMappedPartnerIconImageView[i]).visibility = View.GONE
			findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
			if (gameData.bs!![i] == 0) {
				findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(this, R.color.progressBarPlayer2))
				findViewById<ImageView>(refIDMappedImageView[i]).foreground = ContextCompat.getDrawable(this, R.drawable.pass)
				if ("p$iPlayer" == from) findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(this, R.color.progressBarPlayer2))
			} else {
				findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(this, R.color.layoutBackground))
				if ("p$iPlayer" == from) findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(this, R.color.layoutBackground))
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
			findViewById<ImageView>(refIDMappedImageView[i]).setBackgroundColor(ContextCompat.getColor(this, R.color.layoutBackground))
			findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).visibility = View.GONE //            findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[i]).clearAnimation()
		}
		findViewById<LinearLayout>(R.id.imageGallery).setBackgroundColor(ContextCompat.getColor(this, R.color.layoutBackground))
		findViewById<LinearLayout>(R.id.imageGallery).clearAnimation() //        findViewById<ImageView>(refIDMappedImageView[bidder -1]).setBackgroundColor(ContextCompat.getColor(this, R.color.progressBarPlayer4)) // highlight bidder winner
		if (gameData.bb!! > 0) findViewById<ShimmerFrameLayout>(refIDMappedHighlightView[gameData.bb!! - 1]).visibility = View.VISIBLE

		//        textViewBidValue.clearAnimation()
		//        findViewById<TextView>(R.id.textViewBidder).clearAnimation()
		if (gameData.bb!! > 0) {
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).visibility = View.VISIBLE
			findViewById<ImageView>(refIDMappedPartnerIconImageView[gameData.bb!! - 1]).setImageResource(R.drawable.biddericon)
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
			val j = i + nPlayers // playerInfo has first nPlayers elements as name and later nPlayers elements as profile pic URL so offsetting with nPlayers
			if (playerInfo[j].isNotEmpty() && i != fromInt - 1) {
				Picasso.get().load(playerInfo[j]).resize(300, 300).centerCrop().error(R.drawable.s3)
					.into(findViewById<ImageView>(refIDMappedImageView[i]))
			}
		}
		findViewById<ImageView>(refIDMappedImageView[fromInt - 1]).visibility = View.INVISIBLE
	}

	private fun shufflingWindow(time: Long = 4900, fadeOffTime: Long = 500, gameStateChange: Boolean = false) {
		shuffleOver = false
		if (soundStatus) Handler(Looper.getMainLooper()).postDelayed({
			SoundManager.instance?.playShuffleSound()
		}, 400) //delayed sound play of shuffling
		displayShufflingCards() //show suits cards and animate
		centralText(getString(R.string.shufflingcards), 5200)
		speak("Shuffling cards Please wait", speed = 1.1f)
		Handler(Looper.getMainLooper()).postDelayed({
			if (nPlayers4) {
				imageViewWinnerCenter_4.animation = null
				imageViewWinnerCenter_4.clearAnimation()
				imageViewWinnerCenter_4.visibility = View.GONE
			} else if (nPlayers7) {
				imageViewWinnerCenter.animation = null
				imageViewWinnerCenter.clearAnimation()
				imageViewWinnerCenter.visibility = View.GONE
			}
			findViewById<ConstraintLayout>(R.id.relativeLayoutTableCards).visibility = View.GONE
			findViewById<LinearLayout>(R.id.imageGallery).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down_out))
			Handler(Looper.getMainLooper()).postDelayed({
				shuffleOver = true
				displaySelfCards(animations = true, bidingRequest = true)
			}, fadeOffTime)
		}, time)
	}

	private fun displayShufflingCards(view: View = View(this), sets: Int = 5, distribute: Boolean = true) {
		findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
		if (distribute) shufflingDistribute()
		val gallery = findViewById<LinearLayout>(R.id.imageGallery)
		gallery.removeAllViews()
		val inflater = LayoutInflater.from(this)
		for (xx: Int in 0 until sets) {
			for (x: Int in 0..3) {
				val viewTemp = inflater.inflate(R.layout.cards_item_list_suits, gallery, false)
				viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
					.setImageResource(PlayingCards().suitsDrawable[x])
				viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
					.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_ccw))
				if (x % 2 != 0) {
					viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
						.setBackgroundColor(ContextCompat.getColor(this, R.color.cardsBackgroundDark))
				} else {
					viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard1)
						.setBackgroundColor(ContextCompat.getColor(this, R.color.cardsBackgroundLight))
				}
				gallery.addView(viewTemp)
			}
		}
		findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.layoutBackground))
		gallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_right))
	}

	private fun shufflingDistribute() {
		findViewById<ConstraintLayout>(R.id.relativeLayoutTableCards).visibility = View.VISIBLE
		if (nPlayers7) {
			imageViewWinnerCenter.visibility = View.VISIBLE
			val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_shuffle_7)
			anim.setAnimationListener(object : Animation.AnimationListener {
				override fun onAnimationRepeat(animation: Animation?) {}
				override fun onAnimationEnd(animation: Animation?) {
					imageViewWinnerCenter.startAnimation(anim)
				}

				override fun onAnimationStart(animation: Animation?) {}
			})
			imageViewWinnerCenter.startAnimation(anim)
		} else if (nPlayers4) {
			imageViewWinnerCenter_4.visibility = View.VISIBLE
			val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_shuffle_4)
			anim.setAnimationListener(object : Animation.AnimationListener {
				override fun onAnimationRepeat(animation: Animation?) {}
				override fun onAnimationEnd(animation: Animation?) {
					imageViewWinnerCenter_4.startAnimation(anim)
				}
				override fun onAnimationStart(animation: Animation?) {}
			})
			imageViewWinnerCenter_4.startAnimation(anim)
		}
	}

	private fun write(path: String, value: Any) {
		if (activityExists) refRoomDatabase.child(path).setValue(value)
	}

	private fun writeChild(data: MutableMap<String, Any>){
		if (activityExists) refGameDatabase.updateChildren(data)
	}
	private fun getCardsAndDisplay(player: String = from, display: Boolean = true, animation: Boolean = false) {
		refRoomDatabase.child("CH/$player").  // display the host info in joining room screen
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
		if (!this::snackBar.isInitialized) {
			snackBar = Snackbar.make(findViewById(R.id.gameScreen1), message, Snackbar.LENGTH_LONG)
				.setAction("Dismiss") { snackBar.dismiss() }
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
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (chatLinearLayout.visibility == View.VISIBLE) { //close chat display
			hideKeyboard()
			chatLinearLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_chat_close))
			Handler(Looper.getMainLooper()).postDelayed({
				chatOpenStatus = false
				chatLinearLayout.visibility = View.GONE
			}, 140) //            (imageViewChat.drawable as GifDrawable).start()

		} else { //open chat display
			counterChat = 0 // reset chat counter to 0
			chatOpenStatus = true
			findViewById<TextView>(R.id.textViewChatNo).visibility = View.GONE // make counter invisible
			findViewById<TextView>(R.id.textViewChatNo).clearAnimation() // clear counter animation
			chatLinearLayout.visibility = View.VISIBLE
			chatLinearLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomin_chat_open))
			(imageViewChat.drawable as GifDrawable).stop()
		}
	}

	private fun inflateEmoji() {
		emojiGrid1.removeAllViews()
		val emojiList = Emoji().emojiChatArray
		for (i in emojiList.indices) {
			val textView = MaterialTextView(this)
			textView.text = emojiList[i]
			textView.textSize = 30f
			textView.gravity = Gravity.CENTER
			textView.isFocusable = true
			textView.isClickable = true
			textView.id = i
			val params = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.MATCH_PARENT)
			params.marginEnd = 5
			textView.layoutParams = params //            textView.background = ContextCompat.getDrawable(this, R.drawable.blacksquarebutton)
			textView.foreground = ContextCompat.getDrawable(this, typedValue.resourceId)
			textView.setOnClickListener { view ->
				view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
				sendEmoji(view)
			}
			emojiGrid1.addView(textView)
		}
	}

	@SuppressLint("SimpleDateFormat")
	private fun sendEmoji(view: View) {
		refRoomFirestore.document(roomID + "_chat")
			.set(hashMapOf("M" to "$selfName : ${Emoji().emojiChatArray[view.id]}", "d" to SimpleDateFormat("yyyyMMdd").format(Date())
				.toInt(), "dt" to SimpleDateFormat("HH:mm:ss z").format(Date())), SetOptions.merge())
	}

	@SuppressLint("SimpleDateFormat")
	fun sendChat(view: View) {
		if (findViewById<EditText>(R.id.editTextChatInput).text.toString().isNotEmpty()) {
			refRoomFirestore.document(roomID + "_chat")
				.set(hashMapOf("M" to "$selfName : ${findViewById<EditText>(R.id.editTextChatInput).text}", "d" to SimpleDateFormat("yyyyMMdd").format(Date())
					.toInt(), "dt" to SimpleDateFormat("HH:mm:ss z").format(Date())), SetOptions.merge())
			findViewById<EditText>(R.id.editTextChatInput).setText("")
		}
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
		if (sharedPreferences.contains("vibrateStatus")) {
			vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
		}
		if (sharedPreferences.contains("rated")) { //            rated = sharedPreferences.getBoolean("rated", false)
		} else {
			editor.putBoolean("rated", rated)
			editor.apply()
		}
	}

	@SuppressLint("NewApi", "MissingPermission")
	private fun vibrationStart(duration: Long = 200) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
		} else {
			vibrator.vibrate(duration)
		}
	}

	private fun initializeAds() {
		val adUnitID1 = if (BuildConfig.DEBUG) getString(R.string.interstitialTest_mp) // real interstitial ad id - MoPub
		else getString(R.string.interstitialReal_mp) // test interstitial ad
		mInterstitialAdMP = MoPubInterstitial(this, adUnitID1)

		if (!premiumStatus) {
			val adUnitID = if (BuildConfig.DEBUG) getString(R.string.bannerTest_MP)
			else getString(R.string.bannerReal_MP)

			addViewChatGameScreenBanner.visibility = View.VISIBLE
			addViewChatGameScreenBanner.setAdUnitId(adUnitID)
			addViewChatGameScreenBanner.adSize = MoPubView.MoPubAdSize.HEIGHT_280
			addViewChatGameScreenBanner.loadAd()

			addViewGameScreenBanner.visibility = View.VISIBLE
			addViewGameScreenBanner.setAdUnitId(adUnitID)
			addViewGameScreenBanner.adSize = MoPubView.MoPubAdSize.HEIGHT_280
			addViewGameScreenBanner.loadAd()
			loadInterstitialAd()  // Load interstitial ad
		} else {
			addViewGameScreenBanner.visibility = View.GONE
			addViewChatGameScreenBanner.visibility = View.GONE
		}
	}

	private fun loadInterstitialAd(showAd: Boolean = false) {
		val mInterstitialAdMPListener = object : MoPubInterstitial.InterstitialAdListener {
			override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
				loadInterAdTry = 0
			}
			override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) {
				if (loadInterAdTry < 5) loadInterstitialAd()
			}
			override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
			                loadInterstitialAd()
			}
			override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {}
			override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
				logFirebaseEvent(key = "watched_ad")
				if (fromInt == 1 && gameData.gs == 6) { //					findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.inActiveCard))
					startNextRoundButton.visibility = View.VISIBLE
					startNextRoundButton.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
				}
				if (fromInt == 1) write("OL/$from", 1) // for others except host, onStart will take care to update activity
			}
		}
		mInterstitialAdMP.interstitialAdListener = mInterstitialAdMPListener
		mInterstitialAdMP.load()
	}

	fun showDialogue(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		closeRoom = true
		speak("Are you sure want to leave the game", speed = 0.95f, forceSpeak = true)
		if (!this::alertDialog.isInitialized) {
			val builder = AlertDialog.Builder(this)
			builder.setTitle("Exit Game")
			builder.setMessage("Are you sure want to leave the game ?")
			builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
				toastCenter("Leaving game now")
				speak("Leaving game ", forceSpeak = true)
				Handler(Looper.getMainLooper()).postDelayed({ closeGameRoom(View(this)) }, 300)
			}
			builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
				closeRoom = false
			}
			builder.setOnDismissListener {
				closeRoom = false
			}
			alertDialog = builder.create()
			alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shine_player_stats))
		}
		alertDialog.show()
	}

	fun closeGameRoom(view: View) {
		if (activityExists && (fromInt == 1 || view.tag == "clicked")) refRoomDatabase.child("OL/$from")
			.setValue(2)
		activityExists = false
		countDownBidding.cancel()
		countDownPlayCard.cancel()
		startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) }
			.apply { putExtra("returnFromGameScreen", true) }
			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
		overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
		finishAndRemoveTask()
	}

	override fun onStop() {
		super.onStop()
		refRoomDatabase.child("OL").removeEventListener(onlineStatusListener)
		chatRegistration.remove()

		if (this::gameDataListener.isInitialized) refRoomDatabase.child("G").removeEventListener(gameDataListener)
		countDownTimer("PlayCard", purpose = "cancel")
		countDownTimer("Bidding", purpose = "cancel")
	}

	override fun onPause() {
		super.onPause()
		if (activityExists) write("OL/$from", 0)
	}  // is offline

	override fun onBackPressed() { //minimize the app and avoid destroying the activity
		if (!scoreOpenStatus && !chatOpenStatus) {
			this.moveTaskToBack(true)
		}
		if (scoreOpenStatus) openCloseScoreSheet(View(this))
		if (chatOpenStatus) openCloseChatWindow(View(this))

	} // is offline

	override fun onDestroy() {
		if (this::mInterstitialAdMP.isInitialized) {
			mInterstitialAdMP.interstitialAdListener = null
			mInterstitialAdMP.destroy()
		}
		addViewGameScreenBanner.destroy()
		addViewChatGameScreenBanner.destroy()
		try {
			if (this::textToSpeech.isInitialized) {
				textToSpeech.stop()
				textToSpeech.shutdown()
			}
		} catch (me: java.lang.Exception) {
		}
		chatRecyclerView.adapter = null
		MoPub.onDestroy(this)
		super.onDestroy()

	}
}


