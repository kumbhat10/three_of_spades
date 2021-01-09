package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_rank.view.*

class LVAdapterJoinRoom(private val context: Context, private val userArrayList: MutableList<UserBasicInfo>, private val type: Int = 1) // default type = 1 for all time
	: RecyclerView.Adapter<LVAdapterJoinRoom.ViewHolder>() {

	private var mExpandedPosition = -1
	private var mPrevExpandedPosition = -1

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		///to do something with views here later
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_rank, parent, false))
	}

	override fun getItemCount(): Int {
		return userArrayList.size
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (!userArrayList[position].empty) {
			holder.itemView.visibility = View.VISIBLE
			holder.itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			val index = userArrayList[position].index
//			if(index!=0) holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down_in))
			holder.itemView.userRank.text = if(index==0) "Host" else "P${index+1}"
			holder.itemView.userName.text = userArrayList[position].name
			holder.itemView.userInfo.text = userArrayList[position].userInfo
			holder.itemView.versionInfo.text = context.getString(R.string.versionInfo) + userArrayList[position].appVersion
			Picasso.get().load(userArrayList[position].photoURL).resize(200, 200).centerCrop()
				.into(holder.itemView.userImage)
			if (type == 1) { // changes for All time window
				holder.itemView.userCoins.setText(userArrayList[position].userCoins, true)
				holder.itemView.userScore.text = userArrayList[position].userScore
				holder.itemView.userScore2.text = userArrayList[position].userScoreFill // dummy fill
			} else { // changes for Daily window
				holder.itemView.userCoins.setText(userArrayList[position].userCoinsDaily, true)
				holder.itemView.userCoins2.setText(userArrayList[position].userCoins, true) // all time coins
				holder.itemView.userScore.text = userArrayList[position].userScoreDaily  // daily score to do
				holder.itemView.userScore2.text = userArrayList[position].userScore // all time score
			}

			val isExpanded = position == mExpandedPosition
			if (isExpanded) mPrevExpandedPosition = position
			if (isExpanded) {
				holder.itemView.userInfo.visibility = View.VISIBLE
				holder.itemView.userScore2.visibility = View.VISIBLE
				holder.itemView.versionInfo.visibility = View.VISIBLE
				if (type != 1) {
					holder.itemView.userInfo2.visibility = View.VISIBLE
					holder.itemView.userCoins2.visibility = View.VISIBLE
				}
			} else {
				holder.itemView.userInfo.visibility = View.GONE
				holder.itemView.userInfo2.visibility = View.GONE
				holder.itemView.userScore2.visibility = View.GONE
				holder.itemView.userCoins2.visibility = View.GONE
				holder.itemView.versionInfo.visibility = View.GONE
			}
			holder.itemView.setOnClickListener {
				mExpandedPosition = if (isExpanded) -1 else position  //by pass minimize function
				if (position != mPrevExpandedPosition) notifyItemChanged(mPrevExpandedPosition)
				notifyItemChanged(position)
			}
		}
		else {
			holder.itemView.visibility = View.GONE
			holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
		}
	}
}