package com.example.sanchaekhasong.ranking

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.PersonalRecyclerviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PersonalRankingViewHolder(val binding: PersonalRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class PersonalRankingAdapter(var rankingList: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var context : Context
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }
    override fun getItemCount(): Int = rankingList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = PersonalRankingViewHolder(
        PersonalRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as PersonalRankingViewHolder).binding

        val database = FirebaseDatabase.getInstance()
        val username = rankingList[position]

        val myData = database.getReference(username)
        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentPosition = holder.getAdapterPosition()
                binding.rank.text = "${currentPosition + 1}"
                val profileImage = dataSnapshot.child("profileImage").value.toString()
                val resId = binding.profileImage.context.resources.getIdentifier(
                    profileImage, "drawable", binding.profileImage.context.packageName
                )
                binding.profileImage.setImageResource(resId)
                val college = dataSnapshot.child("college").value.toString()
                binding.college.text = college
                binding.username.text = username
                val walkCount = dataSnapshot.child("sumWalkCount").value.toString()
                binding.walkCount.text = walkCount + " 걸음"
                when(currentPosition){
                    0 -> binding.personalRankBg.backgroundTintList = ContextCompat.getColorStateList(context, R.color.rank1)
                    1 -> binding.personalRankBg.backgroundTintList = ContextCompat.getColorStateList(context, R.color.rank2)
                    2 -> binding.personalRankBg.backgroundTintList = ContextCompat.getColorStateList(context, R.color.rank3)
                    else -> binding.personalRankBg.backgroundTintList = ContextCompat.getColorStateList(context, R.color.rank_other)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }

        })

    }

    fun updateData(newRankingList: List<String>) {
        rankingList = newRankingList
        notifyDataSetChanged()
    }

}
