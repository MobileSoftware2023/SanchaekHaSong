package com.example.sanchaekhasong.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ItemDailytaskBinding

class DailytaskViewHolder(val binding: ItemDailytaskBinding) : RecyclerView.ViewHolder(binding.root)

class DailytaskAdapter(
    private val missionDatas: MutableList<String>,
    private val pointDatas: MutableList<Int>,
    private val completed: MutableList<Boolean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //매개변수 자리에 넣기 (val DailyList: Array<DailyTask>)
    override fun getItemCount(): Int = missionDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = DailytaskViewHolder(
        ItemDailytaskBinding.inflate(
        LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as DailytaskViewHolder).binding
        //미션명 설정
        binding.dailyTask.text = missionDatas[position].toString()
        //미션에 대한 코인 설정
        val point=pointDatas[position]
        binding.dailyCoinCount.text = "$point C"
        //미션 달성 유무
        val isCompleted= completed[position]
        // 미션 달성 여부에 따라 TextView 스타일 변경
        if (isCompleted) {
            // 미션 달성 시 노란색으로 설정 (원하는 색상으로 변경)
            holder.binding.missionCheck.setBackgroundResource(R.drawable.completed_rectangle)

            // 클릭 리스너 설정
            holder.binding.missionCheck.setOnClickListener {
                // TODO: 미션 달성 시 동작할 코드 작성
                // 코인 획득 등의 처리를 수행 : 데이터베이스에 코인 추가 하고 동기화 시킨다.

                // 다시 기본 색상으로 초기화
                holder.binding.missionCheck.setBackgroundResource(R.drawable.dialog_border)

            }
        } else {
            // 클릭 리스너를 null로 설정하여 클릭 무시
            holder.binding.missionCheck.setOnClickListener(null)
        }
    }
}