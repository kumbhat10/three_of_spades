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
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.logging.MoPubLog
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.concurrent.Executors

class SplashScreen : AppCompatActivity() {
    private var user: FirebaseUser? = null
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var snackBar: Snackbar
    private var handler = Handler(Looper.getMainLooper())
    private var isAppLatest = false
    private var isTimerOver = false
    private var isNextActivityStarted = false
    private var background = 4
    private val timer = if (!BuildConfig.DEBUG) 4500L
    else 4500L
    private val requestCodeAppUpdate = 800
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val avd = (icon3.drawable as AnimatedVectorDrawable)
        avd.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationStart(drawable: Drawable?) {
                loadingSplash.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(drawable: Drawable?) {
                loadingSplash.text = getString(R.string.splashLoadingDone)
                loadingProgress.setProgressCompat(100, false)
            }
        })
        avd.start()
        loadUserProfile()
        mobileAds()
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                checkAppUpdate()
                toastCenter("App update failed \n Please try again or Restart app", duration = Snackbar.LENGTH_INDEFINITE)
            } else if (result.resultCode == Activity.RESULT_OK) {
                toastCenter("App was updated successfully", duration = Snackbar.LENGTH_SHORT) // dummy
                isAppLatest = true
                if (isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }
        Executors.newSingleThreadExecutor().execute {
            checkAppUpdate()
        }
        handler.postDelayed({
            isTimerOver = true
			if (isAppLatest) nextActivity()
        }, timer) // dummy 3500
    }

	private fun loadUserProfile() {
        user = FirebaseAuth.getInstance().currentUser // dummy - could be a problem if invalid mAuth and not null
        if (user != null) {
            FirebaseCrashlytics.getInstance().setUserId(user!!.uid)
        }
    }

    private fun mobileAds() {
        //		MobileAds.initialize(this)
        val configBuilder = if (BuildConfig.DEBUG) SdkConfiguration.Builder(getString(R.string.bannerTest_MP)).withLogLevel(MoPubLog.LogLevel.DEBUG)
        else SdkConfiguration.Builder(getString(R.string.bannerReal_MP)).withLogLevel(MoPubLog.LogLevel.NONE)

        //		AdSettings.addTestDevice("bd40e50a-23b8-4798-8370-0ebbd6bf23fb") //onePlus
        //		val facebookConfig = hashMapOf("banner" to "", "interstitial" to "")
        //		AudienceNetworkAds.buildInitSettings(applicationContext).withPlacementIds(listOf("607386246545418_617997035484339")).withInitListener {
        //			MoPub.initializeSdk(applicationContext, configBuilder.build()) { }
        //		}.initialize()
        MoPub.getPersonalInformationManager()?.grantConsent()
        MoPub.initializeSdk(applicationContext, configBuilder.build()) {
//			val interstitialAdID = if (BuildConfig.DEBUG) getString(R.string.interstitialTest_mp) // real interstitial ad id - MoPub
//			else getString(R.string.interstitialReal_mp) // test interstitial ad
//			val mInterstitial = MoPubInterstitial(this, interstitialAdID)
//			mInterstitial.load()
//			val rewardedAdId = if (BuildConfig.DEBUG) getString(R.string.rewardedTest_mp) else getString(R.string.rewardedReal_mp)
//			MoPubRewardedAds.loadRewardedAd(rewardedAdId)
        }
    }

    private fun nextActivity() {
        if (user != null && !isNextActivityStarted) {
            isNextActivityStarted = true
            handler.removeCallbacksAndMessages(null)
            startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) }.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            finishAfterTransition()
        } else if (!isNextActivityStarted) {
            isNextActivityStarted = true
            startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) }.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
            finishAfterTransition()
        }
    }

    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    private fun checkAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                handler.removeCallbacksAndMessages(null)
                toastCenter("New App version is available\nPlease update the app", duration = Snackbar.LENGTH_INDEFINITE)
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
            } else {
                isAppLatest = true
				if (isTimerOver && !isNextActivityStarted) nextActivity()
            }
        }.addOnFailureListener {
            toastCenter("Failed to check App update \nPlease update from PlayStore", duration = 2000)
            isAppLatest = true
            if (isTimerOver && !isNextActivityStarted) nextActivity()
        }.addOnCompleteListener {
            isAppLatest = true
            if (isTimerOver && !isNextActivityStarted) nextActivity()
        }
    }

    private fun toastCenter(message: String, duration: Int = 1000) {
        if (!this::snackBar.isInitialized) {
            snackBar = Snackbar.make(findViewById(R.id.splashRoot), message, duration).setAction("Dismiss") { snackBar.dismiss() }
            snackBar.setActionTextColor(getColor(R.color.borderblue))
            snackBar.view.setOnClickListener { snackBar.dismiss() }
        } else snackBar.setText(message)
        snackBar.show()
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        MoPub.onDestroy(this)
        super.onDestroy()
    }

}
