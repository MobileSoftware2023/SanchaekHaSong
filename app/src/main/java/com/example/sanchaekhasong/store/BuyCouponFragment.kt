package com.example.sanchaekhasong.store

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentBuyCouponBinding

class BuyCouponFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentBuyCouponBinding.inflate(layoutInflater, container, false)
        //db에서 가져오거나 아니면 그냥 이미지 알아서 등록
        val imgDatas = mutableListOf<String>("사과", "오렌지", "감자")
        val nameDatas = mutableListOf<String>("사과", "오렌지", "감자")
        val priceDatas = mutableListOf<Int>(3000, 6000, 30000)

        binding.buyCouponRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        binding.buyCouponRecyclerview.adapter = BuyCouponAdapter(imgDatas, nameDatas, priceDatas)
        binding.buyCouponRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 2, spacing = dpToPx(30, context)))
        return binding.root
    }

    fun dpToPx(dp : Int, context : Context?) : Int{
        val density : Float? = context?.resources?.displayMetrics?.density
        return (dp * density!!).toInt()
    }
}