package com.example.sanchaekhasong

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

class MainActivity : AppCompatActivity() {
    var initTime = 0L
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
            startActivity(intent)
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis() - initTime > 3000){
                Toast.makeText(this, "종료하려면 한 번 더 누르세요.", Toast.LENGTH_SHORT).show()
                initTime = System.currentTimeMillis()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
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