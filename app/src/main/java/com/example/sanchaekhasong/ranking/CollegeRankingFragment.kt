package com.example.sanchaekhasong.ranking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentCollegeRankingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CollegeRankingFragment : Fragment() {
    lateinit var rankingList : ArrayList<Pair<String, Long>>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCollegeRankingBinding.inflate(layoutInflater,container, false)

        val database = FirebaseDatabase.getInstance()
        val myData = database.getReference("@ranking_college")
        myData.orderByValue().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val orderedRankingList = ArrayList<Pair<String, Long>>()
                for (snapshot in dataSnapshot.children) {
                    val collegeName = snapshot.key
                    val walkCount = snapshot.value as Long
                    orderedRankingList.add(Pair(collegeName!!, walkCount))
                }
                rankingList = orderedRankingList.toList().reversed() as ArrayList<Pair<String, Long>>
                binding.college1.text = rankingList[0].first
                binding.walkCount1.text = rankingList[0].second.toString() + " 걸음"

                binding.college2.text = rankingList[1].first
                binding.walkCount2.text = rankingList[1].second.toString()+ " 걸음"

                binding.college3.text = rankingList[2].first
                binding.walkCount3.text = rankingList[2].second.toString()+ " 걸음"

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG_DB", "onCancelled", databaseError.toException())
            }
        })
        return binding.root
    }


}