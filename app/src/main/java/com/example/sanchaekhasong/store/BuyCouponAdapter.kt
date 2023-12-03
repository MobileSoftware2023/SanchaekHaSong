package com.example.sanchaekhasong.store

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.BuyCouponRecyclerviewBinding
import com.example.sanchaekhasong.databinding.BuyDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BuyCouponViewHolder(val binding: BuyCouponRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class BuyCouponAdapter(val imgDatas : MutableList<String>, val nameDatas : MutableList<String>, val priceDatas : MutableList<Int>, var boughtList: MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context : Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }
    override fun getItemCount(): Int = imgDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = BuyCouponViewHolder(
        BuyCouponRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as BuyCouponViewHolder).binding

        val fileName = imgDatas[position]

        val resId = binding.couponImage.context.resources.getIdentifier(
            fileName, "drawable", binding.couponImage.context.packageName
        )
        binding.couponImage.setImageResource(resId)
        binding.couponImage.setTag(position)
        if(boughtList.contains(fileName)){
            binding.couponImage.foreground = ContextCompat.getDrawable(context, R.drawable.item_shade)
            binding.couponImage.isEnabled = false
        }
        else{
            binding.couponImage.foreground = ContextCompat.getDrawable(context, R.drawable.image_border_line_transparent)
            binding.couponImage.isEnabled = true
        }

        binding.couponName.text = nameDatas[position]
        binding.couponPrice.text = priceDatas[position].toString() + " C"

        binding.couponImage.setOnClickListener {
            val index = binding.couponImage.getTag() as Int
            val dbinding : BuyDialogBinding = BuyDialogBinding.inflate(LayoutInflater.from(holder.itemView.context))
            dbinding.buySuccessText.visibility = View.INVISIBLE

            val alertDialog = AlertDialog.Builder(holder.itemView.context, R.style.RoundedCornersAlertDialog).run{
                setView(dbinding.root)
                dbinding.buyText.text = nameDatas[position] + " 쿠폰을 구매할까요?"
                val dialog = show()
                val window = dialog.window
                val width = holder.itemView.context.resources.displayMetrics.widthPixels * 0.85
                val height = ViewGroup.LayoutParams.WRAP_CONTENT
                window?.setLayout(width.toInt(), height)
                dialog
            }
            dbinding.confirmButton.setOnClickListener {
                if(dbinding.cancelButton.visibility == View.VISIBLE){
                    dbinding.cancelButton.visibility = View.INVISIBLE
                    val database = FirebaseDatabase.getInstance()
                    val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
                    val myData = database.getReference("$username")
                    myData.orderByValue().addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var currentPoint = dataSnapshot.child("point").value as Long
                            if(currentPoint >= priceDatas[index]){
                                dbinding.buyText.text = "구매 완료."
                                dbinding.buySuccessText.text = "쿠폰 보관함에서 찾을 수 있어요."
                                dbinding.buySuccessText.visibility = View.VISIBLE
                                currentPoint -= priceDatas[index]
                                myData.child("point").setValue(currentPoint)
                                val boughtImageKey = dataSnapshot.child("boughtCouponImageList").childrenCount.toString()
                                myData.child("boughtCouponImageList").child(boughtImageKey).setValue(imgDatas[index])
                                myData.child("boughtCouponNameList").child(boughtImageKey).setValue(nameDatas[index])
                                myData.orderByValue().addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        boughtList =dataSnapshot.child("boughtCouponImageList").value as MutableList<String>
                                        notifyDataSetChanged()
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Log.e("TAG_DB", "onCancelled", databaseError.toException())
                                    }
                                })
                            }
                            else {
                                dbinding.buyText.text = "코인이 부족합니다."
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("TAG_DB", "onCancelled", databaseError.toException())
                        }
                    })
                }
                else  {
                    alertDialog.dismiss()
                }

            }

            dbinding.cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }

        }
    }

    fun updateData(newBoughtCouponList: MutableList<String>) {
        boughtList = newBoughtCouponList
        notifyDataSetChanged()
    }

}
