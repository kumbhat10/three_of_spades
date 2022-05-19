package com.kaalikiteeggi.three_of_spades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankStateAdapter(fragmentActivity: FragmentActivity, val tabs:Int): FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int {
		return tabs//fragments.size
	}
	override fun createFragment(position: Int): Fragment {
		return RankFragment()
	}
}

class RankFragment : Fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_test, container, false)
	}
}