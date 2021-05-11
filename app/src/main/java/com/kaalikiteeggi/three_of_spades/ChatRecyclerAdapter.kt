package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chat_text.view.*

class ChatRecyclerAdapter(private val chatArray: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {		///to do something with views here later
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_text, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		holder.itemView.chatText.text = chatArray[position]
	}

	override fun getItemCount(): Int {
		return chatArray.size
	}

}