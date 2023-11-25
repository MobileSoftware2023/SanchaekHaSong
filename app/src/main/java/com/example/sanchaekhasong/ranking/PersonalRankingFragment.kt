package com.example.sanchaekhasong.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.databinding.FragmentPersonalRankingBinding

class PersonalRankingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPersonalRankingBinding.inflate(layoutInflater,container,false)
        //db에서 가져와서 datats에 저장
        val rankDatas = mutableListOf<Int>(1, 2)
        val imgDatas = mutableListOf<String>("snow", "snow")
        val collegeDatas = mutableListOf<String>("이과대학", "공과대학")
        val nameDatas = mutableListOf<String>("김눈송", "이눈송")
        val walkCountDatas = mutableListOf<Int>(21222, 20111)

        binding.personalRankingRecyclerview.layoutManager = LinearLayoutManager(activity)
        binding.personalRankingRecyclerview.adapter = PersonalRankingAdapter(rankDatas, imgDatas, collegeDatas, nameDatas, walkCountDatas)

        return binding.root
    }
}