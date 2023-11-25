package com.example.sanchaekhasong

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.sanchaekhasong.databinding.ActivityMainBinding
import com.example.sanchaekhasong.mypage.MyPageActivity
import com.example.sanchaekhasong.ranking.RankingFragment
import com.example.sanchaekhasong.store.StoreFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
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

        //Home?Main?Fragment완성시 setFragment를 Home으로 변경
        setFragment(RankingFragment())

        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when(item.itemId) {
                //R.id.home -> setFragment(MainFragment())
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
}