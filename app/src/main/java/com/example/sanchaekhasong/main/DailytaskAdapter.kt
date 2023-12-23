package com.example.sanchaekhasong.main

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ItemDailytaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DailytaskViewHolder(val binding: ItemDailytaskBinding) : RecyclerView.ViewHolder(binding.root)

class DailytaskAdapter(
    private var missionDatas: MutableList<String>,
    private var pointDatas: MutableList<Int>,
    private var completedDatas: MutableList<Boolean>,
    private var clickedDatas: MutableList<Boolean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context : Context
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun getItemCount(): Int = missionDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = DailytaskViewHolder(
        ItemDailytaskBinding.inflate(
        LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as DailytaskViewHolder).binding

        binding.dailyTask.text = missionDatas[position]
        val index = position

        val point=pointDatas[position]
        binding.dailyCoinCount.text = "$point C"

        val isCompleted = completedDatas[position]

        val isClicked = clickedDatas[position]

        if(isClicked){
            holder.binding.missionCheck.setBackgroundResource(R.drawable.task_button_general)
            holder.binding.missionCheck.foreground = ContextCompat.getDrawable(context, R.drawable.task_button_shade)
        }
        else{
            holder.binding.missionCheck.setBackgroundResource(R.drawable.task_button_general)
        }

        if (isCompleted && !isClicked) {
            holder.binding.missionCheck.setBackgroundResource(R.drawable.completed_rectangle)

            holder.binding.missionCheck.setOnClickListener {
                val database = FirebaseDatabase.getInstance()
                val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
                val myData = database.getReference("$username")
                myData.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint += pointDatas[index]
                        myData.child("point").setValue(currentPoint)

                        myData.child("dailyQuest").child("isCompleted").child("$index").setValue(false)
                        myData.child("dailyQuest").child("isCompletedClicked").child("$index").setValue(true)

                        myData.removeEventListener(this)
                        myData.orderByValue().addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                completedDatas =dataSnapshot.child("dailyQuest").child("isCompleted").value as MutableList<Boolean>
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

        } else {
            holder.binding.missionCheck.setOnClickListener(null)
        }

    }

    fun updateData(newMissionList: MutableList<String>, newPointList: MutableList<Int>, newCompletedList: MutableList<Boolean>, newClickedList: MutableList<Boolean>) {
        missionDatas = newMissionList
        pointDatas = newPointList
        completedDatas = newCompletedList
        clickedDatas = newClickedList
        notifyDataSetChanged()
    }
}