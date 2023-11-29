package com.example.sanchaekhasong.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ActivityCreateAccountBinding

class CreateAccountActivity : AppCompatActivity() {
    lateinit var binding : ActivityCreateAccountBinding
    lateinit var emailText : String
    lateinit var passwordText : String
    lateinit var selectedCollege : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.radioGroup.setOnCheckedChangeListener{ group, checkedID ->
            when (checkedID) {
                binding.snow.id -> {
                    binding.profileImage.setImageResource(R.drawable.snow1)
                    binding.profileImage.setTag("snow")
                }
                binding.snow1.id -> {
                    binding.profileImage.setImageResource(R.drawable.snow2)
                    binding.profileImage.setTag("snow1")
                }
                binding.snow2.id -> {
                    binding.profileImage.setImageResource(R.drawable.snow3)
                    binding.profileImage.setTag("snow2")
                }
                binding.snow3.id -> {
                    binding.profileImage.setImageResource(R.drawable.snow4)
                    binding.profileImage.setTag("snow3")
                }
                else -> {
                    binding.profileImage.setImageResource(R.drawable.snow5)
                    binding.profileImage.setTag("snow4")
                }
            }
        }

        binding.emailText.addTextChangedListener(watcher)
        binding.passwordText.addTextChangedListener(watcher)

        val collegeItems = arrayOf("선택", "문과대학", "이과대학", "공과대학", "생활과학대학",
                "사회과학대학", "법과대학", "경상대학", "음악대학", "약학대학", "미술대학",
            "기초교양대학", "글로벌융합대학", "글로벌서비스학부", "영어영문학부", "미디어학부")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, collegeItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.college.adapter = adapter

        binding.college.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCollege = parent?.getItemAtPosition(position) as String
                checkFieldsForEmptyValues()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCollege = "선택"
            }
        }

        binding.backButton.setOnClickListener {
            val intent : Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.joinBtn.setOnClickListener {
            val profileImage = binding.profileImage.getTag() as String
            val intent = intent
            intent.putExtra("email", emailText)
            intent.putExtra("password", passwordText)
            intent.putExtra("profileImage", profileImage)
            intent.putExtra("college", selectedCollege)
            setResult(Activity.RESULT_OK, intent)
            finish()
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

    //시작 버튼 활성화
    private fun checkFieldsForEmptyValues() {
        emailText = binding.emailText.text.toString()
        passwordText = binding.passwordText.text.toString()
        binding.joinBtn.isEnabled = emailText.isNotEmpty() && passwordText.isNotEmpty() && selectedCollege != "선택"
        if(binding.joinBtn.isEnabled){
            binding.joinBtn.setTextColor(Color.WHITE)
            binding.nickNameText.text = emailText.substringBeforeLast('@')
        }
        else{
            binding.joinBtn.setTextColor(Color.BLACK)
        }

    }
}