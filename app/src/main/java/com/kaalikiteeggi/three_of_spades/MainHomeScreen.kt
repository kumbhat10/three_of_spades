@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import DailyRewardItem
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.android.billingclient.api.*
import com.facebook.login.LoginManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerButton
import com.romainpiel.shimmer.ShimmerTextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class MainHomeScreen : AppCompatActivity(), PurchasesUpdatedListener {
    private lateinit var soundError: MediaPlayer
    private lateinit var soundBkgd: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var soundCollectCards: MediaPlayer
    private var rewardStatus = false

    private var rewardAmount = 0
    private lateinit var soundUpdate: MediaPlayer
    private var createRoomStatus = false
    private var onceAdWatched = false

    private var joinRoomWindowStatus = false
    private var settingsWindowStatus = false
    private var playerStatsWindowStatus = false

    private lateinit var toast: Toast
    private var refUsersData = Firebase.firestore.collection("Users")
    private var refRoomData = Firebase.firestore.collection("Rooms")

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var rewardedAd: RewardedAd
    private lateinit var billingClient: BillingClient
//    private lateinit var sinchClient: SinchClient
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
    private var musicStatus = true
    private var soundStatus = true
    private var vibrateStatus = true
    private var premiumStatus = false
    private val today =  SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
    private var consecutiveDay = 1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var vibrator:Vibrator
    private lateinit var shimmer: Shimmer
//    private var versionStatus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private var totalCoins = 0
    private var loadRewardedAdTry = 0
    private var countRewardWatch = 1
    private lateinit var rewardedAdLoadCallback: RewardedAdLoadCallback
    private var dailuRewardList = listOf(250,500,1000,2000,3000,4000,5000)
    private var dailyRewardAmount = 0
    private var dailyRewardClicked = false
    private var claimedToday = false
    private var dailyRewardStatus = false

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable._s_icon_3shadow_bug11) //default: bug image
            .apply()

        setContentView(R.layout.activity_main_home_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        findViewById<GifImageView>(R.id.watchVideo).visibility = View.GONE
        soundUpdate = MediaPlayer.create(applicationContext, R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext, R.raw.error_entry)
        soundSuccess = MediaPlayer.create(applicationContext, R.raw.player_success_chime)
        soundBkgd = MediaPlayer.create(applicationContext, R.raw.main_screen_bkgd)
        soundCollectCards = MediaPlayer.create(applicationContext, R.raw.collect_cards)
        soundBkgd.isLooping = true
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.view.setBackgroundColor(
            ContextCompat.getColor(
                   applicationContext,
                   R.color.cardsBackgroundDark
               )
           )
           toast.view.findViewById<TextView>(android.R.id.message)
               .setTextColor(ContextCompat.getColor(applicationContext, R.color.font_yellow))
           toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F

           sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
           editor = sharedPreferences.edit()
           shimmer = Shimmer()
           shimmer.duration = 1800

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.uid.toString()

        if(intent.getBooleanExtra("errorStatus",false)){
            toastCenter("Booh !!!!\n The server is in the spirit World ")
        }
        initializeAds()
        getUserData()
        getSharedPrefs()
        animateElements()
        enterText() // press enter to join room
        setupBillingClient()
    }

    override fun onStart() {
        super.onStart()
        if(soundStatus) soundUpdate.start()
        if (musicStatus) soundBkgd.start()
        if (mInterstitialAd.isLoaded) {
            if (!premiumStatus) mInterstitialAd.show()
        } else if (!premiumStatus && getString(R.string.test).contains('n')) mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun showNotification(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificBuilder = NotificationCompat.Builder(applicationContext, "ch_id")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.icon_yellow))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentTitle(getString(R.string.noti_DR_title))
                .setContentText(getString(R.string.noti_DR_info))

            val notificationChannel = NotificationChannel(getString(R.string.default_notification_channel_id), "My Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.vibrationPattern = longArrayOf(0, 700, 500, 700)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }else {

            notificBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.icon_yellow))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(getString(R.string.noti_DR_title))
                .setContentText(getString(R.string.noti_DR_info))
        }

        val notificIntent = Intent(this,MainHomeScreen::class.java).apply {putExtra("newUser",false)}
        val contentIntent = PendingIntent.getActivity(this,0, notificIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificBuilder.setContentIntent(contentIntent)
        notificationManager.notify(0, notificBuilder.build())
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
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.INVISIBLE
            loadRewardAd()
        }else{
            findViewById<LinearLayout>(R.id.dailyRewardGridLayout).visibility = View.GONE
            addPoints()
            findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.INVISIBLE
            loadRewardAd()
            if (premiumStatus && getString(R.string.test).contains('n')) mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }
    private fun setDataList(): ArrayList<DailyRewardItem>{
       val arrayList = ArrayList<DailyRewardItem>()
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 1 \n${dailuRewardList[0]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 2 \n${dailuRewardList[1]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 3 \n${dailuRewardList[2]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 4 \n${dailuRewardList[3]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 5 \n${dailuRewardList[4]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 6 \n${dailuRewardList[5]} coins"))
        arrayList.add(DailyRewardItem(R.drawable.coin_trans_1, "Day 6+ \n${dailuRewardList[6]} coins"))
        return arrayList
    }
    @SuppressLint("SimpleDateFormat")
    private fun getUserData(){
        val user = mAuth.currentUser
        if(user != null) {
            userName = user.displayName.toString().split(" ")[0]
            findViewById<Button>(R.id.welcomeUserNameview).text = userName
            if(intent.getBooleanExtra("newUser",false)) {//check if user has joined room or created one and display Toast
                toastCenter("Hi $userName") // dummy - add tutorial
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
                    findViewById<AppCompatButton>(R.id.userScore).text =
                        String.format("%,d", totalCoins)
                    val ngamesPlayedStats = dataSnapshot.get("p").toString().toInt()
                    val ngamesBidedStats = dataSnapshot.get("b").toString().toInt()
                    val ngamesWonStats = dataSnapshot.get("w").toString().toInt()
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
                           dailyRewardWindowDisplay()},1500)
                    }
                    findViewById<ShimmerTextView>(R.id.ngamesPlayedStats).text =
                        ngamesPlayedStats.toString()
                    findViewById<ShimmerTextView>(R.id.ngamesBidedStats).text =
                        ngamesBidedStats.toString()
                    findViewById<ShimmerTextView>(R.id.ngamesWonStats).text =
                        ngamesWonStats.toString()
                    editor.putString("photoURL", photoURL) // write username to preference file
                    editor.apply()
                    editor.putBoolean("premium", premiumStatus) // write username to preference file
                    editor.apply()
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
    private fun getSharedPrefs(){
        if (sharedPreferences.contains("themeColor")) {
            changeBackground(sharedPreferences.getString("themeColor", "shine_bk").toString())
        }
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
            if(premiumStatus) findViewById<GifImageView>(R.id.removeAds).setImageResource(R.drawable.premium)
        }
        if (sharedPreferences.contains("musicStatus")) {
            musicStatus = sharedPreferences.getBoolean("musicStatus", true)
            findViewById<SwitchCompat>(R.id.musicSwitch).isChecked = musicStatus
            if(musicStatus) {
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
//               Handler().postDelayed({deleteAllRoomdata(roomID)},2000)
                deleteAllRoomdata(roomID)
            }
            editor.remove("Room").apply()
        }
    }
    fun removeAdsorPremium(view: View){
//        showNotification()
        if(premiumStatus) openClosePlayerStats(View(applicationContext))
        else {
            if(vibrateStatus) vibrationStart()
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
                toastCenter("Billing Service was disconnected")
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
        mInterstitialAd = InterstitialAd(this)
        if(getString(R.string.test).contains('n')) mInterstitialAd.adUnitId = getString(R.string.interstitial)
        if(getString(R.string.test).contains('n')) findViewById<AdView>(R.id.addViewMHS).loadAd(AdRequest.Builder().build()) // banner add load
        if(getString(R.string.test).contains('n'))  mInterstitialAd.loadAd(AdRequest.Builder().build()) // interstitial add load
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                onceAdWatched = true
                if(dailyRewardClicked) {
                    addPoints()
                    dailyRewardClicked = false
                } else if(createRoomStatus){
                    createRoom()
                    createRoomStatus
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
//        AdColonyBundleBuilder.setShowPrePopup(true)
//        AdColonyBundleBuilder.setShowPostPopup(true)
        loadRewardedAdTry += 1
        rewardedAd = RewardedAd(applicationContext, getString(R.string.rewarded))
        if(getString(R.string.test).contains('n'))  rewardedAd.loadAd(AdRequest.Builder()
//            .addNetworkExtrasBundle(AdColonyAdapter::class.java,AdColonyBundleBuilder.build())
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
//                    when(errorCode){//dummy
//                        ERROR_CODE_INTERNAL_ERROR -> toastCenter("ERROR_CODE_INTERNAL_ERROR")
//                        ERROR_CODE_AD_REUSED  -> toastCenter("ERROR_CODE_AD_REUSED ")
//                        ERROR_CODE_NOT_READY  -> toastCenter("ERROR_CODE_NOT_READY ")
//                        ERROR_CODE_APP_NOT_FOREGROUND  -> toastCenter("ERROR_CODE_APP_NOT_FOREGROUND ")
//                    }
                }
            }
            rewardedAd.show(activityContext, adCallback)
        }
        else{
            findViewById<GifImageView>(R.id.watchVideo).clearAnimation()
            findViewById<GifImageView>(R.id.watchVideo).visibility = View.INVISIBLE
            loadRewardAd()
        }
    }

    fun openClosePlayerStats(view: View){
        playerStatsWindowStatus = !playerStatsWindowStatus
        if(playerStatsWindowStatus){
            if(soundStatus) soundUpdate.start()
            if(vibrateStatus) vibrationStart()
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
        if(vibrateStatus) vibrationStart()

        findViewById<RelativeLayout>(R.id.settingsLayout).visibility = View.VISIBLE
        Handler().postDelayed({
            findViewById<ImageView>(R.id.closeSettings).visibility = View.VISIBLE
            anim(findViewById(R.id.closeSettings),R.anim.anim_scale_infinite)
        }, 220)

        anim(findViewById(R.id.settingsLayouttemp),R.anim.zoomin)
        anim(findViewById(R.id.settMusicIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.settSoundIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.settVibrateIcon),R.anim.anim_scale_infinite)
    }
    fun closeSettingsWindow(view: View){
        settingsWindowStatus = false
        if(vibrateStatus) vibrationStart()

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
        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched) {
            mInterstitialAd.show()
        } else {
            createRoom()
        }
    }
    private fun createRoom() {
        if (createRoomStatus) {
            val allowedChars = ('A'..'H')+ ('J'..'N') + ('P'..'Z')+('2'..'9')  // 1, I , O and 0 skipped
            val roomID = (1..4).map { allowedChars.random() }.joinToString ("")
            val nPlayers = getString(R.string.nPlayers).toInt()
            if(nPlayers==7) {
                if(getString(R.string.testGameData).contains('n'))   refRoomData.document(roomID).set(CreateRoomData(userName, photoURL).data7)
                else refRoomData.document(roomID).set(CreateRoomData(userName, photoURL).dummyData7)
            }
           else if(nPlayers==4){
                if(getString(R.string.testGameData).contains('n'))   refRoomData.document(roomID).set(CreateRoomData(userName, photoURL).data4)
                else refRoomData.document(roomID).set(CreateRoomData(userName, photoURL).dummyData4)
            }

            soundSuccess.start()
            startActivity(Intent(applicationContext, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
                .apply { putExtra("selfName", userName) }.apply { putExtra("from", "p1") }.apply { putExtra("nPlayers", nPlayers) })
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
            editor.putString("Room", roomID) // write room ID in storage - to delete later
            editor.apply()
            Handler().postDelayed({finish()},500)
        }
    }
    fun joinRoomButtonClicked(view: View) {
        if(vibrateStatus) vibrationStart()
        val roomID = findViewById<EditText>(R.id.roomIDInput).text.toString()//read text field
        if (roomID.isNotEmpty()) {
            refRoomData.document(roomID).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.data != null) {
                    val playersJoined = dataSnapshot.get("PJ").toString().toInt()
                    val nPlayers = dataSnapshot.get("n").toString().toInt()
                    if (playersJoined >= nPlayers) {
                        soundError.start()
                        if(vibrateStatus) vibrationStart()
                        findViewById<EditText>(R.id.roomIDInput).hint = "Room is Full"
                        findViewById<EditText>(R.id.roomIDInput).text.clear()
                    } else {
                        if(vibrateStatus) vibrationStart()
                        soundSuccess.start()
                        val playerJoining = playersJoined + 1
                        refRoomData.document(roomID).set(hashMapOf("p$playerJoining" to userName, "PJ" to playerJoining, "p${playerJoining}h" to photoURL), SetOptions.merge())
                            .addOnSuccessListener {
                                startActivity(Intent(applicationContext, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID", roomID) }
                                    .apply { putExtra("selfName", userName) }.apply { putExtra("from", "p$playerJoining") }.apply {putExtra("nPlayers", nPlayers)})
                                overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
                                Handler().postDelayed({finish()},3000)} }
                }else {
                    soundError.start()
                    if(vibrateStatus) vibrationStart()
                    findViewById<EditText>(R.id.roomIDInput).hint = "Wrong ID"
                    findViewById<EditText>(R.id.roomIDInput).text.clear()
                }
            }.addOnFailureListener {exception -> toastCenter(exception.localizedMessage!!.toString()) }
        } else {
            soundError.start()
            vibrationStart()
            findViewById<EditText>(R.id.roomIDInput).hint = "Enter ID"
        }
    }

    fun joinRoomWindowOpen(view: View) {
        if(soundStatus) soundUpdate.start()
        if(vibrateStatus) vibrationStart()
        if (mInterstitialAd.isLoaded && !premiumStatus && !dailyRewardStatus && !onceAdWatched) {
            mInterstitialAd.show()
        }
        Handler().postDelayed({
            findViewById<ImageView>(R.id.closeJoinRoom).visibility = View.VISIBLE
            anim(findViewById(R.id.closeJoinRoom),R.anim.anim_scale_infinite)
        }, 270)
        findViewById<RelativeLayout>(R.id.joinRoomFrame).visibility = View.VISIBLE
        anim(findViewById(R.id.joinRoomFrameTemp),R.anim.zoomin)
        anim(findViewById(R.id.closeJoinRoom),R.anim.zoomin)
        anim(findViewById(R.id.joinRoomFrameIcon),R.anim.clockwise)
        joinRoomWindowStatus = true
    }
    fun joinRoomWindowExit(view: View) {
        if(vibrateStatus) vibrationStart()
        findViewById<ImageButton>(R.id.joinRoomFrameIcon).clearAnimation()
        findViewById<ImageView>(R.id.closeJoinRoom).clearAnimation()
        findViewById<ImageView>(R.id.closeJoinRoom).visibility = View.INVISIBLE
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
        if(vibrateStatus) vibrationStart()
        if(soundStatus) soundUpdate.start()//Pass username and current activity alias to be able to come back with same info
        startActivity(Intent(this,DeveloperCredits::class.java))
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

        shimmer.start(findViewById<ShimmerButton>(R.id.developerCreditsButton))
        shimmer.start(findViewById<ShimmerTextView>(R.id.musicSettText))
        shimmer.start(findViewById<ShimmerTextView>(R.id.soundSettText))
        shimmer.start(findViewById<ShimmerTextView>(R.id.vibrateSettText))
        shimmer.start(findViewById<ShimmerTextView>(R.id.playerStatsTitle))
        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesPlayedStats))
        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesWonStats))
        shimmer.start(findViewById<ShimmerTextView>(R.id.gamesBidedStats))
        shimmer.start(findViewById<ShimmerTextView>(R.id.dailyRewardsTitle))

        anim(findViewById(R.id.developerIcon),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.claimReward),R.anim.anim_scale_appeal)
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
        editor.putBoolean("musicStatus",musicStatus) // write username to preference file
        editor.apply()
        if(musicStatus) {
            soundBkgd.start()
            findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.music)
        }else {
            soundBkgd.pause()
            findViewById<ImageView>(R.id.settMusicIcon).setImageResource(R.drawable.nomusic)
        }
    }
    fun sound(view: View){
        soundStatus = findViewById<SwitchCompat>(R.id.soundSwitch).isChecked
//        val editor  = sharedPreferences.edit()
        editor.putBoolean("soundStatus",soundStatus) // write username to preference file
        editor.apply()

        if(soundStatus) {
            soundUpdate.start()
            findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_on_png)
        }else findViewById<ImageView>(R.id.settSoundIcon).setImageResource(R.drawable.sound_off_png)
    }
    fun vibrate(view: View){
        vibrateStatus = findViewById<SwitchCompat>(R.id.vibrateSwitch).isChecked
        editor.putBoolean("vibrateStatus",vibrateStatus) // write username to preference file
        editor.apply()
        if(vibrateStatus) {
            vibrationStart(1000)
            findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateon)
        }else findViewById<ImageView>(R.id.settVibrateIcon).setImageResource(R.drawable.vibrateoff)

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

    fun inviteFriends(view: View){
        if(vibrateStatus) vibrationStart()
        if(soundStatus) soundUpdate.start()
        val image = ContextCompat.getDrawable(applicationContext,R.drawable.game_screen)
            ?.toBitmap(1017,2034,Bitmap.Config.ARGB_8888)
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
            startActivity(Intent.createChooser(intent,"Share app via :"))
        }catch(me: Exception){
            toastCenter(me.toString())
        }
    }
    fun howToPlay(view: View){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://sites.google.com/view/kaali-ki-teeggi/")
        }
        if(vibrateStatus) vibrationStart()
        startActivity(intent)
    }
    fun rateUs(view: View){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=com.kaalikiteeggi.three_of_spades")
            setPackage("com.android.vending")
        }
        if(vibrateStatus) vibrationStart()
        startActivity(intent)
//        throw RuntimeException("Boom!");
    }
    fun signOut(view: View){
        if(vibrateStatus) vibrationStart()
        mAuth.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(applicationContext, StartScreen::class.java))
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
        finish()
    }
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        if(!(joinRoomWindowStatus || settingsWindowStatus || playerStatsWindowStatus )) moveTaskToBack(true)
       if(joinRoomWindowStatus) joinRoomWindowExit(View(applicationContext))
        if(settingsWindowStatus) closeSettingsWindow(View(applicationContext))
        if(playerStatsWindowStatus) openClosePlayerStats(View(applicationContext))
//           super.onBackPressed()
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
        if(musicStatus) soundBkgd.start()
    }
    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
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