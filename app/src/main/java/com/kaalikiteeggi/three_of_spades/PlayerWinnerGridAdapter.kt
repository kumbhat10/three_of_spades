package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.kaalikiteeggi.three_of_spades.databinding.PlayerInfoWinnerBinding
import com.squareup.picasso.Picasso

class PlayerWinnerGridAdapter(var arrayList: ArrayList<GenericItemDescription>, val winner:Boolean) : BaseAdapter() {

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: PlayerInfoWinnerBinding.inflate(LayoutInflater.from(parent?.context), parent, false).root
        Picasso.get().load(arrayList[position].imageUrl).resize(400, 400).centerCrop().into(view.findViewById<ShapeableImageView>(R.id.userPhoto1))
        view.findViewById<MaterialTextView>(R.id.userName1).text = arrayList[position].textDescription
        if(winner) view.background =  ContextCompat.getDrawable(parent!!.context, R.drawable.blacksquarebutton) //parent?.context?.getDrawable(R.drawable.blackcirclebutton)
        return view
    }

}