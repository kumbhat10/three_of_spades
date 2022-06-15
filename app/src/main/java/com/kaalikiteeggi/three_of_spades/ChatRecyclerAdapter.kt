package com.kaalikiteeggi.three_of_spades

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.ChatTextBinding
import com.squareup.picasso.Picasso

data class ChatMessage(val message:String, val player:Int, val isEmojiOnly: Boolean = false, val isNotification:Boolean = false)

class ChatRecyclerAdapter(private val chatArray: ArrayList<ChatMessage>,  val userPhotoInfo: List<String>) : RecyclerView.Adapter<ChatRecyclerAdapter.ChatViewHolder>() {

	class ChatViewHolder(private val binder: ChatTextBinding) : RecyclerView.ViewHolder(binder.root) {
		fun bind(chatMessage: ChatMessage, photoUrl: String){
			binder.chatText.text = chatMessage.message
			if(chatMessage.isEmojiOnly){
				binder.chatText.textSize = 28f
			}else{
				binder.chatText.textSize = 15f
			}
			if(chatMessage.isNotification){
				binder.chatText.setTextColor(ContextCompat.getColor(binder.root.context, R.color.font_yellow))
			}else{
				binder.chatText.setTextColor(ContextCompat.getColor(binder.root.context, R.color.font_grey_settings))
			}
			if(photoUrl.isNotEmpty()) Picasso.get().load(photoUrl).resize(100, 100).error(R.drawable.user_photo).into(binder.userPhoto)
		}
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
		return ChatViewHolder(ChatTextBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
		holder.bind(chatArray[position], userPhotoInfo[chatArray[position].player-1])
	}

	override fun getItemCount(): Int {
		return chatArray.size
	}

	override fun getItemViewType(position: Int): Int {
		return  super.getItemViewType(position)
	}

}