package com.kaalikiteeggi.three_of_spades

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
/**
 * Use the [TestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestFragment(index:Int = 0) : Fragment() {

//	private var ind:Int = index
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
//		ind = arguments?.getInt("index", 0)!!
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_test, container, false)
	}

	companion object {
		@JvmStatic
		fun newInstance(index: Int = 0) = TestFragment(index).apply {
			arguments = Bundle().apply {
				putInt("index", index)
			}
//			}
		}
	}
}