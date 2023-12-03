package com.example.sanchaekhasong

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.databinding.ActivityMainBinding
import com.example.sanchaekhasong.databinding.FragmentHomeBinding
import com.example.sanchaekhasong.main.HomeFragment
import com.example.sanchaekhasong.mypage.MyPageActivity
import com.example.sanchaekhasong.ranking.RankingFragment
import com.example.sanchaekhasong.store.StoreFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(),OnDataChangeListener {
    var initTime = 0L
    lateinit var binding : ActivityMainBinding
    private val TAG = "BasicRecordingApi"

    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION = 1

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val auth = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()
        val username = auth?.email?.substringBeforeLast('@')
        val myData = database.getReference("$username")
        myData.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profileImage = dataSnapshot.child("profileImage").value.toString()
                val resId = resources.getIdentifier(profileImage, "drawable", packageName)
                binding.profileImage.setImageResource(resId)
                binding.username.text = username

                val point = dataSnapshot.child("point").value.toString()
                binding.point.text = point + " C"

            }

            override fun onCancelled(error: DatabaseError) {
                val code = error.code
                val message = error.message
                Log.e("TAG_DB", "onCancelled by $code : $message")
            }

        })
        //Home?Main?Fragment완성시 setFragment를 Home으로 변경
        setFragment(HomeFragment())

        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> setFragment(HomeFragment())
                R.id.rank -> setFragment(RankingFragment())
                //R.id.map-> setFragment(MapFragment())
                R.id.store-> setFragment(StoreFragment())
            }
            true
        }

        binding.mypageButton.setOnClickListener {
            val intent: Intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
        // 1. 권한이 부여되지 않았을 경우
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
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

    override fun onBackPressed() {
        if (System.currentTimeMillis() - initTime > 2000) {
            Toast.makeText(this, "종료하려면 한 번 더 누르세요.", Toast.LENGTH_SHORT).show()
            initTime = System.currentTimeMillis()
        } else {
            super.onBackPressed();
            finish()
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containers, fragment)
            .commit()
    }

    /** Records step data by requesting a subscription to background step data. */
    private fun subscribe() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getRecordingClient(this,account)
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
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
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        account?.let {
            Fitness.getHistoryClient(this, it)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { dataSet ->
                    var userInputSteps = 0
                    for (dp in dataSet.dataPoints) {
                        for (field in dp.dataType.fields) {
                            Log.d("Stream Name : ", dp.originalDataSource.streamName)
                            if ("user_input" != dp.originalDataSource.streamName) {
                                val steps = dp.getValue(field).asInt()
                                userInputSteps += steps
                            }
                        }
                    }
                    onDataChanged("$userInputSteps 걸음")
                    Toast.makeText(this, "ondatachanged 호출 완료", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "google fit wasn't return result", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "permission failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 2. 권한을 요청하는 함수
    private fun requestActivityRecognitionAndLocationPermission() {
        // 권한을 요청합니다.
        ActivityCompat.requestPermissions(
            this,
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
                    Toast.makeText(this, "권한 재요청 성공.", Toast.LENGTH_SHORT).show()
                    startYourActivity()
                } else {
                    //권한이 거부되었을 경우 처리합니다.
                    Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            // 필요한 경우 다른 권한 요청도 처리합니다.
        }
    }

    // 4. 권한이 부여된 후에 실행하려는 로직을 담은 함수
    private fun startYourActivity() {
        // 여기에 권한이 부여된 후에 수행하고자 하는 작업을 추가합니다.
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Toast.makeText(this, "google fit과 연동중입니다.", Toast.LENGTH_SHORT).show()
        Log.i("account","계정은 ${GoogleSignIn.hasPermissions(account, fitnessOptions)}")
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Toast.makeText(this, "구글인증이 실패.", Toast.LENGTH_SHORT).show()
            GoogleSignIn.requestPermissions(
                this,
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION_AND_LOCATION,
                account,
                fitnessOptions)
        } else {
            Toast.makeText(this, "google fit과 연동 성공하였습니다", Toast.LENGTH_SHORT).show()
            subscribe()
        }
    }
    override fun onDataChanged(newData: String) {
        // 여기서 HomeFragment의 TextView에 접근하여 값을 변경
        val fragmentTag = HomeFragment::class.java.simpleName
        val homeFragment = supportFragmentManager.findFragmentByTag(fragmentTag) as HomeFragment?
        Toast.makeText(this, "$homeFragment", Toast.LENGTH_SHORT).show()
        homeFragment?.updateTextView(newData)
    }
}