package com.example.sanchaekhasong.ranking

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentRankingBinding
import com.example.sanchaekhasong.databinding.RankingExplanationDialogBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RankingFragment : Fragment() {

    lateinit var binding : FragmentRankingBinding
    class RankPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity){
        val fragments:List<Fragment>
        init {
            fragments = listOf(PersonalRankingFragment(), CollegeRankingFragment())
        }
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRankingBinding.inflate(layoutInflater, container, false)

        binding.questionButton.setOnClickListener {
            val dialogBinding = RankingExplanationDialogBinding.inflate(layoutInflater)

            val alertDialog = AlertDialog.Builder(requireContext(), R.style.RoundedCornersAlertDialog).run{
                setView(dialogBinding.root)
                val dialog = show()
                val window = dialog.window
                val width = resources.displayMetrics.widthPixels * 0.7
                val height = ViewGroup.LayoutParams.WRAP_CONTENT
                window?.setLayout(width.toInt(), height)
                dialog
            }
            dialogBinding.closeButton.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager2 = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.rankTab)

        viewPager2.adapter = RankPagerAdapter(requireActivity())

        val tabTitles = listOf("개인 랭킹", "단과대 랭킹")
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

    }

}