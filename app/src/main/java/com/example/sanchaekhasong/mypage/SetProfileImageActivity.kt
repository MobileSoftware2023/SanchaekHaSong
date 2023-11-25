package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sanchaekhasong.databinding.ActivitySetProfileImageBinding

class SetProfileImageActivity : AppCompatActivity() {
    lateinit var binding : ActivitySetProfileImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetProfileImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
    }
}