@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.android.billingclient.api.*
import com.facebook.login.LoginManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.romainpiel.shimmer.Shimmer
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifImageButton
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class MainHomeScreen : AppCompatActivity(), PurchasesUpdatedListener {
    private var requestRatingAfterDays = 2 //dummy
    private var ratingRequestDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
    private lateinit var soundError: MediaPlayer
    private lateinit var soundBkgd: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var soundCollectCards: MediaPlayer
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var rewardStatus = false
    private var rewardAmount = 0
    private var createRoomStatus = false
    private var onceAdWatched = false

    private var backButtonPressedStatus = false
    private var joinRoomWindowStatus = false
    private var ratingWindowOpenStatus = false
    private var createRoomWindowStatus = false
    private var settingsWindowStatus = false
    private var playerStatsWindowStatus = false
    private var trainAccess = false

    private lateinit var toast: Toast
    private var refUsersData = Firebase.firestore.collection("Users")
    private var refRoomData = Firebase.firestore.collection("Rooms")

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var rewardedAd: RewardedAd
    private lateinit var billingClient: BillingClient
    private lateinit var mAuth: FirebaseAuth
    private var uid = ""
    private lateinit var userName: String
    private lateinit var photoURL: String
    private var target = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap != null) {
                findViewById<ImageView>(R.id.profilePic).setImageDrawable(bitmap.toDrawable(resources))
            }
        }
    }
    private var musicStatus = false
    private var soundStatus = true
    private var vibrateStatus = true
    private var premiumStatus = false
    private var rated = false
    private val today =  SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
    private var consecutiveDay = 1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var vibrator:Vibrator
    private var totalCoins = 0
    private var ngamesPlayed = 0
    private var ngamesWon = 0
    private var ngamesBided = 0
    private var nPlayers = 0

    private var loadRewardedAdTry = 0
    private var countRewardWatch = 1
    private lateinit var rewardedAdLoadCallback: RewardedAdLoadCallback
    private var dailuRewardList = listOf(250,500,750,1000,1500,2000,2500)
    private var dailyRewardAmount = 0
    private var dailyRewardClicked = false
    private var claimedToday = false
    private var dailyRewardStatus = false

    @SuppressLint("ShowToast", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable._s_icon_bug) //default: bug image
            .apply()

        setContentView(R.layout.activity_main_home_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.cardsBackgroundDark))
        toast.view.findViewById<TextView>(android.R.id.message)
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F
        soundBkgd = MediaPlayer.create(applicationContext, R.raw.main_screen_bkgd)
        soundBkgd.isLooping = true

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.uid.toString()
        getSharedPrefs()

        Handler().post(Runnable {
            soundUpdate = MediaPlayer.create(applicationContext, R.raw.player_moved)
            soundError = MediaPlayer.create(applicationContext, R.raw.error_entry)
            soundSuccess = MediaPlayer.create(applicationContext, R.raw.player_success_chime)
            soundCollectCards = MediaPlayer.create(applicationContext, R.raw.collect_cards)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            getUserData()
            animateElements()
            initializeAds()
            enterText() // press enter to join room
            setupBillingClient()
            findViewById<AppCompatTextView>(R.id.versionCode).text = "V: "+packageManager.getPackageInfo(packageName,0).versionName.toString()
        })
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
    }
    override fun onStart() {
        super.onStart()
        if(rated && ratingWindowOpenStatus) closeRatingWindow(View(applicationContext))
        if (musicStatus && this::soundBkgd.isInitialized) soundBkgd.start()

    }
    private fun logFirebaseEvent(event:String, int: Int, key: String){
        val params = Bundle()
        params.putInt(key,int)
        firebaseAnalytics.logEvent(event, params)
    }
    private fun dailyRewardWindowDisplay() {
        soundSuccess.start()
        val gridView = findViewById<GridView>(R.id.dailyRewardGrid)
        val arrayList = setDataList()
        val imageAdapter = ImageAdapter(applicationContext, arrayList, min(7,consecutiveDay))
        gridView.adapter = imageAdapter
        findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.VISIBLE
        anim(findViewById(R.id.dailyRewardGridLayout),R.anim.zoomin_center)
    }
    fun closedailyRewardWindowDisplay(view: View){
        if(soundStatus) soundUpdate.start()
        findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
    }

    fun addPoints(){
        dailyRewardClicked = false
        rewardAmount = dailyRewardAmount
        totalCoins += rewardAmount  // reward with daily reward amount for watching video
        logFirebaseEvent("daily_rewards", rewardAmount, "coins")
        findViewById<TextView>(R.id.watchVideoCoin).text = rewardAmount.toString()
        findViewById<TextView>(R.id.watchVideoCoin).visibility = View.VISIBLE
        soundCollectCards.start()
        anim(findViewById(R.id.watchVideoCoin),R.anim.slide_500_coins)
        Handler().postDelayed({
            if(vibrateStatus) vibrationStart()
            soundSuccess.start()
            findViewById<Button>(R.id.userScore).text = String.format("%,d",totalCoins)
            anim(findViewById(R.id.userScore),R.anim.blink_and_scale)
            findViewById<TextView>(R.id.watchVideoCoin).visibility = View.INVISIBLE
            loadRewardAd()
        },1250)
        refUsersData.document(uid).set(hashMapOf("sc" to totalCoins,"LSD" to today, "nDRC" to consecutiveDay , "claim" to 1), SetOptions.merge())
    }
    fun claimDailyReward(view: View){
        if(vibrateStatus) vibrationStart()
        if(soundStatus) soundUpdate.start()
        dailyRewardClicked = true
        dailyRewardStatus = true
        if(premiumStatus){
            findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
            addPoints()
        }
        else if(rewardedAd.isLoaded) {
            val activityContext: Activity = this
            val adCallback = object: RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
                    findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
                    findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
                    if(musicStatus) soundBkgd.pause()
                }
                override fun onRewardedAdClosed() {
                    if(musicStatus) soundBkgd.start()
                    dailyRewardClicked = false
                    if(rewardStatus) {
                        rewardStatus = false
                        findViewById<TextView>(R.id.watchVideoCoin).text = rewardAmount.toString()
                        findViewById<TextView>(R.id.watchVideoCoin).visibility = View.VISIBLE
                        soundCollectCards.start()
                        anim(findViewById(R.id.watchVideoCoin),R.anim.slide_500_coins)
                        Handler().postDelayed({
                            soundSuccess.start()
                            findViewById<Button>(R.id.userScore).text = String.format("%,d",totalCoins)
                            anim(findViewById(R.id.userScore),R.anim.blink_and_scale)
                            findViewById<TextView>(R.id.watchVideoCoin).visibility = View.INVISIBLE
                            loadRewardAd()
                        },1250)
                    }else {
                        loadRewardAd()
                        toastCenter("Sorry $userName, No coins added")
                    }
                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    dailyRewardClicked = false
                    rewardStatus = true
                    rewardAmount = dailyRewardAmount
                    totalCoins += rewardAmount  // reward with daily reward amount for watching video
                    refUsersData.document(uid).set(hashMapOf("sc" to totalCoins,"LSD" to today, "nDRC" to consecutiveDay , "claim" to 1), SetOptions.merge())
                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    dailyRewardClicked = false
                    findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
                    findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
                    loadRewardAd()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        }
       else if(mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
            findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
            loadRewardAd()
        }else{
            findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
            addPoints()
            findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.INVISIBLE
            loadRewardAd()
            if (premiumStatus && getString(R.string.testAds).contains('n')) mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }
    private fun setDataList(): ArrayList<DailyRewardItem>{
       val arrayList = ArrayList<DailyRewardItem>()
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 1 \n${dailuRewardList[0]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 2 \n${dailuRewardList[1]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 3 \n${dailuRewardList[2]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 4 \n${dailuRewardList[3]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 5 \n${dailuRewardList[4]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 6 \n${dailuRewardList[5]} coins"
            )
        )
        arrayList.add(
            DailyRewardItem(
                R.drawable.coin_trans_1,
                "Day 6+ \n${dailuRewardList[6]} coins"
            )
        )
        return arrayList
    }
    @SuppressLint("SimpleDateFormat")
    private fun getUserData(){
        val user = mAuth.currentUser
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()
        if(user != null) {
            userName = user.displayName.toString().split(" ")[0]
            findViewById<Button>(R.id.welcomeUserNameview).text = userName
            if(intent.getBooleanExtra("newUser",false)) {//check if user has joined room or created one and display Toast
                toastCenter("Hi $userName") // dummy - add tutorial
                editor.putBoolean("rated",rated)
                editor.putInt("joinDate",SimpleDateFormat("yyyyMMdd").format(Date()).toInt())
                editor.putInt("ratingRequestDate",SimpleDateFormat("yyyyMMdd").format(Date()).toInt() + requestRatingAfterDays)
                editor.putBoolean("premium",false) // make premium false
                editor.apply()
            } // check if new user
        }
        else {
            mAuth.signOut()
            startActivity(Intent(applicationContext, StartScreen::class.java))
            finish()
        }
        refUsersData.document(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.data != null) {
                try {
                    photoURL = dataSnapshot.get("ph").toString()
                    Picasso.get().load(photoURL).transform(CircleTransform()).into(target)
                    totalCoins = dataSnapshot.get("sc").toString().toInt()
                    findViewById<AppCompatButton>(R.id.userScore).text = String.format("%,d", totalCoins)
                    findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.GONE
                    findViewById<TextView>(R.id.loadingText).text = getString(R.string.fetching_player)
                    ngamesPlayed = dataSnapshot.get("p").toString().toInt()
                    ngamesWon = dataSnapshot.get("w").toString().toInt()
                    ngamesBided = dataSnapshot.get("b").toString().toInt()
                    premiumStatus = dataSnapshot.get("pr").toString().toInt() == 1
                    if (premiumStatus) {
                        findViewById<GifImageView>(R.id.removeAds).setImageResource(R.drawable.premium)
                        findViewById<AdView>(R.id.addViewMHS).visibility = View.GONE
                    } else {
                        findViewById<GifImageView>(R.id.removeAds).setImageResource(R.drawable.no_ads)
                        findViewById<AdView>(R.id.addViewMHS).visibility = View.VISIBLE
                    }

                    val lastSeenDate = dataSnapshot.get("LSD").toString().toInt()
                    consecutiveDay = dataSnapshot.get("nDRC").toString().toInt()
                    dailyRewardAmount = dailuRewardList[min(consecutiveDay, 7) - 1]
                    claimedToday = dataSnapshot.get("claim").toString().toInt() == 1
                    if (today == lastSeenDate + 1) { // if consecutive day login
                        consecutiveDay += 1
                        claimedToday = false
                        dailyRewardAmount = dailuRewardList[min(consecutiveDay, 7) - 1]
                        Firebase.firestore.collection("Users").document(uid).set(
                            hashMapOf("LSD" to today, "nDRC" to consecutiveDay, "claim" to 0),
                            SetOptions.merge()
                        )
//                    if(!claimedToday) dailyRewardWindowDisplay() //if not claimed today

                    } else if (today > lastSeenDate + 1) { // if more than 1 day gap , reset counter
                        consecutiveDay = 1
                        claimedToday = false
                        dailyRewardAmount = dailuRewardList[min(consecutiveDay, 7) - 1]
                        Firebase.firestore.collection("Users").document(uid).set(
                            hashMapOf("LSD" to today, "nDRC" to consecutiveDay, "claim" to 0),
                            SetOptions.merge()
                        )
//                    if(!claimedToday) dailyRewardWindowDisplay() //if not claimed today
                    }
                    if (!claimedToday) {//if not claimed today
                       Handler().postDelayed({
                           dailyRewardWindowDisplay()},1000)
                    }
                    findViewById<AppCompatTextView>(R.id.ngamesPlayedStats).text =
                        ngamesPlayed.toString()
                    findViewById<AppCompatTextView>(R.id.ngamesBidedStats).text =
                        ngamesBided.toString()
                    findViewById<AppCompatTextView>(R.id.ngamesWonStats).text =
                        ngamesWon.toString()
                    editor.putString("photoURL", photoURL) // write username to preference file
                    editor.apply()
                    editor.putBoolean("premium", premiumStatus) // write username to preference file
                    editor.apply()
                    checkAccesstoTrain()
                }catch(exception: java.lang.Exception){
                    mAuth.signOut()
                    startActivity(Intent(applicationContext, StartScreen::class.java))
                    finish()
                }
            } else {
                mAuth.signOut()
                startActivity(Intent(applicationContext, StartScreen::class.java))
                finish()
            } }.addOnFailureListener {
            mAuth.signOut()
            startActivity(Intent(applicationContext, StartScreen::class.java))
            finish()
        }
    }
    private fun checkAccesstoTrain(){
        Firebase.firestore.collection("Train_Access").document(uid).get().addOnSuccessListener { dataSnapshot ->
            trainAccess = dataSnapshot.data != null
            if(!trainAccess) findViewById<GifImageView>(R.id.helpUs).visibility = View.GONE
        }
    }
    private fun getSharedPrefs(){
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("themeColor")) {
            changeBackground(sharedPreferences.getString("themeColor", "shine_yellow").toString())
        }
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
            if(premiumStatus) findViewById<GifImageView>(R.id.removeAds).setImageResource(R.drawable.premium)
        }
        if (sharedPreferences.contains("rated")) {
            rated = sharedPreferences.getBoolean("rated", false)
        }else{
            editor.putBoolean("rated",rated)
            editor.apply()
        }
        if (!sharedPreferences.contains("joinDate")) {
            editor.putInt("joinDate",SimpleDateFormat("yyyyMMdd").format(Date()).toInt())
            editor.apply()
        }
        if (!sharedPreferences.contains("ratingRequestDate")) {
            ratingRequestDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt() + requestRatingAfterDays
            editor.putInt("ratingRequestDate", ratingRequestDate)
            editor.apply()
        }else{
            ratingRequestDate = sharedPreferences.getInt("ratingRequestDate", 0)
        }
        if (sharedPreferences.contains("musicStatus")) {
            musicStatus = sharedPreferences.getBoolean("musicStatus", true)
            findViewById<SwitchCompat>(R.id.musicSwitch).isChecked = musicStatus
            if(musicStatus) {
                soundBkgd.start()
                findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.music)
            }else {
                findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.nomusic)
            }
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
            findViewById<SwitchCompat>(R.id.soundSwitch).isChecked = soundStatus
            if(soundStatus) {
                findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_on_png)
            }else findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_off_png)
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
            findViewById<SwitchCompat>(R.id.vibrateSwitch).isChecked = vibrateStatus
            if(vibrateStatus) {
                findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateon)
            }else findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateoff)
        }
        if (sharedPreferences.contains("Room")) {
            val roomID = sharedPreferences.getString("Room","").toString()
            if(roomID.isNotEmpty()) {
               Handler().postDelayed({deleteAllRoomdata(roomID)},0)
//                deleteAllRoomdata(roomID)
            }
            editor.remove("Room").apply()
        }
    }
    fun removeAdsorPremium(view: View){
//        showNotification()
        if(premiumStatus) openClosePlayerStats(View(applicationContext))
        else {

            querySkuDetailsRequest()
        }

    }
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener (this).build()
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
    private fun checkPendingPurchases(){
        val purchases  = billingClient.queryPurchases("inapp").purchasesList
       if(purchases!= null) {
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
        if(billingClient.isReady) {
            val params = SkuDetailsParams.newBuilder().setSkusList(listOf("remove_ads")).setType(BillingClient.SkuType.INAPP).build()
            billingClient.querySkuDetailsAsync(params) { response, skuDetailsList ->
                if(response.responseCode == BillingClient.BillingResponseCode.OK) {
                    val billingFlowParams =
                        skuDetailsList?.get(0)?.let {
                            BillingFlowParams.newBuilder().setSkuDetails(
                                it
                            ).build()
                        }
                    if (billingFlowParams != null) {
                        billingClient.launchBillingFlow(this, billingFlowParams)
                    }
                }
            }
        }else {
            toastCenter("Billing client is not ready \nTry again or Restart app")
            setupBillingClient()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun acknowledgePurchase(purchase: Purchase){
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params){}
        if(purchase.sku == "remove_ads") {
            premiumStatus = true
            soundSuccess.start()
            toastCenter("Congratulations!! Your Payment is aproved \n You won't see Ads now")
            refUsersData.document(uid).set(hashMapOf("pr" to 1), SetOptions.merge())
            Firebase.firestore.collection("PremiumUser").document(uid).set(hashMapOf("id" to mAuth.currentUser?.email, "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt() ))
            consumePurchase(purchase)
            findViewById<GifImageView>(R.id.removeAds).setImageResource(R.drawable.premium)
            findViewById<AdView>(R.id.addViewMHS).visibility = View.GONE
            editor.putBoolean("premium", true) // write username to preference file
            editor.apply()
        }

    }
    private fun consumePurchase(purchase: Purchase){
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.consumeAsync(params) { _: BillingResult, _: String -> }
    }

    private fun initializeAds(){
        findViewById<AdView>(R.id.addViewMHS).loadAd(AdRequest.Builder().build()) // banner add load
        mInterstitialAd = InterstitialAd(this)
        if(!BuildConfig.DEBUG)  mInterstitialAd.adUnitId = getString(R.string.interstitialReal) // real interstitial ad
        else  mInterstitialAd.adUnitId = getString(R.string.interstitialTest) // test interstitial ad
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // interstitial add load
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                onceAdWatched = true
                if(dailyRewardClicked) {
                    addPoints()
                    dailyRewardClicked = false
                } else if(createRoomStatus){
                    createRoom()
                    createRoomStatus = false
                }
                if (!premiumStatus) mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        rewardedAdLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                loadRewardedAdTry = 1
                when(countRewardWatch){
                    0-> findViewById<GifImageView>(R.id.watchVideo).setImageResource(R.drawable.watch_ad_100)
                    1-> findViewById<GifImageView>(R.id.watchVideo).setImageResource(R.drawable.watch_ad_250)
                    2-> findViewById<GifImageView>(R.id.watchVideo).setImageResource(R.drawable.watch_ad_500)
                    else-> findViewById<GifImageView>(R.id.watchVideo).setImageResource(R.drawable.watch_ad_500)
                }
                findViewById<GifImageView>(R.id.watchVideo).visibility = View.VISIBLE
                anim(findViewById(R.id.watchVideo),R.anim.anim_scale_infinite)
            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                if(loadRewardedAdTry<=10) loadRewardAd()
            }
        }
        loadRewardAd()
    }
    private fun loadRewardAd(){
        loadRewardedAdTry += 1
        rewardedAd = RewardedAd(applicationContext, getString(R.string.rewarded))
        if(getString(R.string.testAds).contains('n'))  rewardedAd.loadAd(AdRequest.Builder()
            .build(), rewardedAdLoadCallback)
    }
    fun showRewardedVideoAd(view: View){
        if(soundStatus) soundUpdate.start()
        if (rewardedAd.isLoaded) {
            val activityContext: Activity = this
            val adCallback = object: RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
                    findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
                    if(musicStatus) soundBkgd.pause()
                }
                override fun onRewardedAdClosed() {
                    if(musicStatus) soundBkgd.start()
                    if(rewardStatus) {
                        rewardStatus = false
                        findViewById<TextView>(R.id.watchVideoCoin).text = rewardAmount.toString()
                        findViewById<TextView>(R.id.watchVideoCoin).visibility = View.VISIBLE
                        logFirebaseEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY,rewardAmount, "coins")
                        soundCollectCards.start()
                        anim(findViewById(R.id.watchVideoCoin),R.anim.slide_500_coins)
                        Handler().postDelayed({
                            if(vibrateStatus) vibrationStart()
                            soundSuccess.start()
                            findViewById<Button>(R.id.userScore).text = String.format("%,d",totalCoins)
                            anim(findViewById(R.id.userScore),R.anim.blink_and_scale)
                            findViewById<TextView>(R.id.watchVideoCoin).visibility = View.INVISIBLE
                            loadRewardAd()
                        },1250)
                    }else {
                        loadRewardAd()
                        toastCenter("Sorry $userName No coins added")
                    }
                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    rewardStatus = true
                    rewardAmount = when(countRewardWatch){
                        0-> 100
                        1-> 250
                        2-> 500
                        else-> 500
                    }
                    countRewardWatch += 1
                    totalCoins += rewardAmount  // reward with 500 coins for watching video
                    refUsersData.document(uid).set(hashMapOf("sc" to totalCoins ), SetOptions.merge())
                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
                    findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
                    loadRewardAd()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        }
        else{
            findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
            loadRewardAd()
        }
    }

    fun openClosePlayerStats(view: View){
        playerStatsWindowStatus = !playerStatsWindowStatus
        if(playerStatsWindowStatus){
            if(soundStatus) soundUpdate.start()

            findViewById<RelativeLayout>(R.id.playerStats).visibility = View.VISIBLE
            anim(findViewById(R.id.playerStats),R.anim.slide_down_player_stats)
            anim(findViewById(R.id.signoutbutton),R.anim.slide_buttons)
            anim(findViewById(R.id.signoutbuttonImage),R.anim.anim_scale_infinite)
        }
        else {
            anim(findViewById(R.id.playerStats),R.anim.slide_up_player_stats)
            findViewById<RelativeLayout>(R.id.playerStats).visibility = View.INVISIBLE
            findViewById<AppCompatButton>(R.id.signoutbutton).clearAnimation()
            findViewById<ImageView>(R.id.signoutbuttonImage).clearAnimation()
        }
    }
    fun openSettingsWindow(view: View){
        settingsWindowStatus = true
        if(soundStatus) soundUpdate.start()

        findViewById<RelativeLayout>(R.id.settingsLayout).visibility = View.VISIBLE
        Handler().postDelayed({
            findViewById<ImageView>(R.id.closeSettings).visibility = View.VISIBLE
//            anim(findViewById(R.id.closeSettings),R.anim.anim_scale_infinite)
        }, 220)

        anim(findViewById(R.id.settingsLayouttemp),R.anim.zoomin)
        anim(findViewById(R.id.settMusicIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.settSoundIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.settVibrateIcon),R.anim.anim_scale_infinite)
    }
    fun closeSettingsWindow(view: View){
        settingsWindowStatus = false
        anim(findViewById(R.id.settingsLayouttemp),R.anim.zoomout)
        findViewById<ImageView>(R.id.closeSettings).visibility = View.INVISIBLE
        Handler().postDelayed({
            findViewById<RelativeLayout>(R.id.settingsLayout).visibility = View.INVISIBLE
        }, 230)
        findViewById<ImageView>(R.id.settMusicIcon).clearAnimation()
        findViewById<ImageView>(R.id.settSoundIcon).clearAnimation()
        findViewById<ImageView>(R.id.settVibrateIcon).clearAnimation()
        findViewById<ImageView>(R.id.closeSettings).clearAnimation()
    }

    fun createRoomButtonClicked(view: View) {
        createRoomStatus = true
        nPlayers = view.tag.toString().toInt()
        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched) {
            mInterstitialAd.show()
        } else {
            createRoom()
            createRoomStatus = false
        }
    }
    private fun createRoom() {
        findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.VISIBLE
        findViewById<TextView>(R.id.loadingText).text = getString(R.string.creatingRoom)

            val allowedChars = ('A'..'H')+ ('J'..'N') + ('P'..'Z')+('2'..'9')  // 1, I , O and 0 skipped
            val roomID = (1..4).map { allowedChars.random() }.joinToString ("")
            if(nPlayers==7) {
                if(!BuildConfig.DEBUG)   createRoomwithID(roomID, CreateRoomData(userName, photoURL, totalCoins).data7)
                else createRoomwithID(roomID, CreateRoomData(userName, photoURL, totalCoins).dummyData7)
            }
           else if(nPlayers==4){
                if(!BuildConfig.DEBUG)  createRoomwithID(roomID, CreateRoomData(userName, photoURL, totalCoins).data4)
                else createRoomwithID(roomID, CreateRoomData(userName, photoURL, totalCoins).dummyData4)
            }
    }
    private fun createRoomwithID(roomID: String, roomData: Any){
        refRoomData.document(roomID).set(roomData)
            .addOnFailureListener{
                findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.GONE
                toastCenter("Failed to create room \nPlease try again or later")}
            .addOnSuccessListener{
                soundSuccess.start()
                logFirebaseEvent("create_join_room_screen",nPlayers, "create")

                startActivity(Intent(applicationContext, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
            .apply { putExtra("selfName", userName) }.apply { putExtra("from", "p1") }
            .apply { putExtra("nPlayers", nPlayers) }
            .putIntegerArrayListExtra("userStats", ArrayList(listOf(ngamesPlayed, ngamesWon, ngamesBided ))))
                overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
                editor.putString("Room", roomID) // write room ID in storage - to delete later
                editor.apply()
//                finish()
        Handler().postDelayed({finish()},500)
    }
    }
    fun createRoomWindowOpen(view: View) {
        if(soundStatus) soundUpdate.start()
        Handler().postDelayed({
            findViewById<ImageView>(R.id.closeCreateRoom).visibility = View.VISIBLE
//            anim(findViewById(R.id.closeCreateRoom),R.anim.anim_scale_infinite)
        }, 270)
        findViewById<RelativeLayout>(R.id.createRoomFrame).visibility = View.VISIBLE
        anim(findViewById(R.id.createRoomFrameTemp),R.anim.zoomin_center)
        anim(findViewById(R.id.closeCreateRoom),R.anim.zoomin_center)
//        anim(findViewById(R.id.createSingle),R.anim.anim_scale_appeal)
//        anim(findViewById(R.id.createDouble),R.anim.anim_scale_appeal)
        createRoomWindowStatus = true
    }
    fun createRoomWindowExit(view: View) {
        findViewById<ImageView>(R.id.closeCreateRoom).clearAnimation()
        findViewById<GifImageButton>(R.id.createSingle).clearAnimation()
        findViewById<GifImageButton>(R.id.createDouble).clearAnimation()
        findViewById<ImageView>(R.id.closeCreateRoom).visibility = View.GONE
        anim(findViewById(R.id.createRoomFrameTemp),R.anim.zoomout)
        createRoomWindowStatus = false
        Handler().postDelayed({
            findViewById<RelativeLayout>(R.id.createRoomFrame).visibility = View.GONE
        }, 230)
    }

    fun joinRoomButtonClicked(view: View) {
        if(vibrateStatus) vibrationStart()
        val roomID = findViewById<EditText>(R.id.roomIDInput).text.toString()//read text field
        if (roomID.isNotEmpty()) {
            hideKeyboard()
            findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.VISIBLE
            findViewById<TextView>(R.id.loadingText).text = getString(R.string.checkJoinRoom)

            refRoomData.document(roomID).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.data != null) {
                    val playersJoined = dataSnapshot.get("PJ").toString().toInt()
                    val nPlayers = dataSnapshot.get("n").toString().toInt()
                    if (playersJoined >= nPlayers) {
                        soundError.start()
                        if(vibrateStatus) vibrationStart()
                        findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.GONE
                        findViewById<EditText>(R.id.roomIDInput).hint = "Room is Full"
                        findViewById<EditText>(R.id.roomIDInput).text.clear()
                    } else {
                        if(vibrateStatus) vibrationStart()
                        soundSuccess.start()
                        findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.loadingText).text = getString(R.string.joiningRoom)
                        logFirebaseEvent("create_join_room_screen",nPlayers, "join")
                        val playerJoining = playersJoined + 1
                        refRoomData.document(roomID).set(hashMapOf("p$playerJoining" to userName, "PJ" to playerJoining, "p${playerJoining}h" to photoURL, "p${playerJoining}c" to totalCoins), SetOptions.merge())
                            .addOnSuccessListener {
                                startActivity(Intent(applicationContext, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
                                    .apply { putExtra("selfName", userName) }.apply { putExtra("from", "p$playerJoining") }.apply {putExtra("nPlayers", nPlayers)}
                                    .putIntegerArrayListExtra("userStats", ArrayList(listOf(ngamesPlayed, ngamesWon, ngamesBided ))))
                                overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
                                Handler().postDelayed({finish()},500)} }
                }else {
                    soundError.start()
                    if(vibrateStatus) vibrationStart()
                    findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.GONE
                    findViewById<EditText>(R.id.roomIDInput).hint = "No Room found"
                    findViewById<EditText>(R.id.roomIDInput).text.clear()
                }
            }.addOnFailureListener {
                    exception ->
                findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.GONE
                toastCenter("Failed to create room \nPlease try again or later \n${exception.localizedMessage!!}"  ) }
        } else {
            soundError.start()
            vibrationStart()
            findViewById<EditText>(R.id.roomIDInput).hint = "Enter Room ID"
        }
    }
    private fun hideKeyboard(){
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            view?.let { v ->
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }
    fun joinRoomWindowOpen(view: View) {
        if(soundStatus) soundUpdate.start()
        findViewById<EditText>(R.id.roomIDInput).hint = "Room ID"

        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched) {
            mInterstitialAd.show()
        }
        Handler().postDelayed({
            findViewById<ImageView>(R.id.closeJoinRoom).visibility = View.VISIBLE
        }, 270)
        findViewById<RelativeLayout>(R.id.joinRoomFrame).visibility = View.VISIBLE
        anim(findViewById(R.id.joinRoomFrameTemp),R.anim.zoomin)
        anim(findViewById(R.id.closeJoinRoom),R.anim.zoomin)
        anim(findViewById(R.id.joinRoomFrameIcon),R.anim.clockwise)
        joinRoomWindowStatus = true
    }
    fun joinRoomWindowExit(view: View) {
        findViewById<ImageView>(R.id.joinRoomFrameIcon).clearAnimation()
        findViewById<ImageView>(R.id.closeJoinRoom).clearAnimation()
        findViewById<ImageView>(R.id.closeJoinRoom).visibility = View.GONE
        anim(findViewById(R.id.joinRoomFrameTemp),R.anim.zoomout)
        joinRoomWindowStatus = false
        Handler().postDelayed({
            findViewById<RelativeLayout>(R.id.joinRoomFrame).visibility = View.INVISIBLE
        }, 230)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun developerCredits(view: View){
//        makeCall()
//        recordAudio()
        if(soundStatus) soundUpdate.start()//Pass username and current activity alias to be able to come back with same info
        startActivity(Intent(this,DeveloperCredits::class.java).putExtra("uid",uid))
        overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
    }
    private fun animateElements(){
        anim(findViewById(R.id.profilePic),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.coinIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.createRoomButton),R.anim.slide_buttons)
        anim(findViewById(R.id.joinRoomButton),R.anim.slide_buttons)
        anim(findViewById(R.id.buttonInvite),R.anim.slide_buttons)
        anim(findViewById(R.id.buttonSettings),R.anim.slide_buttons)
        anim(findViewById(R.id.userScore),R.anim.slide_buttons_rtl)
//        shimmer = Shimmer()
//        shimmer.duration = 1800
//        shimmer.start(findViewById<ShimmerButton>(R.id.developerCreditsButton))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.musicSettText))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.soundSettText))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.vibrateSettText))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.playerStatsTitle))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesPlayedStats))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesWonStats))
//        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesBidedStats))

//        anim(findViewById(R.id.developerIcon),R.anim.anim_scale_infinite)
//        anim(findViewById(R.id.claimReward),R.anim.anim_scale_appeal)
//        anim(findViewById(R.id.claimRewardCancel),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.createRoomIcon),R.anim.clockwise)
        anim(findViewById(R.id.joinRoomIcon),R.anim.clockwise_ccw_infinite)
        anim(findViewById(R.id.settingsIcon),R.anim.clockwise_ccw_infinite)
        anim(findViewById(R.id.inviteIcon),R.anim.clockwise)
    }

    fun anim(view: View,anim:Int){
        view.startAnimation(AnimationUtils.loadAnimation(applicationContext,anim))
    }
    fun changeBackgroundRequest(view: View) {
        changeBackground(view.tag.toString())
        closeSettingsWindow(View(applicationContext))
        editor.putString("themeColor",view.tag.toString()) // write username to preference file
        editor.apply()
    }
    private fun changeBackground(color: String){
        when(color){
            "shine_blue"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_blue)
            }
            "shine_bk"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_bk)
            }
            "shine_orange"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_orange)
            }
            "shine_green_radial"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_green_radial)
            }
            "shine_pink"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_pink)
            }
            "shine_purple"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_purple)
            }
            "shine_yellow"-> {
                findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_yellow)
            }
            "shine_purple_dark"-> {
            findViewById<ImageView>(R.id.backgroundHomeScreen).setImageResource(R.drawable.shine_purple_dark)
        }
        }
    }
    fun music(view: View){
        musicStatus = findViewById<SwitchCompat>(R.id.musicSwitch).isChecked
        if(musicStatus) {
            soundBkgd.start()
            findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.music)
        }else {
            soundBkgd.pause()
            findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.nomusic)
        }
        editor.putBoolean("musicStatus",musicStatus) // write username to preference file
        editor.apply()
    }
    fun sound(view: View){
        soundStatus = findViewById<SwitchCompat>(R.id.soundSwitch).isChecked
        if(soundStatus) {
            soundUpdate.start()
            findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_on_png)
        }else findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_off_png)
        editor.putBoolean("soundStatus",soundStatus) // write username to preference file
        editor.apply()

    }
    fun vibrate(view: View){
        vibrateStatus = findViewById<SwitchCompat>(R.id.vibrateSwitch).isChecked
        if(vibrateStatus) {
            vibrationStart(1000)
            findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateon)
        }else findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateoff)
        editor.putBoolean("vibrateStatus",vibrateStatus) // write username to preference file
        editor.apply()
    }
    @SuppressLint("NewApi")
    private fun vibrationStart(duration: Long = 150){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(duration,VibrationEffect.DEFAULT_AMPLITUDE))
    } else{
        vibrator.vibrate(duration)
    }
}
    private fun enterText(view: View = View(applicationContext)){
        findViewById<EditText>(R.id.roomIDInput).setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    joinRoomButtonClicked(v)
                    false
                }
                else -> false
            }
        }
    }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }
    fun trainingStart(view: View){
//        logFirebaseEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, rewardAmount, "coins")

        if(trainAccess) {
            findViewById<RelativeLayout>(R.id.maskAllLoading).visibility = View.VISIBLE
            findViewById<TextView>(R.id.loadingText).text = getString(R.string.startTrain)
            if (soundStatus) soundUpdate.start()//Pass username and current activity alias to be able to come back with same info
            startActivity(Intent(applicationContext, TrainActivity::class.java))
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            Handler().postDelayed({ finish() }, 1000)
        }else{
            toastCenter("Sorry You don't have access")
        }
}
    fun inviteFriends(view: View){

        if(soundStatus) soundUpdate.start()
        val image = ContextCompat.getDrawable(applicationContext,R.drawable.game_screen)
            ?.toBitmap(1013,2141,Bitmap.Config.ARGB_8888)
        val imagePath = File(applicationContext.getExternalFilesDir(null).toString() + "/gamescreen.jpg")
        val fos = FileOutputStream(imagePath)
        image?.compress(Bitmap.CompressFormat.JPEG,100,fos)
        fos.flush()
        fos.close()

        val uri = FileProvider.getUriForFile(applicationContext,applicationContext.packageName+".provider",imagePath)
        val message = "Hey, Let's play this cool game 3 of Spades (Kaali ki Teeggi) online. \n\nInstall from below link \nFor Android:  \n${getString(R.string.playStoreLink)}\n\n For iOS: Coming soon..."
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_TITLE,"Share app")
        intent.putExtra(Intent.EXTRA_TEXT,message)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try{
            startActivity(Intent.createChooser(intent,"Invite friends via "))
        }catch(me: Exception){
//            toastCenter(me.toString())
        }
    }
    fun howToPlay(view: View){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://sites.google.com/view/kaali-ki-teeggi/")
        }
        startActivity(intent)
    }
    fun openRatingWindow(view: View){
//        backButtonPressedStatus = false
        anim(findViewById(R.id.rateUsLayout),R.anim.zoomin_center)
        anim(findViewById(R.id.rateUsIcon1),R.anim.anim_scale_appeal)
        findViewById<RelativeLayout>(R.id.rateUsLayout).visibility = View.VISIBLE
        ratingWindowOpenStatus = true
    }
    fun closeRatingWindow(view: View){
//        anim(findViewById(R.id.rateUsLayout),R.anim.zoomout_center)
        findViewById<ShimmerFrameLayout>(R.id.rateUsIcon1).clearAnimation()
        findViewById<RelativeLayout>(R.id.rateUsLayout).visibility = View.GONE
        ratingWindowOpenStatus = false
    }
    fun askLaterRating(view:View){ // request for rating after x days from today if choose ask later
        logFirebaseEvent("rate_us", 1, "rate_later")
        closeRatingWindow(View(applicationContext))
        ratingRequestDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt() + requestRatingAfterDays
        editor.putInt("ratingRequestDate", ratingRequestDate)
        editor.apply()
        if(backButtonPressedStatus) moveTaskToBack(true)
        backButtonPressedStatus = false
    }
    fun rateUs(view: View){ // once clicked never ask to rate again
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.kaalikiteeggi.three_of_spades")
            setPackage("com.android.vending")
        }
        startActivity(intent)
        if(view.tag == "good") logFirebaseEvent("rate_us", 1, "rate_good")
        else if(view.tag == "bad") logFirebaseEvent("rate_us", 1, "rate_bad")
        rated = true
        editor.putBoolean("rated",rated)
        editor.apply()
    }
    private fun checkRatingRequest(): Boolean{
        return !rated && (SimpleDateFormat("yyyyMMdd").format(Date()).toInt() >= ratingRequestDate)
    }
    override fun onBackPressed() { //minimize the app and avoid destroying the activity

        if(!(joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus) && ratingWindowOpenStatus && backButtonPressedStatus) {
            moveTaskToBack(true)
            closeRatingWindow(View(applicationContext))
            backButtonPressedStatus = false
        }
        else if(!(joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus || ratingWindowOpenStatus) && checkRatingRequest()) {
            backButtonPressedStatus = true
            openRatingWindow(View(applicationContext))
        }
        else if(! (joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus || createRoomWindowStatus || ratingWindowOpenStatus)) {
            moveTaskToBack(true)
        } // none should be visible
       else if(ratingWindowOpenStatus) {
            closeRatingWindow(View(applicationContext))
        }
       if(joinRoomWindowStatus) joinRoomWindowExit(View(applicationContext))
       if(createRoomWindowStatus) createRoomWindowExit(View(applicationContext))
        if(settingsWindowStatus) closeSettingsWindow(View(applicationContext))
        if(playerStatsWindowStatus) openClosePlayerStats(View(applicationContext))
//           super.onBackPressed()
    }
    fun signOut(view: View){
        if(vibrateStatus) vibrationStart()
        mAuth.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(applicationContext, StartScreen::class.java))
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
        finish()
    }
    private fun deleteAllRoomdata(roomID: String){
        refRoomData.document(roomID + "_chat").delete()
        refRoomData.document(roomID).delete()
        Firebase.database.getReference("GameData/$roomID").removeValue()
    }
    override fun onPause() {
        super.onPause()
        if(musicStatus) soundBkgd.pause()
    }
    override fun onResume() {
        super.onResume()
        if(musicStatus && this::soundBkgd.isInitialized) soundBkgd.start()
    }
    override fun onDestroy() {
        billingClient.endConnection()
        try{
            textToSpeech.shutdown()
        }catch(me:java.lang.Exception) {}
        super.onDestroy()

    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
    if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null){
        for(purchase in purchases){
            if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                    soundSuccess.start()
                toastCenter("Successful purchase")
                lifecycleScope.launch{acknowledgePurchase(purchase)}
            }else if(purchase.purchaseState == Purchase.PurchaseState.PENDING){
                if(soundStatus) soundUpdate.start()
                toastCenter("Your Payment is processing \nWe will update you after finishing processing")
            }
        }
    } else if(billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED){
        soundError.start()
        toastCenter("Payment Cancelled")
    } else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
        soundError.start()
        toastCenter("Already processing pending item")
    } else {
        soundError.start()
        toastCenter("Payment Failed. Try again \n ${billingResult.responseCode}")
    }
}
}
//    private fun setupSinchClient(){
//        val context = this.applicationContext
//        sinchClient = Sinch.getSinchClientBuilder().context(context)
//            .applicationKey(getString(R.string.sinch_api_key))
//            .applicationSecret(getString(R.string.sinch_secret))
//            .environmentHost("clientapi.sinch.com")
//            .userId(uid)
//            .build()
//        sinchClient.checkManifest()
//        sinchClient.setSupportActiveConnectionInBackground(true)
//        sinchClient.setSupportCalling(true)
//        sinchClient.startListeningOnActiveConnection()
//
//        sinchClient.addSinchClientListener(object : SinchClientListener{
//            override fun onClientStarted(p0: SinchClient?) {
//                toastCenter("SInch Client Started")
//            }
//            override fun onClientStopped(p0: SinchClient?) {
//                toastCenter("SInch Client stopped")
//            }
//            override fun onRegistrationCredentialsRequired(
//                p0: SinchClient?,
//                p1: ClientRegistration?
//            ) {
//                toastCenter("SInch Client credentails required")
//            }
//            override fun onLogMessage(p0: Int, p1: String?, p2: String?) {
//                toastCenter("SInch Client log message p1=  $p1  p2 =  $p2 ")
//            }
//            override fun onClientFailed(p0: SinchClient?, p1: SinchError?) {
//                toastCenter("SInch Client failed")
//            }
//        })
//     sinchClient.start()
//
//        val callClient = sinchClient.callClient
//        callClient.addCallClientListener { _, p1 -> toastCenter(" ${p1.callId}   ${p1.details}   ${p1.remoteUserId}") }
//    }
//    private fun makeCall(){
//         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),0);
//        } else {
//             val callClient = sinchClient.callClient
//             val call = callClient.callUser("t6c8Bgx0OihFkTOgoAjC7o7FLE52")
//             call.addCallListener(object : CallListener{
//                 override fun onCallEstablished(p0: Call?) {
//                     toastCenter("Call Established")
//                 }
//                 override fun onCallProgressing(p0: Call?) {
//                     toastCenter("Call Progressing")
//                 }
//                 override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
//                     toastCenter("Call onShouldSendPushNotification")
//                 }
//                 override fun onCallEnded(p0: Call?) {
//                     toastCenter("Call Ended")
//                 }
//             })
//         }
//    }
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun recordAudio(){
//        val recorder = MediaRecorder()
//        val outputFile = File(applicationContext.getExternalFilesDir(null).toString() + "/abc.3gp")
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            toastCenter("true")
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),0);
//        } else {
//            toastCenter("false")
//            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
//            recorder.setOutputFile(outputFile);
//            recorder.prepare()
//            recorder.start()
//            Handler().postDelayed({recorder.stop()},4000) }
//    }