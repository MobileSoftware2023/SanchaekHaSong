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
import com.example.sanchaekhasong.databinding.FragmentBuyProfileImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BuyProfileImageFragment : Fragment() {
    private lateinit var buyProfileImageAdapter: BuyProfileImageAdapter
    var boughtProfileList : MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentBuyProfileImageBinding.inflate(layoutInflater, container, false)

        val imageDatas = mutableListOf<String>("snow_korean", "snow_history", "snow_french", "snow_chinese", "snow_german", "snow_japanese",
            "snow_library", "snow_culture", "snow_lecordonbleu", "snow_edu", "snow_chemistry", "snow_biology", "snow_math", "snow_statistic",
            "snow_pe", "snow_dance", "snow_chemical_biology", "snow_ai", "snow_electrical", "snow_material", "snow_cs", "snow_ds",
            "snow_mechanical", "snow_basic_engineering", "snow_family", "snow_child", "snow_clothing", "snow_food", "snow_political",
            "snow_public_admin", "snow_advertising", "snow_consumer_eco", "snow_psy", "snow_law", "snow_eco", "snow_business", "snow_piano", "snow_orchestral", "snow_vocal", "snow_composition",
            "snow_pharmacy", "snow_visual_media", "snow_industrial_design", "snow_environ_design", "snow_craft", "snow_painting",
            "snow_global_coop", "snow_entrepreneurship", "snow_eng", "snow_tesl", "snow_media")
        val nameDatas = mutableListOf<String>("한국어문학부", "역사문화학과", "프랑스언어·문화학과", "중어중문학부", "독일언어·문화학과", "일본학과",
            "문헌정보학과", "문화관광학전공", "르꼬르동블루\n외식경영전공", "교육학부", "화학과", "생명시스템학부", "수학과", "통계학과", "체육교육과", "무용과",
            "화공생명공학부", "인공지능학부", "지능형전자시스템전공", "신소재물리전공", "컴퓨터과학과", "데이터사이언스전공", "기계시스템학부", "기초공학부",
            "가족자원경영학과", "아동복지학부", "의류학과", "식품영양학과", "정치외교학과", "행정학과", "홍보광고학과", "소비자경제학과", "사회심리학과",
            "볍학부", "경제학부", "경영학부", "피아노과", "관현악과", "성악과", "작곡과", "약학부", "시각·영상디자인과", "산업디자인과", "환경디자인과",
            "공예과", "회화과", "글로벌협력전공", "앙트러프러너십전공", "영어영문학과전공", "테슬(TESL)전공", "미디어학부")

        binding.buyProfileRecyclerview.layoutManager = GridLayoutManager(activity, 2)
        buyProfileImageAdapter = BuyProfileImageAdapter(imageDatas, nameDatas, boughtProfileList)
        binding.buyProfileRecyclerview.adapter = buyProfileImageAdapter
        binding.buyProfileRecyclerview.addItemDecoration(GridSpacingForRecyclerView(spanCount = 2, spacing = dpToPx(30, context)))

        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val myData = database.getReference("$username")

        myData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                boughtProfileList = dataSnapshot.child("boughtProfileImageList").value as? MutableList<String> ?: mutableListOf()
                buyProfileImageAdapter.updateData(boughtProfileList)
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