package com.kaalikiteeggi.three_of_spades

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class TestFragment : Fragment() {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_test, container, false)
	}

//	companion object {
//		@JvmStatic
//		fun newInstance(index: Int = 0) = TestFragment().apply {
//			arguments = Bundle().apply {
//				putInt("index", index)
//			}
////			}
//		}
//	}

//	override fun onDestroyView() {
//		super.onDestroyView()
//	}
}