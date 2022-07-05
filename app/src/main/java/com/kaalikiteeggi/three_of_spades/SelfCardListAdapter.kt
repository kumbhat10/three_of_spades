package com.kaalikiteeggi.three_of_spades

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.CardsItemListBinding

class SelfCardListAdapter(private val cardsArray: ArrayList<PlayingCardDescription>, private val output: (Int) -> Unit) : RecyclerView.Adapter<SelfCardListAdapter.SelfCardListViewHolder>() {

    class SelfCardListViewHolder(private val binder: CardsItemListBinding) : RecyclerView.ViewHolder(binder.root) {
        fun bind(card: PlayingCardDescription, lastCard: Boolean = false) {
            binder.imageViewDisplayCard.setImageResource(card.cardDrawable)
            if (lastCard) {
                binder.imageViewDisplayCard.setPaddingRelative(0, 0, 0, 0)
                binder.imageViewDisplayCard.layoutParams.width = binder.root.resources.getDimensionPixelSize(R.dimen.widthDisplayCardLast)
            } else {
                binder.imageViewDisplayCard.setPaddingRelative(0, 0, binder.root.resources.getDimensionPixelSize(R.dimen.paddingOtherDisplayCard), 0)
                binder.imageViewDisplayCard.layoutParams.width = binder.root.resources.getDimensionPixelSize(R.dimen.widthDisplayCardOthers)
            }
            if (card.filter) {
                binder.imageViewDisplayCard.foreground = ColorDrawable(ContextCompat.getColor(binder.root.context, R.color.inActiveCard))
            } else {
                binder.imageViewDisplayCard.foreground = ColorDrawable(ContextCompat.getColor(binder.root.context, R.color.transparent))
            }
            if (card.points != 0) {
                binder.textViewDisplayCard.visibility = View.VISIBLE
                binder.textViewDisplayCard.text = card.points.toString()
            } else {
                binder.textViewDisplayCard.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelfCardListViewHolder {
        return SelfCardListViewHolder(CardsItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SelfCardListViewHolder, position: Int) {
        val card = cardsArray[position]

        holder.bind(card, lastCard = card.expandCard || (position == cardsArray.size - 1))

        holder.itemView.setOnClickListener { view ->
            output(card.cardInt)
            if (!card.filter && card.expandCard && position != cardsArray.size - 1) view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.slide_up_out))
            else view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.scale_highlight))
        }
    }

    override fun getItemCount(): Int {
        return cardsArray.size
    }
}