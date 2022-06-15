package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.kaalikiteeggi.three_of_spades.databinding.ScoreBodyBinding
import com.kaalikiteeggi.three_of_spades.databinding.ScoreHeaderBinding

class ScoreBodyAdapter (var arrayList: ArrayList<Int>, val nPlayers: Int) : BaseAdapter() {

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
        val view: View = convertView ?: ScoreBodyBinding.inflate(LayoutInflater.from(parent?.context), parent, false).root
        if(position%(nPlayers+1) !=0){
            if(arrayList[position] >= 0){
                view.findViewById<MaterialTextView>(R.id.scoreBody).text = "+${arrayList[position]}"
                view.findViewById<MaterialTextView>(R.id.scoreBody).setTextColor(ContextCompat.getColor(parent!!.context, R.color.borderblueDark1g))
                view.background =  ContextCompat.getDrawable(parent.context, R.drawable.red_button)
            }
            else {
                view.findViewById<MaterialTextView>(R.id.scoreBody).text = arrayList[position].toString()
                view.findViewById<MaterialTextView>(R.id.scoreBody).setTextColor(ContextCompat.getColor(parent!!.context, R.color.Red))
                view.background =  ContextCompat.getDrawable(parent.context, R.drawable.black_button)
            }
        }
        else{
            view.findViewById<MaterialTextView>(R.id.scoreBody).text = arrayList[position].toString()
            view.findViewById<MaterialTextView>(R.id.scoreBody).setTextColor(ContextCompat.getColor(parent!!.context, R.color.font_yellow))
        }
        return view
    }

}