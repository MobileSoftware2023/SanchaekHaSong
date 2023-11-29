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
import com.example.sanchaekhasong.databinding.FragmentCouponImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CouponImageFragment : Fragment() {
    private lateinit var couponAdapter: CouponAdapter
    var couponImageList:List<String> = emptyList()
    var couponNameList:List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCouponImageBinding.inflate(layoutInflater,container,false)

        binding.couponRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        couponAdapter = CouponAdapter(couponImageList, couponNameList)
        binding.couponRecyclerview.adapter = couponAdapter
        binding.couponRecyclerview.addItemDecoration(
            GridSpacingForRecyclerView(
                spanCount = 2, spacing = dpToPx(30, context)
            )
        )

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                couponImageList = dataSnapshot.child("couponImageList").value as? List<String> ?: emptyList()
                couponNameList = dataSnapshot.child("couponNameList").value as? List<String> ?: emptyList()
                couponAdapter.updateData(couponImageList, couponNameList)
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