package com.kaalikiteeggi.three_of_spades

//import com.adcolony.sdk.AdColony
//import com.adcolony.sdk.AdColonyAppOptions
//import com.facebook.ads.AdSettings
//import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
//import com.google.ads.mediation.inmobi.InMobiConsent
//import com.inmobi.sdk.InMobiSdk
//import com.unity3d.ads.UnityAds
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.*
import kotlin.random.Random

class SplashScreen : AppCompatActivity() {
        private lateinit var soundInto: MediaPlayer
    private var user: FirebaseUser? = null
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var toast: Toast
    private var handler = Handler(Looper.getMainLooper())
    private var isAppLatest = true
    private var isTimerOver = false
    private var isNextActivityStarted = false
    private var background = 4
    private val timer = if (!BuildConfig.DEBUG) 4500
    else 4500

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        background = Random.nextInt(0,6)
        when (background) {
            0 -> splashBckgd.setImageResource(R.drawable.redblackburst)
            1 -> splashBckgd.setImageResource(R.drawable.blueburst)
            2 -> splashBckgd.setImageResource(R.drawable.greenyellowburst)
            3 -> splashBckgd.setImageResource(R.drawable.navyblueburst)
            4 -> splashBckgd.setImageResource(R.drawable.redorangeburst)
            5 -> splashBckgd.setImageResource(R.drawable.yellowburst)
        }
        icon3.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_once))
        splashBckgd.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise))
        updateUI()
        findViewById<LightProgress>(R.id.lightSplash).on()
        vcSplash.text = "Ver: " + packageManager.getPackageInfo(packageName,0).versionName

//        mobileAds() // load mobile ads for everyone
        toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        checkAppUpdate()
        soundInto = MediaPlayer.create(applicationContext, R.raw.card_shuffle)
        soundInto.start()

        handler.postDelayed({
            isTimerOver = true
            if (isAppLatest) nextActivity()
        }, timer.toLong()) // dummy 3500
    }

    private fun updateUI() {
        val mAuth = FirebaseAuth.getInstance()  // dummy - could be a problem if invalid mAuth and not null
        user = mAuth.currentUser
        if (user != null) {
            val userName = user!!.displayName.toString().split(" ")[0]
            val photoURL = user!!.photoUrl.toString()
            userNameSplash.text = userName
            userNameSplash.visibility = View.VISIBLE
            welcome2.visibility = View.VISIBLE
            profilePic2.visibility = View.VISIBLE
            Picasso.get().load(photoURL).resize(300, 300).into(profilePic2)
        }
    }

    private fun nextActivity() {
        if (user != null && !isNextActivityStarted) {
            isNextActivityStarted = true
            handler.removeCallbacksAndMessages(null)
            startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) })
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
        } else if (!isNextActivityStarted) {
            isNextActivityStarted = true
            LoginManager.getInstance().logOut()
            startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            finish()
        }
    }

    private fun mobileAds() {
        MobileAds.initialize(this)
        val testDeviceIds = Arrays.asList(getString(R.string.testDeviceId))
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        val mInterstitialAd = InterstitialAd(this)
        if (!BuildConfig.DEBUG) mInterstitialAd.adUnitId = getString(R.string.interstitial)  // real ADS ID
        else mInterstitialAd.adUnitId = getString(R.string.interstitialTestVideo)   // test ADs id
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time

    }

    private fun checkAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                handler.removeCallbacksAndMessages(null)
                toastCenter("New App Update is available")
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 88)
            } else {
                isAppLatest = true
                if (isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }.addOnFailureListener { e ->
            toastCenter("App update check error \n${e.message.toString()}")
            isAppLatest = true
            if (isTimerOver && !isNextActivityStarted) nextActivity()
        }.addOnCompleteListener {
            isAppLatest = true
            if (isTimerOver && !isNextActivityStarted) nextActivity()
        }
    }

    private fun toastCenter(message: String) {
        toast.setText(message)
        toast.show()
    }

    override fun onResume() {
        super.onResume()
        if (this::appUpdateManager.isInitialized) appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 88)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 88) {
            if (resultCode != Activity.RESULT_OK) {
                checkAppUpdate()
                toastCenter("App update failed \n Please try again")
            } else if (resultCode == Activity.RESULT_OK) {
                toastCenter("App updated successfully") // dummy
                isAppLatest = true
                if (isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}


//                AdSettings.addTestDevice("abb71e62-26ea-4afc-88f7-c370a1da45f1")
//                AdSettings.addTestDevice("ec3dd554-caf4-41cf-8747-f7a755836f05")
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
//
//            UnityAds.initialize(this,getString(R.string.unity_game_id))

//            AudienceNetworkAds.initialize(this)
