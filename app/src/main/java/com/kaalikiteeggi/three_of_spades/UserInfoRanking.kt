package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaalikiteeggi.three_of_spades.databinding.UserRankBinding
import com.squareup.picasso.Picasso

class UserInfoRanking(private val context: Context, private val userArrayList: ArrayList<UserBasicInfo>, private val type: Int = 1) // default type = 1 for all time
    : RecyclerView.Adapter<UserInfoRanking.UserInfoRankingViewHolder>() {

    private var mExpandedPosition = -1
    private var mPrevExpandedPosition = -1

    class UserInfoRankingViewHolder(private val binder: UserRankBinding, val type: Int) : RecyclerView.ViewHolder(binder.root) {
        fun bind(userInfo: UserBasicInfo, isExpanded: Boolean, position:Int) {
            binder.userRank.text = userInfo.userRank
            if (position < 3) {
                binder.userRank.setBackgroundResource(R.drawable.trophy)
                binder.userRank.setTextColor(ContextCompat.getColor(binder.root.context, R.color.Black))
            } else {
                binder.userRank.setBackgroundResource(R.drawable.redcartoonbanner)
                binder.userRank.setTextColor(ContextCompat.getColor(binder.root.context, R.color.white))
            }
            binder.userName.text = userInfo.name
            binder.userInfo.text = userInfo.userInfo
            binder.versionInfo.text = binder.root.context.getString(R.string.versionInfo) + userInfo.appVersion

            Picasso.get().load(userInfo.photoURL).resize(200, 200).into(binder.userImage)
            if (userInfo.newUser) {
                binder.userNewText.visibility = View.VISIBLE
                binder.space7.visibility = View.VISIBLE
            } else {
                binder.userNewText.visibility = View.GONE
                binder.space7.visibility = View.GONE
            }

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
//                binder.lineBreak.visibility = View.VISIBLE
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
//                binder.lineBreak.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserInfoRankingViewHolder {
        return UserInfoRanking.UserInfoRankingViewHolder(UserRankBinding.inflate(LayoutInflater.from(parent.context), parent, false), type = type)
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    @Suppress("UnnecessaryVariable")
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserInfoRankingViewHolder, position1: Int) {
        val position = position1
        val isExpanded = position == mExpandedPosition
        if (isExpanded) mPrevExpandedPosition = position

        holder.bind(userInfo = userArrayList[position], isExpanded = isExpanded, position = position)
        holder.itemView.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_press))
            mExpandedPosition = if (isExpanded) -1 else position  //by pass minimize function
            if (position != mPrevExpandedPosition) notifyItemChanged(mPrevExpandedPosition)
            notifyItemChanged(position)
        }
    }
}