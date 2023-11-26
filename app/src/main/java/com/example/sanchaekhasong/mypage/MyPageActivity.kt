package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sanchaekhasong.MainActivity
import com.example.sanchaekhasong.databinding.ActivityMyPageBinding
import com.example.sanchaekhasong.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyPageActivity : AppCompatActivity() {
    lateinit var binding : ActivityMyPageBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        val requestLauncherForProfileImage : ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            //프로필 이미지 가져오기
        }

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.editButton.setOnClickListener {
            val intent: Intent = Intent(this, SetProfileImageActivity::class.java)
            requestLauncherForProfileImage.launch(intent)
        }

        binding.couponBoxButton.setOnClickListener {
            val intent : Intent = Intent(this, CouponBoxActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }


    }

    //logout, deleteaccount 완성x
    fun logout(){
        auth.signOut()
        Toast.makeText(this, "${binding.username} 님 로그아웃되셨습니다.", Toast.LENGTH_SHORT).show()
        val intent : Intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun deleteAccount(){
       auth.currentUser!!.delete().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Toast.makeText(this, "탈퇴 완료", Toast.LENGTH_SHORT).show()
                auth.signOut()
                val intent : Intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            else
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()

        }
    }


}