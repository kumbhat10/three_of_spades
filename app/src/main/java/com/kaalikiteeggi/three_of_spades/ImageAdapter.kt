package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class ImageAdapter(var context: Context, var arrayList: ArrayList<DailyRewardItem>, var highlightPosition:Int) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view:View = View.inflate(context, R.layout.coin_reward_grid, null)
        val icons:ImageView = view.findViewById(R.id.coinRewardIcon)
        val textDescription:TextView = view.findViewById(R.id.coinRewardText)
        val listItem: DailyRewardItem = arrayList[position]
        textDescription.text = listItem.textDescription

        if(position==highlightPosition-1){
            view.background = ContextCompat.getDrawable(context,R.drawable.border_square_reward)
            icons.startAnimation(AnimationUtils.loadAnimation(context,R.anim.anim_scale_infinite))
//            textDescription.background = ContextCompat.getDrawable(context,R.drawable.button_square)
            icons.setImageResource(R.drawable.coin_trans_1)
            textDescription.setTextColor(ContextCompat.getColor(context, R.color.colorongreen))
        }else{
            icons.setImageResource(R.drawable.coin_trans_1blurr)
            textDescription.background = ContextCompat.getDrawable(context,R.drawable.blacksquarebutton)
        }
        return view
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