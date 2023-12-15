package com.example.sanchaekhasong.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentDailyTaskBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DailyTaskFragment : DialogFragment() {
    private lateinit var dailytaskAdapter: DailytaskAdapter
    var missionDatas: MutableList<String> = mutableListOf()
    var pointDatas: MutableList<Int> = mutableListOf()
    var completedList : MutableList<Boolean> = mutableListOf()
    var clickedList : MutableList<Boolean> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDailyTaskBinding.inflate(layoutInflater,container,false)

        binding.recyclerDailyTasks.layoutManager = LinearLayoutManager(activity)
        dailytaskAdapter= DailytaskAdapter(missionDatas,pointDatas,completedList, clickedList)
        binding.recyclerDailyTasks.adapter = dailytaskAdapter

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username").child("dailyQuest")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                missionDatas = dataSnapshot.child("mission").value as? MutableList<String> ?: mutableListOf()
                pointDatas = dataSnapshot.child("point").value as? MutableList<Int> ?: mutableListOf()
                completedList = dataSnapshot.child("isCompleted").value as? MutableList<Boolean> ?: mutableListOf()
                clickedList = dataSnapshot.child("isCompletedClicked").value as? MutableList<Boolean> ?: mutableListOf()
                dailytaskAdapter.updateData(missionDatas, pointDatas, completedList, clickedList)
            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }
        })
        // btn_daily_close 버튼 클릭 이벤트 설정
        binding.btnCloseDaily.setOnClickListener {
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