@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.android.billingclient.api.*
import com.facebook.login.LoginManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main_home_screen.*
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.user_rank.view.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class MainHomeScreen : AppCompatActivity(), PurchasesUpdatedListener {
    //    region Initialization
    private var requestRatingAfterDays = 2 //dummy
    private var ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        .toInt()

    private var background = 4
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundBkgd: MediaPlayer
    private lateinit var soundCollectCards: MediaPlayer
    private lateinit var soundZip: MediaPlayer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var intentBuilder: CustomTabsIntent.Builder
    private lateinit var intentInvite: Intent
    private val howtoPlayUrl = "https://sites.google.com/view/kaali-ki-teeggi/how-to-play"
    private var rewardStatus = false
    private var rewardAmount = 0
    private var createRoomStatus = false
    private var offlineRoomCreate = true
    private var onceAdWatched = false

    private var errorJoinRoomID = false
    private var backButtonPressedStatus = false
    private var joinRoomWindowStatus = false
    private var ratingWindowOpenStatus = false
    private var createRoomWindowStatus = false
    private var rankWindowStatus = false
    private var rankFetchedStatus = false
    private var settingsWindowStatus = false
    private var playerStatsWindowStatus = false
    private var trainAccess = false
    private var onlineGameAllowed = false

    private lateinit var toast: Toast
    private var refUsersData = Firebase.firestore.collection("Users")
    private var refRoomData = Firebase.firestore.collection("Rooms")

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var rewardedAd: RewardedAd
    private lateinit var billingClient: BillingClient
    private lateinit var mAuth: FirebaseAuth
    private var uid = ""
    private lateinit var fireStoreRef: DocumentReference
    private lateinit var userName: String
    private lateinit var photoURL: String
    private var musicStatus = false
    private var soundStatus = true
    private var vibrateStatus = true
    private var premiumStatus = false
    private var newUser = true
    private var rated = false
    private val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()
    private var consecutiveDay = 1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var vibrator: Vibrator
    private var totalCoins = 0
    private var nGamesPlayed = 0
    private var nGamesWon = 0
    private var nGamesBid = 0
    private var nGamesPlayedBot = 0
    private var nGamesWonBot = 0
    private var nGamesBidBot = 0
    private var nPlayers = 0

    private val emojiCoins = String(Character.toChars(0x1F4B0))
    private val emojiGamePlayed = String(Character.toChars(0x1F3AE))
    private val emojiTrophy = String(Character.toChars(0x1F3C6))
    private val emojiScore = String(Character.toChars(0x1F3AF))
    private val emojiMoney = String(Character.toChars(0x1F4B0))

    private var loadRewardedAdTry = 0
    private var countRewardWatch = 2
    private lateinit var rewardedAdLoadCallback: RewardedAdLoadCallback
    private var dailyRewardList = listOf(500, 1000, 1500, 2000, 2500, 4000, 5000)
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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        lightMHS.on()
        background = Random.nextInt(0,6)
        when (background) {
            0 -> backgroundmhs.setImageResource(R.drawable.redblackburst)
            1 -> backgroundmhs.setImageResource(R.drawable.blueburst)
            2 -> backgroundmhs.setImageResource(R.drawable.greenyellowburst)
            3 -> backgroundmhs.setImageResource(R.drawable.navyblueburst)
            4 -> backgroundmhs.setImageResource(R.drawable.redorangeburst)
            5 -> backgroundmhs.setImageResource(R.drawable.yellowburst)
        }
        mainIconGridDisplay()
        newUser = intent.getBooleanExtra("newUser", true)
        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        soundBkgd = MediaPlayer.create(applicationContext, R.raw.music)
        soundBkgd.isLooping = true

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.uid.toString()
        getSharedPrefs()
        fireStoreRef = Firebase.firestore.collection("Users").document(uid)
        Handler(Looper.getMainLooper()).post(Runnable {
            soundUpdate = MediaPlayer.create(applicationContext, R.raw.card_played)
            soundError = MediaPlayer.create(applicationContext, R.raw.error)
            soundSuccess = MediaPlayer.create(applicationContext, R.raw.success)
            soundCollectCards = MediaPlayer.create(applicationContext, R.raw.card_collect)
            soundZip = MediaPlayer.create(applicationContext, R.raw.zip)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            SoundManager.initialize(applicationContext)
            getUserData()
//            animateElements()
            initializeAds()
            enterText() // press enter to join room
            checkIfOnlineGameAllowed()
            setupBillingClient()
            versionCode.text = "V: " + packageManager.getPackageInfo(packageName, 0).versionName.toString()
            buildCustomTabIntent()
            createIntentInvite()
        })
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

    }

    override fun onStart() {
        super.onStart()
//        if (rated && ratingWindowOpenStatus) closeRatingWindow(View(applicationContext))
        if (musicStatus && this::soundBkgd.isInitialized) soundBkgd.start()
    }

    private fun logFirebaseEvent(event: String, int: Int, key: String) {
        val params = Bundle()
        params.putInt(key, int)
        firebaseAnalytics.logEvent(event, params)
    }

    fun callConfetti(){
        konfettiMHS.visibility = View.VISIBLE
        createKonfetti(applicationContext, konfettiMHS, duration = coinDur, konType = KonType.Money, burst = coinBurst, speed = coinSpeed, burstCount = coinCount)
    }

    private fun dailyRewardWindowDisplay() {
        soundSuccess.start()
        val gridView = dailyRewardGrid
        val imageAdapter = ImageAdapter(applicationContext, setDataListDR(), min(7, consecutiveDay))
        gridView.adapter = imageAdapter
        dailyRewardGridLayout.visibility = View.VISIBLE
        anim(dailyRewardGridLayout, R.anim.zoomin_center)
    }

    fun closeDailyRewardWindowDisplay(view: View) {
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        dailyRewardGridLayout.visibility = View.GONE
    }

    fun addPoints() {
        callConfetti()
        if (soundStatus) soundZip.start()
        dailyRewardClicked = false
        rewardAmount = dailyRewardAmount
        totalCoins += rewardAmount  // reward with daily reward amount for watching video
        logFirebaseEvent("daily_rewards", rewardAmount, "coins")
        watchVideoCoin.text = rewardAmount.toString()
        watchVideoCoin.visibility = View.VISIBLE
        soundCollectCards.start()
        anim(watchVideoCoin, R.anim.slide_500_coins)
        Handler(Looper.getMainLooper()).postDelayed({
            if (vibrateStatus) vibrationStart()
            userScoreMHS.setText("$ " + String.format("%,d", totalCoins), true)
            watchVideoCoin.visibility = View.GONE
            loadRewardAd()
        }, 1250)
        refUsersData.document(uid)
            .set(hashMapOf("sc" to totalCoins, "LSD" to today, "nDRC" to consecutiveDay, "claim" to 1), SetOptions.merge())
    }

    fun claimDailyReward(view: View) {
        if (vibrateStatus) vibrationStart()
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        dailyRewardClicked = true
        dailyRewardStatus = true
        if (premiumStatus) {
            dailyRewardGridLayout.visibility = View.GONE
            addPoints()
        } else if (rewardedAd.isLoaded && !newUser) {
            val activityContext: Activity = this
            val adCallback = object : RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    dailyRewardGridLayout.visibility = View.GONE
                    watchVideo.clearAnimation()
                    watchVideo.visibility = View.GONE
                    if (musicStatus) soundBkgd.pause()
                }
                override fun onRewardedAdClosed() {
                    if (musicStatus) soundBkgd.start()
                    dailyRewardClicked = false
                    if (rewardStatus) {
                        rewardStatus = false
                        callConfetti()
                        if (soundStatus) soundZip.start()
                        watchVideoCoin.text = rewardAmount.toString()
                        watchVideoCoin.visibility = View.VISIBLE
                        soundCollectCards.start()
                        anim(watchVideoCoin, R.anim.slide_500_coins)
                        Handler(Looper.getMainLooper()).postDelayed({
                            userScoreMHS.setText("$ " + String.format("%,d", 78000), true)
                            watchVideoCoin.visibility = View.GONE
                            loadRewardAd()
                        }, 1250)
                    } else {
                        loadRewardAd()
                        toastCenter("Sorry $userName, No coins added")
                    }
                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    dailyRewardClicked = false
                    rewardStatus = true
                    rewardAmount = dailyRewardAmount
                    totalCoins += rewardAmount  // reward with daily reward amount for watching video
                    refUsersData.document(uid)
                        .set(hashMapOf("sc" to totalCoins, "LSD" to today, "nDRC" to consecutiveDay, "claim" to 1), SetOptions.merge())
                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    dailyRewardClicked = false
                    watchVideo.clearAnimation()
                    watchVideo.visibility = View.GONE
                    loadRewardAd()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        } else if (mInterstitialAd.isLoaded && !newUser) {
            mInterstitialAd.show()
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
            if (premiumStatus) mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    private fun setDataListDR(): ArrayList<DailyRewardItem> {
        val arrayList = ArrayList<DailyRewardItem>()
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 1 \n${dailyRewardList[0]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 2 \n${dailyRewardList[1]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 3 \n${dailyRewardList[2]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 4 \n${dailyRewardList[3]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 5 \n${dailyRewardList[4]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 6 \n${dailyRewardList[5]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 6+ \n${dailyRewardList[6]} coins"))
        return arrayList
    }

    private fun mainIconGridDisplay() {
//        dailyRewardWindowDisplay()
        val gridView = mainIcon
        gridView.adapter = IconAdapter(applicationContext, setDataListMHS())
        gridView.visibility = View.VISIBLE
        gridView.setOnItemClickListener { parent, view, position, id ->
            if (soundStatus) SoundManager.getInstance().playUpdateSound()
            when(position){
                0-> createRoomWindowOpen()
                1-> joinRoomWindowOpen()
                2-> ranking()
                3-> intentBuilder.build().launchUrl(this, Uri.parse(howtoPlayUrl)) // howToPlay()
                4-> inviteFriends()
                5-> openSettingsWindow()
            }
        }
        // Attach Text watcher to room ID input
        roomIDInput.doOnTextChanged { _, _, _, _ ->
            if(roomIDInputLayout.error != null && !errorJoinRoomID) {
                roomIDInputLayout.error = null
                roomIDInputLayout.helperText = getString(R.string.joinHelper)
            }else if(errorJoinRoomID) {
                errorJoinRoomID = false
            }
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

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getUserData() {
        val user = mAuth.currentUser
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()
        if (user != null) {
            userName = user.displayName.toString().split(" ")[0]
            usernameCardMHS.text = userName
            if (intent.getBooleanExtra("newUser", false)) { //check if user has joined room or created one and display Toast
                toastCenter("Hi $userName") // dummy - add tutorial
                editor.putBoolean("rated", rated)
                editor.putInt("joinDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    .toInt())
                editor.putInt("ratingRequestDate", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    .toInt() + requestRatingAfterDays)
                editor.putBoolean("premium", false) // make premium false
                editor.apply()
            } // check if new user
        } else {
            mAuth.signOut()
            startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
            finish()
        }
        refUsersData.document(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.data != null) {
                try {
                    photoURL = dataSnapshot.get("ph").toString()
                    Picasso.get().load(photoURL).resize(400, 400).into(profilePic)
//                    callConfetti() // dont call confetti here - call only when coins are earned
                    totalCoins = dataSnapshot.get("sc").toString().toInt()
                    if(soundStatus) soundZip.start()
                    userScoreMHS.setText("$ " + String.format("%,d", totalCoins), true)
                    maskAllLoading.visibility = View.GONE

                    nGamesPlayed = dataSnapshot.get("p").toString().toInt()
                    nGamesWon = dataSnapshot.get("w").toString().toInt()
                    nGamesBid = dataSnapshot.get("b").toString().toInt()

//                    fireStoreRef.set(hashMapOf(
//                        "phone" to "${Build.MANUFACTURER} ${Build.MODEL}",
//                        "phAPI" to Build.VERSION.SDK_INT,
//                        "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()  ),
//                        SetOptions.merge())
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
                    ngamesPlayedStats.text = (nGamesPlayed + nGamesPlayedBot).toString() + " " + emojiGamePlayed
                    ngamesBidedStats.text = (nGamesBid + nGamesBidBot).toString() + " " + emojiScore
                    ngamesWonStats.text = (nGamesWon + nGamesWonBot).toString() + " " + emojiTrophy

                    premiumStatus = dataSnapshot.get("pr").toString().toInt() == 1
                    if (premiumStatus) {
                        removeAds.visibility = View.GONE
                        addViewMHS.visibility = View.GONE
                    } else {
                       removeAds.setImageResource(R.drawable.no_ads)
                       removeAds.visibility = View.VISIBLE
                       addViewMHS.visibility = View.VISIBLE
                    }

                    val lastSeenDate = dataSnapshot.get("LSD").toString().toInt()
                    consecutiveDay = dataSnapshot.get("nDRC").toString().toInt()
                    dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
                    claimedToday = dataSnapshot.get("claim").toString().toInt() == 1
                    if (today == lastSeenDate + 1) { // if consecutive day login
                        consecutiveDay += 1
                        claimedToday = false
                        dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
                        fireStoreRef.set(hashMapOf("LSD" to today, "nDRC" to consecutiveDay, "claim" to 0,
                            "phone" to "${Build.MANUFACTURER} ${Build.MODEL}",
                            "phAPI" to Build.VERSION.SDK_INT,
                            "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())
                    } else if (today > lastSeenDate + 1) { // if more than 1 day gap , reset counter
                        consecutiveDay = 1
                        claimedToday = false
                        dailyRewardAmount = dailyRewardList[min(consecutiveDay, 7) - 1]
                        fireStoreRef.set(hashMapOf("LSD" to today, "nDRC" to consecutiveDay, "claim" to 0,
                            "phone" to "${Build.MANUFACTURER} ${Build.MODEL}",
                            "phAPI" to Build.VERSION.SDK_INT,
                            "VC" to packageManager.getPackageInfo(packageName, 0).versionName.toString()), SetOptions.merge())
                    }
                    if (!claimedToday) { //if not claimed today
                        Handler(Looper.getMainLooper()).postDelayed({
                            dailyRewardWindowDisplay()
                        }, 1000)
                    }

                    editor.putString("photoURL", photoURL) // write username to preference file
                    editor.apply()
                    editor.putBoolean("premium", premiumStatus) // write username to preference file
                    editor.apply()
//                    checkAccessToTrain()
                } catch (exception: java.lang.Exception) {
                    mAuth.signOut()
                    startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
                    finish()
                }
            } else {
                mAuth.signOut()
                startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
                finish()
            }
        }.addOnFailureListener {
            mAuth.signOut()
            startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
            finish()
        }
    }

    private fun getSharedPrefs() {
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()

//        if (!sharedPreferences.contains("defaultThemeColor1")) {
//            changeBackground("shine_bk")
//            editor.putString("themeColor", "shine_bk") // write username to preference file
//            editor.putString("defaultThemeColor1", "shine_bk") // write username to preference file
//            editor.apply()
//        }else if (sharedPreferences.contains("themeColor")) {
//            changeBackground(sharedPreferences.getString("themeColor", "shine_yellow").toString())
//        }
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
            if (premiumStatus) {
               removeAds.visibility = View.GONE
            } else removeAds.visibility = View.VISIBLE
            //               removeAds.setImageResource(R.drawable.premium)
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
        }
        else {
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
        }
        else {
            musicStatus = true
            musicSwitch.isChecked = musicStatus
            soundBkgd.start()
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
                Handler(Looper.getMainLooper()).postDelayed({ deleteAllRoomData(roomID) }, 0)
                //                deleteAllRoomdata(roomID)
            }
            editor.remove("Room").apply()
        }
    }

    fun removeAdsOrPremium(view: View) {
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        if (premiumStatus) openClosePlayerStats(View(applicationContext))
        else {
            querySkuDetailsRequest()
        }

    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                checkPendingPurchases()
            }

            override fun onBillingServiceDisconnected() {
                //                toastCenter("Billing Service was disconnected")
                setupBillingClient()
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
            setupBillingClient()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) {}
        if (purchase.sku == "remove_ads") {
            premiumStatus = true
            soundSuccess.start()
            toastCenter("Congratulations!! Your Payment is approved \n You won't see Ads now")
            refUsersData.document(uid).set(hashMapOf("pr" to 1), SetOptions.merge())
            Firebase.firestore.collection("PremiumUser").document(uid)
                .set(hashMapOf("id" to mAuth.currentUser?.email, "d" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    .toInt()))
            consumePurchase(purchase)
            //           removeAds.setImageResource(R.drawable.premium)
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

    private fun initializeAds() {
       addViewMHS.loadAd(AdRequest.Builder().build()) // banner add load
        mInterstitialAd = InterstitialAd(this)
        if (!BuildConfig.DEBUG) mInterstitialAd.adUnitId = getString(R.string.interstitialReal) // real interstitial ad
        else mInterstitialAd.adUnitId = getString(R.string.interstitialTest) // test interstitial ad
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // interstitial add load
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                onceAdWatched = true
                if (dailyRewardClicked) {
                    addPoints()
                    dailyRewardClicked = false
                } else if (createRoomStatus) {
                    createRoom()
                    createRoomStatus = false
                }
                if (!premiumStatus) mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        rewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                loadRewardedAdTry = 1
                when (countRewardWatch) {
                    0 -> watchVideo.setImageResource(R.drawable.watch_ad_100)
                    1 -> watchVideo.setImageResource(R.drawable.watch_ad_250)
                    2 -> watchVideo.setImageResource(R.drawable.watch_ad_500)
                    else -> watchVideo.setImageResource(R.drawable.watch_ad_500)
                }
                watchVideo.visibility = View.VISIBLE
                anim(watchVideo, R.anim.anim_scale_infinite)
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                if (loadRewardedAdTry <= 10) loadRewardAd()
            }
        }
        loadRewardAd()
    }

    private fun loadRewardAd() {
        loadRewardedAdTry += 1
        rewardedAd = RewardedAd(applicationContext, getString(R.string.rewarded))
        rewardedAd.loadAd(AdRequest.Builder().build(), rewardedAdLoadCallback)
    }

    fun showRewardedVideoAd(view: View) {
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        if (rewardedAd.isLoaded) {
            val activityContext: Activity = this
            val adCallback = object : RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    watchVideo.clearAnimation()
                    watchVideo.visibility = View.GONE
                    if (musicStatus) soundBkgd.pause()
                }

                override fun onRewardedAdClosed() {
                    if (musicStatus) soundBkgd.start()
                    if (rewardStatus) {
                        if (soundStatus) soundZip.start()
                        callConfetti()
                        rewardStatus = false
                        watchVideoCoin.text = rewardAmount.toString()
                        watchVideoCoin.visibility = View.VISIBLE
                        logFirebaseEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, rewardAmount, "coins")
                        soundCollectCards.start()
                        anim(watchVideoCoin, R.anim.slide_500_coins)
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (vibrateStatus) vibrationStart()
                            userScoreMHS.setText("$ " + String.format("%,d", totalCoins), true)
                            watchVideoCoin.visibility = View.GONE
                            loadRewardAd()
                        }, 1250)
                    } else {
                        loadRewardAd()
                        toastCenter("Sorry $userName No coins added")
                    }
                }

                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    rewardStatus = true
                    rewardAmount = when (countRewardWatch) {
                        0 -> 100
                        1 -> 250
                        2 -> 500
                        else -> 500
                    }
                    countRewardWatch += 1
                    totalCoins += rewardAmount  // reward with 500 coins for watching video
                    refUsersData.document(uid)
                        .set(hashMapOf("sc" to totalCoins), SetOptions.merge())
                }

                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    watchVideo.clearAnimation()
                    watchVideo.visibility = View.GONE
                    loadRewardAd()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        } else {
            watchVideo.clearAnimation()
            watchVideo.visibility = View.GONE
            loadRewardAd()
        }
    }

    fun openClosePlayerStats(view: View) {
        playerStatsWindowStatus = !playerStatsWindowStatus
        if (playerStatsWindowStatus) {
                    if (soundStatus) SoundManager.getInstance().playUpdateSound()
            playerStats.visibility = View.VISIBLE
            anim(playerStats, R.anim.slide_down_player_stats)
//            anim(signoutbutton, R.anim.slide_buttons)
//            anim(signoutbuttonImage, R.anim.anim_scale_infinite)
        } else {
            anim(playerStats, R.anim.slide_up_player_stats)
            playerStats.visibility = View.GONE
//            signoutbutton.clearAnimation()
//            signoutbuttonImage.clearAnimation()
        }
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
        }, 190)
        closeSettings.clearAnimation()
    }

    private fun checkIfOnlineGameAllowed() {
        Firebase.database.reference.child("OnlineGame")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(errorDataLoad: DatabaseError) {}
                override fun onDataChange(data: DataSnapshot) {
                    if (data.exists()) {
                        onlineGameAllowed = data.value.toString().toInt() == 1
                    }
                }
            })
    }

    fun createRoomButtonClicked(view: View) {
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        createRoomStatus = true
        offlineRoomCreate = view.tag.toString().toInt() == 0

        nPlayers = when (view.tag.toString().toInt()) {
            0 -> 4
            4 -> 4
            else -> 7
        }
        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched && !newUser) {
            mInterstitialAd.show()
        } else {
            createRoom()
            createRoomStatus = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createRoom() {
        maskAllLoading.visibility = View.VISIBLE
        loadingText.text = getString(R.string.creatingRoom)
        if (offlineRoomCreate) Handler(Looper.getMainLooper()).postDelayed({ startNextActivity() }, 1300)
        else if (!onlineGameAllowed) {
            soundError.start()
            if(vibrateStatus) vibrationStart()
            loadingText.text = "Try playing OFFLINE\n\n Server is under maintenance right now\n\n It will be back shortly"
            Handler(Looper.getMainLooper()).postDelayed({
                maskAllLoading.visibility = View.GONE
            }, 2000)
        } else {
            val allowedChars = ('A'..'H') + ('J'..'N') + ('P'..'Z') + ('2'..'9')  // 1, I , O and 0 skipped
            val roomID = (1..4).map { allowedChars.random() }.joinToString("")
            if (nPlayers == 7) {
                if (!BuildConfig.DEBUG) createRoomWithID(roomID, CreateRoomData(userName, photoURL, totalCoins).data7)
                else createRoomWithID(roomID, CreateRoomData(userName, photoURL, totalCoins).dummyData7)
            } else if (nPlayers == 4 && !offlineRoomCreate) {
                if (!BuildConfig.DEBUG) createRoomWithID(roomID, CreateRoomData(userName, photoURL, totalCoins).data4)
                else createRoomWithID(roomID, CreateRoomData(userName, photoURL, totalCoins).dummyData4)
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
        if (soundStatus) soundSuccess.start()
        if (!offlineRoomCreate) {
            editor.putString("Room", roomID) // write room ID in storage - to delete later
            editor.apply()
            logFirebaseEvent("create_join_room_screen", nPlayers, "create")
        } else {
            logFirebaseEvent("create_join_room_screen", nPlayers, "create_offline")
        }
        val userStats = if (!offlineRoomCreate) ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid))
        else ArrayList(listOf(nGamesPlayedBot, nGamesWonBot, nGamesBidBot))

        startActivity(Intent(applicationContext, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
            .apply { putExtra("selfName", userName) }.apply { putExtra("photoURL", photoURL) }
            .apply { putExtra("totalCoins", totalCoins) }.apply { putExtra("from", "p1") }
            .apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("offline", offlineRoomCreate) }
            .putIntegerArrayListExtra("userStats", userStats))
        overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)
    }

    private fun createRoomWindowOpen() {
        Handler(Looper.getMainLooper()).postDelayed({
            closeCreateRoom.visibility = View.VISIBLE
        }, 270)
        createRoomFrame.visibility = View.VISIBLE
        anim(createRoomFrameTemp, R.anim.zoomin_center)
        anim(closeCreateRoom, R.anim.zoomin_center)
        createRoomWindowStatus = true
    }

    fun createRoomWindowExit(view: View) {
        closeCreateRoom.clearAnimation()
        createSingle.clearAnimation()
        createDouble.clearAnimation()
        closeCreateRoom.visibility = View.GONE
        anim(createRoomFrameTemp, R.anim.zoomout)
        createRoomWindowStatus = false
        Handler(Looper.getMainLooper()).postDelayed({
            createRoomFrame.visibility = View.GONE
        }, 230)
    }

    fun joinRoomButtonClicked(view: View) {
        if (soundStatus) SoundManager.getInstance().playUpdateSound()
        if (vibrateStatus) vibrationStart()
        val roomID = roomIDInput.text.toString() //read text field
        if (roomID.isNotEmpty()) {
            hideKeyboard()
            maskAllLoading.visibility = View.VISIBLE
            loadingText.text = getString(R.string.checkJoinRoom)

            refRoomData.document(roomID).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.data != null) {
                    val playersJoined = dataSnapshot.get("PJ").toString().toInt()
                    val nPlayers = dataSnapshot.get("n").toString().toInt()
                    if (playersJoined >= nPlayers) {
                        soundError.start()
                        if (vibrateStatus) vibrationStart()
                        maskAllLoading.visibility = View.GONE
//                        roomIDInput.hint = "Room is Full"
                        roomIDInputLayout.helperText = null
                        errorJoinRoomID = true
                        roomIDInputLayout.error = "Room is Full"
                        roomIDInput.text?.clear()
                    } else {
                        if (vibrateStatus) vibrationStart()
                        if (soundStatus) soundSuccess.start()
                        maskAllLoading.visibility = View.VISIBLE
                        loadingText.text = getString(R.string.joiningRoom)
                        logFirebaseEvent("create_join_room_screen", nPlayers, "join")
                        val playerJoining = playersJoined + 1
                        refRoomData.document(roomID)
                            .set(hashMapOf("p$playerJoining" to userName, "PJ" to playerJoining, "p${playerJoining}h" to photoURL, "p${playerJoining}c" to totalCoins), SetOptions.merge())
                            .addOnSuccessListener {
                                startActivity(Intent(applicationContext, CreateAndJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
                                    .apply { putExtra("selfName", userName) }
                                    .apply { putExtra("from", "p$playerJoining") }
                                    .apply { putExtra("nPlayers", nPlayers) }
                                    .apply { putExtra("photoURL", photoURL) }
                                    .apply { putExtra("totalCoins", totalCoins) }
                                    .apply { putExtra("offline", false) }
                                    .putIntegerArrayListExtra("userStats", ArrayList(listOf(nGamesPlayed, nGamesWon, nGamesBid))))

                                overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
                                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)
                            }
                    }
                } else {
                    soundError.start()
                    if (vibrateStatus) vibrationStart()
                    maskAllLoading.visibility = View.GONE
                    roomIDInputLayout.helperText = null
                    errorJoinRoomID = true
                    roomIDInputLayout.error = "No Room found"
                    roomIDInput.text?.clear()
                }
            }.addOnFailureListener { exception ->
                maskAllLoading.visibility = View.GONE
                toastCenter("Failed to create room \nPlease try again or later \n${exception.localizedMessage!!}")
            }
        } else {
            soundError.start()
            vibrationStart()
            errorJoinRoomID = false
            roomIDInputLayout.error = null
            roomIDInputLayout.helperText = getString(R.string.joinHelper)
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

    private fun joinRoomWindowOpen() {
        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched && !newUser) {
            mInterstitialAd.show()
        }
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
        }, 190)
    }

    fun developerCredits(view: View) {
        if (soundStatus) SoundManager.getInstance().playUpdateSound()  //Pass username and current activity alias to be able to come back with same info
        startActivity(Intent(this, DeveloperCredits::class.java).putExtra("uid", uid).putExtra("soundStatus",soundStatus))
        overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
    }

    fun anim(view: View, anim: Int) {
        view.startAnimation(AnimationUtils.loadAnimation(applicationContext, anim))
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
            soundUpdate.start()
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

    @SuppressLint("NewApi")
    private fun vibrationStart(duration: Long = 150) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    private fun enterText(view: View = View(applicationContext)) {
        roomIDInput.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    joinRoomButtonClicked(v)
                    false
                }
                else -> false
            }
        }
    }

    fun toastCenter(message: String) {
        toast.setText(message)
        toast.show()
    }

    fun trainingStart(view: View) {
        if (trainAccess) {
            maskAllLoading.visibility = View.VISIBLE
            loadingText.text = getString(R.string.startTrain)
                    if (soundStatus) SoundManager.getInstance().playUpdateSound() //Pass username and current activity alias to be able to come back with same info
            startActivity(Intent(applicationContext, TrainActivity::class.java))
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
        } else {
            toastCenter("Sorry You don't have access")
        }
    }

    private fun ranking() {
        rankStats.visibility = View.VISIBLE
        rankProgress.on()
        anim(rankStats, R.anim.zoomin_center)
        rankWindowStatus = true
        if (!rankFetchedStatus) {
            val top = 30
            refUsersData.orderBy("sc", Query.Direction.DESCENDING).limit(top.toLong()).get()
                .addOnSuccessListener { querySnapshot1 ->
                    rankFetchedStatus = true
                    querySnapshot = querySnapshot1
                    mapRankingStats()
                    logFirebaseEvent("Ranking", 1, "Fetched")
                }
        } else mapRankingStats()
        logFirebaseEvent("Ranking", 1, "Requested")
    }

    @SuppressLint("SetTextI18n")
    fun mapRankingStats() {
        rankGallery.removeAllViewsInLayout()
        val gallery = rankGallery
        gallery.removeAllViews()
        val inflater = LayoutInflater.from(applicationContext)
        val typedValue = TypedValue()
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
        var i = 1
        for (document in querySnapshot) {
            val won = if (document.contains("w_bot")) document["w_bot"].toString()
                .toInt() + document["w"].toString().toInt()
            else document["w"].toString().toInt()
            val played = if (document.contains("p_bot")) document["p_bot"].toString()
                .toInt() + document["p"].toString().toInt()
            else document["p"].toString().toInt()
            val bidGames = if (document.contains("b_bot")) document["b_bot"].toString()
                .toInt() + document["b"].toString().toInt()
            else document["b"].toString().toInt()

            val viewTemp = inflater.inflate(R.layout.user_rank, gallery, false)
            if (document.id == uid) {
                viewTemp.userImage.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
                //                viewTemp.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_appeal))
                viewTemp.userName.setTextColor(ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4))
            }
            viewTemp.userName.text = "$i. " + document["n"].toString()
            viewTemp.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            viewTemp.userCoins.setText("$ " + String.format("%,d", document["sc"]), true)
            viewTemp.userCoins.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            viewTemp.userScore.text = String.format("%,d", played) + " " + emojiGamePlayed + "\n" + String.format("%,d", won) + " " + emojiTrophy + "\n" + String.format("%,d", bidGames) + " " + emojiScore
            viewTemp.userScore.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            Picasso.get().load(document["ph"].toString()).resize(200, 200).into(viewTemp.userImage)
            viewTemp.userImage.foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            gallery.addView(viewTemp)
            i += 1
        }
        rankProgress.off()
        rankProgress.visibility = View.GONE
        anim(gallery, R.anim.slide_down_in)
        if (soundStatus) soundZip.start()
    }

    fun closeRankWindow(view: View) {
        rankWindowStatus = false
        anim(rankStats, R.anim.zoomout_center)
        Handler(Looper.getMainLooper()).postDelayed({
            rankStats.clearAnimation()
            rankStats.visibility = View.GONE
        }, 190)

    }

    private fun inviteFriends() {
        try {
            startActivity(Intent.createChooser(intentInvite, "Invite friends via "))
        } catch (me: Exception) {
        }
    }

    private fun createIntentInvite(){
        val compression = 0.7
        val image = ContextCompat.getDrawable(applicationContext, R.drawable.game_screen)
            ?.toBitmap(round(compression * 997).toInt(), round(compression * 2228).toInt(), Bitmap.Config.ARGB_8888)
        val imagePath = File(applicationContext.getExternalFilesDir(null)
            .toString() + "/gamescreen.jpg")
        val fos = FileOutputStream(imagePath)
        image?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        val uri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".provider", imagePath)
        val message = "Hey, Let's play this cool game 3 of Spades (Kaali ki Tiggi) online. \n\nInstall from below link \nFor Android:  \n${
            getString(R.string.playStoreLink)
        }\n\n For iOS: Coming soon..."
        intentInvite = Intent()
        intentInvite.action = Intent.ACTION_SEND
        intentInvite.type = "image/*"
        intentInvite.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentInvite.putExtra(Intent.EXTRA_TITLE, "Share app")
        intentInvite.putExtra(Intent.EXTRA_TEXT, message)
        intentInvite.putExtra(Intent.EXTRA_STREAM, uri)
    }

    fun buildCustomTabIntent(){
        intentBuilder = CustomTabsIntent.Builder()
        intentBuilder.setStartAnimations(this, R.anim.slide_left_activity, R.anim.slide_left_activity)
        intentBuilder.setExitAnimations(this, R.anim.slide_right_activity, R.anim.slide_right_activity)
        intentBuilder.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.icon_yellow))
        intentBuilder.addDefaultShareMenuItem()
    }

    fun openRatingWindow(view: View) {
        if (soundStatus) SoundManager.getInstance().playUpdateSound()
        rateUsLayout.visibility = View.VISIBLE
        anim(rateUsLayoutFrame, R.anim.zoomin_center)
        anim(rateUsIcon1, R.anim.anim_scale_appeal)
        ratingWindowOpenStatus = true
    }

    fun closeRatingWindow(view: View) {
        rateUsIcon1.clearAnimation()
        rateUsLayout.visibility = View.GONE
        ratingWindowOpenStatus = false
    }

    fun askLaterRating(view: View) { // request for rating after x days from today if choose ask later
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        logFirebaseEvent("rate_us", 1, "rate_later")
        closeRatingWindow(View(applicationContext))
        ratingRequestDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            .toInt() + requestRatingAfterDays
        editor.putInt("ratingRequestDate", ratingRequestDate)
        editor.apply()
        if (backButtonPressedStatus) moveTaskToBack(true)
        backButtonPressedStatus = false
    }

    fun rateUs(view: View) { // once clicked never ask to rate again
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        closeRatingWindow(View(applicationContext))
        if(!rated) openPlayStore() //inAppReview()  - disable inapp review for a while
        else openPlayStore()
        if (view.tag == "good") logFirebaseEvent("rate_us", 1, "rate_good")
        else if (view.tag == "bad") logFirebaseEvent("rate_us", 1, "rate_bad")
    }

