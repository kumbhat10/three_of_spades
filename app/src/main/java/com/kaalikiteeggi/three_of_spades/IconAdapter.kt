package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.icons_home_screen.view.*

class IconAdapter(var context: Context, var arrayList: ArrayList<DailyRewardItem>, val output: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewAdapter.ViewHolder {
		return ListViewAdapter.ViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.icons_home_screen, parent, false))
	}

	override fun getItemCount(): Int {
		return arrayList.size
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		holder.itemView.iconImage1.setImageResource(arrayList[position].imageID!!)
		holder.itemView.iconText.text = arrayList[position].textDescription
		holder.itemView.setOnClickListener {view ->
			view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_press))
			output(position)  // send the position of item clicked as output from this class
		}
	}

}