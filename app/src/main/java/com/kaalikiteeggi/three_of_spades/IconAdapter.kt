package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class IconAdapter(var context: Context, var arrayList: ArrayList<DailyRewardItem>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view:View = View.inflate(context, R.layout.icons_home_screen, null)
        val icons:ImageView = view.findViewById(R.id.iconImage1)
        val textDescription:TextView = view.findViewById(R.id.iconText)
        val listItem: DailyRewardItem = arrayList[position]
        textDescription.text = listItem.textDescription
        icons.setImageResource(listItem.imageID!!)

//        view.setOnClickListener{
//            MainHomeScreen().toastCenter(position.toString())
//        }

//        view.background = ContextCompat.getDrawable(context, R.drawable.border_square_reward)
//        icons.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_scale_infinite))
//        textDescription.background = ContextCompat.getDrawable(context, R.drawable.button_square)
//        icons.setImageResource(R.drawable.rateus)
//        textDescription.setTextColor(ContextCompat.getColor(context, R.color.white))

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