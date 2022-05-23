@file:Suppress("UNUSED_PARAMETER", "DEPRECATION", "NAME_SHADOWING")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
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
import com.android.billingclient.api.BillingClient.ProductType
import com.applovin.sdk.AppLovinPrivacySettings
import com.facebook.login.LoginManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
import com.kaalikiteeggi.three_of_spades.databinding.ActivityMainHomeScreenBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class MainHomeScreen : AppCompatActivity() {
    //    region Initialization
    private var requestRatingAfterDays = 1 //dummy
    private var ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()

    private var background = 4
    private lateinit var soundBackground: MediaPlayer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var intentBuilder: CustomTabsIntent.Builder
    private lateinit var snackbar: Snackbar
    private val howtoPlayUrl = "http://sites.google.com/view/kaali-ki-teeggi/how-to-play"
    private var rewardStatus = false
    private var rewardAmount = 0
    private var offlineRoomCreate = true
    private val today = CreateUser().todayDate
    private val todayClass = GetFormattedDate(dateInput = today)
    private var errorJoinRoomID = false
    private var backButtonPressedStatus = false
    private var joinRoomWindowStatus = false
    private var dailyRewardWindow = false
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
    private lateinit var adapter: UserInfoRanking
    private lateinit var layoutManager: LinearLayoutManager
    private var userArrayList = ArrayList<UserBasicInfo>()

    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView1ScrollListener: RecyclerView.OnScrollListener
    private lateinit var alertDialog: AlertDialog

    private lateinit var adapter1: UserInfoRanking
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

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
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
    private var dailyRewardList = listOf(500, 1000, 2000, 3000, 4000, 5000, 5000)
    private var dailyRewardAmount = 0
    private var clickedDailyReward = false
    private var claimedToday = false
    private lateinit var binding: ActivityMainHomeScreenBinding

    // endregion
    @SuppressLint("ShowToast", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable.bug_icon) //default: bug image
            .apply()
//        if (!resources.getBoolean(R.bool.portrait_only)) {
//			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//        }
        binding = ActivityMainHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_main_home_screen)
        background = Random.nextInt(0, 6)
        checkJoinRoom(intent)
        playerStatsGridDisplay(transition = false)
        mainIconGridDisplay()
        newUser = intent.getBooleanExtra("newUser", true)
        returnFromGameScreen = intent.getBooleanExtra("returnFromGameScreen", false)
        refRoomData = Firebase.firestore.collection(getString(R.string.pathRoom))
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.uid.toString()
        soundBackground = MediaPlayer.create(this, R.raw.music)
        soundBackground.isLooping = true
        soundBackground.setVolume(0.05F, 0.05F)
        getSharedPrefs()
        initializeAds()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        Handler(Looper.getMainLooper()).post {
            getUserData()
            checkIfOnlineGameAllowed()
            initTabLayoutAdapter()
            initializeSpeechEngine()
            enterText() // press enter to join room
            buildCustomTabIntent()
//			setupBillingClient() // memory leak issue so don't initialize everytime except when requested
            if (BuildConfig.DEBUG) binding.trainingButton.visibility = View.VISIBLE
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        logFirebaseEvent("MainHomeScreen",  "open")
    }

    private fun mainIconGridDisplay() {
        binding.mainIconGridView.layoutManager = GridLayoutManager(this, 2)
        binding.mainIconGridView.adapter = MHSIconAdapter(this, setDataListMHS()) { output ->
            actionGridItemClick(position = output) // receive position output here
        }

        //		mainIconGridView.visibility = View.VISIBLE
        // Attach Text watcher to room ID input - For resetting error hint on re-entering roomID
        binding.roomIDInput.doOnTextChanged { _, _, _, _ ->
            if (binding.roomIDInputLayout.error != null && !errorJoinRoomID) {
                binding.roomIDInputLayout.error = null
                binding.roomIDInputLayout.helperText = getString(R.string.joinHelper)
            } else if (errorJoinRoomID) {
                errorJoinRoomID = false
            }
        }
    }

    private fun actionGridItemClick(position: Int) {
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        when (position) {
            0 -> createRoomWindowOpen()
            1 -> joinRoomWindowOpen()
            2 -> ranking()
            3 -> howToPlay()
            4 -> inviteFriends()
            5 -> openSettingsWindow()
        }
    }

    private fun setDataListMHS(): ArrayList<GenericItemDescription> {
        val arrayList = ArrayList<GenericItemDescription>()
        arrayList.add(GenericItemDescription(R.drawable.joystick, getString(R.string.play)))
        arrayList.add(GenericItemDescription(R.drawable.joinroom, getString(R.string.joinRoom)))
        arrayList.add(GenericItemDescription(R.drawable.ranking, getString(R.string.ranking)))
        arrayList.add(GenericItemDescription(R.drawable.howtoplay, getString(R.string.howtoplay)))
        arrayList.add(GenericItemDescription(R.drawable.invite, getString(R.string.invite)))
        arrayList.add(GenericItemDescription(R.drawable.settings, getString(R.string.settings)))
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
                    roomID = intent.data?.query?.split("link=${getString(R.string.scheme)}://${getString(R.string.hostJoinRoom)}/")?.get(1)?.split("&")?.get(0).toString()
                    joinRoomPending = true
                    if (userDataFetched) autoJoinRoom()
                } else if (intent.data?.query?.contains("link=${getString(R.string.scheme)}://${getString(R.string.hostJoinRoomOld)}/")!!) {
                    if (soundStatus) SoundManager.instance?.playErrorSound()
                    if (vibrateStatus) vibrationStart()
                    toastCenter("The link belongs to old version. Please ask your friend to Update their app")
                }
            }
            if (intent.data?.host == getString(R.string.hostJoinRoom)) { // re-routed from browser/chrome
                roomID = intent.data?.lastPathSegment.toString()
                joinRoomPending = true
                if (userDataFetched) autoJoinRoom()
            } else if (intent.data?.host == getString(R.string.hostJoinRoomOld)) { // re-routed from browser/chrome
                if (soundStatus) SoundManager.instance?.playErrorSound()
                if (vibrateStatus) vibrationStart()
                toastCenter("The link belongs to old version. Please ask your friend to Update their app")
            }
        }
    }

    private fun autoJoinRoom() {
        if (joinRoomPending && userDataFetched) {
            openClosePlayerStats(binding.backgroundmhs)
            joinRoomWindowOpen(showAds = false)
            binding.roomIDInput.setText(roomID)
            joinRoomButtonClicked(binding.joinRoomButton)
        }
    }

    override fun onStart() {
        super.onStart()
        if (musicStatus && this::soundBackground.isInitialized) soundBackground.start()
    }

    private fun logFirebaseEvent(event: String, key: String) {
        val params = Bundle()
        params.putInt(key, 1)
        firebaseAnalytics.logEvent(event, params)
    }

    private fun dailyRewardWindowDisplay() {
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        val listDailyRewardItem = setDataListDR()
        val imageAdapter = DailyRewardGridAdapter(listDailyRewardItem, min(listDailyRewardItem.size, consecutiveDay))
        binding.dailyRewardGrid.adapter = imageAdapter
        binding.dailyRewardGridLayout.visibility = View.VISIBLE
        dailyRewardWindow = true
        anim(binding.dailyRewardGridLayout, R.anim.zoomin_center)
    }

    private fun setDataListDR(): ArrayList<GenericItemDescription> {
        val arrayList = ArrayList<GenericItemDescription>()
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 1 \n${dailyRewardList[0]} coins"))
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 2 \n${dailyRewardList[1]} coins"))
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 3 \n${dailyRewardList[2]} coins"))
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 4 \n${dailyRewardList[3]} coins"))
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 5 \n${dailyRewardList[4]} coins"))
        arrayList.add(GenericItemDescription(R.drawable.coin_trans_1, "Day 5+ \n${dailyRewardList[5]} coins"))
        return arrayList
    }

    private fun playerStatsGridDisplay(transition: Boolean = true) {
        binding.playerStatsGrid.layoutManager = GridLayoutManager(this, 3)
        binding.playerStatsGrid.adapter = PlayerStatsGridAdapter(setDataListPS())
        if(transition) Handler(Looper.getMainLooper()).postDelayed({binding.playerStatsML.transitionToEnd()},1500L)

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
        if (soundStatus && view.tag != null) SoundManager.instance?.playUpdateSound()
        binding.dailyRewardGridLayout.visibility = View.GONE
        dailyRewardWindow = false
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getUserData() {
        fireStoreRef = Firebase.firestore.collection("Users").document(uid)

        val user = mAuth.currentUser
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()
        if (user != null) {
            FirebaseCrashlytics.getInstance().setUserId(uid)
            userName = user.displayName!!.split(" ")[0]
            binding.usernameCardMHS.text = userName
            if (intent.getBooleanExtra("newUser", false)) { //check if user has joined room or created one and display Toast
                editor.putBoolean("rated", rated)
                editor.putInt("joinDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt())
                editor.putInt("ratingRequestDate", getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt(), requestRatingAfterDays))
                editor.putBoolean("premium", false) // make premium false
                editor.apply()
            } // check if new user
        } else {
            mAuth.signOut()
            startActivity(Intent(this, StartScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finishAndRemoveTask()
        }
        refUsersData.document(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.data != null) {
                try {
                    premiumStatus = dataSnapshot.get("pr").toString().toInt() == 1
                    if (premiumStatus) {
                        binding.removeAds.visibility = View.GONE
                        binding.bannerMHS.visibility = View.GONE
                    }
                    if (soundStatus) SoundManager.instance?.playSuccessSound()
                    binding.maskAllLoading.visibility = View.GONE
                    photoURL = dataSnapshot.get("ph").toString()
                    Picasso.get().load(photoURL).resize(400, 400).into(binding.profilePic)                    //                    callConfetti() // don't call confetti here - call only when coins are earned
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
                    binding.userScoreMHS.setText(String.format("%,d", totalCoins), false)
                    nGamesPlayed = dataSnapshot.get("p").toString().toInt()
                    nGamesWon = dataSnapshot.get("w").toString().toInt()
                    nGamesBid = dataSnapshot.get("b").toString().toInt()
                    if (!dataSnapshot.contains("zFCM_token")) {
                        createTokenFC()
                    } else {
                        createTokenFC(check = true, oldToken = dataSnapshot.get("zFCM_token").toString())
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
//                    Handler(Looper.getMainLooper()).postDelayed({playerStatsGridDisplay()},700L)
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
                    fireStoreRef.set(hashMapOf("LSDT" to SimpleDateFormat("HH:mm:ss z").format(Date()), "lang" to applicationContext.resources.configuration.locale.displayLanguage, "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())                    //					checkAccessToTrain()
                } catch (exception: java.lang.Exception) {
                    mAuth.signOut()
                    startActivity(Intent(this, StartScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finishAndRemoveTask()
                }
            } else {
                mAuth.signOut()
                startActivity(Intent(this, StartScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finishAndRemoveTask()
            }
        }.addOnFailureListener {
            mAuth.signOut()
            startActivity(Intent(this, StartScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finishAndRemoveTask()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createTokenFC(check: Boolean = false, oldToken: String = "") {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            } else {
                val token = task.result // Get new FCM registration token
                if (!check) {                    //                    Toast.makeText(baseContext, "token created to firebase", Toast.LENGTH_SHORT).show()
                    fireStoreRef.set(hashMapOf("zFCM_token" to token.toString(), "zFCM_dt" to SimpleDateFormat("yyyyMMdd HH:mm:ss z").format(Date())), SetOptions.merge())
                } else if (oldToken == token.toString()) {                    //                    Toast.makeText(baseContext, "token matched- nothing to do", Toast.LENGTH_SHORT).show()
                } else {                    //                    Toast.makeText(baseContext, "token changed ", Toast.LENGTH_SHORT).show()
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
                binding.removeAds.visibility = View.GONE
            } else binding.removeAds.visibility = View.VISIBLE
        }
        if (sharedPreferences.contains("rated")) {
            rated = sharedPreferences.getBoolean("rated", false)
        } else {
            editor.putBoolean("rated", rated)
            editor.apply()
        }
        if (!sharedPreferences.contains("ratingRequestDate")) {
            ratingRequestDate = getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt(), requestRatingAfterDays)
            editor.putInt("ratingRequestDate", ratingRequestDate)
            editor.apply()
        } else {
            ratingRequestDate = sharedPreferences.getInt("ratingRequestDate", 0)
        }
        if (!sharedPreferences.contains("joinDate")) {
            editor.putInt("joinDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt())
            editor.apply()
        }
        if (sharedPreferences.contains("musicStatus")) {
            musicStatus = sharedPreferences.getBoolean("musicStatus", true)
            binding.musicSwitch.isChecked = musicStatus
            if (musicStatus) {
                soundBackground.start()
            }
        } else {
            musicStatus = true
            binding.musicSwitch.isChecked = musicStatus
            if (this::soundBackground.isInitialized) soundBackground.start()
            editor.putBoolean("musicStatus", musicStatus) // write username to preference file
            editor.apply()
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
            binding.soundSwitch.isChecked = soundStatus
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
            binding.vibrateSwitch.isChecked = vibrateStatus
        }
        if (sharedPreferences.contains("Room") && !joinRoomPending) {
            val roomID = sharedPreferences.getString("Room", "").toString()
            if (roomID.isNotEmpty()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    deleteAllRoomData(roomID)
                }, 0)
            }
            editor.remove("Room").apply()
        }
    }

    fun removeAdsOrPremium(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (premiumStatus) openClosePlayerStats(View(this))
        else {
            if (soundStatus) SoundManager.instance?.playUpdateSound()
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
                        toastCenter("Successful purchase", length = Snackbar.LENGTH_INDEFINITE)
                        if (soundStatus) SoundManager.instance?.playSuccessSound()
                        lifecycleScope.launch { acknowledgePurchase(purchase) }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        if (soundStatus) SoundManager.instance?.playUpdateSound()
                        toastCenter("Your Payment is processing \nWe will update you when finished", length = Snackbar.LENGTH_INDEFINITE)
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                SoundManager.instance?.playErrorSound()
                toastCenter("Payment Cancelled")
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                SoundManager.instance?.playErrorSound()
                toastCenter("Already processing pending purchase")
            } else {
                SoundManager.instance?.playErrorSound()
                toastCenter("Payment Failed. Please try again", length = Snackbar.LENGTH_INDEFINITE)
            }
        }
        billingClient = BillingClient.newBuilder(applicationContext).enablePendingPurchases().setListener(purchaseListener).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                checkPendingPurchases()
                querySkuDetailsRequest()
            }
            override fun onBillingServiceDisconnected() {
            //				if(billingClientTry<3) setupBillingClient() // Test if required to try again
            }
        })
    }

    private fun checkPendingPurchases() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()) {
                _, purchases ->
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    lifecycleScope.launch { acknowledgePurchase(purchase) }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    toastCenter("You have a pending payment \nPlease check your Google Play Account")
                }
            }
        }
//        val purchases = billingClient.queryPurchases("inapp").purchasesList
    }

    private fun querySkuDetailsRequest() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams.newBuilder().setSkusList(listOf("remove_ads")).setType(BillingClient.SkuType.INAPP).build()
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
    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params) {}
        if (purchase.skus[0] == "remove_ads") {
            premiumStatus = true
            if (soundStatus) SoundManager.instance?.playSuccessSound()
            toastCenter("Congratulations!! Your Payment is approved \n You won't see Ads now")
            refUsersData.document(uid).set(hashMapOf("pr" to 1), SetOptions.merge())
            Firebase.firestore.collection("PremiumUser").document(uid).set(hashMapOf("id" to mAuth.currentUser?.email, "d" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()))
            consumePurchase(purchase)
            binding.removeAds.visibility = View.GONE
            binding.bannerMHS.visibility = View.GONE
            editor.putBoolean("premium", true) // write username to preference file
            editor.apply()
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
//        billingClient.consumeAsync(params) { _: BillingResult, _: String -> }
        billingClient.consumePurchase(params)
    }

    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    private fun initializeAds() {
        AppLovinPrivacySettings.setHasUserConsent(true, this)
        MobileAds.initialize(this) {
            Log.d("Inter", "onInitializationComplete")
            binding.bannerMHS.loadAd(AdRequest.Builder().build())  //load ad to banner view Admob
            binding.bannerMHS.visibility = View.VISIBLE
            loadRewardAd()
            loadInterstitialAd()
        }
        val requestBuilder = RequestConfiguration.Builder().setTestDeviceIds(listOf(getString(R.string.testDeviceID))).build()
        MobileAds.setRequestConfiguration(requestBuilder)
    }

    private fun loadInterstitialAd() {
        loadInterAdTry += 1 // try 5 times
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.inter_admob), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("Inter", "onAdFailedToLoad")
                if (loadInterAdTry <= 2) loadInterstitialAd()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("Inter", "onAdLoaded")
                loadInterAdTry = 0
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.d("Inter", "onAdFailedToShowFullScreenContent")
                    mInterstitialAd = null
                    loadInterstitialAd()
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d("Inter", "onAdDismissedFullScreenContent")
                    mInterstitialAd = null
                    addPoints()
                }

                override fun onAdShowedFullScreenContent() {}
                override fun onAdImpression() {}
            }
            mInterstitialAd!!.setImmersiveMode(true)
            mInterstitialAd!!.show(this)
        } else {
            Log.d("Inter", "InterstitialAd is Null")
            loadInterstitialAd()
        }
    }

    private fun loadRewardAd() {
        loadRewardedAdTry += 1
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, getString(R.string.reward_admob), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d("Inter", "Rewarded failed to load")
                mRewardedAd = null
                if (loadRewardedAdTry <= 3) loadRewardAd()
            }
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d("Inter", "Rewarded ad loaded")
                mRewardedAd = rewardedAd
                loadRewardedAdTry = 1
                binding.watchVideo.setImageResource(R.drawable.watch_ad_1000)
                binding.watchVideo.visibility = View.VISIBLE
            }
        })
    }

    private fun showRewardedVideoAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {                        // Called when ad is shown.
                    Log.d("Inter", "mRewardedAd Ad was shown.")
                    if (clickedDailyReward) binding.dailyRewardGridLayout.visibility = View.GONE
                    binding.watchVideo.clearAnimation()
                    binding.watchVideo.visibility = View.GONE
                    if (musicStatus) soundBackground.pause()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    Log.d("Inter", "mRewardedAd Ad failed to show.")
                    binding.watchVideo.clearAnimation()
                    binding.watchVideo.visibility = View.GONE
                    loadRewardAd()
                }

                override fun onAdDismissedFullScreenContent() {// Called when ad is dismissed. Set the ad reference to null so you don't show the ad a second time.
                    Log.d("Inter", "mRewardedAd Ad was dismissed.")
                    mRewardedAd = null
                    if (musicStatus) soundBackground.start()
                    if (rewardStatus) {
                        addPoints()
                        rewardStatus = false
                    } else {
                        loadRewardAd()
                        toastCenter("Sorry $userName No coins added")
                    }
                }
            }
            mRewardedAd?.show(this) {
                Log.d("Inter", "Earned Reward")
                rewardStatus = true // successfully received reward
                loadRewardAd()
            }
        } else {
            binding.watchVideo.clearAnimation()
            binding.watchVideo.visibility = View.GONE
            loadRewardAd()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun addPoints() {
        if (soundStatus) SoundManager.instance?.playZipSound()
        rewardAmount = if (clickedDailyReward) dailyRewardAmount
        else 1000
        showDialogue(rewardAmount.toString())
        if (clickedDailyReward) logFirebaseEvent("daily_rewards", "coins$rewardAmount")
        else logFirebaseEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY,  "coins$rewardAmount")

        totalCoins += rewardAmount  // reward with daily reward amount for watching video
        userBasicInfo.score = totalCoins  // reward with daily reward amount for watching video
        binding.watchVideoCoin.text = rewardAmount.toString()
        binding.watchVideoCoin.visibility = View.VISIBLE
        if (soundStatus) SoundManager.instance?.playCardCollectSound()
        anim(binding.watchVideoCoin, R.anim.slide_500_coins)
        if (!clickedDailyReward) {
            refUsersData.document(uid).set(hashMapOf("sc" to totalCoins), SetOptions.merge())
        } else {
            refUsersData.document(uid).set(hashMapOf("sc" to totalCoins, "LSD" to today, "LSDT" to SimpleDateFormat("HH:mm:ss z").format(Date()), "nDRC" to consecutiveDay, "claim" to 1), SetOptions.merge())
        }
        clickedDailyReward = false
        binding.watchVideoCoin.visibility = View.GONE
        loadRewardAd()
        Handler(Looper.getMainLooper()).postDelayed({
            if (vibrateStatus) vibrationStart()
            binding.userScoreMHS.setText(String.format("%,d", totalCoins), true)
//            watchVideoCoin.visibility = View.GONE
//            loadRewardAd()
        }, 1250)
    }

    private fun showDialogue(coins: String) {
        if (!this::alertDialog.isInitialized) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Congratulations !!! ${Emoji().celebrate}${Emoji().celebrate}${Emoji().celebrate}")
            builder.setMessage("\nYou received $coins coins\n")
            builder.setPositiveButton("Ok") { _: DialogInterface, _: Int ->
            }
            alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shine_player_stats))
        }
        alertDialog.show()
    }

    fun claimDailyReward(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (vibrateStatus) vibrationStart()
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        clickedDailyReward = true
        if (premiumStatus) {
            binding.dailyRewardGridLayout.visibility = View.GONE
            addPoints()
        } else if (mRewardedAd != null && !newUser) { // check Rewarded Ads loaded or not
            showRewardedVideoAd()
            binding.watchVideo.clearAnimation()
            binding.watchVideo.visibility = View.GONE
        } else if (mInterstitialAd != null && !newUser) { // check Interstitial Ads loaded or not
            showInterstitialAd()
            binding.dailyRewardGridLayout.visibility = View.GONE
            binding.watchVideo.clearAnimation()
            binding.watchVideo.visibility = View.GONE
            loadRewardAd()
        } else {
            binding.dailyRewardGridLayout.visibility = View.GONE
            addPoints()
            binding.watchVideo.clearAnimation()
            binding.watchVideo.visibility = View.GONE
            loadRewardAd()
            if (!premiumStatus) loadInterstitialAd()
        }
    }

    fun callToShowRewardedVideoAd(view: View) {
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        showRewardedVideoAd()
    }

    fun openClosePlayerStats(view: View) {
        if (view.id != R.id.backgroundmhs && view.id != R.id.playerStats) {
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        }
        if (binding.playerStatsML.progress == 1f) {
            binding.playerStatsML.transitionToStart()
        } else binding.playerStatsML.transitionToEnd()
    }

    private fun openSettingsWindow() {
        settingsWindowStatus = true
        binding.settingsLayout.visibility = View.VISIBLE
//		Handler(Looper.getMainLooper()).postDelayed({
        binding.closeSettings.visibility = View.VISIBLE
//		}, 220)
//		anim(settingsLayoutTemp, R.anim.zoomin_center)
    }

    fun closeSettingsWindow(view: View) {
        settingsWindowStatus = false
//		anim(settingsLayoutTemp, R.anim.zoomout_center)
        binding.closeSettings.visibility = View.GONE
        binding.settingsLayout.visibility = View.GONE
        binding.closeSettings.clearAnimation()
    }

    private fun checkIfOnlineGameAllowed() {
        Firebase.database.reference.child("Check").addListenerForSingleValueEvent(object : ValueEventListener {
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
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        offlineRoomCreate = view.tag.toString().toInt() == 0

        nPlayers = when (view.tag.toString().toInt()) {
            0 -> 4
            4 -> 4
            10 -> 4
            else -> 7
        }
        if(view.tag.toString().toInt() != 10) createRoom() else createRoom(dummyReal = true)
    }


    @SuppressLint("SetTextI18n")
    private fun createRoom(dummyReal: Boolean = false) {
        binding.maskAllLoading.visibility = View.VISIBLE
        binding.loadingText.text = getString(R.string.creatingRoom)
        if (!offlineGameAllowed && offlineRoomCreate) {
            toastCenter(offlineGameAllowedM)
            if (soundStatus) SoundManager.instance?.playErrorSound()
            if (vibrateStatus) vibrationStart()
            binding.loadingText.text = offlineGameAllowedM
            Handler(Looper.getMainLooper()).postDelayed({
                binding.maskAllLoading.visibility = View.GONE
            }, 4000)
        } else if (offlineGameAllowed && offlineRoomCreate) Handler(Looper.getMainLooper()).postDelayed({ startNextActivity() }, 1200)
        else if (!onlineGameAllowed) {
            if (soundStatus) SoundManager.instance?.playErrorSound()
            if (vibrateStatus) vibrationStart()
            binding.loadingText.text = onlineGameAllowedM
            Handler(Looper.getMainLooper()).postDelayed({
                binding.maskAllLoading.visibility = View.GONE
            }, 3500)
        } else {
            val allowedChars = ('A'..'H') + ('J'..'N') + ('P'..'Z') + ('2'..'9')  // 1, I , O and 0 skipped as they look same
            val roomID = if (BuildConfig.DEBUG) "AABB" else (1..4).map { allowedChars.random() }.joinToString("")
            if (nPlayers == 7) {
                if (!dummyReal) createRoomWithID(roomID, CreateRoomData(userBasicInfo).data7)
                else createRoomWithID(roomID, CreateRoomData(userBasicInfo).dummyData7)
            } else if (nPlayers == 4 && !offlineRoomCreate) {
                if (!dummyReal) createRoomWithID(roomID, CreateRoomData(userBasicInfo).data4)
                else createRoomWithID(roomID, CreateRoomData(userBasicInfo).dummyData4)
            }
        }
    }

    private fun createRoomWithID(roomID: String, roomData: Any) {
        refRoomData.document(roomID).set(roomData).addOnFailureListener {
            binding.maskAllLoading.visibility = View.GONE
            toastCenter("Failed to create room \nPlease try again or later")
        }.addOnSuccessListener {
            startNextActivity(roomID)
        }
    }

    private fun startNextActivity(roomID: String = "ABCD") {
        if (soundStatus) SoundManager.instance?.playSuccessSound()
        if (!offlineRoomCreate) {
            editor.putString("Room", roomID) // write room ID in storage - to delete later
            editor.apply()
            logFirebaseEvent("create_join_room_screen",  "create_$nPlayers")
        } else {
            logFirebaseEvent("create_join_room_screen",  "create_offline_$nPlayers")
        }
        val userStatsDaily = ArrayList(listOf(nGamesPlayedDaily, nGamesWonDaily, nGamesBidDaily))
        val userStatsTotal = ArrayList(listOf(nGamesPlayed + nGamesPlayedBot, nGamesWon + nGamesWonBot, nGamesBid + nGamesBidBot))
        val userStats = if (!offlineRoomCreate) ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid))
        else ArrayList(listOf(nGamesPlayedBot, nGamesWonBot, nGamesBidBot))

        startActivity(Intent(this, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }.apply { putExtra("selfName", userName) }.apply { putExtra("photoURL", photoURL) }.apply { putExtra("totalCoins", totalCoins) }.apply { putExtra("totalDailyCoins", totalDailyCoins) }.apply { putExtra("from", "p1") }.apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("offline", offlineRoomCreate) }.putIntegerArrayListExtra("userStats", userStats).putIntegerArrayListExtra("userStatsTotal", userStatsTotal).putIntegerArrayListExtra("userStatsDaily", userStatsDaily).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
        finishAndRemoveTask() //		finish()
        //		Handler(Looper.getMainLooper()).postDelayed({
        //			finish()
        //		}, 500)
    }

    private fun createRoomWindowOpen() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.closeCreateRoom.visibility = View.VISIBLE
        }, 270)
        binding.createRoomFrame.visibility = View.VISIBLE
        anim(binding.createRoomFrameTemp, R.anim.slide_down_player_stats) //		anim(closeCreateRoom, R.anim.zoomin_center)
        createRoomWindowStatus = true
    }

    fun createRoomWindowExit(view: View) {
        binding.closeCreateRoom.clearAnimation()
        binding.createSingle.clearAnimation()
        binding.createDouble.clearAnimation()
        binding.closeCreateRoom.visibility = View.GONE
        binding.maskAllLoading.visibility = View.GONE
        anim(binding.createRoomFrameTemp, R.anim.slide_up_player_stats)
        createRoomWindowStatus = false
        Handler(Looper.getMainLooper()).postDelayed({
            binding.createRoomFrame.visibility = View.GONE
        }, 390)
    }

    fun joinRoomButtonClicked(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        if (vibrateStatus) vibrationStart()
        val roomID = binding.roomIDInput.text.toString() //read text field
        if (roomID.isNotEmpty()) {
            if (!onlineGameAllowed && !joinRoomPending) {
                toastCenter(onlineGameAllowedM)
            } else {
                hideKeyboard()
                binding.maskAllLoading.visibility = View.VISIBLE
                binding.loadingText.text = getString(R.string.checkJoinRoom)
                try {
                    refRoomData.document(roomID).get().addOnSuccessListener { dataSnapshot ->
                        try {
                            if (dataSnapshot.data != null) {
                                val playersJoined = dataSnapshot.get("PJ").toString().toInt()
                                val nPlayers = dataSnapshot.get("n").toString().toInt()
                                val queTemp = checkPlayerJoiningQue(dataSnapshot)
                                val que = if (queTemp != 0) queTemp else playersJoined + 1

                                if (que > nPlayers) {
                                    SoundManager.instance?.playErrorSound()
                                    speak("Sorry. Room is full")
                                    if (vibrateStatus) vibrationStart()
                                    binding.maskAllLoading.visibility = View.GONE
                                    binding.roomIDInputLayout.helperText = null
                                    errorJoinRoomID = true
                                    binding.roomIDInputLayout.error = "Room is Full"
                                    binding.roomIDInput.text?.clear()
                                } else {
                                    if (vibrateStatus) vibrationStart()
                                    if (soundStatus) SoundManager.instance?.playSuccessSound()
                                    binding.maskAllLoading.visibility = View.VISIBLE
                                    binding.loadingText.text = getString(R.string.joiningRoom)
                                    logFirebaseEvent("create_join_room_screen",  "join_$nPlayers")
                                    if (queTemp == 0) refRoomData.document(roomID)  // new player joined - not previously joined
                                        .set(hashMapOf("p$que" to userName, "PJ" to que, "p${que}h" to uid), SetOptions.merge()).addOnSuccessListener {
                                            startCJRS(roomID = roomID, playerJoining = que, nPlayers = nPlayers)
                                        }
                                    else {
                                        speak("Room is already joined", speed = 1.07f)
                                        startCJRS(roomID = roomID, playerJoining = que, nPlayers = nPlayers)
                                    }
                                }
                            } else {
                                SoundManager.instance?.playErrorSound()
                                speak("Sorry,No room found. Please check room ID", speed = 1.0f)
                                if (vibrateStatus) vibrationStart()
                                binding.maskAllLoading.visibility = View.GONE
                                binding.roomIDInputLayout.helperText = null
                                errorJoinRoomID = true
                                binding.roomIDInputLayout.error = "No Room found"
                                binding.roomIDInput.text?.clear()
                            }
                        } catch (successError: java.lang.Exception) {
                            toastCenter("Failed to join room. Try again")
                            if (vibrateStatus) vibrationStart()
                            binding.maskAllLoading.visibility = View.GONE
                            binding.roomIDInputLayout.helperText = null
                            errorJoinRoomID = true
                            binding.roomIDInputLayout.error = "Failed to join room. Try again"
                            binding.roomIDInput.text?.clear()
                        }
                    }.addOnFailureListener {
                        binding.maskAllLoading.visibility = View.GONE
                        toastCenter("Failed to join room. Try again")
                    }
                } catch (e: Exception) {
                    toastCenter("Failed to join room. Try again")
                    if (vibrateStatus) vibrationStart()
                    binding.maskAllLoading.visibility = View.GONE
                    binding.roomIDInputLayout.helperText = null
                    errorJoinRoomID = true
                    binding.roomIDInputLayout.error = "Failed to join room. Try again"
                    binding.roomIDInput.text?.clear()
                }
            }
        } else {
            SoundManager.instance?.playErrorSound()
            speak("Please enter room ID")
            vibrationStart()
            errorJoinRoomID = false
            binding.roomIDInputLayout.error = null
            binding.roomIDInputLayout.helperText = getString(R.string.joinHelper)
        }
    }

    private fun startCJRS(roomID: String, playerJoining: Int, nPlayers: Int) {
        val userStatsDaily = ArrayList(listOf(nGamesPlayedDaily, nGamesWonDaily, nGamesBidDaily))
        val userStatsTotal = ArrayList(listOf(nGamesPlayed + nGamesPlayedBot, nGamesWon + nGamesWonBot, nGamesBid + nGamesBidBot))

        startActivity(Intent(this, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }.apply { putExtra("selfName", userName) }.apply { putExtra("from", "p$playerJoining") }.apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("photoURL", photoURL) }.apply { putExtra("totalCoins", totalCoins) }.apply { putExtra("totalDailyCoins", totalDailyCoins) }.apply { putExtra("offline", false) }.putIntegerArrayListExtra("userStats", ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid))).putIntegerArrayListExtra("userStatsTotal", userStatsTotal).putIntegerArrayListExtra("userStatsDaily", userStatsDaily).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
        finishAndRemoveTask() //		Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)

    }

    private fun checkPlayerJoiningQue(dataSnapshot: DocumentSnapshot?): Int {        //        val playersJoined = dataSnapshot?.get("PJ").toString().toInt()
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

    private fun joinRoomWindowOpen(showAds: Boolean = true) {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.closeJoinRoom.visibility = View.VISIBLE
        }, 270)
        binding.joinRoomFrame.visibility = View.VISIBLE
        anim(binding.joinRoomFrameTemp, R.anim.zoomin_center)
        binding.roomIDInputLayout.error = null
        binding.roomIDInputLayout.helperText = getString(R.string.joinHelper)
        errorJoinRoomID = false
        joinRoomWindowStatus = true
    }

    fun joinRoomWindowExit(view: View) {
        binding.closeJoinRoom.visibility = View.GONE
        anim(binding.joinRoomFrameTemp, R.anim.zoomout_center)
        joinRoomWindowStatus = false
        Handler(Looper.getMainLooper()).postDelayed({
            binding.joinRoomFrame.visibility = View.GONE
        }, 250)
    }

    fun developerCredits(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        //Pass username and current activity alias to be able to come back with same info
        startActivity(Intent(this, DeveloperCredits::class.java).putExtra("uid", uid).putExtra("soundStatus", soundStatus))
        overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
    }

    fun anim(view: View, anim: Int) {
        view.startAnimation(AnimationUtils.loadAnimation(this, anim))
    }

    fun music(view: View) {
        musicStatus = binding.musicSwitch.isChecked
        if (musicStatus) {
            soundBackground.start()
        } else {
            soundBackground.pause()
        }
        editor.putBoolean("musicStatus", musicStatus) // write username to preference file
        editor.apply()
    }

    fun sound(view: View) {
        soundStatus = binding.soundSwitch.isChecked
        if (soundStatus) {
            SoundManager.instance?.playUpdateSound()
        }
        editor.putBoolean("soundStatus", soundStatus) // write username to preference file
        editor.apply()

    }

    fun vibrate(view: View) {
        vibrateStatus = binding.vibrateSwitch.isChecked
        if (vibrateStatus) {
            vibrationStart(1000)
        }
        editor.putBoolean("vibrateStatus", vibrateStatus) // write username to preference file
        editor.apply()
    }

    private fun initializeSpeechEngine() {
        textToSpeech = TextToSpeech(applicationContext) {
//			if (status == TextToSpeech.SUCCESS) {
//				val result = textToSpeech.setLanguage(Locale.ENGLISH)
////				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//			// 					toastCenter("Missing Language data - Text to speech")
////				}
//			}
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
            if (this::vibrator.isInitialized) vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            if (this::vibrator.isInitialized) vibrator.vibrate(duration)
        }
    }

    private fun enterText(view: View = View(this)) {
        binding.roomIDInput.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    joinRoomButtonClicked(binding.joinRoomButton)
                    false
                }
                else -> false
            }
        }
    }

    fun toastCenter(message: String, length: Int= Snackbar.LENGTH_LONG) {
        Snackbar.make(binding.backgroundmhs, message, length).setAction("Dismiss") {}.setActionTextColor(getColor(R.color.borderblue)).show()
    }

    fun trainingStart(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        trainAccess = false
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://kaaliteeri.page.link/?link=http://jo.in1/AABB&apn=com.kaalikiteeggi.three_of_spades&amv=70&st=Join%20my%20room%20ID%20%3D%3E%20AABB&si=https://tinyurl.com/3ofspade")))

        @Suppress("KotlinConstantConditions") if (trainAccess) {
            binding.maskAllLoading.visibility = View.VISIBLE
            binding.loadingText.text = getString(R.string.startTrain)
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            startActivity(Intent(this, TrainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            finishAndRemoveTask()
        }
    }

    private fun ranking() {
        snackbar = Snackbar.make(binding.backgroundmhs, "Click on player to see more details", 2500).setAction("Dismiss") {}
        snackbar.setActionTextColor(getColor(R.color.borderblue))
        snackbar.show()
        snackbar.view.setOnClickListener { snackbar.dismiss() }
        binding.rankStats.visibility = View.VISIBLE
        anim(binding.rankStats, R.anim.slide_left_activity)
        rankWindowStatus = true
    }

    @SuppressLint("SimpleDateFormat")
    private fun initTabLayoutAdapter() {
        binding.viewPager2.adapter = RankStateAdapter(this, tabs = 2)  // attach adapter to viewpager2 view
        binding.viewPager2.offscreenPageLimit = 2  // set limit of pages to keep in memory 2
        binding.viewPager2.isSaveFromParentEnabled = false
        tabLayoutMediator = TabLayoutMediator(binding.tabRank, binding.viewPager2) { tab: TabLayout.Tab, i: Int ->
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
                }
            }
        }
        binding.viewPager2.registerOnPageChangeCallback(viewPagerCallback)
    }

    private fun handleAllTimeView() {
        if (!rankAllTimeSetupDone) {
            logFirebaseEvent("Ranking",  "Requested")
            rankAllTimeSetupDone = true
            binding.rankProgress.visibility = View.VISIBLE
            binding.loadingProgressBar.visibility = View.VISIBLE
            recyclerView = (supportFragmentManager.findFragmentByTag("f1")?.view as View).findViewById(R.id.rankGallery)
            layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager
            adapter = UserInfoRanking(this, userArrayList)
            recyclerView.adapter = adapter
            refUsersData.whereGreaterThanOrEqualTo("sc", 5000).orderBy("sc", Query.Direction.DESCENDING).limit(limitFetchOnceAT).get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) maxItemsFetchAT = -1
                querySnapAT = querySnapshot
                binding.rankProgress.visibility = View.GONE
                val previousSize = userArrayList.size
                val newItems = createUserArrayFromSnapshot(querySnapAT, filterLastSeen = true, lsdLimit = lastSeenLimitAT, startAt = 0)
                userArrayList.addAll(newItems)
                adapter.notifyItemRangeInserted(previousSize, newItems.size)
                binding.loadingProgressBar.visibility = View.GONE
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
                            binding.loadingProgressBar.visibility = View.VISIBLE
                            refUsersData.whereGreaterThanOrEqualTo("sc", 5000).orderBy("sc", Query.Direction.DESCENDING).limit(limitFetchOnceAT).startAfter(querySnapAT.last()).get().addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) maxItemsFetchAT = -1
                                else {
                                    querySnapAT = querySnapshot
                                    val previousSize = userArrayList.size
                                    val newItems = createUserArrayFromSnapshot(querySnapAT, filterLastSeen = true, lsdLimit = lastSeenLimitAT, startAt = previousSize)
                                    userArrayList.addAll(newItems)
                                    adapter.notifyItemRangeInserted(previousSize, newItems.size)
                                    binding.loadingProgressBar.visibility = View.GONE
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
            binding.loadingProgressBar.visibility = View.VISIBLE
            logFirebaseEvent("Ranking",  "Requested")
            rankDailySetupDone = true
            binding.rankProgress.visibility = View.VISIBLE
            recyclerView1 = (supportFragmentManager.findFragmentByTag("f0")?.view as View).findViewById(R.id.rankGallery)
            layoutManager1 = LinearLayoutManager(this)
            recyclerView1.layoutManager = layoutManager1
            adapter1 = UserInfoRanking(this, userArrayList1, type = 0)
            recyclerView1.adapter = adapter1

            refUsersData.whereEqualTo("LSD", today).orderBy("scd", Query.Direction.DESCENDING).limit(limitFetchOnce).get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) maxItemsFetchDaily = -1
                binding.loadingProgressBar.visibility = View.GONE
                binding.rankProgress.visibility = View.GONE
                querySnapDaily = querySnapshot
                val previousSize = userArrayList1.size
                val newItems = createUserArrayFromSnapshot(querySnapDaily, startAt = 0)
                userArrayList1.addAll(newItems)
                adapter1.notifyItemRangeInserted(previousSize, newItems.size)
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
                            binding.loadingProgressBar.visibility = View.VISIBLE
                            refUsersData.whereEqualTo("LSD", today).orderBy("scd", Query.Direction.DESCENDING).limit(limitFetchOnce).startAfter(querySnapDaily.last()).get().addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    maxItemsFetchDaily = -1
                                } else {
                                    querySnapDaily = querySnapshot
                                    val previousSize = userArrayList1.size
                                    val newItems = createUserArrayFromSnapshot(querySnapDaily, startAt = previousSize)
                                    userArrayList1.addAll(newItems)
                                    adapter1.notifyItemRangeInserted(previousSize, newItems.size)
                                    binding.loadingProgressBar.visibility = View.GONE
                                }
                            }
                        } else binding.loadingProgressBar.visibility = View.GONE
                    }
                }
                recyclerView1.addOnScrollListener(recyclerView1ScrollListener)
            }

        }
    }

    fun closeRankWindow(view: View) {        //        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (this::snackbar.isInitialized) snackbar.dismiss()
        rankWindowStatus = false
        anim(binding.rankStats, R.anim.slide_right_activity)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.rankStats.clearAnimation()
            binding.rankStats.visibility = View.GONE
        }, 190)
    }

    private fun inviteFriends() {
        try {
            val message = "Hey, Let's play 3 of Spades (Kaali ki Teeggi) online. \n\nClick below \n${
                getString(R.string.inviteLink)
            }\n"
            val intentInvite = Intent()
            intentInvite.action = Intent.ACTION_SEND
            intentInvite.type = "text/plain"
            intentInvite.putExtra(Intent.EXTRA_TITLE, "Invite friends")
            intentInvite.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(intentInvite, "Invite friends via "))
            logFirebaseEvent(FirebaseAnalytics.Event.SHARE,  "invite")
        } catch (_: Exception) {
        }
    }

    private fun howToPlay() {
        try {
            intentBuilder.build().launchUrl(this, Uri.parse(howtoPlayUrl))
            logFirebaseEvent("HowToPlay", "open")
        } catch (me: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(howtoPlayUrl)
                }
                startActivity(intent)
            } catch (_: Exception) {
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
            if (soundStatus) SoundManager.instance?.playUpdateSound()
        }
        binding.rateUsLayout.visibility = View.VISIBLE
        anim(binding.rateUsLayoutFrame, R.anim.zoomin_center)
        anim(binding.rateUsIcon1, R.anim.anim_scale_appeal)
        ratingWindowOpenStatus = true
    }

    fun closeRatingWindow(view: View) {        //        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        binding.rateUsIcon1.clearAnimation()
        binding.rateUsLayout.visibility = View.GONE
        ratingWindowOpenStatus = false
    }

    fun askLaterRating(view: View) { // request for rating after x days from today if choose ask later
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        logFirebaseEvent("rate_us_vector.xml",  "rate_later")
        closeRatingWindow(View(this))
        ratingRequestDate = getChangedDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt(), requestRatingAfterDays)
        editor.putInt("ratingRequestDate", ratingRequestDate)
        editor.apply()
        if (backButtonPressedStatus) moveTaskToBack(true)
        backButtonPressedStatus = false
    }

    fun rateUs(view: View) { // once clicked never ask to rate again
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        closeRatingWindow(View(this))
        if (!rated) inAppReview()  //openPlayStore() //- disable in app review for a while
        else openPlayStore()
        if (view.tag == "good") logFirebaseEvent("rate_us_vector.xml",  "rate_good")
        else if (view.tag == "bad") logFirebaseEvent("rate_us_vector.xml",  "rate_bad")
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

    private fun checkRatingRequest(minGames: Int = 4): Boolean {
        return !rated && ((nGamesPlayed + nGamesPlayedBot) > minGames || SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt() >= ratingRequestDate)
    }

    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || binding.playerStatsML.progress == 0f || dailyRewardWindow  || createRoomWindowStatus) && ratingWindowOpenStatus && backButtonPressedStatus) {            //            moveTaskToBack(true)
            super.onBackPressed()
            closeRatingWindow(View(this))
            backButtonPressedStatus = false
        } else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || binding.playerStatsML.progress == 0f || createRoomWindowStatus || dailyRewardWindow  || ratingWindowOpenStatus) && checkRatingRequest()) {
            backButtonPressedStatus = true
            openRatingWindow(View(this))
        } else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || binding.playerStatsML.progress == 0f || createRoomWindowStatus || dailyRewardWindow || ratingWindowOpenStatus)) {
            moveTaskToBack(true)            //            super.onBackPressed()
        } // none should be visible
        else if (ratingWindowOpenStatus) {
            closeRatingWindow(View(this))
        }
        if (rankWindowStatus) closeRankWindow(View(this))
        if (joinRoomWindowStatus) joinRoomWindowExit(View(this))
        if (createRoomWindowStatus) createRoomWindowExit(View(this))
        if (settingsWindowStatus) closeSettingsWindow(View(this))
        if (binding.playerStatsML.progress == 0f) binding.playerStatsML.transitionToEnd() //openClosePlayerStats(View(this))
        if(dailyRewardWindow) closeDailyRewardWindowDisplay(View(this))
        //           super.onBackPressed()
    }

    fun signOut(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (vibrateStatus) vibrationStart()
        mAuth.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(this, StartScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
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
        if (musicStatus) soundBackground.pause()
    }

    override fun onResume() {
        super.onResume()
        if (musicStatus && this::soundBackground.isInitialized) soundBackground.start()
    }

    override fun onDestroy() {
        if (this::soundBackground.isInitialized) soundBackground.release()
        if (this::billingClient.isInitialized) {
            billingClient.endConnection()
        }
        if (this::viewPagerCallback.isInitialized) binding.viewPager2.unregisterOnPageChangeCallback(viewPagerCallback)
        if (this::recyclerViewScrollListener.isInitialized) recyclerView.removeOnScrollListener(recyclerViewScrollListener)
        if (this::recyclerView1ScrollListener.isInitialized) recyclerView1.removeOnScrollListener(recyclerView1ScrollListener)
        if (this::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
        binding.viewPager2.adapter = null
        try {
            mInterstitialAd!!.fullScreenContentCallback = null
            mInterstitialAd = null
        } catch (_: java.lang.Exception) {
        }

        try {
            mRewardedAd!!.fullScreenContentCallback = null
            mRewardedAd = null
        } catch (_: java.lang.Exception) {
        }

        try {
            textToSpeech.stop()
            textToSpeech.shutdown()
        } catch (_: java.lang.Exception) {
        }
        binding.bannerMHS.destroy()
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
                        editor.putBoolean("rated", true)
                        editor.apply()
                        logFirebaseEvent("rate_us",  "rated")
                        refUsersData.document(uid).set(hashMapOf("rated" to 1, "ratedD" to today), SetOptions.merge())
                    } else {
                        openPlayStore()
                    }
                }
            } else {
                logFirebaseEvent("rate_us_vector.xml",  "ratedFailure")
                openPlayStore()
            }
        }
    }

}