package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.kaalikiteeggi.three_of_spades.databinding.CoinRewardGridBinding

class DailyRewardGridAdapter( var arrayList: ArrayList<GenericItemDescription>, private var highlightPosition:Int) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = CoinRewardGridBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        view.coinRewardText.text = arrayList[position].textDescription
        if(position==highlightPosition-1){
            view.root.background = ContextCompat.getDrawable(parent!!.context,R.drawable.shine_player_stats)
            view.coinLottie.visibility = View.VISIBLE
            view.coinRewardIcon.visibility = View.GONE
            view.coinRewardText.setTextColor(ContextCompat.getColor(parent.context, R.color.font_yellow))
            view.coinRewardText.textSize = parent.context.resources.getDimension(R.dimen._13ssp)/parent.context.resources.displayMetrics.density
        }
        return view.root
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getCount(): Int {
        return arrayList.size
    }
}