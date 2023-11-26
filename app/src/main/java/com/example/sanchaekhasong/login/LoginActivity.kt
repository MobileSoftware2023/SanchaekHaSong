package com.example.sanchaekhasong.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sanchaekhasong.MainActivity
import com.example.sanchaekhasong.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth
    lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val requestLauncherForCreateAccount : ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            val email = it.data?.getStringExtra("email")
            val password = it.data?.getStringExtra("password")
            val profileImage = it.data?.getStringExtra("profileImage")
            val college = it.data?.getStringExtra("college")
            if(email != null  && password != null && profileImage != null && college != null)
                createAccount(email, password, profileImage, college)

        }

        binding.createAccountBtn.setOnClickListener {
            val intent: Intent = Intent(this, CreateAccountActivity::class.java)
            requestLauncherForCreateAccount.launch(intent)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            username = email.substringBeforeLast('@')
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    binding.emailText.text.clear()
                    binding.passwordText.text.clear()
                    if(task.isSuccessful){
                        if(auth.currentUser?.isEmailVerified == true){
                            Toast.makeText(baseContext, "로그인 성공. $username 님 환영합니다.", Toast.LENGTH_SHORT).show()
                            val intent : Intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else {
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()

                        }
                    }else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun createAccount(email: String, password: String, profileImage:String, college : String) {
        if(isSchoolDomain(email)){
            auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    binding.emailText.text.clear()
                    binding.passwordText.text.clear()
                    if(task.isSuccessful){
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener{ sendTask ->
                                if(sendTask.isSuccessful){
                                    //프로필사진(png), username, 단과대 db에 저장
                                    username = email.substringBeforeLast('@')
                                    val database = FirebaseDatabase.getInstance()
                                    val myData = database.getReference("$username")
                                    myData.child("profileImage").setValue("$profileImage")
                                    myData.child("college").setValue("$college")
                                    myData.child("point").setValue(0)

                                    Toast.makeText(this, "회원가입 성공, 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT).show()
                                }else {
                                    Toast.makeText(this, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }else {
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        Log.e("srb", " ${task.exception?.message}")
                    }
                }
        }
        else
        {
            Toast.makeText(this, "회원가입 실패, 숙명계정으로 회원가입 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isSchoolDomain(email : String? ): Boolean {
        val allowedDomain = "sookmyung.ac.kr"
        if(email != null){
            val userDomain = extractDomain(email)
            return userDomain == allowedDomain
        }
        return false
    }

    private fun extractDomain(email: String) : String {
        return email.substringAfterLast('@')
    }


}