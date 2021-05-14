package com.kaalikiteeggi.three_of_spades

//import com.adcolony.sdk.AdColony
//import com.adcolony.sdk.AdColonyAppOptions
//import com.facebook.ads.AdSettings
//import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
//import com.google.ads.mediation.inmobi.InMobiConsent
//import com.inmobi.sdk.InMobiSdk
//import com.unity3d.ads.UnityAds
import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
//import com.facebook.login.LoginManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration //import com.mopub.common.MoPub
import com.mopub.common.logging.MoPubLog //import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubInterstitial //import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubRewardedAds //import com.mopub.mobileads.MoPubInterstitial
//import com.mopub.mobileads.MoPubRewardedAds
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.concurrent.Executors
import kotlin.random.Random

class SplashScreen : AppCompatActivity() {
	private lateinit var soundInto: MediaPlayer
	private var user: FirebaseUser? = null
	private lateinit var appUpdateManager: AppUpdateManager
	private lateinit var snackBar: Snackbar
	private var handler = Handler(Looper.getMainLooper())
	private var isAppLatest = true
	private var isTimerOver = false
	private var isNextActivityStarted = false
	private var background = 4
	private val timer = if (!BuildConfig.DEBUG) 4200L
	else 4200L
	private val timerLoading = timer - 500L
	private val requestCodeAppUpdate = 800

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.Theme_App)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash_screen)
		background =  Random.nextInt(0, 6)
				when (background) {
		            0 -> splashBckgd.setImageResource(R.drawable.redblackburst)
		            4 -> splashBckgd.setImageResource(R.drawable.blueburst)
		            2 -> splashBckgd.setImageResource(R.drawable.greenyellowburst)
		            3 -> splashBckgd.setImageResource(R.drawable.navyblueburst)
		            1 -> splashBckgd.setImageResource(R.drawable.redorangeburst)
		            5 -> splashBckgd.setImageResource(R.drawable.yellowburst)
				}
		icon3.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_once))
		updateUI()
		soundInto = MediaPlayer.create(applicationContext, R.raw.card_shuffle)
		soundInto.setVolume(0.12F, 0.12F)
		soundInto.start()
		mobileAds()
		Executors.newSingleThreadExecutor().execute {
			checkAppUpdate()
		}
		handler.postDelayed({
			isTimerOver = true
			if (isAppLatest) nextActivity()
		}, timer) // dummy 3500
	}

	@SuppressLint("SetTextI18n")
	private fun updateUI() {
		val loadingAnim = ObjectAnimator.ofInt(loading, "progress", 0, 10000)
		loadingAnim.duration = timerLoading //		loadingAnim.setInterpolator { object: BounceInterpolator() }
		loadingAnim.addListener(object : Animator.AnimatorListener {
			override fun onAnimationStart(animation: Animator?) {}
			override fun onAnimationEnd(animation: Animator?) {
				loadingSplash.text = "Lets Play"
			}
			override fun onAnimationCancel(animation: Animator?) {}
			override fun onAnimationRepeat(animation: Animator?) {}
		})
		loadingAnim.start()
		user = FirebaseAuth.getInstance().currentUser // dummy - could be a problem if invalid mAuth and not null
		if (user != null) {
			FirebaseCrashlytics.getInstance().setUserId(user!!.uid)
			userNameSplash.text = user?.displayName.toString().split(" ")[0]
			userNameSplash.visibility = View.VISIBLE
			welcome2.visibility = View.VISIBLE
			profilePic2.visibility = View.VISIBLE
			Picasso.get().load(user?.photoUrl.toString()).resize(300, 300).into(profilePic2)
		}
	}

	private fun nextActivity() {
		if (user != null && !isNextActivityStarted) {
			isNextActivityStarted = true
			handler.removeCallbacksAndMessages(null)
			startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", false) }
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
			overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
			finishAfterTransition()
		} else if (!isNextActivityStarted) {
			isNextActivityStarted = true
//			LoginManager.getInstance().logOut()
			startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) }
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
			overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
			finishAfterTransition()
		}
	}

	override fun onDestroy() {
		handler.removeCallbacksAndMessages(null)

		super.onDestroy()
	}
	@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
	private fun mobileAds() { //		MobileAds.initialize(this)
		val configBuilder = if (BuildConfig.DEBUG) SdkConfiguration.Builder(getString(R.string.bannerTest_MP))
			.withLogLevel(MoPubLog.LogLevel.DEBUG)
		else SdkConfiguration.Builder(getString(R.string.bannerReal_MP))
			.withLogLevel(MoPubLog.LogLevel.NONE)
		MoPub.initializeSdk(applicationContext, configBuilder.build()) { }

		val interstitialAdID = if (BuildConfig.DEBUG) getString(R.string.interstitialTest_mp) // real interstitial ad id - MoPub
		else getString(R.string.interstitialReal_mp) // test interstitial ad
		MoPubInterstitial(this, interstitialAdID).load()
		val rewardedAdId = if (BuildConfig.DEBUG) getString(R.string.rewardedTest_mp) else getString(R.string.rewardedReal_mp)
		MoPubRewardedAds.loadRewardedAd(rewardedAdId)
	}

	private fun checkAppUpdate() {
		appUpdateManager = AppUpdateManagerFactory.create(baseContext)
		val appUpdateInfoTask = appUpdateManager.appUpdateInfo
		appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
			if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
				handler.removeCallbacksAndMessages(null)
				toastCenter("New App version is available", duration = Snackbar.LENGTH_INDEFINITE)
				appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
			} else {
				isAppLatest = true
				if (isTimerOver && !isNextActivityStarted) nextActivity()
			}
		}.addOnFailureListener {
			//			toastCenter("App update check error \n${e.message.toString()}", duration = 1200)
			isAppLatest = true
			if (isTimerOver && !isNextActivityStarted) nextActivity()
		}.addOnCompleteListener {
			isAppLatest = true
			if (isTimerOver && !isNextActivityStarted) nextActivity()
		}
	}

	private fun toastCenter(message: String, duration: Int = 1000) {
		if (!this::snackBar.isInitialized) {
			snackBar = Snackbar.make(findViewById(R.id.splashRoot), message, duration)
				.setAction("Dismiss") { snackBar.dismiss() }
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == requestCodeAppUpdate) {
			if (resultCode != Activity.RESULT_OK) {
				checkAppUpdate()
				toastCenter("App update failed \n Please try again or Restart app", duration = Snackbar.LENGTH_INDEFINITE)
			} else if (resultCode == Activity.RESULT_OK) {
				toastCenter("App was updated successfully", duration = Snackbar.LENGTH_SHORT) // dummy
				isAppLatest = true
				if (isTimerOver && !isNextActivityStarted) nextActivity()
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}
}
