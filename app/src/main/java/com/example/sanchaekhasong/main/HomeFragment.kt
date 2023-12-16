package com.example.sanchaekhasong.main

import android.Manifest
import android.app.Activity
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sanchaekhasong.OnDataChangeListener
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class HomeFragment : Fragment() {
    lateinit var binding:FragmentHomeBinding
    private lateinit var countdownTimer: CountDownTimer
    private var onDataChangeListener: OnDataChangeListener? = null
    private val TAG = "BasicRecordingApi"
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION = 1


    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentHomeBinding.inflate(inflater)



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
                } else {
                    countdownTextView.text = " 랭킹 종료 순위 집계중..."
                }
            }

            override fun onFinish() {
                countdownTextView.text = "랭킹 종료 순위 집계중..."
            }
        }

        countdownTimer.start()
        return binding.root
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
        mondayTextView.setOnClickListener { onDayClicked(mondayTextView) }
        tuesdayTextView.setOnClickListener { onDayClicked(tuesdayTextView) }
        wednesdayTextView.setOnClickListener { onDayClicked(wednesdayTextView) }
        thursdayTextView.setOnClickListener { onDayClicked(thursdayTextView) }
        fridayTextView.setOnClickListener { onDayClicked(fridayTextView) }
        saturdayTextView.setOnClickListener { onDayClicked(saturdayTextView) }
        sundayTextView.setOnClickListener { onDayClicked(sundayTextView) }


        // ... (다른 요일들의 클릭 이벤트 설정 추가)
        // 1. 권한이 부여되지 않았을 경우
        if (ContextCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 부여되지 않았으므로 권한을 요청합니다.
            requestActivityRecognitionAndLocationPermission()
        } else {
            // 권한이 이미 부여되었으므로 로직을 진행합니다.
            // 예를 들어, 액티비티를 시작하거나 어떤 작업을 수행합니다.
            startYourActivity()
        }
    }

    // 클릭 이벤트 핸들러에서 필요한 데이터를 전달하는 함수
    private fun onDayClicked(textView: TextView) {
        val desiredDayOfWeek = when (textView.id) {
            R.id.Monday -> Calendar.MONDAY
            R.id.Tuesday -> Calendar.TUESDAY
            R.id.Wednesday -> Calendar.WEDNESDAY
            R.id.Thursday -> Calendar.THURSDAY
            R.id.Friday -> Calendar.FRIDAY
            R.id.Saturday -> Calendar.SATURDAY
            R.id.Sunday -> Calendar.SUNDAY
            else -> return // 예상치 못한 경우, 처리 필요
        }

        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = (currentDayOfWeek - desiredDayOfWeek + 7) % 7

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)

        // 선택한 요일의 자정
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // 해당 날의 23시 59분 59초
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val dataTypesToRead = listOf(
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_DISTANCE_DELTA,
            DataType.TYPE_CALORIES_EXPENDED
        )

        // 비동기적으로 데이터 읽기
        lifecycleScope.launch {
            val (StepCount, Distance, Calories) = readHistoryData(startTime, endTime, dataTypesToRead)

            setDayBackgrounds(textView.id)
            // 클릭한 요일에 대한 걸음 수, 거리, 칼로리를 StepCountHistory TextView에 설정
            binding.StepCountHistory.text = "$StepCount 걸음"
            binding.DistancenCaloriesHistory.text = "${String.format("%.2f", Distance / 1000)} km   |   ${String.format("%.2f", Calories)} kcal"
        }
    }
    private fun calculateEndDate(): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        // 현재 날짜에서 토요일까지의 일수 계산
        val daysUntilSaturday = (Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilSaturday + 1) // 일요일 자정으로 수정
        // 토요일 자정 설정
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
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

    /** Records step data by requesting a subscription to background step data. */
    private fun subscribe() {
        val account = GoogleSignIn.getAccountForExtension(this.requireActivity(), fitnessOptions)
        Fitness.getRecordingClient(this.requireActivity(),account)
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed!")
                readData()
            }
            .addOnFailureListener {e ->
                Log.w(TAG, "There was a problem subscribing.", e)
            }
        Fitness.getRecordingClient(this.requireActivity(),account)
            .subscribe(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed!")
                readData()
            }
            .addOnFailureListener {e ->
                Log.w(TAG, "There was a problem subscribing.", e)
            }
        Fitness.getRecordingClient(this.requireActivity(),account)
            .subscribe(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed!")
                readData()
            }
            .addOnFailureListener {e ->
                Log.w(TAG, "There was a problem subscribing.", e)
            }
    }
    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readData() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val account = GoogleSignIn.getAccountForExtension(this.requireActivity(), fitnessOptions)
        account?.let {
            Fitness.getHistoryClient(this.requireActivity(), it)
                .readData(
                    DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .read(DataType.TYPE_DISTANCE_DELTA)
                        .read(DataType.TYPE_CALORIES_EXPENDED)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build()
                )
                .addOnSuccessListener { response ->
                    var userInputSteps = 0
                    var totalDistance = 0f
                    var totalCalories = 0f

                    for (dataSet in response.dataSets) {
                        for (dp in dataSet.dataPoints) {
                            for (field in dp.dataType.fields) {
                                when (dp.dataType.name) {
                                    DataType.TYPE_STEP_COUNT_DELTA.name -> {
                                        if ("user_input" != dp.originalDataSource.streamName) {
                                            val steps = dp.getValue(field).asInt()
                                            userInputSteps += steps
                                        }
                                    }
                                    DataType.TYPE_DISTANCE_DELTA.name -> {
                                        val distance = dp.getValue(field).asFloat()
                                        totalDistance += distance
                                    }
                                    DataType.TYPE_CALORIES_EXPENDED.name -> {
                                        val calories = dp.getValue(field).asFloat()
                                        totalCalories += calories
                                    }
                                }
                            }
                        }
                    }

                    binding.StepCount.text = "$userInputSteps 걸음"
                    binding.DistancenCalories.text = "${String.format("%.2f", totalDistance / 1000)} km / ${String.format("%.2f", totalCalories)} kcal"
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting the step count.", e)
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION -> subscribe()
                else -> {
                    Toast.makeText(this.requireContext(), "google fit wasn't return result", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this.requireContext(), "permission failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 2. 권한을 요청하는 함수
    private fun requestActivityRecognitionAndLocationPermission() {
        // 권한을 요청합니다.
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION
        )
    }

    // 3. 권한 요청 결과를 처리하는 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("requestCode" ,"$requestCode")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION -> {
                // 권한 요청 결과를 확인합니다.
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // 권한이 부여되었으므로 로직을 진행합니다.
                    // 예를 들어, 액티비티를 시작하거나 어떤 작업을 수행합니다.
                    Toast.makeText(this.requireContext(), "권한 재요청 성공.", Toast.LENGTH_SHORT).show()
                    startYourActivity()
                } else {
                    //권한이 거부되었을 경우 처리합니다.
                    Toast.makeText(this.requireContext(), "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            // 필요한 경우 다른 권한 요청도 처리합니다.
        }
    }

    // 4. 권한이 부여된 후에 실행하려는 로직을 담은 함수
    private fun startYourActivity() {
        // 여기에 권한이 부여된 후에 수행하고자 하는 작업을 추가합니다.
        val account = GoogleSignIn.getAccountForExtension(this.requireContext(), fitnessOptions)
        Toast.makeText(this.requireContext(), "google fit과 연동중입니다.", Toast.LENGTH_SHORT).show()
        Log.i("account","계정은 ${GoogleSignIn.hasPermissions(account, fitnessOptions)}")
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Toast.makeText(this.requireContext(), "구글인증이 실패.", Toast.LENGTH_SHORT).show()
            GoogleSignIn.requestPermissions(
                this,
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION,
                account,
                fitnessOptions)
        } else {
            Toast.makeText(this.requireContext(), "google fit과 연동 성공하였습니다", Toast.LENGTH_SHORT).show()
            subscribe()
        }
    }
    private suspend fun readHistoryData(startTime: Long, endTime : Long, dataTypes: List<DataType>): Triple<Int, Float, Float> {
        return suspendCoroutine { continuation ->
            val account = GoogleSignIn.getAccountForExtension(this.requireActivity(), fitnessOptions)
            account?.let {
                val requestBuilder = DataReadRequest.Builder()
                    .aggregate(DataType.AGGREGATE_DISTANCE_DELTA)
                    .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                    .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)

                Fitness.getHistoryClient(this.requireActivity(), it)
                    .readData(requestBuilder.build())
                    .addOnSuccessListener { response ->
                        val result = processResponseData(response, dataTypes)
                        continuation.resume(result)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "There was a problem getting the data.", e)
                        continuation.resume(Triple(0, 0f, 0f))
                    }
            } ?: continuation.resume(Triple(0, 0f, 0f))
        }
    }

    private fun processResponseData(response: DataReadResponse, dataTypes: List<DataType>): Triple<Int, Float, Float> {
        // 여기에 데이터를 가공하는 로직을 추가
        var stepCount = 0
        var distance = 0f
        var calories = 0f

        // DataReadResponse에서 버킷 가져오기
        val buckets = response.buckets

        for (bucket in buckets) {
            // 각 버킷의 데이터 세트 얻기
            for (dataType in dataTypes) {
                val dataSet = bucket.getDataSet(dataType)

                // 데이터 세트에서 데이터 포인트 가져오기
                if (dataSet != null) {
                    for (dp in dataSet.dataPoints) {
                        for (field in dp.dataType.fields) {
                            when (dp.dataType.name) {
                                DataType.AGGREGATE_STEP_COUNT_DELTA.name -> {
                                    if ("user_input" != dp.originalDataSource.streamName) {
                                        val steps = dp.getValue(field).asInt()
                                        stepCount += steps
                                    }
                                }

                                DataType.AGGREGATE_DISTANCE_DELTA.name -> {
                                    val distanceValue = dp.getValue(field).asFloat()
                                    distance += distanceValue
                                }

                                DataType.AGGREGATE_CALORIES_EXPENDED.name -> {
                                    val caloriesValue = dp.getValue(field).asFloat()
                                    calories += caloriesValue
                                }
                            }
                        }
                    }
                }
            }
        }
        return Triple(stepCount, distance, calories)
    }
    private fun setDayBackgrounds(id: Int) {
        // 모든 요일의 아이디를 배열로 정의
        val allDayIds = arrayOf(
            binding.Monday.id, binding.Tuesday.id, binding.Wednesday.id,
            binding.Thursday.id, binding.Friday.id, binding.Saturday.id, binding.Sunday.id
        )

        // 선택된 요일을 제외하고 나머지 요일의 배경을 초기 상태로 되돌림
        for (dayId in allDayIds) {
            val dayTextView = binding.root.findViewById<TextView>(dayId)
            dayTextView.setBackgroundResource(if (dayId == id) R.drawable.day_select else android.R.color.transparent)
        }
    }

}
