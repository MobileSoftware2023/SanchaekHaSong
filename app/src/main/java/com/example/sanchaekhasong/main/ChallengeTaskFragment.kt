package com.example.sanchaekhasong.main

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentChallengeTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChallengeTaskFragment : DialogFragment() {

    private lateinit var challengeTaskAdapter: ChallengeTaskAdapter
    var missionDatas: MutableList<String> = mutableListOf()
    var pointDatas: MutableList<Int> = mutableListOf()
    var progressDatas : MutableList<Int> = mutableListOf()
    var completedList : MutableList<Boolean> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChallengeTaskBinding.inflate(layoutInflater,container,false)

        binding.recyclerChallengeTasks.layoutManager = LinearLayoutManager(activity)
        challengeTaskAdapter = ChallengeTaskAdapter(missionDatas, pointDatas, progressDatas, completedList)
        binding.recyclerChallengeTasks.adapter = challengeTaskAdapter

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username").child("challenge")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                missionDatas = dataSnapshot.child("mission").value as? MutableList<String> ?: mutableListOf()
                pointDatas = dataSnapshot.child("point").value as? MutableList<Int> ?: mutableListOf()
                progressDatas = dataSnapshot.child("progress").value as? MutableList<Int> ?: mutableListOf()
                completedList = dataSnapshot.child("isCompleted").value as? MutableList<Boolean> ?: mutableListOf()
                challengeTaskAdapter.updateData(missionDatas, pointDatas, progressDatas, completedList)
            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }
        })


        // btn_challenge_close 버튼 클릭 이벤트 설정
        binding.btnCloseChallenge.setOnClickListener {
            // 다이얼로그 프래그먼트를 닫음
            dismiss()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // full Screen code
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt() // 90% of the screen width
        val roundedCornerDrawable= resources.getDrawable(R.drawable.task_dialog_border, null)
        dialog?.window?.setBackgroundDrawable(roundedCornerDrawable)
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
    }
}