package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ActivityCouponDescriptionBinding

class CouponDescriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCouponDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent로 받아온 값으로 변경
        binding.couponImg.setImageResource(R.drawable.coupon_desc)
        
        binding.closeButton.setOnClickListener {
            val intent : Intent = Intent(this, CouponBoxActivity::class.java)
            startActivity(intent)
        }


    }
}