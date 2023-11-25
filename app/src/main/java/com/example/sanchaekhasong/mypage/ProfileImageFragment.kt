package com.example.sanchaekhasong.mypage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentProfileImageBinding

class ProfileImageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileImageBinding.inflate(layoutInflater,container,false)
        //db에서 img이름 가져와서 datats에 저장
        val imgDatas = mutableListOf<String>("사과", "오렌지", "망고", "키위", "석류", "레몬")

        binding.profileRecyclerview.layoutManager = GridLayoutManager(activity, 5)
        binding.profileRecyclerview.adapter = ProfileAdapter(imgDatas)

        binding.profileRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 5, spacing = dpToPx(15, context)))
        return binding.root
    }

    fun dpToPx(dp : Int, context : Context?) : Int{
        val density : Float? = context?.resources?.displayMetrics?.density
        return (dp * density!!).toInt()
    }
}