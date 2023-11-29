package com.example.sanchaekhasong.main

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ItemChallengetaskBinding
class ChallengetaskViewHolder(val binding: ItemChallengetaskBinding) : RecyclerView.ViewHolder(binding.root)

class ChallengeTaskAdapter(
    private val missionDatas: MutableList<String>,
    private val pointDatas: MutableList<Int>,
    private val progressDatas: MutableList<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //매개변수 자리에 넣기 (val ChallengeList: Array<ChallengeTask>)
    override fun getItemCount(): Int = missionDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ChallengetaskViewHolder(
        ItemChallengetaskBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ChallengetaskViewHolder).binding

        //미션명 설정
        binding.challengeTask.text=missionDatas[position].toString()
        //db연결시 교체
        //binding.challengeTask.text= ChallengeList.get(position).mission.toString() ?: ""
        //미션에 대한 코인 설정
        val point=pointDatas[position]
        binding.challengeCoinCount.text="$point C"
        //프로그래스바 설정
        val progress = progressDatas[position]
        binding.progressBar.progress= progress
        //프로그래스바 퍼센트 설정
        binding.progressCount.text="$progress / 30"
        // 미션 달성 여부에 따라 TextView 스타일 변경
        if (progress == 30) {
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