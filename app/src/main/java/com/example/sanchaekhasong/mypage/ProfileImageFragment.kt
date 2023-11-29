package com.example.sanchaekhasong.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentProfileImageBinding
import com.google.android.material.internal.ViewUtils.dpToPx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileImageFragment : Fragment() {
    private lateinit var profileAdapter: ProfileAdapter
    var profileImageList: List<String> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileImageBinding.inflate(layoutInflater, container, false)

        binding.profileRecyclerview.layoutManager = GridLayoutManager(activity, 5)
        profileAdapter = ProfileAdapter(profileImageList)
        binding.profileRecyclerview.adapter = profileAdapter

        binding.profileRecyclerview.addItemDecoration(
            GridSpacingForRecyclerView(
                spanCount = 5,
                spacing = dpToPx(15, context)
            )
        )

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username").child("profileImageList")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                profileImageList = dataSnapshot.value as? List<String> ?: emptyList()
                profileAdapter.updateData(profileImageList)
            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }
        })

        return binding.root
    }

    fun dpToPx(dp : Int, context : Context?) : Int{
        val density : Float? = context?.resources?.displayMetrics?.density
        return (dp * density!!).toInt()
    }
}