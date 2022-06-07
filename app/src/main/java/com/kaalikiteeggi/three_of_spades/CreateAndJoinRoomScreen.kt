@file:Suppress("UNUSED_PARAMETER", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kaalikiteeggi.three_of_spades.databinding.ActivityCreateJoinRoomScreenBinding
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class CreateAndJoinRoomScreen : AppCompatActivity() {
    // region Initialization
    private lateinit var soundBackground: MediaPlayer

    private lateinit var v: Vibrator
    private val myRefGameData = Firebase.database.getReference("GameData") // initialize database reference
    private lateinit var refRoomData: CollectionReference
    private var refUsersData = Firebase.firestore.collection("Users")

    private var tableBackground = listOf(R.drawable.table_gs_blue, R.drawable.table_gs_brown, R.drawable.table_gs_green, R.drawable.table_gs_orange, R.drawable.table_gs_pink, R.drawable.table_gs_purple, R.drawable.table_gs_red, R.drawable.table_gs_x1, R.drawable.table_gs_yellow)

    private lateinit var registration: ListenerRegistration
    private lateinit var alertDialog: AlertDialog

    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var photoURL: String
    private var offline: Boolean = true
    private lateinit var from: String
    private var nPlayers = 0
    private var fromInt = 0
    private var totalCoins = 0
    private var totalDailyCoins = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var p1Status = false
    private var p2Status = false
    private var p3Status = false
    private var p4Status = false
    private var p5Status = false
    private var p6Status = false
    private var p7Status = false
    private var musicStatus = false
    private var soundStatus = true
    private var speechStatus = true
    private var vibrateStatus = true
    private var premiumStatus = false
    private var p1 = ""
    private var p2 = ""
    private var p3 = ""
    private var p4 = ""
    private var versionStatus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private lateinit var textToSpeech: TextToSpeech
    private var closeRoom: Boolean = false

    private lateinit var playerInfo: ArrayList<String>
    private lateinit var playerInfoCoins: ArrayList<Int>
    private var p5 = ""
    private var p6 = ""
    private var p7 = ""

    private lateinit var userStats: ArrayList<Int>
    private lateinit var userStatsTotal: ArrayList<Int>
    private lateinit var userStatsDaily: ArrayList<Int>
    private lateinit var mAuth: FirebaseAuth
    private var uid = ""
    private var shareLink = ""
    private var handler = Handler(Looper.getMainLooper())
    private var userBasicInfoList = mutableListOf<UserBasicInfo>()

    private var userBasicInfoStatus = MutableLiveData<MutableList<Boolean>>()
    private lateinit var adapter: UserInfoRankingJoinRoom
    private lateinit var binding: ActivityCreateJoinRoomScreenBinding

    // endregion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable.bug_icon) //default: bug image
            .apply()
        binding = ActivityCreateJoinRoomScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            controller!!.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding.backgroundJR.setImageResource(tableBackground.random())
        refRoomData = Firebase.firestore.collection(getString(R.string.pathRoom))
        nPlayers = intent.getIntExtra("nPlayers", 0)
        offline = intent.getBooleanExtra("offline", true)

        if (!offline) {
            userBasicInfoList = MutableList(nPlayers) { UserBasicInfo() }
            userBasicInfoStatus.value = MutableList(nPlayers) { false }
        }
        binding.playersJoin.layoutManager = LinearLayoutManager(this)
        adapter = UserInfoRankingJoinRoom(this, userBasicInfoList)
        binding.playersJoin.adapter = adapter

        Handler(Looper.getMainLooper()).post {
            v = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            }
            else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            soundBackground = MediaPlayer.create(this, R.raw.music)
            soundBackground.isLooping = true
            soundBackground.setVolume(0.05F, 0.05F)
            getSharedPrefs()
            updateUIAndAnimateElements()
            initializeSpeechEngine()
            if (!offline) getRoomLiveUpdates()  // keep updating the screen as the users join
            else updateRoomInfoOffline()
        }
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.uid.toString()
        FirebaseCrashlytics.getInstance().setUserId(uid)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIAndAnimateElements() {
        roomID = intent.getStringExtra("roomID")!!.toString()    //Get roomID and display
        selfName = intent.getStringExtra("selfName")!!.toString()  //Get Username
        photoURL = intent.getStringExtra("photoURL")!!.toString()  //Get Photo URL
        totalCoins = intent.getIntExtra("totalCoins", 0)
        totalDailyCoins = intent.getIntExtra("totalDailyCoins", 0)
        from = intent.getStringExtra("from")!!.toString()     //check if user has joined room or created one and display Toast
        fromInt = from.split("")[2].toInt()
        userStats = intent.getIntegerArrayListExtra("userStats")!!
        userStatsTotal = intent.getIntegerArrayListExtra("userStatsTotal")!!
        userStatsDaily = intent.getIntegerArrayListExtra("userStatsDaily")!!
        if (!offline) {
            binding.imageViewShareButton2.visibility = View.VISIBLE
            binding.waitingToJoinText.visibility = View.VISIBLE
            binding.imageViewShareButton2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_scale_infinite))
            binding.roomIDTitle.text = "Room ID: $roomID"   // display the room ID
        } else {
            binding.offlineProgressbar.visibility = View.VISIBLE
        }
        if (fromInt == 1) { //  close icon only to host
            binding.leaveJoiningRoomIcon.visibility = View.VISIBLE
        }
        createDynamicLink()
    }

    override fun onStart() {
        super.onStart()
        if (musicStatus && this::soundBackground.isInitialized) soundBackground.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRoomInfoOffline() {
        userBasicInfoList.add(0, UserBasicInfo(empty = false, index = 0, name = selfName, score = totalCoins, photoURL = photoURL, played = userStatsTotal[0], won = userStatsTotal[1], bid = userStatsTotal[2]))
        adapter.notifyDataSetChanged()
        val data = CreateRoomData(userBasicInfoList[0]).offlineData  // create other 3 offline players data
        playerInfo = ArrayList()
        playerInfoCoins = ArrayList()
        val p1 = data[0].name
        val p2 = data[1].name
        val p3 = data[2].name
        val p4 = data[3].name
        val p1h = data[0].photoURL
        val p2h = data[1].photoURL
        val p3h = data[2].photoURL
        val p4h = data[3].photoURL
        val p1c = totalCoins
        val p2c = data[1].score
        val p3c = data[2].score
        val p4c = data[3].score

        playerInfo.addAll(listOf(p1, p2, p3, p4, p1h, p2h, p3h, p4h))
        playerInfoCoins.addAll(listOf(p1c, p2c, p3c, p4c))

        handler.postDelayed({
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            speak("$p2 joined", speed = 1f, forceSpeak = false)
            userBasicInfoList.add(1, data[1])
            adapter.notifyItemInserted(1)
        }, 800)
        handler.postDelayed({
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            speak("$p3 joined", speed = 1.1f, forceSpeak = false)
            userBasicInfoList.add(2, data[2])
            adapter.notifyItemInserted(2)
        }, 1900)
        handler.postDelayed({
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            speak("$p4 joined", speed = 1.1f, forceSpeak = false)
            userBasicInfoList.add(3, data[3])
            adapter.notifyItemInserted(3)
            Handler(Looper.getMainLooper()).postDelayed({ allPlayersJoined() }, 800)
        }, 2900)
    }

    private fun getRoomLiveUpdates() {
        registration = refRoomData.document(roomID).addSnapshotListener { dataSnapshot, error ->
            if (dataSnapshot != null && dataSnapshot.exists() && error == null) {
                updateRoomInfoOnline(dataSnapshot)
            } else if (dataSnapshot != null && !dataSnapshot.exists()) {                //				soundError.start()
                SoundManager.instance?.playErrorSound()
                toastCenter("Sorry $selfName \n$p1 has left the room")
                speak("$p1 has left", forceSpeak = false)

                Handler(Looper.getMainLooper()).postDelayed({ closeJoiningRoom() }, 4000)
            } else if (error != null) {
                toastCenter(error.localizedMessage!!.toString())
                Handler(Looper.getMainLooper()).postDelayed({ closeJoiningRoom() }, 4000)
            }
        }
    }

    private fun updateRoomInfoOnline(dataSnapshot: DocumentSnapshot?) {
        val playerJoining = dataSnapshot?.data?.get("PJ").toString().toInt()

        if (vibrateStatus) vibrationStart()
        if (playerJoining in 2..6 && playerJoining != fromInt && soundStatus) SoundManager.instance?.playUpdateSound()
        p1 = dataSnapshot?.data?.get("p1").toString()
        val p1h = dataSnapshot?.data?.get("p1h").toString()
        p2 = dataSnapshot?.data?.get("p2").toString()
        p3 = dataSnapshot?.data?.get("p3").toString()
        p4 = dataSnapshot?.data?.get("p4").toString()
        val p2h = dataSnapshot?.data?.get("p2h").toString()
        val p3h = dataSnapshot?.data?.get("p3h").toString()
        val p4h = dataSnapshot?.data?.get("p4h").toString()

        if (p1h.isNotEmpty() && !p1Status) {
            getDataNUpdateAdapter(p1h, 0)
            p1Status = true
        }
        if (p2h.isNotEmpty() && !p2Status) {
            getDataNUpdateAdapter(p2h, 1)
            if (fromInt < 2) speak("$p2 has joined", speed = 1f, forceSpeak = false)
            p2Status = true
        }
        if (p3h.isNotEmpty() && !p3Status) {
            getDataNUpdateAdapter(p3h, 2)
            if (fromInt < 3) speak("$p3 has joined", speed = 1f, forceSpeak = false)
            p3Status = true
        }
        if (p4h.isNotEmpty() && !p4Status) {
            getDataNUpdateAdapter(p4h, 3)
            if (fromInt < 4) speak("$p4 has joined", speed = 1f, forceSpeak = false)
            p4Status = true
        }
        if (nPlayers == 7) {
            p5 = dataSnapshot?.data?.get("p5").toString()
            p6 = dataSnapshot?.data?.get("p6").toString()
            p7 = dataSnapshot?.data?.get("p7").toString()
            val p5h = dataSnapshot?.data?.get("p5h").toString()
            val p6h = dataSnapshot?.data?.get("p6h").toString()
            val p7h = dataSnapshot?.data?.get("p7h").toString()

            if (p5h.isNotEmpty() && !p5Status) {
                getDataNUpdateAdapter(p5h, 4)
                if (fromInt < 5) speak("$p5 has joined", speed = 1f, forceSpeak = false)
                p5Status = true
            }
            if (p6h.isNotEmpty() && !p6Status) {
                getDataNUpdateAdapter(p6h, 5)
                if (fromInt < 6) speak("$p6 has joined", speed = 1f, forceSpeak = false)
                p6Status = true
            }
            if (p7h.isNotEmpty() && !p7Status) {
                getDataNUpdateAdapter(p7h, 6)
                if (fromInt < 7) speak("$p7 has joined", speed = 1f, forceSpeak = false)
                p7Status = true
            }
        }
        if (playerJoining == nPlayers) allPlayersJoined()
        if (playerJoining >= 10) {
            if (fromInt != 1 || playerJoining == 11) {
                binding.maskAllLoading1.visibility = View.VISIBLE
                binding.progressBarLoading4.visibility = View.VISIBLE
                binding.loadingText1.visibility = View.VISIBLE
            }
            binding.loadingText1.text = getString(R.string.starting_game)
            registration.remove()
            playerInfo = ArrayList()
            playerInfoCoins = ArrayList()
            if (playerJoining == 10) startNextActivity()
        }
        if (playerJoining == 11) {
            userBasicInfoStatus.observe(this) { eachStatus ->
                if (eachStatus.all { it }) {
//                    toastCenter("Ready To start")
//                    handler.postDelayed({ startNextActivity() }, 1500L)
                    startNextActivity(transition = false)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataNUpdateAdapter(uid: String, index: Int = 0) {
        refUsersData.document(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                userBasicInfoList[index] = extractUserData(dataSnapshot, index = index)
                val tempList = userBasicInfoStatus.value!!
                tempList[index] = true
                userBasicInfoStatus.value = tempList
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun allPlayersJoined() {
        if (soundStatus) SoundManager.instance?.playSuccessSound()
        speak("All Players have joined now. Ready to Start", speed = 0.95f, forceSpeak = false)
        binding.imageViewShareButton2.clearAnimation()
        binding.imageViewShareButton2.visibility = View.GONE
        binding.offlineProgressbar.visibility = View.GONE
        binding.waitingToJoinText.visibility = View.GONE
        val delayTime = if(offline) 600L else 2000L
        Handler(Looper.getMainLooper()).postDelayed({ binding.startGameButton.visibility = View.VISIBLE }, delayTime)
    }

    @SuppressLint("SimpleDateFormat")
    fun startGame(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        if (fromInt == 1) {
            binding.maskAllLoading1.visibility = View.VISIBLE
            binding.progressBarLoading4.visibility = View.VISIBLE
            binding.loadingText1.visibility = View.VISIBLE
            if (!offline) {
                binding.loadingText1.text = getString(R.string.firingServer)
                val gameData = if (!BuildConfig.DEBUG) {  //(getString(R.string.testGameData).contains('n')) {
                    if (nPlayers == 7) CreateGameData(uid, selfName).gameData7
                    else CreateGameData(uid, selfName).gameData4
                } else {
                    when (nPlayers) {
                        7 -> CreateGameData(uid, selfName).gameDataDummy7
                        else -> CreateGameData(uid, selfName).gameDataDummy4
                    }
                }
                myRefGameData.child(roomID).setValue(gameData).addOnSuccessListener {
                    refRoomData.document(roomID).set(hashMapOf("PJ" to 10), SetOptions.merge()).addOnSuccessListener {
                        binding.startGameButton.clearAnimation()
                        binding.startGameButton.visibility = View.GONE
                        refRoomData.document(roomID + "_chat").set(hashMapOf(
                            "M" to "",
                            "F" to 1,
                            "Fn" to "",
                            "d" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(),
                            "dt" to SimpleDateFormat("HH:mm:ss z").format(Date()),
                        ))
                    }.addOnFailureListener { exception ->
                        binding.maskAllLoading1.visibility = View.GONE
                        binding.progressBarLoading4.visibility = View.GONE
                        binding.loadingText1.visibility = View.GONE
                        toastCenter("Failed to create server \nPlease try again\n${exception.localizedMessage!!}")
                    }
                }.addOnFailureListener { exception ->
                    binding.maskAllLoading1.visibility = View.GONE
                    binding.progressBarLoading4.visibility = View.GONE
                    binding.loadingText1.visibility = View.GONE
                    toastCenter("Failed to create server \nPlease try again\n${exception.localizedMessage!!}")
                }
            } else {
                binding.loadingText1.text = getString(R.string.starting_game)
                binding.startGameButton.clearAnimation()
                binding.startGameButton.visibility = View.GONE
                startNextActivity()
            }
        } else {            //			soundError.start()
            SoundManager.instance?.playUpdateSound()
            toastCenter("Only Host can start the game")
            speak("Only Host can start", speed = 1.15f, forceSpeak = false)
        }
    }

    private fun startNextActivity(transition:Boolean = true) {
        if (!offline) registration.remove()
        if (nPlayers == 7) {
            playerInfo.addAll(listOf(p1, p2, p3, p4, p5, p6, p7, userBasicInfoList[0].photoURL, userBasicInfoList[1].photoURL, userBasicInfoList[2].photoURL, userBasicInfoList[3].photoURL, userBasicInfoList[4].photoURL, userBasicInfoList[5].photoURL, userBasicInfoList[6].photoURL))
            playerInfoCoins.addAll(listOf(userBasicInfoList[0].score, userBasicInfoList[1].score, userBasicInfoList[2].score, userBasicInfoList[3].score, userBasicInfoList[4].score, userBasicInfoList[5].score, userBasicInfoList[6].score))
        } else if (nPlayers == 4) {
            playerInfo.addAll(listOf(p1, p2, p3, p4, userBasicInfoList[0].photoURL, userBasicInfoList[1].photoURL, userBasicInfoList[2].photoURL, userBasicInfoList[3].photoURL))
            playerInfoCoins.addAll(listOf(userBasicInfoList[0].score, userBasicInfoList[1].score, userBasicInfoList[2].score, userBasicInfoList[3].score))
        }
        if (!offline) {
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            startActivity(Intent(this@CreateAndJoinRoomScreen, GameScreen::class.java).apply { putExtra("selfName", selfName) }  // AutoPlay
                .apply { putExtra("from", from) }.apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("totalDailyCoins", totalDailyCoins) }.apply { putExtra("roomID", roomID) }.putStringArrayListExtra("playerInfo", playerInfo).putIntegerArrayListExtra("playerInfoCoins", playerInfoCoins).putIntegerArrayListExtra("userStats", userStats).putIntegerArrayListExtra("userStatsDaily", userStatsDaily).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        } else {
            startActivity(Intent(this@CreateAndJoinRoomScreen, GameScreenAutoPlay::class.java).apply { putExtra("selfName", selfName) }  // AutoPlay
                .apply { putExtra("from", from) }.apply { putExtra("nPlayers", nPlayers) }.apply { putExtra("totalDailyCoins", totalDailyCoins) }.apply { putExtra("roomID", roomID) }.putStringArrayListExtra("playerInfo", playerInfo).putIntegerArrayListExtra("playerInfoCoins", playerInfoCoins).putIntegerArrayListExtra("userStats", userStats).putIntegerArrayListExtra("userStatsDaily", userStatsDaily).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        if(transition) overridePendingTransition(R.anim.slide_top_in_activity, R.anim.slide_top_in_activity) //		finishAfterTransition()
        finishAndRemoveTask()
    }

    private fun initializeSpeechEngine() {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastCenter("Missing Language data - Text to speech")
                } else if (!offline && fromInt != 1 && fromInt < nPlayers) { //don't speak for host and last player
                    speak("Invite your friends to join this room", speed = 1.1f, forceSpeak = false)
                }
            }
        }
    }

    private fun speak(speechText: String, pitch: Float = 1f, speed: Float = 1.05f, que: Int = TextToSpeech.QUEUE_FLUSH, forceSpeak: Boolean = false) {
        if (speechStatus && this::textToSpeech.isInitialized && (forceSpeak || !closeRoom)) {
            textToSpeech.setPitch(pitch)
            textToSpeech.setSpeechRate(speed)
            val params = Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.1f)
            textToSpeech.speak(speechText, que, bundleOf(Pair(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.15f)), null)
        }
    }

    private fun initializeAds() {
        if (!premiumStatus) {
            MobileAds.initialize(this) //initialize admob ads
            binding.bannerCJRS.loadAd(AdRequest.Builder().build())  //load ad to banner view Admob
            binding.bannerCJRS.visibility = View.VISIBLE
        } else {
            binding.bannerCJRS.visibility = View.GONE
        }
    }

    fun anim(view: View, anim: Int) {
        view.startAnimation(AnimationUtils.loadAnimation(this, anim))
    }

    private fun getSharedPrefs() {
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
            if (premiumStatus) binding.bannerCJRS.visibility = View.GONE
            else initializeAds()
        }
        if (sharedPreferences.contains("musicStatus")) {
            musicStatus = sharedPreferences.getBoolean("musicStatus", true)
            if (musicStatus) soundBackground.start()
        }
        if (sharedPreferences.contains("soundStatus")) {
            soundStatus = sharedPreferences.getBoolean("soundStatus", true)
        }
        if (sharedPreferences.contains("speechStatus")) {
            speechStatus = sharedPreferences.getBoolean("speechStatus", true)
        }
        if (sharedPreferences.contains("vibrateStatus")) {
            vibrateStatus = sharedPreferences.getBoolean("vibrateStatus", true)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun vibrationStart(duration: Long = 150) {
        if (versionStatus) {
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(duration)
        }
    }

    private fun closeJoiningRoom() {
        if (!offline) registration.remove()
        handler.removeCallbacksAndMessages(null)
        startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) })
        overridePendingTransition(R.anim.slide_right_activity, R.anim.slide_right_activity)
        finishAfterTransition()
    }

    fun showDialogueCR(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
        closeRoom = true
        speak("Are you sure want to leave the room", speed = 1f, forceSpeak = true)
        if (!this::alertDialog.isInitialized) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Leave Room")
            builder.setMessage("Are you sure want to leave the room ?")
            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                toastCenter("Leaving room.....")
                speak("Leaving room ", forceSpeak = true)
                Handler(Looper.getMainLooper()).postDelayed({ closeJoiningRoom() }, 700)
            }
            builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
                closeRoom = false
                speak("Glad to hear that", forceSpeak = true)
            }
            builder.setOnDismissListener {
                closeRoom = false
            }
            alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shine_player_stats))
        }
        alertDialog.show()
    }

    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        moveTaskToBack(true)
    }

    override fun onPause() {
        super.onPause()
        if (musicStatus) soundBackground.pause()
        try {
            if (this::textToSpeech.isInitialized) textToSpeech.stop()
        } catch (_: java.lang.Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (musicStatus && this::soundBackground.isInitialized) soundBackground.start()
    }

    override fun onDestroy() {
        binding.bannerCJRS.destroy() // destroy MoPub banner add view
        if (this::soundBackground.isInitialized) soundBackground.release() //		adapter = null
        binding.playersJoin.adapter = null
        try {
            if (this::textToSpeech.isInitialized) {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
        } catch (_: java.lang.Exception) {
        }
        try {
            if (!offline) registration.remove()
        } catch (error: Exception) {
            toastCenter(error.localizedMessage)
        }
        super.onDestroy()
    }

    private fun createDynamicLink() {
        shareLink = "https://kaaliteeri.page.link/?link=${getString(R.string.scheme)}://${getString(R.string.hostJoinRoom)}/$roomID" + "&apn=${getString(R.string.packageName)}" + "&amv=70" +
                "&st=Join%20my%20room%20ID%20%3D%3E%20" + roomID + "&si=https://tinyurl.com/3ofspade"
    }

    fun shareRoomInfo(view: View) {
        if (!offline) {
            binding.imageViewShareButton2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
            if (soundStatus) SoundManager.instance?.playUpdateSound()
            val message = "${Emoji().gamePlayed}${Emoji().trophy} Join my room => $roomID ${Emoji().score}${Emoji().money}" + "\n\n$shareLink"
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TITLE, "Join my room => $roomID")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            try {
                startActivity(Intent.createChooser(intent, "Share Room ID $roomID via :"))
            } catch (me: Exception) {
                toastCenter(me.toString()) // dummy
            }
        }
    }

    private fun toastCenter(message: String) {
        Snackbar.make(binding.backgroundJR, message, Snackbar.LENGTH_SHORT).show()

    }
}





