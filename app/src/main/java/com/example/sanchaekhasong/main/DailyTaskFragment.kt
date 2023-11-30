package com.example.sanchaekhasong.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentDailyTaskBinding

class DailyTaskFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDailyTaskBinding.inflate(layoutInflater,container,false)
        //db에서 가져와서 datats에 저장
        val missionDatas = mutableListOf<String>("6,000 걸음 이상 걸어요.", "8,000 걸음 이상 걸어요.", " OO 루트를 1회 걸어요")
        val pointDatas = mutableListOf<Int>(100, 200, 100)
        val completed = mutableListOf<Boolean>(true, false, false)
        binding.recyclerDailyTasks.layoutManager = LinearLayoutManager(activity)
        binding.recyclerDailyTasks.adapter = DailytaskAdapter(missionDatas,pointDatas,completed)
        //db연결시 대체
        //binding.recyclerChallengeTasks.adapter = ChallengeTaskAdapter(Datas)

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