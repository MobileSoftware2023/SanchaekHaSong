package com.example.sanchaekhasong.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
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
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class HomeFragment : Fragment() {
    lateinit var binding:FragmentHomeBinding
    private lateinit var countdownTimer: CountDownTimer
    private val TAG = "BasicRecordingApi"
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION = 1
    // 추가: SharedPreferences 사용을 위한 키 정의
    private val lastResetTimestampKey = "lastResetTimestamp"
    private val weekStartTimestampKey = "weekStartTimestamp"
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )
    private var lastResetTimestamp: Long
        get() {
            return PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getLong(lastResetTimestampKey, 0)
        }
        set(value) {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putLong(lastResetTimestampKey, value)
                .apply()
        }

    private var weekStartTimestamp: Long
        get() {
            return PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getLong(weekStartTimestampKey, 0)
        }
        set(value) {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putLong(weekStartTimestampKey, value)
                .apply()
        }

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
        binding= FragmentHomeBinding.inflate(inflater)

        binding.ChallengeTaskLayout.setOnClickListener {
            val dialogFragment = ChallengeTaskFragment()
            dialogFragment.show(parentFragmentManager, "ChallengeTaskFragmentTag")
        }
        binding.DailyTaskLayout.setOnClickListener {
            val dialogFragment = DailyTaskFragment()
            dialogFragment.show(parentFragmentManager, "DailyTaskFragmentTag")
        }

        val countdownTextView: TextView = binding.tvDate

        // 현재 날짜와 랭킹 종료일 계산
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDate = calendar.time
        val endDate = calculateEndDate()

        val timeDifference = endDate.time - currentDate.time

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
        Log.d("lastResetTimestamp", "lastResetTimestamp: $lastResetTimestamp")

        countdownTimer.start()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        val mondayTextView = binding.Monday
        val tuesdayTextView = binding.Tuesday
        val wednesdayTextView = binding.Wednesday
        val thursdayTextView = binding.Thursday
        val fridayTextView = binding.Friday
        val saturdayTextView = binding.Saturday
        val sundayTextView = binding.Sunday

        setDayClickListener(mondayTextView, Calendar.MONDAY, currentDayOfWeek)
        setDayClickListener(tuesdayTextView, Calendar.TUESDAY, currentDayOfWeek)
        setDayClickListener(wednesdayTextView, Calendar.WEDNESDAY, currentDayOfWeek)
        setDayClickListener(thursdayTextView, Calendar.THURSDAY, currentDayOfWeek)
        setDayClickListener(fridayTextView, Calendar.FRIDAY, currentDayOfWeek)
        setDayClickListener(saturdayTextView, Calendar.SATURDAY, currentDayOfWeek)
        setDayClickListener(sundayTextView, Calendar.SUNDAY, currentDayOfWeek)

        if (requiredPermissions.any {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            requestActivityRecognitionAndLocationPermission()
        } else {
            startYourActivity()
        }

    }


    private fun setDayClickListener(textView: TextView, dayOfWeek: Int, currentDayOfWeek: Int) {
        if (dayOfWeek > currentDayOfWeek && currentDayOfWeek != Calendar.SUNDAY) {
            textView.setOnClickListener(null)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_grey))
        }else if (dayOfWeek == Calendar.SUNDAY) {
            textView.setOnClickListener(null)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_grey))
        }else {
            textView.setOnClickListener { onDayClicked(textView) }
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }
    private fun onDayClicked(textView: TextView) {
        val desiredDayOfWeek = when (textView.id) {
            R.id.Monday -> Calendar.MONDAY
            R.id.Tuesday -> Calendar.TUESDAY
            R.id.Wednesday -> Calendar.WEDNESDAY
            R.id.Thursday -> Calendar.THURSDAY
            R.id.Friday -> Calendar.FRIDAY
            R.id.Saturday -> Calendar.SATURDAY
            R.id.Sunday -> Calendar.SUNDAY
            else -> return
        }

        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (currentDayOfWeek  == Calendar.SUNDAY) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }
        calendar.set(Calendar.DAY_OF_WEEK, desiredDayOfWeek)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val dataTypesToRead = listOf(
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_DISTANCE_DELTA,
            DataType.TYPE_CALORIES_EXPENDED
        )

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
        val daysUntilSaturday = (Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilSaturday + 1) // 일요일 자정으로 수정
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
        val days = millis / (1000 * 60 * 60 * 24)
        val hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%02dd %02dh %02dm", days, hours, minutes)
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
                    missionCheck(userInputSteps)
                    binding.StepCount.text = "$userInputSteps 걸음"
                    binding.DistancenCalories.text = "${String.format("%.2f", totalDistance / 1000)} km / ${String.format("%.2f", totalCalories)} kcal"

                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting the step count.", e)
                }
        }
    }

    private fun updateWalkCountforDB() {
        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')

        val userData = database.getReference("$username")
        val college = userData.child("college")

        var collegeName =""
        college.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                collegeName = snapshot.getValue(String::class.java) ?: "단과대"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value.", error.toException())
            }
        })

        val rankingCollegeData =  database.getReference("@ranking_college")
        val collegeData = database.getReference("@college_walkCount")
        val rankingData = database.getReference("@ranking")
        val sumWalkCountReference = userData.child("sumWalkCount")
        val collegeReference = database.getReference("@college")

        //오늘날짜
        val currentDate = Calendar.getInstance().apply {
            timeInMillis =  System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        initializeApp()


        val startTime = weekStartTimestamp

        var calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis
        var currentSumWalkCount  =0L
        var newwalkCount =0L
        var newCollegeData=0L
        var collegePerson =0L
        // 월요일부터 앱을 실행한 날 전날까지의 축적 걸음수 일괄 업데이트
        if (currentDate > lastResetTimestamp) {
            lifecycleScope.launch {
                Toast.makeText(requireContext(), "걸음수를 업데이트 했습니다", Toast.LENGTH_SHORT).show()

                val (newStepCount, _, _) = readHistoryData(startTime, endTime, listOf(DataType.TYPE_STEP_COUNT_DELTA))

                sumWalkCountReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        currentSumWalkCount = snapshot.getValue(Long::class.java) ?: 0
                        sumWalkCountReference.setValue(newStepCount)
                        newwalkCount = newStepCount.toLong()
                        Log.d("walkCount", "walkCount: $newwalkCount")
                        Log.d("currentSumWalkCount", "currentSumWalkCount: $currentSumWalkCount")

                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read value.", error.toException())
                    }
                })
                // ranking 업데이트
                rankingData.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val rankingDataValue= snapshot.value as? HashMap<String, Any>
                        if (rankingDataValue != null) {
                            val rankingDataValue = rankingDataValue["$username"] as? Long ?: 0
                            val updateMap = HashMap<String, Any>()
                            updateMap["$username"] = rankingDataValue - currentSumWalkCount + newwalkCount
                            rankingData.updateChildren(updateMap)
                                .addOnSuccessListener {
                                    Log.d("cyabcdefg", "Ranking data updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("cyabcdefg", "Failed to update ranking data.", e)
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read value.", error.toException())
                    }
                })
                //college_walkCount업데이트
                collegeData.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val collegeRankingData =snapshot.value as? HashMap<String, Any>
                        if (collegeRankingData != null) {
                            val collegeRankingData = collegeRankingData["$collegeName"] as? Long ?: 0
                            val updateMap = HashMap<String, Any>()
                            newCollegeData = collegeRankingData - currentSumWalkCount + newwalkCount
                            updateMap["$collegeName"]= newCollegeData
                            collegeData.updateChildren(updateMap)
                                .addOnSuccessListener {
                                    Log.d("cyabcdefg", "college Ranking data updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("cyabcdefg", "Failed to update college ranking data.", e)
                                }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read value.", error.toException())
                    }
                })
                collegeReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val collegePersonValue = snapshot.value as? HashMap<String, Any>
                        if (collegePersonValue != null) {
                            collegePerson =
                                collegePersonValue["$collegeName"] as? Long ?: 0

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read value.", error.toException())
                    }
                })
                //단과대 랭킹을 위한 평균걸음수 업데이트
                rankingCollegeData.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val rankingCollegeValue =snapshot.value as? HashMap<String, Any>
                        if (rankingCollegeValue != null) {
                            val rankingCollegeValue = rankingCollegeValue["$collegeName"] as? Long ?: 0
                            val updateMap = HashMap<String, Any>()
                            val averageCollegeWalk= newCollegeData/collegePerson
                            updateMap["$collegeName"]=averageCollegeWalk
                            rankingCollegeData.updateChildren(updateMap)
                                .addOnSuccessListener {
                                    Log.d("cyabcdefg", "college Ranking data updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("cyabcdefg", "Failed to update college ranking data.", e)
                                }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read value.", error.toException())
                    }
                })


                //마지막 걸음수 갱신 시간 업데이트
                lastResetTimestamp = System.currentTimeMillis()

            }
        }
    }
    fun initializeApp() {
        calculateWeekStartTimestamp()
        if (lastResetTimestamp == 0L) {
            lastResetTimestamp = weekStartTimestamp
        }
    }

    private fun calculateWeekStartTimestamp() {
        val calendar = Calendar.getInstance()

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (dayOfWeek == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -6)
        } else {
            val daysUntilMonday = (dayOfWeek + 5) % 7
            calendar.add(Calendar.DAY_OF_YEAR, -daysUntilMonday)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        weekStartTimestamp = calendar.timeInMillis
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION -> {
                    startYourActivity()
                }
                else -> {
                    Toast.makeText(this.requireContext(), "google fit wasn't return result", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this.requireContext(), "permission failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun requestActivityRecognitionAndLocationPermission() {
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            requiredPermissions,
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION
        )
        startYourActivity()
    }

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
                    Toast.makeText(this.requireContext(), "권한 재요청 성공.", Toast.LENGTH_SHORT).show()
                } else {
                    //권한이 거부되었을 경우 처리합니다.
                    Toast.makeText(this.requireContext(), "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startYourActivity() {
        // 여기에 권한이 부여된 후에 수행하고자 하는 작업을 추가합니다.
        val account = GoogleSignIn.getAccountForExtension(this.requireContext(), fitnessOptions)
        Toast.makeText(this.requireContext(), "google fit과 연동중입니다.", Toast.LENGTH_SHORT).show()
        Log.i("account","계정은 ${GoogleSignIn.hasPermissions(account, fitnessOptions)}")
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION,
                account,
                fitnessOptions)
        } else {
            Toast.makeText(this.requireContext(), "google fit과 연동 성공하였습니다", Toast.LENGTH_SHORT).show()
            subscribe()
            updateWalkCountforDB()
            // 요일에 따라 백그라운드 설정
            // 오늘의 요일을 얻어옴
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            when (today) {
                Calendar.MONDAY -> onDayClicked(binding.Monday)
                Calendar.TUESDAY -> onDayClicked(binding.Tuesday)
                Calendar.WEDNESDAY -> onDayClicked(binding.Wednesday)
                Calendar.THURSDAY -> onDayClicked(binding.Thursday)
                Calendar.FRIDAY -> onDayClicked(binding.Friday)
                Calendar.SATURDAY -> onDayClicked(binding.Saturday)
                Calendar.SUNDAY -> onDayClicked(binding.Sunday)
            }
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
                        Log.w("HistoryData", "There was a problem getting the data.", e)
                        continuation.resume(Triple(0, 0f, 0f))
                    }
            } ?: continuation.resume(Triple(0, 0f, 0f))
        }
    }

    private fun processResponseData(response: DataReadResponse, dataTypes: List<DataType>): Triple<Int, Float, Float> {
        var stepCount = 0
        var distance = 0f
        var calories = 0f

        val buckets = response.buckets

        for (bucket in buckets) {
            for (dataType in dataTypes) {
                val dataSet = bucket.getDataSet(dataType)

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
        val allDayIds = arrayOf(
            binding.Monday.id, binding.Tuesday.id, binding.Wednesday.id,
            binding.Thursday.id, binding.Friday.id, binding.Saturday.id, binding.Sunday.id
        )

        for (dayId in allDayIds) {
            val dayTextView = binding.root.findViewById<TextView>(dayId)
            dayTextView.setBackgroundResource(if (dayId == id) R.drawable.day_select else android.R.color.transparent)
        }
    }

    private fun missionCheck(StepCount: Int) {
        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val dailyQuestData = database.getReference("$username").child("dailyQuest").child("isCompleted")

        dailyQuestData.addListenerForSingleValueEvent(object : ValueEventListener  {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isCompletedData = dataSnapshot.value as? MutableList<Boolean>

                if (StepCount >= 6000 && !isCompletedData?.get(0)!!) {
                    isCompletedData?.set(0, true)
                    dailyQuestData.setValue(isCompletedData)

                    challengeDataListener(0)
                }

                if (StepCount >= 8000 && !isCompletedData?.get(1)!!) {
                    isCompletedData?.set(1, true)
                    dailyQuestData.setValue(isCompletedData)

                    challengeDataListener(1)
                }


                if (StepCount >= 10000 && !isCompletedData?.get(2)!!) {
                    isCompletedData?.set(2, true)
                    dailyQuestData.setValue(isCompletedData)

                    challengeDataListener(2)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value.", error.toException())
            }
        })
    }
    private fun challengeDataListener (num: Int) {
        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val challengeData = database.getReference("$username").child("challenge").child("progress")
        challengeData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val progressData = dataSnapshot.value as? HashMap<String, Any>
                if ((progressData?.get("$num") as? Long ?: 0).toInt() < 30) {
                    val progress = progressData?.get("$num") as? Long ?: 0
                    val updateMap = HashMap<String, Any>()
                    updateMap["$num"] = progress + 1
                    challengeData.updateChildren(updateMap)
                        .addOnSuccessListener {
                            Log.d("cyabcdefg", "progressData[$num]updated successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("cyabcdefg", "Failed to update progressData[$num]", e)
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value.", error.toException())
            }
        })

    }

}
