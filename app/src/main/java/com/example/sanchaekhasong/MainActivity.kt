package com.example.sanchaekhasong

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.databinding.ActivityMainBinding
import com.example.sanchaekhasong.databinding.FragmentHomeBinding
import com.example.sanchaekhasong.main.HomeFragment
import com.example.sanchaekhasong.mypage.MyPageActivity
import com.example.sanchaekhasong.ranking.RankingFragment
import com.example.sanchaekhasong.route.RouteFragment
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
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    var initTime = 0L
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        askNotificationPermission()

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
        // FCM 토큰 얻기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // 얻은 토큰을 서버로 전송하거나 다른 작업 수행
                // 예: 서버 API 호출 등
                println("FCM 토큰: $token")
            } else {
                // 토큰 얻기 실패
                println("FCM 토큰 얻기 실패")
            }
        }

        // FCM 토큰 갱신 이벤트 리스너 등록
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val refreshedToken = task.result
                // 토큰 갱신 처리
                println("갱신된 FCM 토큰: $refreshedToken")
            } else {
                // 토큰 갱신 실패
                println("FCM 토큰 갱신 실패")
            }
        }

        setFragment(HomeFragment())



        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> setFragment(HomeFragment())
                R.id.rank -> setFragment(RankingFragment())
                R.id.map-> setFragment(RouteFragment())
                R.id.store-> setFragment(StoreFragment())
            }
            true
        }

        binding.mypageButton.setOnClickListener {
            val intent: Intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
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


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


}