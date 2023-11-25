package com.example.sanchaekhasong.store

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.BuyDialogBinding
import com.example.sanchaekhasong.databinding.BuyProfileRecyclerviewBinding

class BuyProfileViewHolder(val binding : BuyProfileRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class BuyProfileImageAdapter (val imgDatas : MutableList<String>, val nameDatas : MutableList<String>, val priceDatas : MutableList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = imgDatas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = BuyProfileViewHolder(
        BuyProfileRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as BuyProfileViewHolder).binding
        //imgDatas에서 가져오기
        binding.couponImage.setImageResource(R.drawable.snow)
        binding.couponImage.setTag(position)
        binding.couponName.text = nameDatas[position]
        binding.couponPrice.text = priceDatas[position].toString() + " C"

        binding.couponImage.setOnClickListener {
            //쿠폰 구매 dialog
            val position = binding.couponImage.getTag() as Int
            val dbinding : BuyDialogBinding = BuyDialogBinding.inflate(LayoutInflater.from(holder.itemView.context))
            dbinding.buySuccessText.visibility = View.INVISIBLE

            val alertDialog = AlertDialog.Builder(holder.itemView.context, R.style.RoundedCornersAlertDialog).run{
                setView(dbinding.root)
                dbinding.buyText.text = nameDatas[position] + " 을/를 구매할까요?"
                val dialog = show()
                val window = dialog.window
                val width = holder.itemView.context.resources.displayMetrics.widthPixels *.85
                val height = holder.itemView.context.resources.displayMetrics.heightPixels * 0.25
                window?.setLayout(width.toInt(), height.toInt())
                dialog
            }
            dbinding.confirmButton.setOnClickListener {
                if(dbinding.cancelButton.visibility == View.VISIBLE){
                    //로컬 db에서 가져온 현재 포인트가 가격보다 많으면 구매 성공
                    dbinding.cancelButton.visibility = View.INVISIBLE
                    //구매 성공조건 변경
                    if(1<0){
                        dbinding.buyText.text = "구매 완료."
                        dbinding.buySuccessText.text = "프로필 사진 설정 화면에서 찾을 수 있어요."
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