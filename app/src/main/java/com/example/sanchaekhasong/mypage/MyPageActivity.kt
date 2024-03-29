package com.example.sanchaekhasong.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sanchaekhasong.MainActivity
import com.example.sanchaekhasong.databinding.ActivityMyPageBinding
import com.example.sanchaekhasong.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyPageActivity : AppCompatActivity() {
    lateinit var binding : ActivityMyPageBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val username = auth.currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profileImage = dataSnapshot.child("profileImage").value.toString()
                val resId = resources.getIdentifier(profileImage, "drawable", packageName)
                binding.profileImage.setImageResource(resId)
                binding.username.text = username
                val college = dataSnapshot.child("college").value.toString()
                binding.college.text = college
            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }
        })

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.editButton.setOnClickListener {
            val intent : Intent = Intent(this, SetProfileImageActivity::class.java)
            startActivity(intent)
        }

        binding.couponBoxButton.setOnClickListener {
            val intent : Intent = Intent(this, CouponBoxActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            logout(username)
        }

        binding.deleteAccountButton.setOnClickListener {
            val intent : Intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }


    }

    fun logout(username : String){
        auth.signOut()
        Toast.makeText(this, "${username} 님 로그아웃되셨습니다.", Toast.LENGTH_SHORT).show()
        val intent : Intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}