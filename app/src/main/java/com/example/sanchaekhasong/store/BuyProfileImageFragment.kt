package com.example.sanchaekhasong.store

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentBuyProfileImageBinding

class BuyProfileImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentBuyProfileImageBinding.inflate(layoutInflater, container, false)
        //db에서 가져오거나(확장 필요시) 아님 구매가능한 상품 등록
        val imgDatas = mutableListOf<String>("사과", "오렌지", "감자")
        val nameDatas = mutableListOf<String>("사과", "오렌지", "감자")
        val priceDatas = mutableListOf<Int>(3000, 6000, 30000)

        binding.buyProfileRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        binding.buyProfileRecyclerview.adapter = BuyProfileImageAdapter(imgDatas, nameDatas, priceDatas)
        binding.buyProfileRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 2, spacing = dpToPx(30, context)))
        return binding.root
    }

    fun dpToPx(dp : Int, context : Context?) : Int{
        val density : Float? = context?.resources?.displayMetrics?.density
        return (dp * density!!).toInt()
    }
}