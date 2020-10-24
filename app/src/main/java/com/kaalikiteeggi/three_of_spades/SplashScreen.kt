package com.kaalikiteeggi.three_of_spades

//import com.adcolony.sdk.AdColony
//import com.adcolony.sdk.AdColonyAppOptions
//import com.facebook.ads.AdSettings
//import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
//import com.google.ads.mediation.inmobi.InMobiConsent
//import com.inmobi.sdk.InMobiSdk
//import com.unity3d.ads.UnityAds
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.bitvale.lightprogress.LightProgress
import com.facebook.login.LoginManager
import com.google.android.gms.ads.*
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.*

class SplashScreen: AppCompatActivity() {
    private lateinit var soundInto: MediaPlayer
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mInterstitialAd: InterstitialAd
    private var user: FirebaseUser? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var toast:Toast
    private var premiumStatus = false
    private var handler = Handler(Looper.getMainLooper())

    private var isAppLatest = false
    private var isTimerOver = false
    private var isNextActivityStarted = false
    private var timer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        findViewById<LightProgress>(R.id.lightSplash).on()
        findViewById<ImageView>(R.id.icon_3).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite))

            mAuth = FirebaseAuth.getInstance()  // dummy - could be a problem if invalid mAuth and not null
            user = mAuth.currentUser
            if(user != null) {
                val userName = user!!.displayName.toString().split(" ")[0]
                val photoURL = user!!.photoUrl.toString()
                welcomeUserNameview2.text = userName
                welcomeUserNameview2.visibility = View.VISIBLE
                findViewById<TextView>(R.id.welcome2).visibility = View.VISIBLE
                profilePic2.visibility = View.VISIBLE
                welcomeUserNameview2.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_buttons))
                Picasso.get().load(photoURL).resize(350,350).transform(CircleTransform()).into(profilePic2)
            }
            sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
            premiumStatus = if (sharedPreferences.contains("premium")) {
                sharedPreferences.getBoolean("premium", false)
            }else{
                true // dont show ADS when login for first time
            }
            soundInto = MediaPlayer.create(applicationContext,R.raw.card_shuffle)
            soundInto.start()
            MobileAds.initialize(this)
//            AudienceNetworkAds.initialize(this)
            if(getString(R.string.useTestDevice).contains('y')) {
                val testDeviceIds = Arrays.asList(getString(R.string.testDeviceId))
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                MobileAds.setRequestConfiguration(configuration)
//                AdSettings.addTestDevice("abb71e62-26ea-4afc-88f7-c370a1da45f1")
//                AdSettings.addTestDevice("ec3dd554-caf4-41cf-8747-f7a755836f05")
            }
            mInterstitialAd = InterstitialAd(this)
//        val appOptions = AdColonyMediationAdapter.getAppOptions()
//        appOptions.keepScreenOn = true
//        appOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, true)
//            .setPrivacyConsentString(AdColonyAppOptions.GDPR,"1")
//        AdColony.configure(this, appOptions,"app17c8dd48fb9945b9b4","vz3791ce293adf41e69e","vzfb9b1050f8c74cc5ad","vz6cded1664bb44e1cb9")
//
//        val consentObject = JSONObject()
//            try{
//                consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE,true)
//                consentObject.put("gdpr","1")
//            }catch(exception: JSONException){
//            }
//            InMobiConsent.updateGDPRConsent(consentObject)
//            UnityAds.initialize(this,getString(R.string.unity_game_id))
            mobileAds() // load mobile ads for everyone

        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 300)

        timer = if(!BuildConfig.DEBUG) 3500
        else 3500
        handler.postDelayed({
            isTimerOver = true
            when {
                (premiumStatus && isAppLatest) -> nextActivity()
                (!premiumStatus && mInterstitialAd.isLoaded) -> {
                    if(!BuildConfig.DEBUG) mInterstitialAd.show() // dummy
                    else nextActivity() // dummy
                }
                isAppLatest -> nextActivity()
            }
        }, timer.toLong()) // dummy 3500
        checkAppUpdate()
    }

    override fun onStart() {
        super.onStart()
    }
    fun nextActivity(){
        if(user != null && !isNextActivityStarted){
            isNextActivityStarted = true
            handler.removeCallbacksAndMessages(null)
            startActivity(Intent(this, MainHomeScreen::class.java).apply {putExtra("newUser",false)})
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
            Handler(Looper.getMainLooper()).postDelayed({finish()},1000)
        }else if(!isNextActivityStarted){
            isNextActivityStarted = true
            LoginManager.getInstance().logOut()
            startActivity(Intent(applicationContext,StartScreen::class.java))
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
            finish()
        }
    }
    private fun mobileAds(){
        mInterstitialAd = InterstitialAd(this)
        if(!BuildConfig.DEBUG) mInterstitialAd.adUnitId = getString(R.string.interstitial)  // real ADS ID
        else mInterstitialAd.adUnitId = getString(R.string.interstitialTestVideo)   // test ADs id
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
              if(isAppLatest)  nextActivity()
            }
        }
    }
    private fun checkAppUpdate(){
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
                appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                handler.removeCallbacksAndMessages(null)
                toastCenter("New App Update is available")
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 88)
            }else{
                isAppLatest = true
                if(isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }.addOnFailureListener { e -> toastCenter("failed ${e.message.toString()}")
            if(isTimerOver && !isNextActivityStarted) nextActivity()
        }.addOnCompleteListener {
            if(isTimerOver && !isNextActivityStarted) nextActivity()
//            toastCenter("complete app update")
        }
    }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }
    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            appUpdateInfo -> if(appUpdateInfo.updateAvailability()== UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 88)
        }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode== 88){
            if(resultCode != Activity.RESULT_OK){
                checkAppUpdate()
                toastCenter("App update failed \n Please try again")
            }else if(resultCode == Activity.RESULT_OK){
                toastCenter("App updated successfully") // dummy
                isAppLatest = true
                if(isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}