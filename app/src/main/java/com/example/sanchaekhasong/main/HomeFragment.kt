package com.example.sanchaekhasong.main

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var countdownTimer: CountDownTimer
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root



        binding.ChallengeTaskLayout.setOnClickListener {
            // 다이얼로그 프래그먼트를 띄우기
            val dialogFragment = ChallengeTaskFragment()
            dialogFragment.show(parentFragmentManager, "ChallengeTaskFragmentTag")
        }
        binding.DailyTaskLayout.setOnClickListener {
            // 다이얼로그 프래그먼트를 띄우기
            val dialogFragment = DailyTaskFragment()
            dialogFragment.show(parentFragmentManager, "DailyTaskFragmentTag")
        }

        val countdownTextView: TextView = binding.tvDate

        // 현재 날짜와 랭킹 종료일 계산
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDate = calendar.time
        val endDate = calculateEndDate()
        // 시간 차이 계산
        val timeDifference = endDate.time - currentDate.time

        // 카운트다운 타이머 설정
        countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val formattedTime = formatTime(millisUntilFinished)
                if (currentDayOfWeek != Calendar.SUNDAY){
                    countdownTextView.text = " 오늘 ${getCurrentDate()} ,\n 랭킹 종료까지 ${formattedTime}"
                }
            }

            override fun onFinish() {
                countdownTextView.text = "랭킹 종료 순위 집계중..."
            }
        }

        countdownTimer.start()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 각 요일의 TextView 참조
        val mondayTextView = binding.Monday
        val tuesdayTextView = binding.Tuesday
        val wednesdayTextView = binding.Wednesday
        val thursdayTextView = binding.Thursday
        val fridayTextView = binding.Friday
        val saturdayTextView = binding.Saturday
        val sundayTextView = binding.Sunday

        // 각 요일에 대한 클릭 이벤트 설정
        mondayTextView.setOnClickListener { /*onDayClicked(mondayStepCount, mondayDistance, mondayCalories)*/ }
        tuesdayTextView.setOnClickListener { /* Tuesday clicked */ }
        wednesdayTextView.setOnClickListener { /* Wednesday clicked */ }
        thursdayTextView.setOnClickListener { /* Thursday clicked */ }
        fridayTextView.setOnClickListener { /* Friday clicked */ }
        saturdayTextView.setOnClickListener { /* Saturday clicked */ }
        sundayTextView.setOnClickListener { /* Sunday clicked */ }
        // ... (다른 요일들의 클릭 이벤트 설정 추가)
    }

    private fun onDayClicked(stepCount: Int, distance: Double, calories: Double) {
        // 클릭한 요일에 대한 걸음 수, 거리, 칼로리를 StepCountHistory TextView에 설정

    }
    private fun calculateEndDate(): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        // 현재 날짜에서 토요일까지의 일수 계산
        val daysUntilSaturday = (Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilSaturday)
        // 토요일 자정 설정
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time

        return calendar.time
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MM월 dd일")
        return dateFormat.format(Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).time)
    }

    private fun formatTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millis % (1000 * 60)) / 1000
        return String.format("%02d시 %02d분 %02d초", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer.cancel()
    }
}