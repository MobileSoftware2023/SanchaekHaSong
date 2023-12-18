package com.example.sanchaekhasong.mypage

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.sanchaekhasong.MainActivity
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ActivityDeleteAccountBinding
import com.example.sanchaekhasong.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteAccountActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding : ActivityDeleteAccountBinding
    lateinit var passwordText : String
    lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        binding.emailText.text = auth.currentUser?.email.toString()
        binding.passwordText.addTextChangedListener(watcher)
        username = binding.emailText.text.toString().substringBeforeLast('@')

        binding.deleteBtn.setOnClickListener {
            reLoginForAccountDeletion()
        }
    }

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            checkFieldsForEmptyValues()
        }
    }

    private fun checkFieldsForEmptyValues() {
        passwordText = binding.passwordText.text.toString()
        binding.deleteBtn.isEnabled =passwordText.isNotEmpty()
        if(binding.deleteBtn.isEnabled){
            binding.deleteBtn.setTextColor(Color.WHITE)
        }
        else{
            binding.deleteBtn.setTextColor(Color.BLACK)
        }
    }

    fun reLoginForAccountDeletion(){
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                binding.passwordText.text.clear()
                if(task.isSuccessful){
                    deleteAccount(username)
                }else {
                    Toast.makeText(baseContext, "비밀번호를 다시한번 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun deleteAccount(username: String){
        auth.currentUser!!.delete().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Toast.makeText(this, "탈퇴 완료", Toast.LENGTH_SHORT).show()
                auth.signOut()
                val database = FirebaseDatabase.getInstance()
                val myData = database.getReference("$username")
                var college : String
                //단과대 학생수 조정
                myData.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        college = dataSnapshot.child("college").value as String
                        val myData1 = database.getReference("@college").child("$college")
                        myData1.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val currentValue = dataSnapshot.value as Long
                                val newValue = currentValue - 1
                                myData1.setValue(newValue)

                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("TAG_DB", "onCancelled", databaseError.toException())
                            }
                        })
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })
                //사용자 정보 제거
                myData.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent : Intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        Log.d("Firebase", "경로 삭제 성공")
                    } else {
                        Log.e("Firebase", "경로 삭제 실패", task.exception)
                    }
                }
                val myData2 = database.getReference("@ranking").child("$username")
                myData2.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "경로 삭제 성공")
                    } else {
                        Log.e("Firebase", "경로 삭제 실패", task.exception)
                    }
                }
            }
            else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}