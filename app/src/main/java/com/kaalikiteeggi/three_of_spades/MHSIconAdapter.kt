package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.IconsHomeScreenBinding

class MHSIconAdapter(var context: Context, var arrayList: ArrayList<GenericItemDescription>, val output: (Int) -> Unit) : RecyclerView.Adapter<MHSIconAdapter.MHSIconViewHolder>() {

	class MHSIconViewHolder(private val binder: IconsHomeScreenBinding): RecyclerView.ViewHolder(binder.root){
		fun bind(item: GenericItemDescription){
			binder.iconImage1.setImageResource(item.imageID)
			binder.iconText.text = item.textDescription
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MHSIconViewHolder {
		return MHSIconViewHolder(IconsHomeScreenBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}
	override fun getItemCount(): Int {
		return arrayList.size
	}
	override fun onBindViewHolder(holder: MHSIconViewHolder, position: Int) {
		holder.bind(arrayList[position])
		holder.itemView.setOnClickListener {view ->
			view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_press))
			output(position)  // send the position of item clicked as output from this class
		}
	}
}