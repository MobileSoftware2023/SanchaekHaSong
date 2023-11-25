package com.example.sanchaekhasong.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ProfileRecyclerviewBinding

class ProfileViewHolder(val binding: ProfileRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class ProfileAdapter(val datas : MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ProfileViewHolder(ProfileRecyclerviewBinding.inflate(
        LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ProfileViewHolder).binding
        //datas에서 이미지 정보 가져오기
        binding.profileImagesButton.setImageResource(R.drawable.snow)

        binding.profileImagesButton.setOnClickListener {
            //자기 자신의 이미지로 변경
            //binding.profileImage.setImageResource(R.drawable.snow1)
            Toast.makeText(it.context, "Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}