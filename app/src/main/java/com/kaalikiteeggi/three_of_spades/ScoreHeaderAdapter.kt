package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.kaalikiteeggi.three_of_spades.databinding.ScoreHeaderBinding
import com.squareup.picasso.Picasso

class ScoreHeaderAdapter (var arrayList: ArrayList<PlayerScoreItemDescription>) : BaseAdapter() {

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
        val view: View = convertView ?: ScoreHeaderBinding.inflate(LayoutInflater.from(parent?.context), parent, false).root
        if(position!=0){
        if(arrayList[position].imageUrl.isNotEmpty()) Picasso.get().load(arrayList[position].imageUrl).resize(400, 400).centerCrop().into(view.findViewById<ShapeableImageView>(R.id.userPhoto1))
        view.findViewById<MaterialTextView>(R.id.userName1).text = arrayList[position].playerName
        view.findViewById<MaterialTextView>(R.id.userScore2).text = if(arrayList[position].points > 0) "+${arrayList[position].points}" else arrayList[position].points.toString()
        if(arrayList[position].points >= 0) view.background =  ContextCompat.getDrawable(parent!!.context, R.drawable.shine_user_rank1)
            else view.background =  ContextCompat.getDrawable(parent!!.context, R.drawable.shine_user_rank)
        }
        return view
    }

}