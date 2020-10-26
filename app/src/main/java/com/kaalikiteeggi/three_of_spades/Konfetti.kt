package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.ArrayList

enum class KonType{
    Money, Spade, Win, Lost
}

fun createKonfetti(context: Context, view: KonfettiView, duration:Long = 2000L, size:Int = 50,
    konType:KonType = KonType.Win, burst:Boolean = false, speed:Float = 3f, ratePerSec:Int = 200, burstCount:Int = 100){
    val ca = when(konType){
        KonType.Money -> listOf(ContextCompat.getColor(context, R.color.coin_yellow))
        KonType.Spade-> listOf(Color.DKGRAY)
        else-> listOf( Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.MAGENTA, Color.GREEN)
    }

    val moneyD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.moneykon)!!, true)
    val spadeD = Shape.DrawableShape(ContextCompat.getDrawable(context, R.drawable.spadekon)!!, true)
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
    } else if(konType == KonType.Spade){
        shapes.add(spadeD)
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
        .addColors(ca)
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
            .addColors(ca)
            .setDirection(0.0, 359.0)
            .setSpeed(speed, speed+4f)
            .setFadeOutEnabled(true)
            .setTimeToLive(8000L)
            .addShapes(*shapes.toTypedArray()) // hD, starD,// Shape.Circle, Shape.Square
//            .addSizes(Size(size))
            .addSizes(Size(50,5f), Size(40,2f), Size(60,3f))
            .setPosition(-50f, view.width.toFloat()+50, -50f, -50f)
            .streamFor(ratePerSec, duration)

}