//    private fun inAppReview() {
//        val manager = ReviewManagerFactory.create(applicationContext)
//        val request = manager.requestReviewFlow()
//        request.addOnCompleteListener { request1 ->
//            if (request1.isSuccessful) {
//                val reviewInfo = request1.result
//                val flow = manager.launchReviewFlow(this, reviewInfo)
//                flow.addOnCompleteListener { result ->
//                    if (result.isSuccessful) {
//                        rated = true
//                        editor.putBoolean("rated", rated)
//                        editor.apply()
//                        logFirebaseEvent("rate_us", 1, "rated")
//                        refUsersData.document(uid).set(hashMapOf("rated" to 1), SetOptions.merge())
//                    } else openPlayStore() //toastCenter("failed")
//                }
//            } else{
//                logFirebaseEvent("rate_us", 1, "ratedFailure")
//                openPlayStore()
//            }
//        }
//    }

    fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.kaalikiteeggi.three_of_spades")
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    private fun checkRatingRequest(): Boolean {
        return !rated && (SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            .toInt() >= ratingRequestDate)
    }

    override fun onBackPressed() { //minimize the app and avoid destroying the activity
                if (soundStatus) SoundManager.getInstance().playUpdateSound()
        if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus) && ratingWindowOpenStatus && backButtonPressedStatus) {
            moveTaskToBack(true)
            closeRatingWindow(View(applicationContext))
            backButtonPressedStatus = false
        } else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus || ratingWindowOpenStatus) && checkRatingRequest()) {
            backButtonPressedStatus = true
            openRatingWindow(View(applicationContext))
        } else if (!(rankWindowStatus || joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus || ratingWindowOpenStatus)) {
            moveTaskToBack(true)
        } // none should be visible
        else if (ratingWindowOpenStatus) {
            closeRatingWindow(View(applicationContext))
        }
        if (rankWindowStatus) closeRankWindow(View(applicationContext))
        if (joinRoomWindowStatus) joinRoomWindowExit(View(applicationContext))
        if (createRoomWindowStatus) createRoomWindowExit(View(applicationContext))
        if (settingsWindowStatus) closeSettingsWindow(View(applicationContext))
        if (playerStatsWindowStatus) openClosePlayerStats(View(applicationContext))
        //           super.onBackPressed()
    }

    fun signOut(view: View) {
        if (vibrateStatus) vibrationStart()
        mAuth.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
        overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
        finish()
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
        if (this::billingClient.isInitialized) billingClient.endConnection()
        try {
            textToSpeech.shutdown()
        } catch (me: java.lang.Exception) {
        }
        super.onDestroy()

    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
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
            soundError.start()
            toastCenter("Payment Cancelled")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            soundError.start()
            toastCenter("Already processing pending item")
        } else {
            soundError.start()
            toastCenter("Payment Failed. Try again \n ${billingResult.responseCode}")
        }
    }
}

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun howToPlay() {
//        intentBuilder.build().launchUrl(this, Uri.parse(howtoPlayUrl))
//    }

//    private fun checkAccessToTrain() {
//        Firebase.firestore.collection("Train_Access").document(uid).get()
//            .addOnSuccessListener { dataSnapshot ->
//                trainAccess = dataSnapshot.data != null
//                if (!trainAccess) helpUs.visibility = View.GONE
////                else helpUs.visibility = View.VISIBLE
//            }
//    }

//fun changeBackgroundRequest(view: View) {
//    if (soundStatus) SoundManager.getInstance().playUpdateSound()
//    changeBackground(view.tag.toString())
//    closeSettingsWindow(View(applicationContext))
//    editor.putString("themeColor", view.tag.toString()) // write username to preference file
//    editor.apply()
//}

//private fun changeBackground(color: String) {
//    when (color) {
//        "shine_blue" -> {
//            backgroundmhs.setImageResource(R.drawable.blueburst)
//        }
//        "shine_bk" -> {
//            backgroundmhs.setImageResource(R.drawable.greenyellowburst)
//        }
//        "shine_orange" -> {
//            backgroundmhs.setImageResource(R.drawable.navyblueburst)
//        }
//        "shine_pink" -> {
//            backgroundmhs.setImageResource(R.drawable.redorangeburst)
//        }
//        "shine_purple" -> {
//            backgroundmhs.setImageResource(R.drawable.redblackburst)
//        }
//        "shine_yellow" -> {
//            backgroundmhs.setImageResource(R.drawable.yellowburst)
//        }
//    }
//}