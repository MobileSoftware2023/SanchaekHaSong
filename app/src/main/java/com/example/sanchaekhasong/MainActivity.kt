package com.example.sanchaekhasong

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.databinding.ActivityMainBinding
import com.example.sanchaekhasong.main.HomeFragment
import com.example.sanchaekhasong.mypage.MyPageActivity
import com.example.sanchaekhasong.ranking.RankingFragment
import com.example.sanchaekhasong.route.RouteFragment
import com.example.sanchaekhasong.store.StoreFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    var initTime = 0L
    lateinit var binding : ActivityMainBinding

    private var routeFragment : RouteFragment? = null
    private var homeFragment : HomeFragment? = null
    private var storeFragment : StoreFragment? = null
    private var rankingFragment : RankingFragment? = null

    @RequiresApi(Build.VERSION_CODES.S)
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

        homeFragment=HomeFragment()
        setFragment(homeFragment!!)

        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> {
                    if (homeFragment==null){
                        homeFragment= HomeFragment()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.containers, homeFragment!!)
                            .commit()                    }
                    if(homeFragment!=null) supportFragmentManager.beginTransaction().show(homeFragment!!).commit()
                    if(routeFragment!=null) supportFragmentManager.beginTransaction().hide(routeFragment!!).commit()
                    if(storeFragment!=null) supportFragmentManager.beginTransaction().hide(storeFragment!!).commit()
                    if(rankingFragment!=null) supportFragmentManager.beginTransaction().hide(rankingFragment!!).commit()

                    return@setOnItemSelectedListener true
                }
                R.id.rank -> {
                    if (rankingFragment==null){
                        rankingFragment= RankingFragment()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.containers, rankingFragment!!)
                            .commit()                    }
                    if(rankingFragment!=null) supportFragmentManager.beginTransaction().show(rankingFragment!!).commit()
                    if(routeFragment!=null) supportFragmentManager.beginTransaction().hide(routeFragment!!).commit()
                    if(homeFragment!=null) supportFragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(storeFragment!=null) supportFragmentManager.beginTransaction().hide(storeFragment!!).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.map-> {
                    if (routeFragment==null){
                        routeFragment= RouteFragment()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.containers, routeFragment!!)
                            .commit()                    }
                    if(routeFragment!=null) supportFragmentManager.beginTransaction().show(routeFragment!!).commit()
                    if(homeFragment!=null) supportFragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(storeFragment!=null) supportFragmentManager.beginTransaction().hide(storeFragment!!).commit()
                    if(rankingFragment!=null) supportFragmentManager.beginTransaction().hide(rankingFragment!!).commit()

                    return@setOnItemSelectedListener true
                }
                R.id.store-> {
                    if (storeFragment==null){
                        storeFragment= StoreFragment()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.containers, storeFragment!!)
                            .commit()                    }
                    if(storeFragment!=null) supportFragmentManager.beginTransaction().show(storeFragment!!).commit()
                    if(routeFragment!=null) supportFragmentManager.beginTransaction().hide(routeFragment!!).commit()
                    if(homeFragment!=null) supportFragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(rankingFragment!=null) supportFragmentManager.beginTransaction().hide(rankingFragment!!).commit()

                    return@setOnItemSelectedListener true
                }
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


}