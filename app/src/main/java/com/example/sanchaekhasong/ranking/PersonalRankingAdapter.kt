package com.example.sanchaekhasong.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.PersonalRecyclerviewBinding

class PersonalRankingViewHolder(val binding: PersonalRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class PersonalRankingAdapter(val rankDatas : MutableList<Int>, val imgDatas : MutableList<String>, val collegeDatas : MutableList<String>, val nameDatas : MutableList<String>, val walkCountDatas : MutableList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = rankDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = PersonalRankingViewHolder(
        PersonalRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as PersonalRankingViewHolder).binding

        //rankDatas
        binding.rank.text = rankDatas[position].toString()

        //db에서 img 가져오기 when문 사용
        // when(imgDatas[position] : "snow" -> ...)
        binding.profileImage.setImageResource(R.drawable.snow)

        //college
        binding.college.text = collegeDatas[position]

        //name
        binding.username.text = nameDatas[position]

        //walkcount
        binding.walkCount.text = walkCountDatas[position].toString() + "걸음"


    }
}
