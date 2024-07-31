@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kaalikiteeggi.three_of_spades.databinding.ActivityDeveloperCreditsBinding

class DeveloperCredits : AppCompatActivity() {
    private lateinit var text:String
    private lateinit var uid:String
    private lateinit var vn:String
    private lateinit var vc:String
    private lateinit var emailIntent: Intent
    private var soundStatus = true
    private val homePageKKT = "http://sites.google.com/view/kaali-ki-teeggi/"
    private val whatsapp1 = "https://wa.me/919582648284"
    private val whatsapp2 = "https://wa.me/447496393966"
    private lateinit var intentBuilder: CustomTabsIntent.Builder
    private lateinit var binding: ActivityDeveloperCreditsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityDeveloperCreditsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.icon3developer.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_zoom))

        vn = packageManager.getPackageInfo(packageName,0).versionName.toString()
        vc = packageManager.getPackageInfo(packageName,0).versionCode.toString()
        uid = intent.getStringExtra("uid")!!.toString()    //Get roomID and display
        FirebaseCrashlytics.getInstance().setUserId(uid)
        soundStatus = intent.getBooleanExtra("soundStatus", true)    //Get roomID and display
        text = "VC: $vc\nVN: $vn\n W: ${resources.configuration.screenWidthDp}\nH: ${resources.configuration.screenHeightDp}\nUser ID: $uid"
        binding.sizeDC.text = text

        Handler(Looper.getMainLooper()).post {
            createEmailIntent()
            buildCustomTabIntent()
        }
    }

    fun openWebsite(view: View){
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        try{
            intentBuilder.build().launchUrl(this, Uri.parse(homePageKKT))
        }catch (me:Exception){
            Snackbar.make(binding.backgdDC, "No browser installed to open website", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            shareLink()
        }
    }

    private fun shareLink() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(homePageKKT)
            }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun buildCustomTabIntent(){
        intentBuilder = CustomTabsIntent.Builder()
        intentBuilder.setStartAnimations(this, R.anim.slide_left_activity, R.anim.slide_left_activity)
        intentBuilder.setExitAnimations(this, R.anim.slide_right_activity, R.anim.slide_right_activity)
        intentBuilder.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.icon_yellow))
        intentBuilder.addDefaultShareMenuItem()
    }

    fun copyToClipBoard(view: View){
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        Toast.makeText(applicationContext,"Text copied to clipboard",Toast.LENGTH_SHORT).show()
        val clipBoard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Text copied",text)
        clipBoard.setPrimaryClip(clip)
    }

    private fun createEmailIntent(sendEmail:Boolean = false){
        val body = "(Auto generated info for debugging purpose) " +
                "\n\nUserName: ${FirebaseAuth.getInstance().currentUser?.displayName}" +
                "\nAndroid: ${Build.VERSION.RELEASE}" +
                "\nAPI:      ${Build.VERSION.SDK_INT}" +
                "\nVC:         $vc " +
                "\nVN:         $vn " +
                "\nWidth:    ${resources.configuration.screenWidthDp} " +
                "\nHeight:   ${resources.configuration.screenHeightDp} " +
                "\nDevice:   ${Build.MANUFACTURER} ${Build.MODEL}" +
                "\n\n\nHi, I need support for...."

        val mailTo = "mailto:kaaliteerifun@gmail.com" +
                "?cc=" + "kumbhat10@gmail.com" +
                "&subject=" + Uri.encode("User ID: $uid") +
                "&body=" + Uri.encode(body)
        emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        emailIntent.setDataAndType(Uri.parse(mailTo), "text/plain")
        emailIntent.data = Uri.parse(mailTo)
        if(sendEmail) sendEmail(View(this))
    }

    fun sendEmail(view: View){
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        if(this::emailIntent.isInitialized) {
            try{
                startActivity(Intent.createChooser( emailIntent, "Contact us : ") )
            }catch (me:Exception){
                Toast.makeText(this, "No app can support this action", Toast.LENGTH_SHORT).show()
            }
        }
        else createEmailIntent()
    }
    fun sendWhatsapp(view: View){
        /*if (soundStatus) SoundManager.instance?.playUpdateSound()
        val content =   "(Auto generated info for debugging purpose) " +
                "\n\nUserName: ${FirebaseAuth.getInstance().currentUser?.displayName}" +
                "\nEmail: ${FirebaseAuth.getInstance().currentUser?.email}" +
                "\nUID: $uid" +
                "\n\nAndroid: ${Build.VERSION.RELEASE}" +
                "\nAPI:      ${Build.VERSION.SDK_INT}" +
                "\nVC:         $vc " +
                "\nVN:         $vn " +
                "\nWidth:    ${resources.configuration.screenWidthDp} " +
                "\nHeight:   ${resources.configuration.screenHeightDp} " +
                "\nDevice:   ${Build.MANUFACTURER} ${Build.MODEL}" +
                "\n\nHi Dushyant, I need support...."
        val whatsapp =   "$whatsapp1?text=$content"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsapp))
        try{
            startActivity(intent)
        }catch (me:Exception){
            Toast.makeText(this, "No app can support this action", Toast.LENGTH_SHORT).show()
        }
        */

    }
    fun closeDC(view: View){
        onBackPressed()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
//        super.onBackPressed()
        if (soundStatus) SoundManager.instance?.playUpdateSound()
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
        finishAndRemoveTask()
//        finishAfterTransition()
    }


}
