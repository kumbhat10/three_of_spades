@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.*
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.pow

@Suppress("DEPRECATION")
class TrainActivity : AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var vibrator:Vibrator
    private lateinit var cardsInHand: MutableList<Int>
    private lateinit var cardsDrawable: List<Int>
    private lateinit var cardsPoints: List<Int>
    private lateinit var cardsSuit: List<String>
    private lateinit var cardsDrawablePartner: List<Int>
    private lateinit var cardsIndexSortedPartner: List<Int>
    private lateinit var cardsPointsPartner: List<Int>
    private lateinit var cardsSuitPartner: List<String>
    private val myRefTrainingData = Firebase.database.getReference("Training") // initialize database reference
    private var bidValue = 0
    private var trump = "x"
    private var partnerCard = -1
    private var currentCardsSet = 0
    private lateinit  var cardsShuffled: List<Int>
    private var typedValue = TypedValue()
    private lateinit var bidText:TextView
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable.bug_icon) //default: bug image
            .restartActivity(MainHomeScreen::class.java)
            .apply()
        setContentView(R.layout.activity_train)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE // keep screen in landscape mode always
        bidText = findViewById(R.id.BidValue)
        toast = Toast.makeText(applicationContext,"",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER,0,20)
        Handler(Looper.getMainLooper()).post {
            soundUpdate = MediaPlayer.create(applicationContext,R.raw.update)
            soundError = MediaPlayer.create(applicationContext,R.raw.error)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        }
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,typedValue, true) // for click effect on self playing cards
        cardsDrawable = PlayingCards().cardsDrawable4
        cardsPoints    = PlayingCards().cardsPoints4
        cardsIndexSortedPartner    = PlayingCards().cardsIndexSortedPartner4
        cardsDrawablePartner = PlayingCards().cardsDrawable4
        cardsPointsPartner    = PlayingCards().cardsPoints4
        cardsSuitPartner    = PlayingCards().cardSuit4
        seekBarListener()
        nextCardsSet()
        displayAllCardsForPartnerSelection1()
    }
    private fun vibrationStart(duration: Long = 200){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else{
            vibrator.vibrate(duration)
        }
    }

    private fun seekBarListener(){
    val seekBar = findViewById<SeekBar>(R.id.seekBar)
    seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            bidValue = 175 + 5*progress
            bidText.text = bidValue.toString()
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            bidText.text = bidValue.toString()
        }
    })
}
    @SuppressLint("SetTextI18n")
    private fun nextCardsSet(){
        if(currentCardsSet == 4 || currentCardsSet == 0){
            cardsShuffled =  (0..51).shuffled()
            currentCardsSet = 0
        }
        currentCardsSet += 1
        cardsInHand = (cardsShuffled.slice((13*(currentCardsSet-1)) until 13*currentCardsSet).sortedBy{it}).toMutableList()
        val cardsInHandStats = CardsInHandStats(cardsInHand)
        findViewById<TextView>(R.id.infoExtra).text = cardsInHandStats.vEachSuits.toString() + "\n" + cardsInHandStats.trumpChosen + "\n" + cardsInHandStats.otherSuitPoints.toString()+ "\n" + cardsInHandStats.bidChosen.toString()
        bidText.text = cardsInHandStats.bidChosen.toString()
        findViewById<TextView>(R.id.currentState).text = "Hands State: $currentCardsSet"
        displaySelfCards()
}

    fun onTrumpSelectionClick(view: View) {
     if(trump != view.tag) {
            selectTrump(view.tag.toString())
        }else selectTrump("x")
    }

    private fun selectTrump(trumpSelection: String){
        trump = trumpSelection
        findViewById<ImageView>(R.id.imageViewTrumpHearts).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
        findViewById<ImageView>(R.id.imageViewTrumpSpades).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
        findViewById<ImageView>(R.id.imageViewTrumpDiamonds).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
        findViewById<ImageView>(R.id.imageViewTrumpClubs).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
        when (trumpSelection) {
            "h" -> findViewById<ImageView>(R.id.imageViewTrumpHearts).setBackgroundColor(
                ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
            )
            "s" -> findViewById<ImageView>(R.id.imageViewTrumpSpades).setBackgroundColor(
                ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
            )
            "d" -> findViewById<ImageView>(R.id.imageViewTrumpDiamonds).setBackgroundColor(
                ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
            )
            "c" -> findViewById<ImageView>(R.id.imageViewTrumpClubs).setBackgroundColor(
                ContextCompat.getColor(applicationContext, R.color.progressBarPlayer4)
            )
        }
    }
    @Suppress("ConstantConditionIf")
    fun pass(view: View){
        if(false){ //(partnerCard == -1 || trump == "x" ) {
            vibrationStart()
            soundError.start()
            toast.setText("Partner Card or trump not choosen")
            toast.show()
        }
        else {
//            val data = "${cardsInHandToHex()};$bidValue;$trump;$partnerCard"
//            myRefTrainingData.push().setValue(data)
            nextCardsSet()
            if (partnerCard >= 0) findViewById<ImageView>(partnerCard).foreground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.transparent))
            partnerCard = -1
            trump = "x"
            findViewById<ImageView>(R.id.imageViewTrumpHearts).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
            findViewById<ImageView>(R.id.imageViewTrumpSpades).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
            findViewById<ImageView>(R.id.imageViewTrumpDiamonds).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
            findViewById<ImageView>(R.id.imageViewTrumpClubs).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_grey_settings))
        }
    }
    private fun cardsInHandToHex(): String{
        var decimal = 0.0
        val base = 2.0
        for(x in cardsInHand){
            decimal += base.pow(x)
        }
//        toast.setText("Cards in hand : ${cardsInHand.joinToString(separator = ",")}  \nDecimal = ${decimal.toBigDecimal()}  \nHexadecimal = ${"%X".format(decimal.toBigDecimal().toBigInteger())}")
//        toast.show()
        return "%X".format(decimal.toBigDecimal().toBigInteger())
    }
    private fun displaySelfCards() {
        findViewById<LinearLayout>(R.id.cardsGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.cardsGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in cardsInHand) {
            val viewTemp = inflater.inflate(R.layout.cards_item_train, gallery, false)
            if(x== cardsInHand[cardsInHand.size-1]){
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setPaddingRelative(0,0,0,0)
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).layoutParams.width = resources.getDimensionPixelSize(R.dimen.widthDisplayCardLastTrain)
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setImageResource(cardsDrawable[x.toInt()])
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).tag = x.toString() // tag the card number to the image
            if (cardsPoints.elementAt(x.toInt()) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).text =
                    "${cardsPoints.elementAt(x.toInt())}"
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewDisplayCard).visibility = View.GONE
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).foreground = ContextCompat.getDrawable(applicationContext,typedValue.resourceId)
            viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).setOnClickListener(View.OnClickListener {
                viewTemp.findViewById<ImageView>(R.id.imageViewDisplayCard).startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_highlight))
            })
            gallery.addView(viewTemp)
        }
      }
    private fun displayAllCardsForPartnerSelection1(view: View = View(applicationContext)) {
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true) // for click effect on self playing cards
        findViewById<LinearLayout>(R.id.partnerTrainGallery).removeAllViews()
        val gallery = findViewById<LinearLayout>(R.id.partnerTrainGallery)
        val inflater = LayoutInflater.from(applicationContext)
        for (x: Int in cardsIndexSortedPartner) {
            val viewTemp = inflater.inflate(R.layout.cards_item_list_partner, gallery, false)
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner)
                .setImageResource(cardsDrawablePartner[x])
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).tag =
                x.toString() //set tag to every card of its own value
            if (cardsPointsPartner.elementAt(x) != 0) {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).text =
                    "${cardsPointsPartner.elementAt(x)} pts"
            } else {
                viewTemp.findViewById<TextView>(R.id.textViewPartner).visibility = View.GONE
            } // make it invisible
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).foreground = ContextCompat.getDrawable(applicationContext, typedValue.resourceId)
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).setOnClickListener {
                partnerCardSelected(it)
            }
            viewTemp.findViewById<ImageView>(R.id.imageViewPartner).id = x // tag the card number to the image
            gallery.addView(viewTemp)
        }
    }
    private fun partnerCardSelected(view: View){
        if(partnerCard>=0) findViewById<ImageView>(partnerCard).foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.transparent))
        partnerCard = view.id
        val suit = (cardsSuitPartner[partnerCard]).decapitalize()
        selectTrump(suit)
//        Toast.makeText(applicationContext, "Partner card suit selected is  : $suit ${view.id}", Toast.LENGTH_SHORT).show()
        view.foreground = ColorDrawable(ContextCompat.getColor(applicationContext,R.color.highlightCard1))
    }
    fun closeTraining(view: View){
        startActivity(
            Intent(this, MainHomeScreen::class.java)
                .apply {putExtra("newUser",false)}
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
    }
}