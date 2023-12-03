package com.example.sanchaekhasong.store

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sanchaekhasong.GridSpacingForRecyclerView
import com.example.sanchaekhasong.databinding.FragmentBuyCouponBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BuyCouponFragment : Fragment() {

    private lateinit var buyCouponAdapter: BuyCouponAdapter
    var boughtCouponList : MutableList<String> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentBuyCouponBinding.inflate(layoutInflater, container, false)

        val imgDatas = mutableListOf<String>("compose_coffee", "compose_green", "gongcha_milktea",
            "gongcha_strawberry", "mega_coffee")
        val nameDatas = mutableListOf<String>("컴포즈커피 아메리카노", "컴포즈커피 그린티라떼", "공차 블랙 밀크티",
            "공차 딸기 쥬얼리 밀크티","메가커피 아메리카노" )
        val priceDatas = mutableListOf<Int>(3000, 6500, 8000, 9000, 3000)

        binding.buyCouponRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        buyCouponAdapter = BuyCouponAdapter(imgDatas, nameDatas, priceDatas, boughtCouponList)
        binding.buyCouponRecyclerview.adapter = buyCouponAdapter
        binding.buyCouponRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 2, spacing = dpToPx(30, context)))

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                boughtCouponList = dataSnapshot.child("boughtCouponImageList").value as? MutableList<String> ?: mutableListOf()
                buyCouponAdapter.updateData(boughtCouponList)
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