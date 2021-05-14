package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cards_item_list_partner.view.*

class PartnerCardListAdapter(private val nPlayers:Int, private val output: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val cardsDrawablePartner = if(nPlayers==4) PlayingCards().cardsDrawable4
	else PlayingCards().cardsDrawablePartner7

	private val cardsIndexSortedPartner = if(nPlayers==4) PlayingCards().cardsIndexSortedPartner4
	else PlayingCards().cardsIndexSortedPartner7

	private val cardsPointsPartner = if(nPlayers==4) PlayingCards().cardsPoints4
	else PlayingCards().cardsPointsPartner7

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return ViewHolder(LayoutInflater.from(parent.context)
			.inflate(R.layout.cards_item_list_partner, parent, false))
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val card = cardsIndexSortedPartner[position]
		holder.itemView.imageViewPartner.setImageResource(cardsDrawablePartner[card])
		if(cardsPointsPartner[card] != 0 ){
			holder.itemView.textViewPartner.text = "${cardsPointsPartner[card]} pts"
			holder.itemView.textViewPartner.visibility = View.VISIBLE
		}else{
			holder.itemView.textViewPartner.visibility = View.INVISIBLE
		}
		holder.itemView.setOnClickListener{view ->
			view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.click_press))
			output(card)
		}
	}

	override fun getItemCount(): Int {
		return cardsIndexSortedPartner.size
	}


}