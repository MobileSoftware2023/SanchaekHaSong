package com.example.sanchaekhasong.main

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ItemChallengetaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChallengetaskViewHolder(val binding: ItemChallengetaskBinding) : RecyclerView.ViewHolder(binding.root)

class ChallengeTaskAdapter(
    private var missionDatas: MutableList<String>,
    private var pointDatas: MutableList<Int>,
    private var progressDatas: MutableList<Int>,
    private var completedDatas:MutableList<Boolean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //매개변수 자리에 넣기 (val ChallengeList: Array<ChallengeTask>)
    override fun getItemCount(): Int = missionDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ChallengetaskViewHolder(
        ItemChallengetaskBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ChallengetaskViewHolder).binding

        binding.challengeTask.text=missionDatas[position]
        val index = position

        val point=pointDatas[position]
        binding.challengeCoinCount.text="$point C"

        val progress = progressDatas[position]
        binding.progressBar.progress= progress

        binding.progressCount.text="$progress / 30"

        val isCompleted = completedDatas[position]

        // 미션 달성 여부에 따라 TextView 스타일 변경
        if (progress == 30 && !isCompleted) {
            // 미션 달성 시 노란색으로 설정 (원하는 색상으로 변경)
            holder.binding.missionCheck.setBackgroundResource(R.drawable.completed_rectangle)

            // 클릭 리스너 설정
            holder.binding.missionCheck.setOnClickListener {
                val database = FirebaseDatabase.getInstance()
                val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
                val myData = database.getReference("$username")
                myData.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint += pointDatas[index]
                        myData.child("point").setValue(currentPoint)
                        myData.child("challenge").child("isCompleted").child("$index").setValue(true)
                        myData.removeEventListener(this)
                        myData.orderByValue().addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                completedDatas =dataSnapshot.child("challenge").child("isCompleted").value as MutableList<Boolean>
                                notifyDataSetChanged()
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("TAG_DB", "onCancelled", databaseError.toException())
                            }
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {
                        val code = error.code
                        val message = error.message
                        Log.e("TAG_DB", "onCancelled by $code : $message")
                    }
                })

            }
        } else
        {
            holder.binding.missionCheck.setBackgroundResource(R.drawable.dialog_border)
            // 클릭 리스너를 null로 설정하여 클릭 무시
            holder.binding.missionCheck.setOnClickListener(null)
        }
    }

    fun updateData(newMissionList: MutableList<String>, newPointList: MutableList<Int>, newProgressList: MutableList<Int>, newCompletedList: MutableList<Boolean>) {
        missionDatas = newMissionList
        pointDatas = newPointList
        progressDatas = newProgressList
        completedDatas = newCompletedList
        notifyDataSetChanged()
    }
}