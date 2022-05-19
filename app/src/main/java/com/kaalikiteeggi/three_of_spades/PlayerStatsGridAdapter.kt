package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.PlayerStatsGridBinding

class PlayerStatsItem(var parameterIcon: Int, var parameter: String, var parameterValue: String)

class PlayerStatsGridAdapter(var arrayList: ArrayList<PlayerStatsItem>) : RecyclerView.Adapter<PlayerStatsGridAdapter.PlayerStatsViewHolder>()  {

	class PlayerStatsViewHolder(private val binder: PlayerStatsGridBinding) : RecyclerView.ViewHolder(binder.root) {		///to do something with views here later
		fun bind(playerStatsItem: PlayerStatsItem){
			binder.parameter.text = playerStatsItem.parameter
			binder.parameterValue.text = playerStatsItem.parameterValue
			binder.parameterIcon.setImageResource(playerStatsItem.parameterIcon)
		}
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerStatsViewHolder {
		return PlayerStatsViewHolder(PlayerStatsGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}
	override fun onBindViewHolder(holder: PlayerStatsViewHolder, position: Int) {

		holder.bind(arrayList[position])
	}
	override fun getItemCount(): Int {
		return arrayList.size
	}

}
