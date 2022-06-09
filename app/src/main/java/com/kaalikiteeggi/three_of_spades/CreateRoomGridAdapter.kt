package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.kaalikiteeggi.three_of_spades.databinding.CreateRoomOptionsBinding

class CreateRoomGridAdapter(var context: Context, var arrayList: ArrayList<CreateRoomItemDescription>, val output: (Int) -> Unit) : BaseAdapter() {

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: CreateRoomOptionsBinding.inflate(LayoutInflater.from(parent?.context), parent, false).root

        view.findViewById<MaterialTextView>(R.id.gameInfo).text = arrayList[position].gameInfo
        view.findViewById<MaterialTextView>(R.id.gameInfo1).text = " " + arrayList[position].gameInfo1
        view.findViewById<MaterialTextView>(R.id.gameInfo2).text = arrayList[position].gameInfo2
        if (position == 1) view.background = ContextCompat.getDrawable(parent!!.context, R.drawable.bluerectanglesimple)
        else if (position == 2) view.background = ContextCompat.getDrawable(parent!!.context, R.drawable.yellowrectanglesimple)

        view.setOnClickListener { view1 ->
            view1.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_press))
            output(position)  // send the position of item clicked as output from this class
        }
        return view
    }

}