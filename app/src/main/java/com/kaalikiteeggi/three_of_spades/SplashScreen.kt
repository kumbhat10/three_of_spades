package com.kaalikiteeggi.three_of_spades

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
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.adcolony.sdk.AdColony
import com.adcolony.sdk.AdColonyAppOptions
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.login.LoginManager
import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
import com.google.ads.mediation.inmobi.InMobiConsent
import com.google.android.gms.ads.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.inmobi.sdk.InMobiSdk
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.unity3d.ads.UnityAds
import com.unity3d.ads.metadata.MetaData
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView
import java.util.*

class SplashScreen: AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mInterstitialAd: InterstitialAd
    private var user: FirebaseUser? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var premiumStatus = false
    private var target = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap != null) {
                findViewById<ImageView>(R.id.profilePic2).setImageDrawable(bitmap.toDrawable(resources))
                findViewById<ImageView>(R.id.profilePic2).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite))
            }
        }
    }
    private var isAppLatest = false
    private var isTimerOver = false
    private var timer = 0

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
        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        MobileAds.initialize(this)
        AudienceNetworkAds.initialize(this)

        if(getString(R.string.useTestDevice).contains('y')) {
            val testDeviceIds = Arrays.asList(getString(R.string.testDeviceId))
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(configuration)
            AdSettings.addTestDevice("abb71e62-26ea-4afc-88f7-c370a1da45f1")
        }

        AdColony.configure(this,"app17c8dd48fb9945b9b4","vz3791ce293adf41e69e","vzfb9b1050f8c74cc5ad","vz6cded1664bb44e1cb9")
        val appOptions = AdColonyMediationAdapter.getAppOptions()
        appOptions.gdprConsentString = "1"
        appOptions.gdprRequired = true
        appOptions.keepScreenOn = true
        val consentObject = JSONObject()
        try{
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE,true)
            consentObject.put("gdpr","1")
        }catch(exception: JSONException){
        }
        InMobiConsent.updateGDPRConsent(consentObject)
//        val gdprMetaData = MetaData(this)  // unity consent - not required as unity asks automatically for the first time to the user based on the location
//        gdprMetaData.set("gdpr.consent",true)
//        gdprMetaData.commit()
        UnityAds.initialize(this,getString(R.string.unity_game_id))

        findViewById<ImageView>(R.id.icon_3).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite))
        findViewById<ProgressBar>(R.id.progressBarLoading).visibility = View.VISIBLE

        mAuth = FirebaseAuth.getInstance()  // dummy - could be a problem if invalid mAuth and not null
        user = mAuth.currentUser
        if(user != null) {
            val userName = user!!.displayName.toString().split(" ")[0]
            val photoURL = user!!.photoUrl.toString()
            findViewById<ImageView>(R.id.profilePic2).visibility = View.VISIBLE
            findViewById<Button>(R.id.welcomeUserNameview2).visibility = View.VISIBLE
            findViewById<Button>(R.id.welcomeUserNameview2).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_buttons))
            findViewById<TextView>(R.id.welcome2).visibility = View.VISIBLE
            findViewById<Button>(R.id.welcomeUserNameview2).text = userName
            Picasso.get().load(photoURL).transform(CircleTransform()).into(target)
        }
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)  //init preference file in private mode
        if (sharedPreferences.contains("premium")) {
            premiumStatus = sharedPreferences.getBoolean("premium", false)
        }
        if(!premiumStatus) mobileAds()
        checkAppUpdate()
        timer = if(getString(R.string.test).contains('n')) 3500
        else 300
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            isTimerOver = true
            when {
                (premiumStatus && !isAppLatest) -> checkAppUpdate()
                (premiumStatus && isAppLatest) -> nextActivity()
                mInterstitialAd.isLoaded -> {
                   if(getString(R.string.test).contains('n')) mInterstitialAd.show() // dummy
                   else nextActivity() // dummy
                }
                isAppLatest -> nextActivity()
                !isAppLatest -> checkAppUpdate()
                else -> {
//                    nextActivity()
                }
            }
        }, timer.toLong()) // dummy 3500
    }

    fun nextActivity(){
        if(user != null){
            startActivity(Intent(this, MainHomeScreen::class.java).apply {putExtra("newUser",false)})
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
            finish()
        }else{
            LoginManager.getInstance().logOut()
            startActivity(Intent(applicationContext,StartScreen::class.java))
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)
            finish()
        }
    }
    private fun mobileAds(){
//        MobileAds.initialize(this) {}   // initialize mobileAdds
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.interstitial)
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
              if(isAppLatest)  nextActivity()
                else checkAppUpdate()
            }
        }
    }

    fun checkAppUpdate(){
//        val versionCode = BuildConfig.VERSION_CODE
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 88)
            }else{
                isAppLatest = true
                if(isTimerOver) nextActivity()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode== 88){
            if(resultCode != Activity.RESULT_OK){
                Toast.makeText(applicationContext,"App is not Updated\nPlease update app to continue",Toast.LENGTH_SHORT).show()
                checkAppUpdate()
            }else if(resultCode == Activity.RESULT_OK){
                isAppLatest = true
                if(isTimerOver) nextActivity()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}