package com.kaalikiteeggi.three_of_spades

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.ScoreBodyBinding
import kotlin.math.min

class ScoreBodyAdapter(var arrayList: ArrayList<Int>, private val nColumns: Int) : RecyclerView.Adapter<ScoreBodyAdapter.ScoreBodyViewHolder>() {
    class ScoreBodyViewHolder(private val binder: ScoreBodyBinding, private val nColumns: Int) : RecyclerView.ViewHolder(binder.root) {
        fun bind(playerScore: Int, position: Int) {
            if (position % (nColumns) != 0) {
                binder.scoreBodyText.text = if (playerScore > 0) "+${playerScore}" else playerScore.toString()
                if (playerScore >= 0) {
                    binder.root.background = ContextCompat.getDrawable(binder.root.context, R.drawable.bluerectanglesimple)
//                    binder.scoreBodyText.setTextColor(ContextCompat.getColor(binder.root.context, R.color.yellow))
                } else {
                    binder.root.background = ContextCompat.getDrawable(binder.root.context, R.drawable.redrectanlgesimple)
//                    binder.scoreBodyText.setTextColor(ContextCompat.getColor(binder.root.context, R.color.red1))
                }
            } else {
                binder.scoreBodyText.text = playerScore.toString()
                binder.root.setBackgroundResource(R.color.transparent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreBodyViewHolder {
        val v = ScoreBodyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        v.root.layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, parent.context.resources.configuration.screenWidthDp.toFloat(), parent.context.resources.displayMetrics).toInt() / nColumns
        v.root.layoutParams.width = min(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140F, parent.context.resources.displayMetrics).toInt(), TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, parent.context.resources.configuration.screenWidthDp.toFloat(), parent.context.resources.displayMetrics).toInt()/nColumns)

        return ScoreBodyViewHolder(v, nColumns = nColumns)
    }

    override fun onBindViewHolder(holder: ScoreBodyViewHolder, position: Int) {
        holder.bind(playerScore = arrayList[position], position = position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

}