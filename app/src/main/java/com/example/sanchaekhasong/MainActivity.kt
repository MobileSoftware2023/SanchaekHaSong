package com.example.sanchaekhasong

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private val TAG = "BasicRecordingApi"

    private val REQUEST_OAUTH_REQUEST_CODE = 1

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_DISTANCE_DELTA)
        .build()

    private val fragmentManager = supportFragmentManager
    val yourFragment = fragmentManager.findFragmentById(R.id.fragment_home) as HomeFragment?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val requestLauncherForMyPage : ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            //프로필 이미지 가져오기
        }


        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions)
        } else {
            subscribe()
        }

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
            requestLauncherForMyPage.launch(intent)
        }

    }
    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containers, fragment)
            .commit()
    }
    /** Records step data by requesting a subscription to background step data. */
    private fun subscribe() {
        Fitness.getRecordingClient(this,GoogleSignIn.getAccountForExtension(this, fitnessOptions))
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
        GoogleSignIn.getLastSignedInAccount(this)?.let {
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
                    val homeFragmentBinding = yourFragment?.let { FragmentHomeBinding.bind(it.requireView()) }
                    if (homeFragmentBinding != null) {
                        homeFragmentBinding.StepCount.text = "$userInputSteps 걸음"
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting the step count.", e)
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe()
            }
        }
    }
}