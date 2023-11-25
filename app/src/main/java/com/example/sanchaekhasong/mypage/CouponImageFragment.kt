package com.example.sanchaekhasong.mypage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentCouponImageBinding

class CouponImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCouponImageBinding.inflate(layoutInflater,container,false)
        //db에서 img이름 가져와서 datats에 저장
        val imgDatas = mutableListOf<String>("사과", "오렌지", "감자")
        val nameDatas = mutableListOf<String>("사과", "오렌지", "감자")

        binding.couponRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        binding.couponRecyclerview.adapter = CouponAdapter(imgDatas, nameDatas)
        binding.couponRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 2, spacing = dpToPx(30, context)))


        return binding.root
    }

    fun dpToPx(dp : Int, context : Context?) : Int{
        val density : Float? = context?.resources?.displayMetrics?.density
        return (dp * density!!).toInt()
    }
}