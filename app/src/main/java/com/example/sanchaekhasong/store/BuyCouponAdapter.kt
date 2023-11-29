package com.example.sanchaekhasong.store

import android.app.AlertDialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.BuyCouponRecyclerviewBinding
import com.example.sanchaekhasong.databinding.BuyDialogBinding

class BuyCouponViewHolder(val binding: BuyCouponRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class BuyCouponAdapter(val imgDatas : MutableList<String>, val nameDatas : MutableList<String>, val priceDatas : MutableList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = imgDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = BuyCouponViewHolder(
        BuyCouponRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as BuyCouponViewHolder).binding
        binding.couponImage.setImageResource(R.drawable.compose_coffee)
        binding.couponImage.setTag(position)
        binding.couponName.text = nameDatas[position]
        binding.couponPrice.text = priceDatas[position].toString() + " C"

        binding.couponImage.setOnClickListener {
            val position = binding.couponImage.getTag() as Int
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
                    //로컬 db에서 가져온 현재 포인트가 가격보다 많으면 구매 성공
                    dbinding.cancelButton.visibility = View.INVISIBLE
                    //구매 성공조건 변경 필요
                    if(1>0){
                        dbinding.buyText.text = "구매 완료."
                        dbinding.buySuccessText.text = "쿠폰 보관함에서 찾을 수 있어요."
                        dbinding.buySuccessText.visibility = View.VISIBLE
                    }
                    else {
                        dbinding.buyText.text = "코인이 부족합니다."
                    }
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

}
