package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ActivityCouponDescriptionBinding

class CouponDescriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCouponDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.getStringExtra("coupon_desc")
        val resId = resources.getIdentifier(
            fileName, "drawable", packageName
        )
        binding.couponImg.setImageResource(resId)
        
        binding.closeButton.setOnClickListener {
            val intent : Intent = Intent(this, CouponBoxActivity::class.java)
            startActivity(intent)
        }


    }
}