package com.kaalikiteeggi.three_of_spades

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.applovin.sdk.AppLovinAd
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kaalikiteeggi.three_of_spades.databinding.ActivitySplashScreenBinding
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
    private val timer = if (!BuildConfig.DEBUG) 3500L
    else 500L
    private val requestCodeAppUpdate = 800
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding:ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_splash_screen)

        val avd = (binding.icon3.drawable as AnimatedVectorDrawable)
        avd.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationStart(drawable: Drawable?) {
                binding.loadingSplash.visibility = View.VISIBLE
                handler.postDelayed({
                    isTimerOver = true
                    if (isAppLatest) nextActivity()
                }, timer) // dummy 3500
            }
            override fun onAnimationEnd(drawable: Drawable?) {
                binding.loadingSplash.text = getString(R.string.splashLoadingDone)
                binding.loadingProgress.setProgressCompat(100, true)
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
    }

    private fun loadUserProfile() {
        user = FirebaseAuth.getInstance().currentUser // dummy - could be a problem if invalid mAuth and not null
        if (user != null) {
            FirebaseCrashlytics.getInstance().setUserId(user!!.uid)
        }
    }

    private fun mobileAds() {
        AppLovinPrivacySettings.setHasUserConsent(true, applicationContext)
        AppLovinPrivacySettings.setIsAgeRestrictedUser(false, applicationContext)
        AppLovinPrivacySettings.setDoNotSell( false, applicationContext )
        AppLovinSdk.getInstance(applicationContext).initializeSdk()

        MobileAds.initialize(applicationContext){initializationStatus->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d("MyApp", String.format(
                    "Adapter name: %s, Description: %s, Latency: %d",
                    adapterClass, status!!.description, status.latency))
            }
        }
        val requestBuilder = RequestConfiguration.Builder().setTestDeviceIds(listOf(getString(R.string.testDeviceID))).build()
        MobileAds.setRequestConfiguration(requestBuilder)
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

}
