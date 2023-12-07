package com.example.sanchaekhasong.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentChallengeTaskBinding

class ChallengeTaskFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChallengeTaskBinding.inflate(layoutInflater,container,false)
        //db에서 가져와서 datats에 저장
        val missionDatas = mutableListOf<String>("6,000 걸음 이상 걷기를 30회 달성", "8,000 걸음 이상 걷기를 30회 달성", " OO 루트 걷기를 30회 달성")
        val pointDatas = mutableListOf<Int>(1000, 1000, 1000)
        val progressDatas = mutableListOf<Int>(0, 1, 13)
        binding.recyclerChallengeTasks.layoutManager = LinearLayoutManager(activity)
        binding.recyclerChallengeTasks.adapter = ChallengeTaskAdapter(missionDatas, pointDatas,progressDatas)
        //db연결시 교체
        //binding.recyclerChallengeTasks.adapter = ChallengeTaskAdapter(Datas)

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