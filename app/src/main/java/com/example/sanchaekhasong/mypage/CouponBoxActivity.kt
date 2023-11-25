package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sanchaekhasong.databinding.ActivityCouponBoxBinding

class CouponBoxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCouponBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
    }
}