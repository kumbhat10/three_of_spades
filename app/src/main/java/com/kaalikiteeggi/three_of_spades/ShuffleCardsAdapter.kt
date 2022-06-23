package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.CardsItemListSuitsBinding

class ShuffleCardsAdapter() : RecyclerView.Adapter<ShuffleCardsAdapter.ShuffleCardsViewHolder>() {
    private val cards = createShuffleCards()

    private fun createShuffleCards(): ArrayList<Int> {
        val card = arrayListOf<Int>()
        for (i in 1..3) {
            card.addAll(PlayingCards().suitsDrawable)
        }
        return card
    }

    class ShuffleCardsViewHolder(val binder: CardsItemListSuitsBinding) : RecyclerView.ViewHolder(binder.root) {
        fun bind(suitDrawable: Int){
            binder.imageViewDisplayCard1.setImageResource(suitDrawable)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuffleCardsViewHolder {
        return ShuffleCardsViewHolder(CardsItemListSuitsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ShuffleCardsViewHolder, position: Int) {
        holder.bind(cards[position])
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.clockwise_ccw))
    }

    override fun getItemCount(): Int {
        return cards.size
    }

}