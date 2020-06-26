@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerTextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.io.FileOutputStream

class CreatenJoinRoomScreen : AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundSuccess:MediaPlayer
    private lateinit var soundBkgd:MediaPlayer

    private lateinit var v:Vibrator
    private val myRefGameData = Firebase.database.getReference("GameData") // initialize database reference
    private var refRoomData = Firebase.firestore.collection("Rooms")
    private lateinit var registration: ListenerRegistration

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private var nPlayers = 0
    private lateinit var toast: Toast
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var shimmer: Shimmer
    private lateinit var mAuth: FirebaseAuth
    private lateinit var roomData: Map<String, Any>
    private var p1Status = false
    private var p2Status = false
    private var p3Status = false
    private var p4Status = false
    private var p5Status = false
    private var p6Status = false
    private var p7Status = false
    private var musicStatus = true
    private var soundStatus = true
    private var vibrateStatus = true
    private var premiumStatus = false
    private lateinit var p1:String
    private var versionStatus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var t1: Target
    private lateinit var t2: Target
    private lateinit var t3: Target
    private lateinit var t4: Target
    private lateinit var t5: Target
    private lateinit var t6: Target
    private lateinit var t7: Target
    private lateinit var playerInfo: ArrayList<String>
    private lateinit var playerInfoCoins: ArrayList<Int>
    private var p5c = 0
    private var p6c = 0
    private var p7c = 0
    private lateinit var p5: String
    private lateinit var p6: String
    private lateinit var p7: String
    private lateinit var p5h: String
    private lateinit var p6h: String
    private lateinit var p7h: String

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

         setContentView(R.layout.activity_create_join_room_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode

        if (sharedPreferences.contains("themeColor")) {
//            changeBackground(sharedPreferences.getString("themeColor", "shine_bk").toString())
        }
        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundSuccess= MediaPlayer.create(applicationContext,R.raw.player_success_chime)
        soundBkgd = MediaPlayer.create(applicationContext, R.raw.main_screen_bkgd)
        soundBkgd.isLooping = true
        toast = Toast.makeText(applicationContext,"",Toast.LENGTH_SHORT)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F

        updateUIandAnimateElements()
        getSharedPrefs()
        getRoomLiveUpdates()  // keep updating the screen as the users join
        if(getString(R.string.test).contains('n')) initializeAds()
        createTargetPicasso()
    }

    override fun onStart() {
        super.onStart()
        if (musicStatus) soundBkgd.start()
    }

    private fun getRoomLiveUpdates(){
        registration = refRoomData.document(roomID).addSnapshotListener{ dataSnapshot, error ->
            if (dataSnapshot != null && dataSnapshot.exists() && error == null) {
                 roomData = dataSnapshot.data as Map<String, Any>
                updateRoomInfo(dataSnapshot)
            }else if (dataSnapshot != null && !dataSnapshot.exists()) {
                soundError.start()
                toastCenter("Sorry $selfName \n$p1 has removed the room. \nYou can create your own room or join other")
                Handler().postDelayed({closeJoiningRoom(View(applicationContext))},4000)
            } else if(error != null){
                    toastCenter(error.localizedMessage!!.toString())
                Handler().postDelayed({closeJoiningRoom(View(applicationContext))},4000)
            }
        }
    }
    private  fun updateRoomInfo(dataSnapshot: DocumentSnapshot? ) {
        val playerJoining = dataSnapshot?.data?.get("PJ").toString().toInt()
        if(vibrateStatus) vibrationStart()
        if(playerJoining in 2..6 && "p$playerJoining" != from) soundUpdate.start()
        p1 = dataSnapshot?.data?.get("p1").toString()
        val p1h = dataSnapshot?.data?.get("p1h").toString()
        val p2 = dataSnapshot?.data?.get("p2").toString()
        val p3 = dataSnapshot?.data?.get("p3").toString()
        val p4 = dataSnapshot?.data?.get("p4").toString()
        val p2h = dataSnapshot?.data?.get("p2h").toString()
        val p3h = dataSnapshot?.data?.get("p3h").toString()
        val p4h = dataSnapshot?.data?.get("p4h").toString()
        val p1c = dataSnapshot?.data?.get("p1c").toString().toInt()
        val p2c = dataSnapshot?.data?.get("p2c").toString().toInt()
        val p3c = dataSnapshot?.data?.get("p3c").toString().toInt()
        val p4c = dataSnapshot?.data?.get("p4c").toString().toInt()

        if(nPlayers==7){
            p5c = dataSnapshot?.data?.get("p5c").toString().toInt()
            p6c = dataSnapshot?.data?.get("p6c").toString().toInt()
            p7c = dataSnapshot?.data?.get("p7c").toString().toInt()
            p5 = dataSnapshot?.data?.get("p5").toString()
            p6 = dataSnapshot?.data?.get("p6").toString()
            p7 = dataSnapshot?.data?.get("p7").toString()
            p5h = dataSnapshot?.data?.get("p5h").toString()
            p6h = dataSnapshot?.data?.get("p6h").toString()
            p7h = dataSnapshot?.data?.get("p7h").toString()
            if(p5.isNotEmpty() && p5h.isNotEmpty() && !p5Status){
                findViewById<AppCompatButton>(R.id.player5Text).text = p5
                Picasso.get().load(p5h).transform(CircleTransform()).into(t5)
                findViewById<AppCompatButton>(R.id.player5Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
                p5Status = true
            }
            if(p6.isNotEmpty() && p6h.isNotEmpty() && !p6Status){
                findViewById<AppCompatButton>(R.id.player6Text).text = p6
                Picasso.get().load(p6h).transform(CircleTransform()).into(t6)
                findViewById<AppCompatButton>(R.id.player6Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
                p6Status = true
            }
            if(p7.isNotEmpty() && p7h.isNotEmpty() && !p7Status){
                findViewById<AppCompatButton>(R.id.player7Text).text = p7
                Picasso.get().load(p7h).transform(CircleTransform()).into(t7)
                findViewById<AppCompatButton>(R.id.player7Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
                p7Status = true
            }
        }
        if(!p1Status){
            findViewById<Button>(R.id.hostName).text = p1
            Picasso.get().load(p1h).transform(CircleTransform()).into(t1)
            p1Status = true
        }
        if(p2.isNotEmpty() && p2h.isNotEmpty() && !p2Status){
            findViewById<AppCompatButton>(R.id.player2Text).text = p2
            Picasso.get().load(p2h).transform(CircleTransform()).into(t2)
            findViewById<AppCompatButton>(R.id.player2Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
            p2Status = true
        }
        if(p3.isNotEmpty() && p3h.isNotEmpty() && !p3Status){
            findViewById<AppCompatButton>(R.id.player3Text).text = p3
            Picasso.get().load(p3h).transform(CircleTransform()).into(t3)
            findViewById<AppCompatButton>(R.id.player3Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
            p3Status = true
        }
        if(p4.isNotEmpty() && p4h.isNotEmpty() && !p4Status){
            findViewById<AppCompatButton>(R.id.player4Text).text = p4
            Picasso.get().load(p4h).transform(CircleTransform()).into(t4)
            findViewById<AppCompatButton>(R.id.player4Text).setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))
            p4Status = true
        }

        if(playerJoining==nPlayers){
            soundSuccess.start()
//            findViewById<ProgressBar>(R.id.progressBarMain).isIndeterminate = false
//            findViewById<ProgressBar>(R.id.progressBarMain).progress = 100
            findViewById<ProgressBar>(R.id.progressBarMain).visibility = View.GONE
            findViewById<AppCompatButton>(R.id.shareText).clearAnimation()
            findViewById<ImageView>(R.id.imageViewShareButton).clearAnimation()
            findViewById<AppCompatButton>(R.id.shareText).visibility = View.GONE
            findViewById<ImageView>(R.id.imageViewShareButton).visibility = View.GONE
            findViewById<ShimmerTextView>(R.id.waitingToJoinText).text = getString(R.string.playerJoinedConfirmation)
            findViewById<ShimmerTextView>(R.id.waitingToJoinText).setTextColor(ContextCompat.getColor(applicationContext,R.color.white))
            shimmer.cancel()
            findViewById<ImageView>(R.id.startGameButton).visibility = View.VISIBLE
            anim(findViewById(R.id.startGameButton),R.anim.blink_infinite_700ms)
            anim(findViewById(R.id.waitingToJoinText),R.anim.blink_infinite_700ms)
        }
        if(playerJoining==10){
            registration.remove()
            playerInfo = ArrayList()
            playerInfoCoins = ArrayList()
            if(nPlayers==7) {
                playerInfo.addAll(listOf(p1, p2, p3, p4, p5, p6, p7, p1h, p2h, p3h, p4h, p5h, p6h, p7h))
                playerInfoCoins.addAll(listOf(p1c, p2c, p3c, p4c, p5c, p6c, p7c))
            }
            else if(nPlayers == 4){
                playerInfo.addAll(listOf(p1, p2, p3, p4, p1h, p2h, p3h, p4h))
                playerInfoCoins.addAll(listOf(p1c, p2c, p3c, p4c))
            }
            when {
                premiumStatus -> startNextActivity()
//                mInterstitialAd.isLoaded -> {
//                    mInterstitialAd.show() // dummy - check this implementation - Synchronize premium and non premium - or Interstitial with only images
//                }
                else -> {
                    startNextActivity()
                }
            }
        }
    }
    fun startGame(view: View){
        if(from=="p1") {
            val gd = if(getString(R.string.testGameData).contains('n')) {
                CreateGameData().gameData
            }else{
                CreateGameData().gameDataDummy
            }
            myRefGameData.child(roomID).setValue(gd).addOnSuccessListener {
                refRoomData.document(roomID).set(hashMapOf("PJ" to 10), SetOptions.merge())
                    .addOnSuccessListener {
                        findViewById<ImageView>(R.id.startGameButton).clearAnimation()
                        findViewById<ImageView>(R.id.startGameButton).visibility = View.GONE
                        refRoomData.document(roomID+"_chat").set(hashMapOf( "M" to ""))
                }
            }
        }
        else{
            soundError.start()
            toastCenter("Host will START")
        }
    }
    private fun createTargetPicasso() {
        t1 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<ImageView>(R.id.hostPhoto).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t2 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player2Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t3 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player3Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t4 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player4Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t5 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player5Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t6 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player6Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
        t7 = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    findViewById<GifImageView>(R.id.player7Photo).setImageDrawable(bitmap.toDrawable(resources))
                }
            }
        }
    }
    private fun initializeAds(){
        if(!premiumStatus){
//            MobileAds.initialize(this)
            findViewById<AdView>(R.id.addViewCreateJoinRoom).visibility = View.VISIBLE
            findViewById<AdView>(R.id.addViewCreateJoinRoom).loadAd(AdRequest.Builder().build())
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = resources.getString(R.string.interstitial)
            mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    startNextActivity()
                }
            }
        }
        else  {
            findViewById<AdView>(R.id.addViewCreateJoinRoom).visibility = View.GONE
        }
    }
    private fun startNextActivity(){
        soundUpdate.start()
        startActivity(Intent(this@CreatenJoinRoomScreen,GameScreen::class.java).apply { putExtra("selfName",selfName) }
            .apply { putExtra("from",from) }.apply { putExtra("nPlayers", nPlayers) }
            .apply { putExtra("roomID",roomID) }.putStringArrayListExtra("playerInfo",playerInfo).putIntegerArrayListExtra("playerInfoCoins",playerInfoCoins))
        overridePendingTransition(R.anim.slide_top_in_activity,R.anim.slide_top_in_activity)
        Handler().postDelayed({finish()},400)
//        finish()
    }
    private fun updateUIandAnimateElements(){
        roomID   = intent.getStringExtra("roomID")!!.toString()    //Get roomID and display
        selfName = intent.getStringExtra("selfName")!!.toString()  //Get Username first  - selfName ,roomID available
        from     = intent.getStringExtra("from")!!.toString()     //check if user has joined room or created one and display Toast
        nPlayers = intent.getIntExtra("nPlayers", 4)
        findViewById<ImageView>(R.id.imageViewShareButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
        findViewById<Button>(R.id.button_roomID).text = "Room : $roomID"   // display the room ID

        if(from=="p1") { //  close icon only to host
            findViewById<ImageView>(R.id.leaveJoiningRoomIcon).visibility = View.VISIBLE
            anim(findViewById(R.id.leaveJoiningRoomIcon),R.anim.anim_scale_infinite)
        }
        else{ //dummy diabaled
//            findViewById<ImageView>(R.id.leaveJoiningRoomIcon).visibility = View.GONE
//            findViewById<ImageView>(R.id.leaveJoiningRoomIcon).clearAnimation()
        }
        shimmer = Shimmer()
        shimmer.duration = 1800
        shimmer.start(findViewById<ShimmerTextView>(R.id.waitingToJoinText))
        anim(findViewById(R.id.roomIDIcon),R.anim.clockwise_ccw_infinite)
        anim(findViewById(R.id.player2Text),R.anim.slide_buttons)
        anim(findViewById(R.id.player3Text),R.anim.slide_buttons_rtl)
        anim(findViewById(R.id.player4Text),R.anim.slide_buttons)
        anim(findViewById(R.id.shareText),R.anim.slide_buttons)
        anim(findViewById(R.id.button_roomID),R.anim.slide_buttons_rtl)
        anim(findViewById(R.id.hostPhoto),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.player2Photo),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.player3Photo),R.anim.anim_scale_infinite)
        anim(findViewById(R.id.player4Photo),R.anim.anim_scale_infinite)

        if(nPlayers==7){
            findViewById<ShimmerFrameLayout>(R.id.player7Shimmer).visibility = View.VISIBLE
            findViewById<ShimmerFrameLayout>(R.id.player6Shimmer).visibility = View.VISIBLE
            findViewById<ShimmerFrameLayout>(R.id.player5Shimmer).visibility = View.VISIBLE
            findViewById<GifImageView>(R.id.player7Photo).visibility = View.VISIBLE
            findViewById<GifImageView>(R.id.player6Photo).visibility = View.VISIBLE
            findViewById<GifImageView>(R.id.player5Photo).visibility = View.VISIBLE
            findViewById<AppCompatButton>(R.id.player7Text).visibility = View.VISIBLE
            findViewById<AppCompatButton>(R.id.player6Text).visibility = View.VISIBLE
            findViewById<AppCompatButton>(R.id.player5Text).visibility = View.VISIBLE
            anim(findViewById(R.id.player5Photo),R.anim.anim_scale_infinite)
            anim(findViewById(R.id.player6Photo),R.anim.anim_scale_infinite)
            anim(findViewById(R.id.player7Photo),R.anim.anim_scale_infinite)
            anim(findViewById(R.id.player5Text),R.anim.slide_buttons_rtl)
            anim(findViewById(R.id.player6Text),R.anim.slide_buttons)
            anim(findViewById(R.id.player7Text),R.anim.slide_buttons_rtl)
        }else{
            findViewById<ShimmerFrameLayout>(R.id.player7Shimmer).visibility = View.GONE
            findViewById<ShimmerFrameLayout>(R.id.player6Shimmer).visibility = View.GONE
            findViewById<ShimmerFrameLayout>(R.id.player5Shimmer).visibility = View.GONE
            findViewById<GifImageView>(R.id.player7Photo).visibility = View.GONE
            findViewById<GifImageView>(R.id.player6Photo).visibility = View.GONE
            findViewById<GifImageView>(R.id.player5Photo).visibility = View.GONE
            findViewById<AppCompatButton>(R.id.player7Text).visibility = View.GONE
            findViewById<AppCompatButton>(R.id.player6Text).visibility = View.GONE
            findViewById<AppCompatButton>(R.id.player5Text).visibility = View.GONE
        }
    }

    fun anim(view: View,anim:Int){
        view.startAnimation(AnimationUtils.loadAnimation(applicationContext,anim))
    }
    private fun changeBackground(color: String){
        when(color){
            "shine_blue"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_blue)
            }
            "shine_bk"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_bk)
