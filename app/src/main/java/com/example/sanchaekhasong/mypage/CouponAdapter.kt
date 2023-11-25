package com.example.sanchaekhasong.mypage

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.CouponRecyclerviewBinding


class CouponViewHolder(val binding: CouponRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class CouponAdapter(val imgDatas : MutableList<String>, val nameDatas : MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var context : Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun getItemCount(): Int = imgDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = CouponViewHolder(
        CouponRecyclerviewBinding.inflate(
        LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CouponViewHolder).binding
        //imgDatas에서 가져오기
        binding.couponImage.setImageResource(R.drawable.coupon)
        binding.couponName.text = nameDatas[position]
        
        binding.couponImage.setOnClickListener {
            val intent : Intent = Intent(context, CouponDescriptionActivity::class.java)
            //putStringExtra?로 이미지 string전달하고 해당 액티비티에서 변경
            context.startActivity(intent)
        }
    }
}
