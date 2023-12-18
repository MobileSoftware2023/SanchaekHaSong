package com.example.sanchaekhasong.route

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.FragmentRouteBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class RouteFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentRouteBinding
    private lateinit var locationSource : FusedLocationSource

    val database = FirebaseDatabase.getInstance()
    val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
    val myData = database.getReference("$username")


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity){
        val fragments:List<Fragment>
        init {
            fragments = listOf(FirstFragment(), SecondFragment(), ThirdFragment())
        }
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                               permissions: Array<String>,
                                               grantResults: IntArray) {
           if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                   grantResults)) {
               if (!locationSource.isActivated) {  // 권한 거부됨
                   naverMap.locationTrackingMode = LocationTrackingMode.None
               }
               return
           }
           super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(
            R.layout.fragment_route, container, false) as ViewGroup

        mapView = rootView.findViewById<View>(R.id.navermap) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.RouteViewPager)
        viewPager.adapter = ViewPagerAdapter(requireActivity())
    }

    override fun onMapReady(naverMap: NaverMap) {
        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(37.546, 126.965), 15.0))  // 카메라 위치 (위도,경도,줌)
            .mapType(NaverMap.MapType.Basic)    //지도 유형
            .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)  //빌딩 표시
        MapFragment.newInstance(options)

        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        val locationButtonView : LocationButtonView? = view?.findViewById<LocationButtonView>(R.id.locationBtn)
        locationButtonView?.setMap(naverMap)

        val start = Marker()
        start.position = LatLng(37.54639, 126.96466)
        start.map = naverMap

        val end = Marker()
        end.position = LatLng(37.544150, 126.966123)
        end.map = naverMap

        val path = PathOverlay()
        path.coords = route1
        path.width = 20
        path.color = Color.parseColor("#008BFF")
        path.outlineWidth = 6
        path.outlineColor = Color.WHITE
        path.map = naverMap

        var _route1inRange = false
        var route1inRange_ = false
        var _route2inRange = false
        var route2inRange_ = false
        var _route3inRange = false
        var route3inRange_ = false


        naverMap.addOnLocationChangeListener { location ->
            val currentLocation = LatLng(location.latitude, location.longitude)
            _route1inRange = isWithinRadius(currentLocation, route1[0], 3000.0)
            route1inRange_ = isWithinRadius(currentLocation, route1.last(), 30.0)
            _route2inRange = isWithinRadius(currentLocation, route2[0], 30.0)
            route2inRange_ = isWithinRadius(currentLocation, route2.last(), 30.0)
            _route3inRange = isWithinRadius(currentLocation, route3[0], 30.0)
            route3inRange_ = isWithinRadius(currentLocation, route3.last(), 30.0)

        }

        val btnStart1 = activity?.findViewById<Button>(R.id.btnStart1)
        btnStart1?.setOnClickListener {
            routeProgress(_route1inRange,route1inRange_,R.drawable.route_one,R.string.route_one)
        }

        //Toast.makeText(activity, "도착!!", Toast.LENGTH_SHORT).show()

        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentFragment = (viewPager.adapter as ViewPagerAdapter).fragments[position]
                when (position){
                    0 ->{
                        start.position = route1[0]
                        end.position = route1.last()
                        path.coords = route1
                        path.map = naverMap
                        val cameraUpdate1 = CameraUpdate.scrollTo(LatLng(37.54519, 126.96532))
                        val cameraUpdate2 = CameraUpdate.zoomTo(16.0)
                        naverMap.moveCamera(cameraUpdate1)
                        naverMap.moveCamera(cameraUpdate2)
                    }
                    1->{
                        start.position = route2[0]
                        end.position = route2.last()
                        path.coords = route2
                        path.map = naverMap
                        val cameraUpdate1 = CameraUpdate.scrollTo(LatLng(37.545985, 126.965217))
                        val cameraUpdate2 = CameraUpdate.zoomTo(16.0)
                        naverMap.moveCamera(cameraUpdate1)
                        naverMap.moveCamera(cameraUpdate2)
                    }
                    2->{
                        start.position = route3[0]
                        end.position = route3.last()
                        path.coords = route3
                        path.map = naverMap
                        val cameraUpdate1 = CameraUpdate.scrollTo(LatLng(37.545304, 126.968325))
                        val cameraUpdate2 = CameraUpdate.zoomTo(14.5)
                        naverMap.moveCamera(cameraUpdate1)
                        naverMap.moveCamera(cameraUpdate2)
                    }
                }
                when (currentFragment){
                    is FirstFragment ->{
                        val btnStart1 = currentFragment.view?.findViewById<Button>(R.id.btnStart1)
                        btnStart1?.setOnClickListener {
                            routeProgress(_route1inRange,route1inRange_,R.drawable.route_one,R.string.route_one)
                        }

                    }
                    is SecondFragment ->{
                        val btnStart2 = currentFragment.view?.findViewById<Button>(R.id.btnStart2)
                        btnStart2?.setOnClickListener {
                            routeProgress(_route2inRange,route2inRange_,R.drawable.route_two,R.string.route_two)
                        }
                    }
                    is ThirdFragment ->{
                        val btnStart3 = currentFragment.view?.findViewById<Button>(R.id.btnStart3)
                        btnStart3?.setOnClickListener {
                            routeProgress(_route3inRange,route3inRange_,R.drawable.route_three,R.string.route_three)
                        }

                    }

                }

            }
        })

        val routeComplete = activity?.findViewById<Button>(R.id.route_complete)
        val bottomSheet = activity?.findViewById<LinearLayout>(R.id.bottom_sheet)
        val progress = activity?.findViewById<LinearLayout>(R.id.route_progress)

        val start1 = activity?.findViewById<View>(R.id._route1)
        val start2 = activity?.findViewById<View>(R.id.route1_)
        val start3 = activity?.findViewById<View>(R.id._route2)
        val start4 = activity?.findViewById<View>(R.id.route2_)
        val start5 = activity?.findViewById<View>(R.id._route3)
        val start6 = activity?.findViewById<View>(R.id.route3_)

        routeComplete?.setOnClickListener {
            /*if (start1?.visibility==View.INVISIBLE&&route1inRange_){
                Toast.makeText(activity, "도착1!!", Toast.LENGTH_SHORT).show()
                start1?.visibility=View.GONE
            }*/
            if (start1?.visibility==View.INVISIBLE&&_route1inRange){
                //Toast.makeText(activity, "도착1!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("도서관-순헌관 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("3").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("3").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("3").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("3").setValue(progress)
                        }


                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            } else if (start2?.visibility==View.INVISIBLE&&_route1inRange){
                //Toast.makeText(activity, "도착2!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("도서관-순헌관 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("3").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("3").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("3").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("3").setValue(progress)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            }else if (start3?.visibility==View.INVISIBLE&&route2inRange_){
                //Toast.makeText(activity, "도착3!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("명재관-프라임관 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        val index = 3
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("4").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("4").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("4").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("4").setValue(progress)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            }else if (start4?.visibility==View.INVISIBLE&&_route2inRange){
                //Toast.makeText(activity, "도착4!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("명재관-프라임관 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("4").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("4").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("4").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("4").setValue(progress)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            }else if (start5?.visibility==View.INVISIBLE&&route3inRange_){
                //Toast.makeText(activity, "도착5!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("숙대입구역-1캠정문 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("5").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("5").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("5").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("5").setValue(progress)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            }else if (start6?.visibility==View.INVISIBLE&&_route3inRange){
                //Toast.makeText(activity, "도착6!!", Toast.LENGTH_SHORT).show()
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("숙대입구역-1캠정문 루트 걷기를 완료했어요!")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }

                myData.orderByValue().addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var currentPoint = dataSnapshot.child("point").value as Long
                        currentPoint+=100
                        myData.child("point").setValue(currentPoint)

                        val isClicked = dataSnapshot.child("dailyQuest").child("isCompletedClicked").child("5").value as Boolean
                        if(!isClicked)
                            myData.child("dailyQuest").child("isCompleted").child("5").setValue(true)

                        var progress = dataSnapshot.child("challenge").child("progress").child("5").value as Long
                        if(progress < 30){
                            progress+=1
                            myData.child("challenge").child("progress").child("5").setValue(progress)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                    }
                })

                start1?.visibility=View.GONE
                bottomSheet?.visibility=View.VISIBLE
                progress?.visibility=View.GONE

            } else {
                Toast.makeText(activity, "도착지점 반경 30m 내에서 완료해주세요", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun isWithinRadius(userLocation: LatLng, targetLocation: LatLng, radius: Double): Boolean {
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(targetLocation.latitude - userLocation.latitude)
        val dLng = Math.toRadians(targetLocation.longitude - userLocation.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLocation.latitude)) * Math.cos(Math.toRadians(targetLocation.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val distance = earthRadius * c

        return distance <= radius
    }

    fun routeProgress(_route : Boolean, route_: Boolean, routeImg : Int, routeName : Int) {
        val start1 = activity?.findViewById<View>(R.id._route1)
        val start2 = activity?.findViewById<View>(R.id.route1_)
        val start3 = activity?.findViewById<View>(R.id._route2)
        val start4 = activity?.findViewById<View>(R.id.route2_)
        val start5 = activity?.findViewById<View>(R.id._route3)
        val start6 = activity?.findViewById<View>(R.id.route3_)

        val bottomSheet = activity?.findViewById<LinearLayout>(R.id.bottom_sheet)
        val progress = activity?.findViewById<LinearLayout>(R.id.route_progress)
        val img = activity?.findViewById<ImageView>(R.id.routeImg)
        val name = activity?.findViewById<TextView>(R.id.routeName)

        if (_route || route_) {
            when (routeName){
                R.string.route_one ->{
                    if (_route==true){
                        start1?.visibility=View.INVISIBLE
                    } else if (route_==true){
                        start2?.visibility=View.INVISIBLE
                    }
                }
                R.string.route_two ->{
                    if (_route==true){
                        start3?.visibility=View.INVISIBLE
                    } else if (route_==true){
                        start4?.visibility=View.INVISIBLE
                    }
                }
                R.string.route_three ->{
                    if (_route==true){
                        start5?.visibility=View.INVISIBLE
                    } else if (route_==true){
                        start6?.visibility=View.INVISIBLE
                    }
                }
            }

            Toast.makeText(activity, "루트 시작 $_route, $route_!", Toast.LENGTH_SHORT).show()
            bottomSheet?.visibility = View.GONE
            img?.setImageResource(routeImg)
            name?.setText(routeName)
            progress?.visibility = View.VISIBLE
            val routeCancel = activity?.findViewById<Button>(R.id.route_cancel)
            routeCancel?.setOnClickListener{
                val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                if (builder != null) {
                    builder.setMessage("루트 진행을 중단하시겠습니까?")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                                bottomSheet?.visibility = View.VISIBLE
                                progress?.visibility = View.GONE
                            })
                        .setNegativeButton("취소",
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                }
                // 다이얼로그를 띄워주기
                if (builder != null) {
                    builder.show()
                }
            }

        }
        else {
            Toast.makeText(activity, "양 끝 지점의 반경 30m 내에서 시작해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    val route1 = listOf(
        LatLng(37.546390, 126.964660),
        LatLng(37.546320, 126.964668),
        LatLng(37.546327, 126.964755),
        LatLng(37.545250, 126.964868),
        LatLng(37.545245, 126.964940),
        LatLng(37.545088, 126.964935),
        LatLng(37.544602, 126.965635),
        LatLng(37.544552, 126.966135),
        LatLng(37.544150, 126.966123)
    )
    val route2 = listOf(
        LatLng(37.546883, 126.965500),
        LatLng(37.546660, 126.965400),
        LatLng(37.546470, 126.965730),
        LatLng(37.546468, 126.965930),
        LatLng(37.546550, 126.966585),
        LatLng(37.546300, 126.966585),
        LatLng(37.546200, 126.966655),
        LatLng(37.545675, 126.966720),
        LatLng(37.545490, 126.966240),
        LatLng(37.545450, 126.965610),
        LatLng(37.545300, 126.965610),
        LatLng(37.545180, 126.965500),
        LatLng(37.545250, 126.964950),
        LatLng(37.544900, 126.964930)
    )
    val route3 = listOf(
        LatLng(37.545498, 126.971750),
        LatLng(37.545220, 126.971750),
        LatLng(37.545150, 126.971620),
        LatLng(37.545000, 126.970400),
        LatLng(37.544950, 126.970350),
        LatLng(37.544790, 126.968280),
        LatLng(37.544790, 126.967900),
        LatLng(37.545090, 126.965900),
        LatLng(37.545290, 126.964900)
    )
}