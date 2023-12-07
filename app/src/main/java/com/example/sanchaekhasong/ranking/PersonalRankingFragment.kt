package com.example.sanchaekhasong.ranking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanchaekhasong.databinding.FragmentPersonalRankingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PersonalRankingFragment : Fragment() {
    private lateinit var personalRankingAdapter: PersonalRankingAdapter
    var rankingList : List<String> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPersonalRankingBinding.inflate(layoutInflater,container,false)

        binding.personalRankingRecyclerview.layoutManager = LinearLayoutManager(activity)
        personalRankingAdapter = PersonalRankingAdapter(rankingList)
        binding.personalRankingRecyclerview.adapter = personalRankingAdapter

        val database = FirebaseDatabase.getInstance()
        val myData = database.getReference("@ranking")
        myData.orderByValue().addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val orderedRankingList = ArrayList<String>()
                for (snapshot in dataSnapshot.children) {
                    val username = snapshot.key
                    orderedRankingList.add(username!!)
                }
                //걸음수대로 정렬된 사용자이름
                rankingList = orderedRankingList.toList().reversed()
                personalRankingAdapter.updateData(rankingList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG_DB", "onCancelled", databaseError.toException())
            }
        })
        return binding.root
    }
}