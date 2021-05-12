@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.text.Html
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.android.billingclient.api.*
import com.facebook.login.LoginManager
import com.google.android.gms.ads.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main_home_screen.*
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.fragment_test.view.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class MainHomeScreen : AppCompatActivity() {
	//    region Initialization
	private var requestRatingAfterDays = 1 //dummy
	private var ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
		.toInt()

	private var background = 4
	private lateinit var soundBkgd: MediaPlayer
	private lateinit var textToSpeech: TextToSpeech
	private lateinit var firebaseAnalytics: FirebaseAnalytics
	private lateinit var intentBuilder: CustomTabsIntent.Builder
	private lateinit var snackbar: Snackbar
	private val howtoPlayUrl = "http://sites.google.com/view/kaali-ki-teeggi/how-to-play"
	private var rewardStatus = false
	private var rewardAmount = 0
	private var createRoomStatus = false
	private var offlineRoomCreate = true
	private var onceAdWatched = true
	private val today = CreateUser().todayDate
	private val todayClass = GetFormattedDate(dateInput = today)
	private var errorJoinRoomID = false
	private var backButtonPressedStatus = false
	private var joinRoomWindowStatus = false
	private var ratingWindowOpenStatus = false
	private var createRoomWindowStatus = false

	private lateinit var tabLayoutMediator: TabLayoutMediator
	private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback
	private var rankWindowStatus = false

	private var rankAllTimeSetupDone = false
	private var rankDailySetupDone = false
	private val limitFetchOnceAT = 15L
	private val limitFetchOnce = 10L
	private var lastSeenLimitAT = getChangedDate(today, -7)
	private var maxItemsFetchAT = 50
	private var maxItemsFetchDaily = 80
	private var isScrollingAllTime = false
	private var isScrollingDaily = false
	private lateinit var querySnapAT: QuerySnapshot
	private lateinit var querySnapDaily: QuerySnapshot
	private lateinit var recyclerView: RecyclerView
	private lateinit var recyclerViewScrollListener: RecyclerView.OnScrollListener
	private lateinit var adapter: ListViewAdapter
	private lateinit var layoutManager: LinearLayoutManager
	private var userArrayList = ArrayList<UserBasicInfo>()

	private lateinit var recyclerView1: RecyclerView
	private lateinit var recyclerView1ScrollListener: RecyclerView.OnScrollListener

	private lateinit var adapter1: ListViewAdapter
	private lateinit var layoutManager1: LinearLayoutManager
	private var userArrayList1 = ArrayList<UserBasicInfo>()

	private var settingsWindowStatus = false
	private var trainAccess = true
	private var onlineGameAllowed = false
	private var onlineGameAllowedM = "Cannot reach server\nCheck your network"
	private var offlineGameAllowed = true
	private var offlineGameAllowedM = ""

	private var refUsersData = Firebase.firestore.collection("Users")
	private lateinit var refRoomData: CollectionReference
	private lateinit var fireStoreRef: DocumentReference

	private lateinit var mInterstitialAdMP: MoPubInterstitial
	private lateinit var rewardedAdUnitId: String
	private lateinit var billingClient: BillingClient
	private var billingClientTry = 0
	private lateinit var mAuth: FirebaseAuth
	private var uid = ""
	private lateinit var userName: String
	private lateinit var photoURL: String
	private var userBasicInfo = UserBasicInfo()
	private var musicStatus = false
	private var soundStatus = true
	private var vibrateStatus = true
	private var premiumStatus = false
	private var newUser = true
	private var returnFromGameScreen = false
	private var rated = false
	private var consecutiveDay = 1
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor

	private lateinit var vibrator: Vibrator
	private var userDataFetched = false
	private var joinRoomPending = false
	private var roomID = ""
	private var totalCoins = 0
	private var totalDailyCoins = 0
	private var nGamesPlayedDaily = 0
	private var nGamesWonDaily = 0
	private var nGamesBidDaily = 0
	private var nGamesPlayed = 0
	private var nGamesWon = 0
	private var nGamesBid = 0
	private var nGamesPlayedBot = 0
	private var nGamesWonBot = 0
	private var nGamesBidBot = 0
	private var nPlayers = 0

	private var loadRewardedAdTry = 0
	private var loadInterAdTry = 0
	private var countRewardWatch = 3
	private var dailyRewardList = listOf(500, 1000, 2000, 3000, 4000, 5000, 5000)
	private var coinDur = 900L
	private var coinBurst = true
	private var coinSpeed = 4f
	private var coinCount = 50
	private var dailyRewardAmount = 0
	private var dailyRewardClicked = false
	private var claimedToday = false
	private var dailyRewardStatus = false

	// endregion

	@SuppressLint("ShowToast", "SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.Theme_App)
		super.onCreate(savedInstanceState)
		CaocConfig.Builder.create()
			.backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
			.enabled(true) //default: true
			.showErrorDetails(true) //default: true
			.showRestartButton(true) //default: true
			.logErrorOnRestart(false) //default: true
			.trackActivities(false) //default: false
			.errorDrawable(R.drawable.bug_icon) //default: bug image
			.apply()
		setContentView(R.layout.activity_main_home_screen)
		background = Random.nextInt(0, 6)
		when (background) {
			0 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.redblackburst)
			1 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.blueburst)
			4 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.greenyellowburst)
			3 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.navyblueburst)
			2 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.redorangeburst)
			5 -> backgroundmhs.background = ContextCompat.getDrawable(this, R.drawable.yellowburst)
		}
		checkJoinRoom(intent)
		mainIconGridDisplay()
		newUser = intent.getBooleanExtra("newUser", true)
		returnFromGameScreen = intent.getBooleanExtra("returnFromGameScreen", false)
		refRoomData = Firebase.firestore.collection(getString(R.string.pathRoom))
		mAuth = FirebaseAuth.getInstance()
		uid = mAuth.uid.toString()
		soundBkgd = MediaPlayer.create(this, R.raw.music)
		soundBkgd.isLooping = true
		soundBkgd.setVolume(0.05F, 0.05F)
		getSharedPrefs()
		fireStoreRef = Firebase.firestore.collection("Users").document(uid)
		SoundManager.initialize(this)
		initializeAds()
		Handler(Looper.getMainLooper()).post {
			vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
			getUserData()
			checkIfOnlineGameAllowed()
			initTabLayoutAdapter()
			initializeSpeechEngine()
			enterText() // press enter to join room
			buildCustomTabIntent()
//			setupBillingClient() // memory leak issue so don't initialize everytime except when requested
			if (BuildConfig.DEBUG) trainingButton.visibility = View.VISIBLE
		}
		firebaseAnalytics = FirebaseAnalytics.getInstance(this)
		logFirebaseEvent("MainHomeScreen", 1, "open")
	}

	private fun mainIconGridDisplay() {
		mainIconGridView.layoutManager = GridLayoutManager(this, 2)
		mainIconGridView.adapter = IconAdapter(this, setDataListMHS()) { output ->
			actionGridItemClick(position = output) // receive position output here
		}

		//		mainIconGridView.visibility = View.VISIBLE
		// Attach Text watcher to room ID input - For resetting error hint on re-entering roomID
		roomIDInput.doOnTextChanged { _, _, _, _ ->
			if (roomIDInputLayout.error != null && !errorJoinRoomID) {
				roomIDInputLayout.error = null
				roomIDInputLayout.helperText = getString(R.string.joinHelper)
			} else if (errorJoinRoomID) {
				errorJoinRoomID = false
			}
		}
	}

	private fun actionGridItemClick(position: Int) {
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		when (position) {
			0 -> createRoomWindowOpen()
			1 -> joinRoomWindowOpen()
			2 -> ranking()
			3 -> howToPlay()
			4 -> inviteFriends()
			5 -> openSettingsWindow()
		}
	}

	private fun setDataListMHS(): ArrayList<DailyRewardItem> {
		val arrayList = ArrayList<DailyRewardItem>()
		arrayList.add(DailyRewardItem(R.drawable.joystick, getString(R.string.play)))
		arrayList.add(DailyRewardItem(R.drawable.joinroom, getString(R.string.joinRoom)))
		arrayList.add(DailyRewardItem(R.drawable.ranking, getString(R.string.ranking)))
		arrayList.add(DailyRewardItem(R.drawable.howtoplay, getString(R.string.howtoplay)))
		arrayList.add(DailyRewardItem(R.drawable.invite, getString(R.string.invite)))
		arrayList.add(DailyRewardItem(R.drawable.settings, getString(R.string.settings)))
		return arrayList
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		checkJoinRoom(intent)
	}

	private fun checkJoinRoom(intent: Intent?) {
		if (intent != null) {
			if (intent.data?.host == getString(R.string.hostFirebaseDL) && !intent.data?.query.isNullOrEmpty()) { //opened directly in app
				if (intent.data?.query?.contains("link=${getString(R.string.scheme)}://${getString(R.string.hostJoinRoom)}/")!!) {
					roomID = intent.data?.query?.split("link=${getString(R.string.scheme)}://${getString(R.string.hostJoinRoom)}/")
						?.get(1)?.split("&")?.get(0).toString()
					joinRoomPending = true
					if (userDataFetched) autoJoinRoom()
				}
			}
			if (intent.data?.host == getString(R.string.hostJoinRoom)) { // re-routed from browser/chrome
				roomID = intent.data?.lastPathSegment.toString()
				joinRoomPending = true
				if (userDataFetched) autoJoinRoom()
			}
		}
	}

	private fun autoJoinRoom() {
		if (joinRoomPending && userDataFetched) {
			openClosePlayerStats(backgroundmhs)
			joinRoomWindowOpen(showAds = false)
			roomIDInput.setText(roomID)
			joinRoomButtonClicked(joinRoomButton)
		}
	}

	override fun onStart() {
		super.onStart()
		if (musicStatus && this::soundBkgd.isInitialized) soundBkgd.start()
	}

	private fun logFirebaseEvent(event: String, int: Int, key: String) {
		val params = Bundle()
		params.putInt(key, int)
		firebaseAnalytics.logEvent(event, params)
	}

	private fun callConfetti() {
	}

	private fun dailyRewardWindowDisplay() {
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		val listDailyRewardItem = setDataListDR()
		val imageAdapter = DailyRewardGridAdapter(listDailyRewardItem, min(listDailyRewardItem.size, consecutiveDay))
		dailyRewardGrid.adapter = imageAdapter
		dailyRewardGridLayout.visibility = View.VISIBLE
		anim(dailyRewardGridLayout, R.anim.zoomin_center)
	}

	private fun setDataListDR(): ArrayList<DailyRewardItem> {
		val arrayList = ArrayList<DailyRewardItem>()
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 1 \n${dailyRewardList[0]} coins"))
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 2 \n${dailyRewardList[1]} coins"))
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 3 \n${dailyRewardList[2]} coins"))
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 4 \n${dailyRewardList[3]} coins"))
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 5 \n${dailyRewardList[4]} coins"))
		arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 5+ \n${dailyRewardList[5]} coins"))
		return arrayList
	}

	private fun playerStatsGridDisplay() {

		playerStatsGrid.layoutManager = GridLayoutManager(this, 3)
		playerStatsGrid.adapter = PlayerStatsGridAdapter(setDataListPS())
		playerStatsML.transitionToStart()
	}

	private fun setDataListPS(): ArrayList<PlayerStatsItem> {
		val arrayList = ArrayList<PlayerStatsItem>()
		val winRate = (if ((nGamesPlayed + nGamesPlayedBot) > 0) round((100 * (nGamesWon + nGamesWonBot) / (nGamesPlayed + nGamesPlayedBot)).toDouble()).toInt() else "-").toString() + "% "
		arrayList.add(PlayerStatsItem(R.drawable.coin_trans_1, "Today's score", totalDailyCoins.toString()))
		arrayList.add(PlayerStatsItem(R.drawable.trend, "Win Rate", winRate))
		arrayList.add(PlayerStatsItem(R.drawable.active, "Active days", consecutiveDay.toString()))
		arrayList.add(PlayerStatsItem(R.drawable.joystick, "Games Played", (nGamesPlayed + nGamesPlayedBot).toString()))
		arrayList.add(PlayerStatsItem(R.drawable.trophy, "Games Won", (nGamesBid + nGamesBidBot).toString()))
		arrayList.add(PlayerStatsItem(R.drawable.target, "Games Bid", (nGamesWon + nGamesWonBot).toString()))
		arrayList.add(PlayerStatsItem(R.drawable.joystick, "Played today", nGamesPlayedDaily.toString()))
		arrayList.add(PlayerStatsItem(R.drawable.trophy, "Won today", nGamesWonDaily.toString()))
		arrayList.add(PlayerStatsItem(R.drawable.target, "Bid today", nGamesBidDaily.toString()))
		return arrayList
	}

	fun closeDailyRewardWindowDisplay(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		dailyRewardGridLayout.visibility = View.GONE
	}

	fun addPoints() {
		callConfetti()
		if (soundStatus) SoundManager.getInstance().playZipSound()
		dailyRewardClicked = false
		rewardAmount = dailyRewardAmount
		totalCoins += rewardAmount  // reward with daily reward amount for watching video
		userBasicInfo.score = totalCoins  // reward with daily reward amount for watching video
		logFirebaseEvent("daily_rewards", 1, "coins$rewardAmount")
		watchVideoCoin.text = rewardAmount.toString()
		watchVideoCoin.visibility = View.VISIBLE
		if (soundStatus) SoundManager.getInstance().playCardCollectSound()
		anim(watchVideoCoin, R.anim.slide_500_coins)
		Handler(Looper.getMainLooper()).postDelayed({
			if (vibrateStatus) vibrationStart()
			userScoreMHS.setText(String.format("%,d", totalCoins), true)
			watchVideoCoin.visibility = View.GONE
			loadRewardAd()
		}, 1250)
		refUsersData.document(uid)
			.set(hashMapOf("sc" to totalCoins, "LSD" to today, "nDRC" to consecutiveDay, "claim" to 1), SetOptions.merge())
	}

	fun claimDailyReward(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (vibrateStatus) vibrationStart()
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		dailyRewardClicked = true
		dailyRewardStatus = true
		if (premiumStatus) {
			dailyRewardGridLayout.visibility = View.GONE
			addPoints()
		} else if (MoPubRewardedAds.hasRewardedAd(rewardedAdUnitId) && !newUser) { // MoPubRewardedAds loaded or not check
			MoPubRewardedAds.showRewardedAd(rewardedAdUnitId)
			watchVideo.clearAnimation()
			watchVideo.visibility = View.GONE
		} else if (mInterstitialAdMP.isReady && !newUser) {
			mInterstitialAdMP.show()
			dailyRewardGridLayout.visibility = View.GONE
			watchVideo.clearAnimation()
			watchVideo.visibility = View.GONE
			loadRewardAd()
		} else {
			dailyRewardGridLayout.visibility = View.GONE
			addPoints()
			watchVideo.clearAnimation()
			watchVideo.visibility = View.GONE
			loadRewardAd()
			if (!premiumStatus) loadInterstitialAd()
		}
	}

	@SuppressLint("SimpleDateFormat", "SetTextI18n")
	private fun getUserData() {
		val user = mAuth.currentUser
		sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
		editor = sharedPreferences.edit()
		if (user != null) {
			FirebaseCrashlytics.getInstance().setUserId(uid)
			userName = user.displayName?.toString()!!.split(" ")[0]
			usernameCardMHS.text = userName
			if (intent.getBooleanExtra("newUser", false)) { //check if user has joined room or created one and display Toast
				editor.putBoolean("rated", rated)
				editor.putInt("joinDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
					.toInt())
				editor.putInt("ratingRequestDate", getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
					.toInt(), requestRatingAfterDays))
				editor.putBoolean("premium", false) // make premium false
				editor.apply()
			} // check if new user
		} else {
			mAuth.signOut()
			startActivity(Intent(this, StartScreen::class.java).apply { putExtra("background", background) }
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
			finishAndRemoveTask()
		}
		refUsersData.document(uid).get().addOnSuccessListener { dataSnapshot ->
			if (dataSnapshot.data != null) {
				try {
					premiumStatus = dataSnapshot.get("pr").toString().toInt() == 1
					if (premiumStatus) {
						removeAds.visibility = View.GONE
						addViewMHS.visibility = View.GONE
					}
					if (soundStatus) SoundManager.getInstance().playSuccessSound()
					maskAllLoading.visibility = View.GONE
					photoURL = dataSnapshot.get("ph").toString()
					Picasso.get().load(photoURL).resize(400, 400)
						.into(profilePic)					//                    callConfetti() // don't call confetti here - call only when coins are earned
					totalCoins = dataSnapshot.get("sc").toString().toInt()
					if (dataSnapshot.contains("scd") && dataSnapshot.contains("p_daily")) {
						totalDailyCoins = dataSnapshot.get("scd").toString().toInt()
						nGamesPlayedDaily = dataSnapshot.get("p_daily").toString().toInt()
						nGamesWonDaily = dataSnapshot.get("w_daily").toString().toInt()
						nGamesBidDaily = dataSnapshot.get("b_daily").toString().toInt()
					} else {
						totalDailyCoins = 0
						fireStoreRef.set(hashMapOf("scd" to 0, "p_daily" to 0, "w_daily" to 0, "b_daily" to 0), SetOptions.merge())
					}
					userScoreMHS.setText(String.format("%,d", totalCoins), true)
					nGamesPlayed = dataSnapshot.get("p").toString().toInt()
					nGamesWon = dataSnapshot.get("w").toString().toInt()
					nGamesBid = dataSnapshot.get("b").toString().toInt()
					if (!dataSnapshot.contains("zFCM_token")) {
						createTokenFC()
					} else {
						createTokenFC(check = true, oldToken = dataSnapshot.get("zFCM_token")
							.toString())
					}
					if (!dataSnapshot.contains("rated")) {
						if (!rated) fireStoreRef.set(hashMapOf("rated" to 0), SetOptions.merge()) // set to false 1st time - new user
						else fireStoreRef.set(hashMapOf("rated" to 1), SetOptions.merge())
					} else {
						rated = dataSnapshot.get("rated").toString().toInt() == 1
						editor.putBoolean("rated", rated)
						editor.apply()
					}
					when {
						dataSnapshot.contains("p_bot") -> {
							nGamesPlayedBot = dataSnapshot.get("p_bot").toString().toInt()
							nGamesWonBot = dataSnapshot.get("w_bot").toString().toInt()
							nGamesBidBot = dataSnapshot.get("b_bot").toString().toInt()
						}
						else -> fireStoreRef.set(hashMapOf("b_bot" to 0, "p_bot" to 0, "w_bot" to 0), SetOptions.merge())
					}
					val lastSeenDate = dataSnapshot.get("LSD").toString().toInt()
					consecutiveDay = dataSnapshot.get("nDRC").toString().toInt()
					dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
					claimedToday = dataSnapshot.get("claim").toString().toInt() == 1
					if (today == getChangedDate(lastSeenDate, 1)) { // if consecutive day login
						consecutiveDay += 1
						claimedToday = false
						dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
						nGamesBidDaily = 0
						nGamesPlayedDaily = 0
						nGamesWonDaily = 0
						totalDailyCoins = 0
						fireStoreRef.set(hashMapOf("LSD" to today, "scd" to 0, "p_daily" to 0, "w_daily" to 0, "b_daily" to 0, "nDRC" to consecutiveDay, "claim" to 0, "phone" to "${Build.MANUFACTURER} ${Build.MODEL}", "phAPI" to Build.VERSION.SDK_INT, "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())
					} else if (today > getChangedDate(lastSeenDate, 1)) { // if more than 1 day gap , reset counter
						consecutiveDay = 1
						claimedToday = false
						dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
						nGamesBidDaily = 0
						nGamesPlayedDaily = 0
						nGamesWonDaily = 0
						totalDailyCoins = 0
						fireStoreRef.set(hashMapOf("LSD" to today, "scd" to 0, "p_daily" to 0, "w_daily" to 0, "b_daily" to 0, "nDRC" to consecutiveDay, "claim" to 0, "phone" to "${Build.MANUFACTURER} ${Build.MODEL}", "phAPI" to Build.VERSION.SDK_INT, "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())
					}
					userBasicInfo = extractUserData(dataSnapshot)
					userDataFetched = true
					if (joinRoomPending) autoJoinRoom()
					playerStatsGridDisplay()
					if (!claimedToday && !joinRoomPending) { //if not claimed today
						Handler(Looper.getMainLooper()).postDelayed({
							dailyRewardWindowDisplay()
						}, 1600)
					}
					if (returnFromGameScreen && checkRatingRequest(minGames = 6)) {
						Handler(Looper.getMainLooper()).postDelayed({
							backButtonPressedStatus = true
							openRatingWindow(View(this))
						}, 1600)
					}
					editor.putString("photoURL", photoURL) // write username to preference file
					editor.apply()
					editor.putBoolean("premium", premiumStatus) // write username to preference file
					editor.apply()
					fireStoreRef.set(hashMapOf("LSDT" to SimpleDateFormat("HH:mm:ss z").format(Date()), "lang" to applicationContext.resources.configuration.locale.displayLanguage, "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())					//					checkAccessToTrain()
				} catch (exception: java.lang.Exception) {
					mAuth.signOut()
					startActivity(Intent(this, StartScreen::class.java).apply { putExtra("background", background) }
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
					finishAndRemoveTask()
				}
			} else {
				mAuth.signOut()
				startActivity(Intent(this, StartScreen::class.java).apply { putExtra("background", background) }
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
				finishAndRemoveTask()
			}
		}.addOnFailureListener {
			mAuth.signOut()
			startActivity(Intent(this, StartScreen::class.java).apply { putExtra("background", background) }
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
			finishAndRemoveTask()
		}
	}

	private fun createTokenFC(check: Boolean = false, oldToken: String = "") {
		FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
			if (!task.isSuccessful) {
				return@OnCompleteListener
			} else {
				val token = task.result // Get new FCM registration token
				if (!check) {					//                    Toast.makeText(baseContext, "token created to firebase", Toast.LENGTH_SHORT).show()
					fireStoreRef.set(hashMapOf("zFCM_token" to token.toString(), "zFCM_dt" to SimpleDateFormat("yyyyMMdd HH:mm:ss z").format(Date())), SetOptions.merge())
				} else if (oldToken == token.toString()) {					//                    Toast.makeText(baseContext, "token matched- nothing to do", Toast.LENGTH_SHORT).show()
				} else {					//                    Toast.makeText(baseContext, "token changed ", Toast.LENGTH_SHORT).show()
					fireStoreRef.set(hashMapOf("zFCM_token" to token.toString(), "zFCM_dt" to SimpleDateFormat("yyyyMMdd HH:mm:ss z").format(Date())), SetOptions.merge())
				}
			}
		})
	}

	private fun getSharedPrefs() {
		sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
		editor = sharedPreferences.edit()

		if (sharedPreferences.contains("premium")) {
			premiumStatus = sharedPreferences.getBoolean("premium", false)
			if (premiumStatus) {
				removeAds.visibility = View.GONE
			} else removeAds.visibility = View.VISIBLE
		}
		if (sharedPreferences.contains("rated")) {
			rated = sharedPreferences.getBoolean("rated", false)
		} else {
			editor.putBoolean("rated", rated)
			editor.apply()
		}
		if (!sharedPreferences.contains("ratingRequestDate")) {
			ratingRequestDate = getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
				.toInt(), requestRatingAfterDays)
			editor.putInt("ratingRequestDate", ratingRequestDate)
			editor.apply()
		} else {
			ratingRequestDate = sharedPreferences.getInt("ratingRequestDate", 0)
		}
		if (!sharedPreferences.contains("joinDate")) {
			editor.putInt("joinDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
				.toInt())
			editor.apply()
		}
		if (sharedPreferences.contains("musicStatus")) {
			musicStatus = sharedPreferences.getBoolean("musicStatus", true)
			musicSwitch.isChecked = musicStatus
			if (musicStatus) {
				soundBkgd.start()
				settMusicIcon.setImageResource(R.drawable.music)
			} else {
				settMusicIcon.setImageResource(R.drawable.nomusic)
			}
		} else {
			musicStatus = true
			musicSwitch.isChecked = musicStatus
			if (this::soundBkgd.isInitialized) soundBkgd.start()
			editor.putBoolean("musicStatus", musicStatus) // write username to preference file
			editor.apply()
		}
		if (sharedPreferences.contains("soundStatus")) {
			soundStatus = sharedPreferences.getBoolean("soundStatus", true)
			soundSwitch.isChecked = soundStatus
			if (soundStatus) {
				settSoundIcon.setImageResource(R.drawable.sound_on_png)
			} else settSoundIcon.setImageResource(R.drawable.sound_off_png)
		}
		if (sharedPreferences.contains("vibrateStatus")) {
			vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
			vibrateSwitch.isChecked = vibrateStatus
			if (vibrateStatus) {
				settVibrateIcon.setImageResource(R.drawable.vibrateon)
			} else settVibrateIcon.setImageResource(R.drawable.vibrateoff)
		}
		if (sharedPreferences.contains("Room")) {
			val roomID = sharedPreferences.getString("Room", "").toString()
			if (roomID.isNotEmpty()) {
				Handler(Looper.getMainLooper()).postDelayed({ deleteAllRoomData(roomID) }, 0)				//                deleteAllRoomdata(roomID)
			}
			editor.remove("Room").apply()
		}
	}

	fun removeAdsOrPremium(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (premiumStatus) openClosePlayerStats(View(this))
		else {
			if (soundStatus) SoundManager.getInstance().playUpdateSound()
			setupBillingClient() // memory leak issue so not to initialize everytime
//			querySkuDetailsRequest()
		}

	}

	private fun setupBillingClient() {
		billingClientTry += 1
		val purchaseListener = PurchasesUpdatedListener { billingResult, purchases ->
			if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
				for (purchase in purchases) {
					if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
						toastCenter("Successful purchase")
						lifecycleScope.launch { acknowledgePurchase(purchase) }
					} else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
						if (soundStatus) SoundManager.getInstance().playUpdateSound()
						toastCenter("Your Payment is processing \nWe will update you after finishing processing")
					}
				}
			} else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
				SoundManager.getInstance().playErrorSound()
				toastCenter("Payment Cancelled")
			} else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
				SoundManager.getInstance().playErrorSound()
				toastCenter("Already processing pending item")
			} else {
				SoundManager.getInstance().playErrorSound()
				toastCenter("Payment Failed. Try again \n ${billingResult.responseCode}")
			}
		}
		billingClient = BillingClient.newBuilder(applicationContext).enablePendingPurchases()
			.setListener(purchaseListener).build()
		billingClient.startConnection(object : BillingClientStateListener {
			override fun onBillingSetupFinished(billingResult: BillingResult) {
				querySkuDetailsRequest()
				checkPendingPurchases()
			}
			override fun onBillingServiceDisconnected() { //				if(billingClientTry<3) setupBillingClient() // Test if required to try again
			}
		})
	}

	private fun checkPendingPurchases() {
		val purchases = billingClient.queryPurchases("inapp").purchasesList
		if (purchases != null) {
			for (purchase in purchases) {
				if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
					lifecycleScope.launch { acknowledgePurchase(purchase) }
				} else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
					toastCenter("You have a pending payment \nPlease check your Google Play Account")
				}
			}
		}
	}

	private fun querySkuDetailsRequest() {
		if (billingClient.isReady) {
			val params = SkuDetailsParams.newBuilder().setSkusList(listOf("remove_ads"))
				.setType(BillingClient.SkuType.INAPP).build()
			billingClient.querySkuDetailsAsync(params) { response, skuDetailsList ->
				if (response.responseCode == BillingClient.BillingResponseCode.OK) {
					val billingFlowParams = skuDetailsList?.get(0)?.let {
						BillingFlowParams.newBuilder().setSkuDetails(it).build()
					}
					if (billingFlowParams != null) {
						billingClient.launchBillingFlow(this, billingFlowParams)
					}
				}
			}
		} else {
			toastCenter("Billing client is not ready \nTry again or Restart app")
//			setupBillingClient()
		}
	}

	@SuppressLint("SimpleDateFormat")
	private fun acknowledgePurchase(purchase: Purchase) {
		val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
			.build()
		billingClient.acknowledgePurchase(params) {}
		if (purchase.sku == "remove_ads") {
			premiumStatus = true
			if (soundStatus) SoundManager.getInstance().playSuccessSound()
			toastCenter("Congratulations!! Your Payment is approved \n You won't see Ads now")
			refUsersData.document(uid).set(hashMapOf("pr" to 1), SetOptions.merge())
			Firebase.firestore.collection("PremiumUser").document(uid)
				.set(hashMapOf("id" to mAuth.currentUser?.email, "d" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
					.toInt()))
			consumePurchase(purchase)
			removeAds.visibility = View.GONE
			addViewMHS.visibility = View.GONE
			editor.putBoolean("premium", true) // write username to preference file
			editor.apply()
		}

	}

	private fun consumePurchase(purchase: Purchase) {
		val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
		billingClient.consumeAsync(params) { _: BillingResult, _: String -> }
	}

	@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
	private fun initializeAds() {
		val configBuilder = if (BuildConfig.DEBUG) SdkConfiguration.Builder(getString(R.string.bannerTest_MP))
			.withLogLevel(MoPubLog.LogLevel.DEBUG)
		else SdkConfiguration.Builder(getString(R.string.bannerReal_MP))
			.withLogLevel(MoPubLog.LogLevel.NONE)
		MoPub.initializeSdk(this, configBuilder.build()) { loadDifferentAds() }
	}

	private fun loadDifferentAds() {
		val adUnitID = if (BuildConfig.DEBUG) getString(R.string.bannerTest_MP)
		else getString(R.string.bannerReal_MP)
		addViewMHS.setAdUnitId(adUnitID)
		addViewMHS.adSize = MoPubView.MoPubAdSize.HEIGHT_280
		addViewMHS.loadAd()
		loadInterstitialAd()
		rewardedAdUnitId = if (BuildConfig.DEBUG) getString(R.string.rewardedTest_mp) else getString(R.string.rewardedReal_mp)
		loadRewardAd()
	}

	private fun loadInterstitialAd() {
		loadInterAdTry += 1 // try 5 times
		val adUnitID = if (BuildConfig.DEBUG) getString(R.string.interstitialTest_mp) // real interstitial ad id - MoPub
		else getString(R.string.interstitialReal_mp) // test interstitial ad

		mInterstitialAdMP = MoPubInterstitial(this, adUnitID)
		val mInterstitialAdMPListener = object : MoPubInterstitial.InterstitialAdListener {
			override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
				loadInterAdTry = 0
			}

			override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) {
				if (loadInterAdTry < 5) loadInterstitialAd()
			}

			override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
				onceAdWatched = true
				loadInterstitialAd()
			}

			override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {}
			override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
				onceAdWatched = true
				if (dailyRewardClicked) {
					addPoints()
					dailyRewardClicked = false
				} else if (createRoomStatus) {
					createRoom()
					createRoomStatus = false
				}
			}
		}
		mInterstitialAdMP.interstitialAdListener = mInterstitialAdMPListener
		mInterstitialAdMP.load()
	}

	private fun loadRewardAd() {
		loadRewardedAdTry += 1
		val rewardAdListener = object : MoPubRewardedAdListener {
			override fun onRewardedAdClicked(adUnitId: String) {}
			override fun onRewardedAdClosed(adUnitId: String) {
				if (musicStatus) soundBkgd.start()
				if (rewardStatus) {
					if (soundStatus) SoundManager.getInstance().playZipSound()
					rewardStatus = false
					watchVideoCoin.text = rewardAmount.toString()
					watchVideoCoin.visibility = View.VISIBLE
					logFirebaseEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, 1, "coins$rewardAmount")
					if (soundStatus) SoundManager.getInstance().playCardCollectSound()
					anim(watchVideoCoin, R.anim.slide_500_coins)
					Handler(Looper.getMainLooper()).postDelayed({
						if (vibrateStatus) vibrationStart()
						userScoreMHS.setText(String.format("%,d", totalCoins), true)
						watchVideoCoin.visibility = View.GONE
						loadRewardAd()
					}, 1250)
				} else {
					loadRewardAd()
					toastCenter("Sorry $userName No coins added")
				}
			}

			override fun onRewardedAdCompleted(adUnitIds: Set<String?>, reward: MoPubReward) {
				rewardStatus = true
				rewardAmount = if (dailyRewardClicked) dailyRewardAmount
				else when (countRewardWatch) {
					0 -> 100
					1 -> 250
					2 -> 500
					else -> 1000
				}
				totalCoins += rewardAmount  // reward with 500 coins for watching video
				userBasicInfo.score = totalCoins
				if (!dailyRewardClicked) {
					countRewardWatch += 1
					refUsersData.document(uid)
						.set(hashMapOf("sc" to totalCoins), SetOptions.merge())
				} else {
					refUsersData.document(uid)
						.set(hashMapOf("sc" to totalCoins, "LSD" to today, "LSDT" to SimpleDateFormat("HH:mm:ss z").format(Date()), "nDRC" to consecutiveDay, "claim" to 1), SetOptions.merge())
				}
				dailyRewardClicked = false
			}

			override fun onRewardedAdLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
				if (loadRewardedAdTry <= 2) loadRewardAd()
			}

			override fun onRewardedAdLoadSuccess(adUnitId: String) {
				loadRewardedAdTry = 1
				when (countRewardWatch) {
					0 -> watchVideo.setImageResource(R.drawable.watch_ad_100)
					1 -> watchVideo.setImageResource(R.drawable.watch_ad_250)
					2 -> watchVideo.setImageResource(R.drawable.watch_ad_500)
					else -> watchVideo.setImageResource(R.drawable.watch_ad_1000)
				}
				watchVideo.visibility = View.VISIBLE
			}

			override fun onRewardedAdShowError(adUnitId: String, errorCode: MoPubErrorCode) {
				watchVideo.clearAnimation()
				watchVideo.visibility = View.GONE
				loadRewardAd()
			}

			override fun onRewardedAdStarted(adUnitId: String) {
				dailyRewardGridLayout.visibility = View.GONE
				watchVideo.clearAnimation()
				watchVideo.visibility = View.GONE
				if (musicStatus) soundBkgd.pause()
			}
		} //		MoPubRewardedAds.setRewardedAdListener(rewardAdListener)
		//		if(loadRewardedAdTry<=2) MoPubRewardedAds.loadRewardedAd(rewardedAdUnitId)
	}

	fun showRewardedVideoAd(view: View) {
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		if (MoPubRewardedAds.hasRewardedAd(rewardedAdUnitId)) {
			MoPubRewardedAds.showRewardedAd(rewardedAdUnitId)
		} else {
			watchVideo.clearAnimation()
			watchVideo.visibility = View.GONE
			loadRewardAd()
		}
	}

	fun openClosePlayerStats(view: View) {
		if (view.id != R.id.backgroundmhs && view.id != R.id.playerStats) {
			if (soundStatus) SoundManager.getInstance().playUpdateSound()
			view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		}
		if (playerStatsML.progress == 1f) {
			playerStatsML.transitionToStart()
		} else playerStatsML.transitionToEnd()
	}

	private fun openSettingsWindow() {
		settingsWindowStatus = true
		settingsLayout.visibility = View.VISIBLE
		Handler(Looper.getMainLooper()).postDelayed({
			closeSettings.visibility = View.VISIBLE
		}, 220)
		anim(settingsLayoutTemp, R.anim.zoomin_center)
	}

	fun closeSettingsWindow(view: View) {
		settingsWindowStatus = false
		anim(settingsLayoutTemp, R.anim.zoomout_center)
		closeSettings.visibility = View.GONE
		Handler(Looper.getMainLooper()).postDelayed({
			settingsLayout.visibility = View.GONE
		}, 250)
		closeSettings.clearAnimation()
	}

	private fun checkIfOnlineGameAllowed() {
		Firebase.database.reference.child("Check")
			.addListenerForSingleValueEvent(object : ValueEventListener {
				override fun onCancelled(errorDataLoad: DatabaseError) {
					onlineGameAllowedM = errorDataLoad.message
				}

				override fun onDataChange(data: DataSnapshot) {
					if (data.exists()) {
						onlineGameAllowed = data.child("OnlineGame").value.toString().toInt() == 1
						onlineGameAllowedM = data.child("OnlineGameM").value.toString()
						offlineGameAllowed = data.child("OfflineGame").value.toString().toInt() == 1
						offlineGameAllowedM = data.child("OfflineGameM").value.toString()
						if (!onlineGameAllowed or !offlineGameAllowed) {
							toastCenter(onlineGameAllowedM + "\n" + offlineGameAllowedM)
						}
					}
				}
			})
	}

	fun createRoomButtonClicked(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		createRoomStatus = true
		offlineRoomCreate = view.tag.toString().toInt() == 0

		nPlayers = when (view.tag.toString().toInt()) {
			0 -> 4
			4 -> 4
			else -> 7
		}
		if (premiumStatus || dailyRewardStatus || onceAdWatched || newUser) {
			createRoom()
			createRoomStatus = false
		} else if (this::mInterstitialAdMP.isInitialized) {
			if (mInterstitialAdMP.isReady) mInterstitialAdMP.show()
		}
	}

	@SuppressLint("SetTextI18n")
	private fun createRoom() {
		maskAllLoading.visibility = View.VISIBLE
		loadingText.text = getString(R.string.creatingRoom)
		if (!offlineGameAllowed && offlineRoomCreate) {
			toastCenter(offlineGameAllowedM)
			if (soundStatus) SoundManager.getInstance().playErrorSound()
			if (vibrateStatus) vibrationStart()
			loadingText.text = offlineGameAllowedM
			Handler(Looper.getMainLooper()).postDelayed({
				maskAllLoading.visibility = View.GONE
			}, 4000)
		} else if (offlineGameAllowed && offlineRoomCreate) Handler(Looper.getMainLooper()).postDelayed({ startNextActivity() }, 1200)
		else if (!onlineGameAllowed) {
			if (soundStatus) SoundManager.getInstance().playErrorSound()
			if (vibrateStatus) vibrationStart()
			loadingText.text = onlineGameAllowedM
			Handler(Looper.getMainLooper()).postDelayed({
				maskAllLoading.visibility = View.GONE
			}, 3500)
		} else {
			val allowedChars = ('A'..'H') + ('J'..'N') + ('P'..'Z') + ('2'..'9')  // 1, I , O and 0 skipped
			val roomID = (1..4).map { allowedChars.random() }.joinToString("")
			if (nPlayers == 7) {
				if (!BuildConfig.DEBUG) createRoomWithID(roomID, CreateRoomData(userBasicInfo).data7)
				else createRoomWithID(roomID, CreateRoomData(userBasicInfo).dummyData7)
			} else if (nPlayers == 4 && !offlineRoomCreate) {
				if (!BuildConfig.DEBUG) createRoomWithID(roomID, CreateRoomData(userBasicInfo).data4)
				else createRoomWithID(roomID, CreateRoomData(userBasicInfo).dummyData4)
			}
		}
	}

	private fun createRoomWithID(roomID: String, roomData: Any) {
		refRoomData.document(roomID).set(roomData).addOnFailureListener {
			maskAllLoading.visibility = View.GONE
			toastCenter("Failed to create room \nPlease try again or later")
		}.addOnSuccessListener {
			startNextActivity(roomID)
		}
	}

	private fun startNextActivity(roomID: String = "ABCD") {
		if (soundStatus) SoundManager.getInstance().playSuccessSound()
		if (!offlineRoomCreate) {
			editor.putString("Room", roomID) // write room ID in storage - to delete later
			editor.apply()
			logFirebaseEvent("create_join_room_screen", 1, "create_$nPlayers")
		} else {
			logFirebaseEvent("create_join_room_screen", 1, "create_offline_$nPlayers")
		}
		val userStatsDaily = ArrayList(listOf(nGamesPlayedDaily, nGamesWonDaily, nGamesBidDaily))
		val userStatsTotal = ArrayList(listOf(nGamesPlayed + nGamesPlayedBot, nGamesWon + nGamesWonBot, nGamesBid + nGamesBidBot))
		val userStats = if (!offlineRoomCreate) ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid))
		else ArrayList(listOf(nGamesPlayedBot, nGamesWonBot, nGamesBidBot))

		startActivity(Intent(this, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
			.apply { putExtra("selfName", userName) }.apply { putExtra("photoURL", photoURL) }
			.apply { putExtra("totalCoins", totalCoins) }
			.apply { putExtra("totalDailyCoins", totalDailyCoins) }.apply { putExtra("from", "p1") }
			.apply { putExtra("nPlayers", nPlayers) }
			.apply { putExtra("offline", offlineRoomCreate) }
			.putIntegerArrayListExtra("userStats", userStats)
			.putIntegerArrayListExtra("userStatsTotal", userStatsTotal)
			.putIntegerArrayListExtra("userStatsDaily", userStatsDaily)
			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
		overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
		finishAndRemoveTask() //		finish()
		//		Handler(Looper.getMainLooper()).postDelayed({
		//			finish()
		//		}, 500)
	}

	private fun createRoomWindowOpen() {
		Handler(Looper.getMainLooper()).postDelayed({
			closeCreateRoom.visibility = View.VISIBLE
		}, 270)
		createRoomFrame.visibility = View.VISIBLE
		anim(createRoomFrameTemp, R.anim.slide_down_player_stats) //		anim(closeCreateRoom, R.anim.zoomin_center)
		createRoomWindowStatus = true
	}

	fun createRoomWindowExit(view: View) {
		closeCreateRoom.clearAnimation()
		createSingle.clearAnimation()
		createDouble.clearAnimation()
		closeCreateRoom.visibility = View.GONE
		maskAllLoading.visibility = View.GONE
		anim(createRoomFrameTemp, R.anim.slide_up_player_stats)
		createRoomWindowStatus = false
		Handler(Looper.getMainLooper()).postDelayed({
			createRoomFrame.visibility = View.GONE
		}, 410)
	}

	fun joinRoomButtonClicked(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		if (vibrateStatus) vibrationStart()
		val roomID = roomIDInput.text.toString() //read text field
		if (roomID.isNotEmpty()) {
			if (!onlineGameAllowed && !joinRoomPending) {
				toastCenter(onlineGameAllowedM)
			} else {
				hideKeyboard()
				maskAllLoading.visibility = View.VISIBLE
				loadingText.text = getString(R.string.checkJoinRoom)
				try {
					refRoomData.document(roomID).get().addOnSuccessListener { dataSnapshot ->
						try {
							if (dataSnapshot.data != null) {
								val playersJoined = dataSnapshot.get("PJ").toString().toInt()
								val nPlayers = dataSnapshot.get("n").toString().toInt()
								val queTemp = checkPlayerJoiningQue(dataSnapshot)
								val que = if (queTemp != 0) queTemp else playersJoined + 1

								if (que > nPlayers) {
									SoundManager.getInstance().playErrorSound()
									speak("Sorry. Room is full")
									if (vibrateStatus) vibrationStart()
									maskAllLoading.visibility = View.GONE
									roomIDInputLayout.helperText = null
									errorJoinRoomID = true
									roomIDInputLayout.error = "Room is Full"
									roomIDInput.text?.clear()
								} else {
									if (vibrateStatus) vibrationStart()
									if (soundStatus) SoundManager.getInstance().playSuccessSound()
									maskAllLoading.visibility = View.VISIBLE
									loadingText.text = getString(R.string.joiningRoom)
									logFirebaseEvent("create_join_room_screen", 1, "join_$nPlayers")
									if (queTemp == 0) refRoomData.document(roomID)  // new player joined - not previously joined
										.set(hashMapOf("p$que" to userName, "PJ" to que, "p${que}h" to uid), SetOptions.merge())
										.addOnSuccessListener {
											startCJRS(roomID = roomID, playerJoining = que, nPlayers = nPlayers)
										}
									else {
										speak("Room is already joined", speed = 1.07f)
										startCJRS(roomID = roomID, playerJoining = que, nPlayers = nPlayers)
									}
								}
							} else {
								SoundManager.getInstance().playErrorSound()
								speak("Sorry,No room found. Please check room ID", speed = 1.0f)
								if (vibrateStatus) vibrationStart()
								maskAllLoading.visibility = View.GONE
								roomIDInputLayout.helperText = null
								errorJoinRoomID = true
								roomIDInputLayout.error = "No Room found"
								roomIDInput.text?.clear()
							}
						}catch(successError:java.lang.Exception){
							toastCenter("Failed to join room. Try again")
							if (vibrateStatus) vibrationStart()
							maskAllLoading.visibility = View.GONE
							roomIDInputLayout.helperText = null
							errorJoinRoomID = true
							roomIDInputLayout.error = "Failed to join room. Try again"
							roomIDInput.text?.clear()
						}
					}.addOnFailureListener {
						maskAllLoading.visibility = View.GONE
						toastCenter("Failed to join room. Try again")
					}
				}catch(e:Exception){
					toastCenter("Failed to join room. Try again")
					if (vibrateStatus) vibrationStart()
					maskAllLoading.visibility = View.GONE
					roomIDInputLayout.helperText = null
					errorJoinRoomID = true
					roomIDInputLayout.error = "Failed to join room. Try again"
					roomIDInput.text?.clear()
				}
			}
		} else {
			SoundManager.getInstance().playErrorSound()
			speak("Please enter room ID")
			vibrationStart()
			errorJoinRoomID = false
			roomIDInputLayout.error = null
			roomIDInputLayout.helperText = getString(R.string.joinHelper)
		}
	}

	private fun startCJRS(roomID: String, playerJoining: Int, nPlayers: Int) {
		val userStatsDaily = ArrayList(listOf(nGamesPlayedDaily, nGamesWonDaily, nGamesBidDaily))
		val userStatsTotal = ArrayList(listOf(nGamesPlayed + nGamesPlayedBot, nGamesWon + nGamesWonBot, nGamesBid + nGamesBidBot))

		startActivity(Intent(this, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
			.apply { putExtra("selfName", userName) }.apply { putExtra("from", "p$playerJoining") }
			.apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("photoURL", photoURL) }
			.apply { putExtra("totalCoins", totalCoins) }
			.apply { putExtra("totalDailyCoins", totalDailyCoins) }
			.apply { putExtra("offline", false) }
			.putIntegerArrayListExtra("userStats", ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid)))
			.putIntegerArrayListExtra("userStatsTotal", userStatsTotal)
			.putIntegerArrayListExtra("userStatsDaily", userStatsDaily)
			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
		overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
		finishAndRemoveTask() //		Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)

	}

	private fun checkPlayerJoiningQue(dataSnapshot: DocumentSnapshot?): Int {		//        val playersJoined = dataSnapshot?.get("PJ").toString().toInt()
		val nPlayers = dataSnapshot?.get("n").toString().toInt()
		val p1 = dataSnapshot?.data?.get("p1h").toString()
		val p2 = dataSnapshot?.data?.get("p2h").toString()
		val p3 = dataSnapshot?.data?.get("p3h").toString()
		val p4 = dataSnapshot?.data?.get("p4h").toString()
		val p5 = if (nPlayers == 7) dataSnapshot?.data?.get("p5h").toString() else ""
		val p6 = if (nPlayers == 7) dataSnapshot?.data?.get("p6h").toString() else ""
		val p7 = if (nPlayers == 7) dataSnapshot?.data?.get("p7h").toString() else ""
		return when (uid) {
			p1 -> 1
			p2 -> 2
			p3 -> 3
			p4 -> 4
			p5 -> 5
			p6 -> 6
			p7 -> 7
			else -> 0
		}
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

	private fun joinRoomWindowOpen(showAds: Boolean = true) { //		if (mInterstitialAdMP.isReady && !premiumStatus && !dailyRewardStatus && !onceAdWatched && !newUser && showAds) {
		//			mInterstitialAdMP.show()
		//		}
		Handler(Looper.getMainLooper()).postDelayed({
			closeJoinRoom.visibility = View.VISIBLE
		}, 270)
		joinRoomFrame.visibility = View.VISIBLE
		anim(joinRoomFrameTemp, R.anim.zoomin_center)
		roomIDInputLayout.error = null
		roomIDInputLayout.helperText = getString(R.string.joinHelper)
		errorJoinRoomID = false
		joinRoomWindowStatus = true
	}

	fun joinRoomWindowExit(view: View) {
		closeJoinRoom.visibility = View.GONE
		anim(joinRoomFrameTemp, R.anim.zoomout_center)
		joinRoomWindowStatus = false
		Handler(Looper.getMainLooper()).postDelayed({
			joinRoomFrame.visibility = View.GONE
		}, 250)
	}

	fun developerCredits(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance()
			.playUpdateSound()  //Pass username and current activity alias to be able to come back with same info
		startActivity(Intent(this, DeveloperCredits::class.java).putExtra("uid", uid)
			.putExtra("soundStatus", soundStatus))
		overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
	}

	fun anim(view: View, anim: Int) {
		view.startAnimation(AnimationUtils.loadAnimation(this, anim))
	}

	fun music(view: View) {
		musicStatus = musicSwitch.isChecked
		if (musicStatus) {
			soundBkgd.start()
			settMusicIcon.setImageResource(R.drawable.music)
		} else {
			soundBkgd.pause()
			settMusicIcon.setImageResource(R.drawable.nomusic)
		}
		editor.putBoolean("musicStatus", musicStatus) // write username to preference file
		editor.apply()
	}

	fun sound(view: View) {
		soundStatus = soundSwitch.isChecked
		if (soundStatus) {
			SoundManager.getInstance().playUpdateSound()
			settSoundIcon.setImageResource(R.drawable.sound_on_png)
		} else settSoundIcon.setImageResource(R.drawable.sound_off_png)
		editor.putBoolean("soundStatus", soundStatus) // write username to preference file
		editor.apply()

	}

	fun vibrate(view: View) {
		vibrateStatus = vibrateSwitch.isChecked
		if (vibrateStatus) {
			vibrationStart(1000)
			settVibrateIcon.setImageResource(R.drawable.vibrateon)
		} else settVibrateIcon.setImageResource(R.drawable.vibrateoff)
		editor.putBoolean("vibrateStatus", vibrateStatus) // write username to preference file
		editor.apply()
	}

	private fun initializeSpeechEngine() {
		textToSpeech = TextToSpeech(applicationContext) { status ->
			if (status == TextToSpeech.SUCCESS) {
				val result = textToSpeech.setLanguage(Locale.ENGLISH)
				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) { //					toastCenter("Missing Language data - Text to speech")
				}
			}
		}
	}

	private fun speak(speechText: String, pitch: Float = 0.95f, speed: Float = 1.05f) {
		if (soundStatus && this::textToSpeech.isInitialized) {
			textToSpeech.setPitch(pitch)
			textToSpeech.setSpeechRate(speed)
			textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, bundleOf(Pair(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.15f)), null)
		}
	}

	@SuppressLint("NewApi")
	private fun vibrationStart(duration: Long = 150) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
		} else {
			vibrator.vibrate(duration)
		}
	}

	private fun enterText(view: View = View(this)) {
		roomIDInput.setOnEditorActionListener { _, actionId, _ ->
			return@setOnEditorActionListener when (actionId) {
				EditorInfo.IME_ACTION_SEND -> {
					joinRoomButtonClicked(joinRoomButton)
					false
				}
				else -> false
			}
		}
	}

	fun toastCenter(message: String) {
		Snackbar.make(backgroundmhs, message, Snackbar.LENGTH_LONG).setAction("Dismiss") {}
			.setActionTextColor(getColor(R.color.borderblue)).show()
	}

	fun trainingStart(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press)) //		if(mInterstitialAdMP.isReady) mInterstitialAdMP.show()
		System.gc()
		trainAccess = false
		if (trainAccess) {
			maskAllLoading.visibility = View.VISIBLE
			loadingText.text = getString(R.string.startTrain)
			if (soundStatus) SoundManager.getInstance().playUpdateSound()
			startActivity(Intent(this, TrainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
			overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
			finishAndRemoveTask() //			Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
		} else {
			toastCenter("Sorry You don't have access")
		}
	}

	private fun ranking() {
		snackbar = Snackbar.make(backgroundmhs, "Click on player to see more details", 2500)
			.setAction("Dismiss") {}
		snackbar.setActionTextColor(getColor(R.color.borderblue))
		snackbar.show()
		snackbar.view.setOnClickListener { snackbar.dismiss() }
		rankStats.visibility = View.VISIBLE
		anim(rankStats, R.anim.slide_left_activity)
		rankWindowStatus = true
	}

	@SuppressLint("SimpleDateFormat")
	private fun initTabLayoutAdapter() {
		viewPager2.adapter = RankStateAdapter(this, tabs = 2)  // attach adapter to viewpager2 view
		viewPager2.offscreenPageLimit = 2  // set limit of pages to keep in memory 2
		viewPager2.isSaveFromParentEnabled = false
		tabLayoutMediator = TabLayoutMediator(tabRank, viewPager2) { tab: TabLayout.Tab, i: Int ->
			when (i) {
				1 -> {
					tab.text = "All Time"
					tab.icon = ContextCompat.getDrawable(this, R.drawable.alltime)
				}
				0 -> {
					tab.text = Html.fromHtml("Today ${todayClass.date}<sup>${rankExtFromInt(todayClass.date)}</sup> ${todayClass.month}", HtmlCompat.FROM_HTML_MODE_LEGACY)
					tab.icon = ContextCompat.getDrawable(this, R.drawable.daily24)
				}
				else -> {
					tab.text = "Weekly"
					tab.icon = ContextCompat.getDrawable(this, R.drawable.weekly)
				}
			}
		}
		tabLayoutMediator.attach()
		viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				when (position) {
					0 -> {
						handleDailyView()
					}
					1 -> {
						handleAllTimeView()
					}
					else -> {
					}
				}
			}
		}
		viewPager2.registerOnPageChangeCallback(viewPagerCallback)
	}

	private fun handleAllTimeView() {
		if (!rankAllTimeSetupDone) {
			logFirebaseEvent("Ranking", 1, "Requested")
			rankAllTimeSetupDone = true
			rankProgress.visibility = View.VISIBLE
			loadingProgressBar.visibility = View.VISIBLE
			recyclerView = (supportFragmentManager.findFragmentByTag("f1")?.view as View).rankGallery
			layoutManager = LinearLayoutManager(this)
			recyclerView.layoutManager = layoutManager
			adapter = ListViewAdapter(this, userArrayList)
			recyclerView.adapter = adapter
			refUsersData.whereGreaterThanOrEqualTo("sc", 5000)
				.orderBy("sc", Query.Direction.DESCENDING).limit(limitFetchOnceAT).get()
				.addOnSuccessListener { querySnapshot ->
					if (querySnapshot.isEmpty) maxItemsFetchAT = -1
					querySnapAT = querySnapshot
					rankProgress.visibility = View.GONE
					userArrayList.addAll(createUserArrayFromSnapshot(querySnapAT, filterLastSeen = true, lsdLimit = lastSeenLimitAT))
					adapter.notifyDataSetChanged()
					loadingProgressBar.visibility = View.GONE
					anim(recyclerView, R.anim.slide_down_in)
					recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
						override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
							super.onScrollStateChanged(recyclerView, newState)
							if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) isScrollingAllTime = true
						}

						override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
							super.onScrolled(recyclerView, dx, dy)
							val itemCount = layoutManager.itemCount
							if (itemCount < maxItemsFetchAT && isScrollingAllTime && layoutManager.findLastCompletelyVisibleItemPosition() == itemCount - 1) {
								isScrollingAllTime = false
								loadingProgressBar.visibility = View.VISIBLE
								refUsersData.whereGreaterThanOrEqualTo("sc", 5000)
									.orderBy("sc", Query.Direction.DESCENDING)
									.limit(limitFetchOnceAT).startAfter(querySnapAT.last()).get()
									.addOnSuccessListener { querySnapshot ->
										if (querySnapshot.isEmpty) maxItemsFetchAT = -1
										else {
											querySnapAT = querySnapshot
											userArrayList.addAll(createUserArrayFromSnapshot(querySnapAT, filterLastSeen = true, lsdLimit = lastSeenLimitAT))
											adapter.notifyDataSetChanged()
											loadingProgressBar.visibility = View.GONE
										}
									}
							}
						}
					}
					recyclerView.addOnScrollListener(recyclerViewScrollListener)
				}
		}
	}

	private fun handleDailyView() {
		if (!rankDailySetupDone) {
			loadingProgressBar.visibility = View.VISIBLE
			logFirebaseEvent("Ranking", 1, "Requested")
			rankDailySetupDone = true
			rankProgress.visibility = View.VISIBLE
			recyclerView1 = (supportFragmentManager.findFragmentByTag("f0")?.view as View).rankGallery
			layoutManager1 = LinearLayoutManager(this)
			recyclerView1.layoutManager = layoutManager1
			adapter1 = ListViewAdapter(this, userArrayList1, type = 0)
			recyclerView1.adapter = adapter1

			refUsersData.whereEqualTo("LSD", today).orderBy("scd", Query.Direction.DESCENDING)
				.limit(limitFetchOnce).get().addOnSuccessListener { querySnapshot ->
					if (querySnapshot.isEmpty) maxItemsFetchDaily = -1
					loadingProgressBar.visibility = View.GONE
					rankProgress.visibility = View.GONE
					querySnapDaily = querySnapshot
					userArrayList1.addAll(createUserArrayFromSnapshot(querySnapDaily))
					adapter1.notifyDataSetChanged()
					anim(recyclerView1, R.anim.slide_down_in)
					recyclerView1ScrollListener = object : RecyclerView.OnScrollListener() {
						override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
							super.onScrollStateChanged(recyclerView, newState)
							if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) isScrollingDaily = true
						}

						override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
							super.onScrolled(recyclerView, dx, dy)
							val itemCount = layoutManager1.itemCount
							if (itemCount < maxItemsFetchDaily && isScrollingDaily && layoutManager1.findLastCompletelyVisibleItemPosition() == itemCount - 1) {
								isScrollingDaily = false
								loadingProgressBar.visibility = View.VISIBLE
								refUsersData.whereEqualTo("LSD", today)
									.orderBy("scd", Query.Direction.DESCENDING)
									.limit(limitFetchOnce).startAfter(querySnapDaily.last()).get()
									.addOnSuccessListener { querySnapshot ->
										if (querySnapshot.isEmpty) {
											maxItemsFetchDaily = -1
										} else {
											querySnapDaily = querySnapshot
											userArrayList1.addAll(createUserArrayFromSnapshot(querySnapDaily))
											adapter1.notifyDataSetChanged()
											loadingProgressBar.visibility = View.GONE
										}
									}
							} else loadingProgressBar.visibility = View.GONE
						}
					}
					recyclerView1.addOnScrollListener(recyclerView1ScrollListener)
				}

		}
	}

	fun closeRankWindow(view: View) {		//        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (this::snackbar.isInitialized) snackbar.dismiss()
		rankWindowStatus = false
		anim(rankStats, R.anim.slide_right_activity)
		Handler(Looper.getMainLooper()).postDelayed({
			rankStats.clearAnimation()
			rankStats.visibility = View.GONE
		}, 190)
	}

	private fun inviteFriends() {
		try {
			val message = "Hey, Let's play 3 of Spades (Kaali ki Teeggi) online. \n\nClick below \n${				//            getString(R.string.playStoreLink)
				getString(R.string.inviteLink)
			}\n"
			val intentInvite = Intent()
			intentInvite.action = Intent.ACTION_SEND
			intentInvite.type = "text/plain"
			intentInvite.putExtra(Intent.EXTRA_TITLE, "Invite friends")
			intentInvite.putExtra(Intent.EXTRA_TEXT, message)
			startActivity(Intent.createChooser(intentInvite, "Invite friends via "))
			logFirebaseEvent(FirebaseAnalytics.Event.SHARE, 1, "invite")
		} catch (me: Exception) {
		}
	}

	private fun howToPlay() {
		try {
			intentBuilder.build().launchUrl(this, Uri.parse(howtoPlayUrl))
			logFirebaseEvent("HowToPlay", 1, "open")
		} catch (me: Exception) {
			try {
				val intent = Intent(Intent.ACTION_VIEW).apply {
					data = Uri.parse(howtoPlayUrl)
				}
				startActivity(intent)
			} catch (me: Exception) {
			}
		}
	}

	private fun buildCustomTabIntent() {
		intentBuilder = CustomTabsIntent.Builder()
		intentBuilder.setStartAnimations(this, R.anim.slide_left_activity, R.anim.slide_left_activity)
		intentBuilder.setExitAnimations(this, R.anim.slide_right_activity, R.anim.slide_right_activity)
		intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.icon_yellow))
		intentBuilder.addDefaultShareMenuItem()
	}

	fun openRatingWindow(view: View) {
		if (view.tag == "rate") {
			view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
			if (soundStatus) SoundManager.getInstance().playUpdateSound()
		}
		rateUsLayout.visibility = View.VISIBLE
		anim(rateUsLayoutFrame, R.anim.zoomin_center)
		anim(rateUsIcon1, R.anim.anim_scale_appeal)
		ratingWindowOpenStatus = true
	}

	fun closeRatingWindow(view: View) {		//        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		rateUsIcon1.clearAnimation()
		rateUsLayout.visibility = View.GONE
		ratingWindowOpenStatus = false
	}

	fun askLaterRating(view: View) { // request for rating after x days from today if choose ask later
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		logFirebaseEvent("rate_us_vector.xml", 1, "rate_later")
		closeRatingWindow(View(this))
		ratingRequestDate = getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
			.toInt(), requestRatingAfterDays)
		editor.putInt("ratingRequestDate", ratingRequestDate)
		editor.apply()
		if (backButtonPressedStatus) moveTaskToBack(true)
		backButtonPressedStatus = false
	}

	fun rateUs(view: View) { // once clicked never ask to rate again
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		closeRatingWindow(View(this))
		if (!rated) inAppReview()  //openPlayStore() //- disable in app review for a while
		else openPlayStore()
		if (view.tag == "good") logFirebaseEvent("rate_us_vector.xml", 1, "rate_good")
		else if (view.tag == "bad") logFirebaseEvent("rate_us_vector.xml", 1, "rate_bad")
	}

	private fun openPlayStore() {
		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse("https://play.google.com/store/apps/details?id=com.kaalikiteeggi.three_of_spades")
			setPackage("com.android.vending")
		}
		startActivity(intent)
	}

	private fun checkRatingRequest(minGames: Int = 4): Boolean {
		return !rated && ((nGamesPlayed + nGamesPlayedBot) > minGames || SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
			.toInt() >= ratingRequestDate)
	}

	override fun onBackPressed() { //minimize the app and avoid destroying the activity
		if (soundStatus) SoundManager.getInstance().playUpdateSound()
		if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsML.progress == 0f || createRoomWindowStatus) && ratingWindowOpenStatus && backButtonPressedStatus) {			//            moveTaskToBack(true)
			super.onBackPressed()
			closeRatingWindow(View(this))
			backButtonPressedStatus = false
			this.enterPictureInPictureMode()
		} else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsML.progress == 0f || createRoomWindowStatus || ratingWindowOpenStatus) && checkRatingRequest()) {
			backButtonPressedStatus = true
			openRatingWindow(View(this))
		} else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsML.progress == 0f || createRoomWindowStatus || ratingWindowOpenStatus)) {
			moveTaskToBack(true)			//            super.onBackPressed()
		} // none should be visible
		else if (ratingWindowOpenStatus) {
			closeRatingWindow(View(this))
		}
		if (rankWindowStatus) closeRankWindow(View(this))
		if (joinRoomWindowStatus) joinRoomWindowExit(View(this))
		if (createRoomWindowStatus) createRoomWindowExit(View(this))
		if (settingsWindowStatus) closeSettingsWindow(View(this))
		if (playerStatsML.progress == 0f) playerStatsML.transitionToEnd() //openClosePlayerStats(View(this))
		//           super.onBackPressed()
	}

	fun signOut(view: View) {
		view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
		if (vibrateStatus) vibrationStart()
		mAuth.signOut()
		LoginManager.getInstance().logOut()
		startActivity(Intent(this, StartScreen::class.java).apply { putExtra("background", background) }
			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
		overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
		finishAndRemoveTask()
	}

	private fun deleteAllRoomData(roomID: String) {
		refRoomData.document(roomID + "_chat").delete()
		refRoomData.document(roomID).delete()
		Firebase.database.getReference("GameData/$roomID").removeValue()
	}

	override fun onPause() {
		super.onPause()
		if (musicStatus) soundBkgd.pause()
	}

	override fun onResume() {
		super.onResume()
		if (musicStatus && this::soundBkgd.isInitialized) soundBkgd.start()
	}

	override fun onDestroy() {
		if (this::soundBkgd.isInitialized) soundBkgd.release()
		if (this::billingClient.isInitialized) billingClient.endConnection()

		if (this::viewPagerCallback.isInitialized) viewPager2.unregisterOnPageChangeCallback(viewPagerCallback)
		if (this::recyclerViewScrollListener.isInitialized) recyclerView.removeOnScrollListener(recyclerViewScrollListener)
		if (this::recyclerView1ScrollListener.isInitialized) recyclerView1.removeOnScrollListener(recyclerView1ScrollListener)
		if (this::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
		viewPager2.adapter = null

		try {
			textToSpeech.stop()
			textToSpeech.shutdown()
		} catch (me: java.lang.Exception) {
		}
		addViewMHS.destroy() // destroy MoPub banner add view
		if (this::mInterstitialAdMP.isInitialized) {
			mInterstitialAdMP.interstitialAdListener = null
			mInterstitialAdMP.destroy()
		} // Destroy MoPub Interstitial add
		MoPubRewardedAds.setRewardedAdListener(null)
		MoPub.onDestroy(this)
		super.onDestroy()
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
						editor.putBoolean("rated", rated)
						editor.apply()
						logFirebaseEvent("rate_us_vector.xml", 1, "rated")
						refUsersData.document(uid)
							.set(hashMapOf("rated" to 1, "ratedD" to today), SetOptions.merge())
					} else {
						openPlayStore()
					}
				}
			} else {
				logFirebaseEvent("rate_us_vector.xml", 1, "ratedFailure")
				openPlayStore()
			}
		}
	}

}