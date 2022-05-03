package com.kaalikiteeggi.three_of_spades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_text.view.*

data class ChatMessage(val message:String, val player:Int, val isEmojiOnly: Boolean = false)

class ChatRecyclerAdapter(private val chatArray: ArrayList<ChatMessage>, private val userPhotoInfo: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {		///to do something with views here later
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_text, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		holder.itemView.chatText.text = chatArray[position].message
		if(chatArray[position].isEmojiOnly){
			holder.itemView.chatText.textSize = 28f
		}else{
			holder.itemView.chatText.textSize = 15f
		}
		Picasso.get().load(userPhotoInfo[chatArray[position].player -1]).resize(100, 100).error(R.drawable.user_photo).into(holder.itemView.userPhoto)
//		Picasso.get().load(userPhotoInfo[2]).resize(50, 50).error(R.drawable.user_photo).into(holder.itemView.userPhoto)
	}

	override fun getItemCount(): Int {
		return chatArray.size
	}

}