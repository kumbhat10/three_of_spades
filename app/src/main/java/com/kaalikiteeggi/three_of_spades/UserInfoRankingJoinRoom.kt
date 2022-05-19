package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.UserRankBinding
import com.squareup.picasso.Picasso

//type 1 = All time ranking
//type other = Daily ranking
@Suppress("UnnecessaryVariable")
class UserInfoRankingJoinRoom(private val context: Context, private val userArrayList: MutableList<UserBasicInfo>, val type: Int = 1) // default type = 1 for all time
    : RecyclerView.Adapter<UserInfoRankingJoinRoom.JoinRoomViewHolder>() {
    private var mExpandedPosition = -1
    private var mPrevExpandedPosition = -1

    class JoinRoomViewHolder(private val binder: UserRankBinding, val type: Int) : RecyclerView.ViewHolder(binder.root) {
        @SuppressLint("SetTextI18n")
        fun bind(userInfo: UserBasicInfo, isExpanded: Boolean) {
            binder.userRankRoot.background = ContextCompat.getDrawable(binder.root.context, R.drawable.shine_player_stats)
            binder.root.visibility = View.VISIBLE
            val layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.bottomMargin = 7
            binder.root.layoutParams = layoutParams
            binder.userRank.text = if (userInfo.index == 0) "Host" else "P${userInfo.index + 1}"
            binder.userName.text = userInfo.name
            binder.userInfo.text = userInfo.userInfo
            binder.versionInfo.text = binder.root.context.getString(R.string.versionInfo) + userInfo.appVersion
            Picasso.get().load(userInfo.photoURL).resize(200, 200).centerCrop().into(binder.userImage)
            if (type == 1) { // changes for All time window
                binder.userCoins.setText(userInfo.userCoins, true)
                binder.userScore.text = userInfo.userScore
                binder.userScore2.text = userInfo.userScoreFill // dummy fill
            } else { // changes for Daily window
                binder.userCoins.setText(userInfo.userCoinsDaily, true)
                binder.userCoins2.setText(userInfo.userCoins, true) // all time coins
                binder.userScore.text = userInfo.userScoreDaily  // daily score to do
                binder.userScore2.text = userInfo.userScore // all time score
            }
            if (isExpanded) {
                binder.userInfo.visibility = View.VISIBLE
                binder.userScore2.visibility = View.VISIBLE
                binder.versionInfo.visibility = View.VISIBLE
                if (type != 1) {
                    binder.userInfo2.visibility = View.VISIBLE
                    binder.userCoins2.visibility = View.VISIBLE
                }
            } else {
                binder.userInfo.visibility = View.GONE
                binder.userInfo2.visibility = View.GONE
                binder.userScore2.visibility = View.GONE
                binder.userCoins2.visibility = View.GONE
                binder.versionInfo.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinRoomViewHolder {
        return JoinRoomViewHolder(UserRankBinding.inflate(LayoutInflater.from(parent.context), parent, false), type = type)
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: JoinRoomViewHolder, position1: Int) {
        val position = position1
        if (!userArrayList[position].empty) {
            val isExpanded = position == mExpandedPosition
            if (isExpanded) mPrevExpandedPosition = position

            holder.bind(userInfo = userArrayList[position], isExpanded = isExpanded)

            holder.itemView.setOnClickListener {
                mExpandedPosition = if (isExpanded) -1 else position  //by pass minimize function
                if (position != mPrevExpandedPosition) notifyItemChanged(mPrevExpandedPosition)
                notifyItemChanged(position)
            }
        } else {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }
}