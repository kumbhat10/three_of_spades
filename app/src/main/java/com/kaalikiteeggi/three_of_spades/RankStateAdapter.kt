package com.kaalikiteeggi.three_of_spades

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankStateAdapter(fragmentActivity: FragmentActivity, val tabs:Int):
	FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int {
		return tabs//fragments.size
	}

	override fun createFragment(positiosn: Int): Fragment {
		return TestFragment() //.newInstance(position)
	}

}