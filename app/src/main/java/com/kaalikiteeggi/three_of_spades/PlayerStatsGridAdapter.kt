package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.player_stats_grid.view.*

class PlayerStatsItem(var parameterIcon: Int, var parameter: String, var parameterValue: String)

class PlayerStatsGridAdapter(var arrayList: ArrayList<PlayerStatsItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {		///to do something with views here later
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.player_stats_grid, parent, false))
	}
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		holder.itemView.parameter.text = arrayList[position].parameter
		holder.itemView.parameterValue.text = arrayList[position].parameterValue
		holder.itemView.parameterIcon.setImageResource(arrayList[position].parameterIcon)
	}
	override fun getItemCount(): Int {
		return arrayList.size
	}

}