//                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_player_stats)

            }
            "shine_orange"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_orange)
            }
            "shine_green_radial"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_green_radial)
            }
            "shine_pink"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_pink)
            }
            "shine_purple"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_purple)
            }
            "shine_yellow"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_yellow)
            }
            "shine_purple_dark"-> {
                findViewById<ImageView>(R.id.backgroundJoiningRoom).setImageResource(R.drawable.shine_purple_dark)
            }
        }
    }
    private fun getSharedPrefs(){
        if (sharedPreferences.contains("themeColor")) {
//            changeBackground(sharedPreferences.getString("themeColor", "shine_bk").toString())
        }
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
        }
        if (sharedPreferences.contains("musicStatus")) {
            musicStatus = sharedPreferences.getBoolean("musicStatus", true)
            if(musicStatus) soundBkgd.start()
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
        }
    }
    @SuppressLint("NewApi")
    fun vibrationStart(duration: Long = 150){
        if(versionStatus){
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            v.vibrate(duration)
        }
    }
    fun closeJoiningRoom(view: View){
        registration.remove()
        startActivity(Intent(this, MainHomeScreen::class.java)
            .apply {putExtra("newUser",false)})
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
        finish()
    }
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        moveTaskToBack(true)
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
        try{
            registration.remove()
        }catch(error: Exception){
            toastCenter(error.localizedMessage)
        }
    } // remove snapshot listener
    fun shareRoomInfo(view: View){
        soundUpdate.start()
        val screenShot = takeScreenShot(view)
        val imagePath = File(applicationContext.getExternalFilesDir(null).toString() + "/ss.jpg")
        val fos = FileOutputStream(imagePath)
        screenShot.compress(Bitmap.CompressFormat.JPEG,100,fos)
        fos.flush()
        fos.close()
        val uri = FileProvider.getUriForFile(applicationContext,applicationContext.packageName+".provider",imagePath)
        val message = "Hey, Let's play 3 of Spades(Kaali ki Teeggi) online. \n\nJoin my room - \nRoom ID $roomID \n\nInstall from below link \n" +
                "For Android:  ${getString(R.string.playStoreLink)}"
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_TITLE,"Share Room ID")
        intent.putExtra(Intent.EXTRA_TEXT,message)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try{
            startActivity(Intent.createChooser(intent,"Share Room ID via :"))
        }catch(me: Exception){
            toastCenter(me.toString()) // dummy
        }
    }
    private fun takeScreenShot(view: View): Bitmap{
        findViewById<AdView>(R.id.addViewCreateJoinRoom).visibility = View.INVISIBLE
        val b = view.rootView.drawToBitmap(Bitmap.Config.ARGB_8888)
        findViewById<AdView>(R.id.addViewCreateJoinRoom).visibility = View.VISIBLE
        return b
    }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }
}





