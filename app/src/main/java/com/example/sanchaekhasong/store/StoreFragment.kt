package com.example.sanchaekhasong.store

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentStoreBinding
import com.example.sanchaekhasong.ranking.CollegeRankingFragment
import com.example.sanchaekhasong.ranking.PersonalRankingFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class StoreFragment : Fragment() {
    lateinit var binding: FragmentStoreBinding

    class StorePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity){
        val fragments:List<Fragment>
        init {
            fragments = listOf(BuyCouponFragment(), BuyProfileImageFragment())
        }
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentStoreBinding.inflate(layoutInflater, container, false)
        val paint = binding.storeText.paint
        val width = paint.measureText(binding.storeText.text.toString())
        binding.storeText.paint.shader = LinearGradient(
            0f, 0f, width, binding.storeText.textSize, intArrayOf(
                Color.parseColor("#667DB6"),
                Color.parseColor("#0082C8"),
            ), null, Shader.TileMode.REPEAT
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewpager2 = view.findViewById<ViewPager2>(R.id.storeViewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.storeTab)

        viewpager2.adapter = StorePagerAdapter(requireActivity())
        val tabTitles = listOf<String>("쿠폰", "프로필 사진")
        TabLayoutMediator(tabLayout, viewpager2) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }


}