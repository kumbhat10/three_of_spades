package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.CardsItemListPartnerBinding

class PartnerCardListAdapter(private val nPlayers: Int, private val output: (Int) -> Unit) : RecyclerView.Adapter<PartnerCardListAdapter.PartnerCardListViewHolder>() {

    private val cardsIndexSortedPartner = if (nPlayers == 4) PlayingCards().cardsIndexSortedPartner4
    else PlayingCards().cardsIndexSortedPartner7

    class PartnerCardListViewHolder(private val binder: CardsItemListPartnerBinding, val nPlayers: Int) : RecyclerView.ViewHolder(binder.root) {
        private val cardsDrawablePartner = if (nPlayers == 4) PlayingCards().cardsDrawable4
        else PlayingCards().cardsDrawablePartner7
        private val cardsPointsPartner = if (nPlayers == 4) PlayingCards().cardsPoints4
        else PlayingCards().cardsPointsPartner7

        @SuppressLint("SetTextI18n")
        fun bind(card: Int){
            binder.imageViewPartner.setImageResource(cardsDrawablePartner[card])
            if (cardsPointsPartner[card] != 0) {
                binder.textViewPartner.text = "${cardsPointsPartner[card]} pts"
                binder.textViewPartner.visibility = View.VISIBLE
            } else {
                binder.textViewPartner.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerCardListViewHolder {
        return PartnerCardListViewHolder(CardsItemListPartnerBinding.inflate(LayoutInflater.from(parent.context), parent, false), nPlayers = nPlayers)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PartnerCardListViewHolder, position: Int) {
        val card = cardsIndexSortedPartner[position]

        holder.bind(card = card)
        holder.itemView.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.click_press))
            output(card)
        }
    }

    override fun getItemCount(): Int {
        return cardsIndexSortedPartner.size
    }

}