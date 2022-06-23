package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.kaalikiteeggi.three_of_spades.databinding.ScoreHeaderBinding
import com.squareup.picasso.Picasso
import kotlin.math.min

class ScoreHeaderAdapter (var arrayList: ArrayList<PlayerScoreItemDescription>) : RecyclerView.Adapter<ScoreHeaderAdapter.ScoreHeaderViewHolder>() {

    class ScoreHeaderViewHolder(private val binder: ScoreHeaderBinding) : RecyclerView.ViewHolder(binder.root) {
        fun bind(playerScore: PlayerScoreItemDescription, position:Int) {
            if(position!=0){
                if(playerScore.imageUrl.isNotEmpty()) Picasso.get().load(playerScore.imageUrl).resize(400, 400).centerCrop().into(binder.userPhoto1)
                binder.userName1.text = playerScore.playerName
                if(playerScore.showRank){
                    binder.userRank1.visibility = View.VISIBLE
                    binder.userRank1.text = playerScore.rank
                }
                binder.userScore2.text = if(playerScore.points > 0) "+${playerScore.points}" else playerScore.points.toString()
                if(playerScore.points >= 0) {
//                    binder.root.background =  ContextCompat.getDrawable(binder.root.context, R.drawable.black_button)
                    binder.userScore2.background =  ContextCompat.getDrawable(binder.root.context, R.drawable.bluerectanglesimple)
                }
                else {
//                    binder.root.background =  ContextCompat.getDrawable(binder.root.context, R.drawable.red_button)
                    binder.userScore2.background =  ContextCompat.getDrawable(binder.root.context, R.drawable.redrectanlgesimple)
                }
            }else{
                binder.root.setBackgroundColor(ContextCompat.getColor(binder.root.context, R.color.transparent))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHeaderViewHolder {
        val v = ScoreHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        v.root.layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, parent.context.resources.configuration.screenWidthDp.toFloat(), parent.context.resources.displayMetrics).toInt()/arrayList.size
        v.root.layoutParams.width = min(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140F, parent.context.resources.displayMetrics).toInt(), TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, parent.context.resources.configuration.screenWidthDp.toFloat(), parent.context.resources.displayMetrics).toInt()/arrayList.size)
        return ScoreHeaderViewHolder(v)
    }

    override fun onBindViewHolder(holder: ScoreHeaderViewHolder, position: Int) {
        holder.bind(playerScore = arrayList[position], position = position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

}