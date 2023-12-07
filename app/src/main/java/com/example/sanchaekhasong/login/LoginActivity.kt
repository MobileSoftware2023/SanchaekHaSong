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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth
    lateinit var username: String

    override fun onStart() {
        super.onStart()
        if(auth.currentUser!= null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

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
            if(email != null  && password != null && profileImage != null && college != null) {
                createAccount(email, password, profileImage, college)
            }

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

        binding.createDBBtn.setOnClickListener {
            setData("${ binding.emailText.text}", "${binding.collegeText.text}", "${binding.profileImageText.text}")
        }
    }

    private fun createAccount(email: String, password: String, profileImage:String, college : String) {
        if(isSchoolDomain(email)){
            auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener{ sendTask ->
                                if(sendTask.isSuccessful){
                                    setData(email, college, profileImage)
                                    Toast.makeText(this, "회원가입 성공, 전송된 메일을 확인해 주세요", Toast.LENGTH_SHORT).show()
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

    private fun setData(email: String, college: String, profileImage: String){
        //username -> 프로필사진, 단과대, 포인트, 보유프로필사진(기본 + 구매), 구매한프로필사진, 구매한쿠폰, 걸음수총합
        //@college -> 단과대 학생수 + 1
        username = email.substringBeforeLast('@')
        val database = FirebaseDatabase.getInstance()
        val myData = database.getReference("$username")
        myData.child("profileImage").setValue("$profileImage")
        myData.child("college").setValue("$college")
        myData.child("point").setValue(0)
        val initProfileImage = listOf<String>("snow1", "snow2", "snow3", "snow4", "snow5")
        myData.child("profileImageList").setValue(initProfileImage)
        myData.child("boughtCouponImageList").setValue(true)
        myData.child("boughtCouponNameList").setValue(true)
        myData.child("boughtProfileImageList").setValue(true)
        myData.child("sumWalkCount").setValue(0)
        val challengeMissionList = listOf<String>(
            "6,000 걸음 이상 걷기를 100회 달성", "8,000 걸음 이상 걷기를 100회 달성",
            "10,000 걸음 이상 걷기를 100회 달성", "ㅇㅇ 루트 걷기를 100회 달성",
            "ㅁㅁ 루트 걷기를 100회 달성", "ㄹㄹ 루트 걷기를 100회 달성"
        )
        myData.child("challenge").child("mission").setValue(challengeMissionList)
        val challengePoint = listOf<Int>(10000, 15000, 20000, 10000, 10000, 10000)
        myData.child("challenge").child("point").setValue(challengePoint)
        val progressList = listOf<Int>(0, 0, 0, 0, 0, 0)
        myData.child("challenge").child("progress").setValue(progressList)
        myData.child("challenge").child("isCompleted").setValue(true)
        val dailyQuestList = listOf<String>("6,000 걸음 이상 걸어요.", "8,000 걸음 이상 걸어요.",
            "10,000 걸음 이상 걸어요.", "ㅇㅇ 루트를 1회 걸어요.",
            "ㅁㅁ 루트를 1회 걸어요.", "ㄹㄹ 루트를 1회 걸어요."
        )
        myData.child("dailyQuest").child("mission").setValue(dailyQuestList)
        val dailyPoint = listOf<Int>(100, 200, 300, 100, 100, 100)
        myData.child("dailyQuest").child("point").setValue(dailyPoint)
        myData.child("dailyQuest").child("isCompleted").setValue(true)


        //단과대 학생수 등록
        val myData1 = database.getReference("@college").child("$college")
        myData1.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentValue = dataSnapshot.value as Long
                val newValue = currentValue + 1
                myData1.setValue(newValue)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 처리 중 오류 발생 시 처리
                Log.e("TAG_DB", "onCancelled", databaseError.toException())
            }
        })
        //개인별 걸음수 data만 따로 등록 : @ranking - username : sumWalkCount
        val myData2 = database.getReference("@ranking").child("$username")
        myData2.setValue(0)

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