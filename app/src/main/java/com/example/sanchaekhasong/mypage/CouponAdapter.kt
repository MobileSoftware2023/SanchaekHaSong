package com.example.sanchaekhasong.mypage

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.CouponRecyclerviewBinding


class CouponViewHolder(val binding: CouponRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class CouponAdapter(var imgDatas : List<String>, var nameDatas : List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

        val fileName = imgDatas[position]
        val resId = binding.couponImage.context.resources.getIdentifier(
            fileName, "drawable", binding.couponImage.context.packageName
        )
        binding.couponImage.setTag(fileName)
        binding.couponImage.setImageResource(resId)
        binding.couponName.text = nameDatas[position]
        
        binding.couponImage.setOnClickListener {
            val intent : Intent = Intent(context, CouponDescriptionActivity::class.java)
            val fileName = binding.couponImage.getTag().toString()
            intent.putExtra("coupon_desc", fileName + "desc")
            context.startActivity(intent)
        }
    }

    fun updateData(newCouponImageList: List<String>, newCouponNameList : List<String>) {
        imgDatas = newCouponImageList
        nameDatas = newCouponNameList
        notifyDataSetChanged()
    }
}
