package com.kaalikiteeggi.three_of_spades

import android.content.Context
import androidx.core.content.ContextCompat
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.*

enum class KonType{
    Money, Win, Lost
}

fun createKonfetti(context: Context, view: KonfettiView, duration: Long = 2000L, konType: KonType = KonType.Win, burst: Boolean = false, speed: Float = 3f, ratePerSec: Int = 200, burstCount: Int = 100){


    val moneyD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.moneykon)!!, false)
    val coinD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.coinkon)!!, false)
    val sadD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.sadcry)!!, false)
    val sad2D = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.sad)!!, false)
    val cryD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.cry)!!, false)
    val happyD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.happykon)!!, false)
    val trophyD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.trophykon)!!, false)

    val shapes = ArrayList<Shape>()
    if(konType == KonType.Money){
        shapes.add(moneyD)
        shapes.add(coinD)
    }else if(konType == KonType.Win){
        shapes.add(trophyD)
        shapes.add(coinD)
        shapes.add(happyD)
    }else if(konType == KonType.Lost){
        shapes.add(sadD)
        shapes.add(cryD)
        shapes.add(sad2D)
    }

    if(burst) view.build()
        .setDirection(100.0, 260.0)
        .setSpeed(speed, speed+5f)
        .setAccelerationEnabled(true)
        .setFadeOutEnabled(true)
        .setTimeToLive(8000L)
        .addShapes(*shapes.toTypedArray()) // hD, starD,// Shape.Circle, Shape.Square
        .addSizes(Size(50,4f), Size(40,5f), Size(60,4f), Size(70,5f))
        .setPosition(view.width.toFloat()-100f,  30f)
        .burst(burstCount)
    else
        view.build()
            .setDirection(0.0, 359.0)
            .setSpeed(speed, speed+4f)
            .setFadeOutEnabled(true)
            .setTimeToLive(5000L)
            .addShapes(*shapes.toTypedArray()) // hD, starD,// Shape.Circle, Shape.Square
//            .addSizes(Size(size))
            .addSizes(Size(50,5f), Size(40,2f), Size(60,3f))
            .setPosition(-50f, view.width.toFloat()+50f, -50f, -50f)
            .streamFor(ratePerSec, duration)

}