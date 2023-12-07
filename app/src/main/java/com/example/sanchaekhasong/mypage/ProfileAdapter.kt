package com.example.sanchaekhasong.mypage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sanchaekhasong.R
import com.example.sanchaekhasong.databinding.ProfileRecyclerviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileViewHolder(val binding: ProfileRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class ProfileAdapter(var datas : List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context : Context
    val database = FirebaseDatabase.getInstance()
    val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
    val myData = database.getReference("$username")
    var curProfileImage : String = ""

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ProfileViewHolder(ProfileRecyclerviewBinding.inflate(
        LayoutInflater.from(parent.context),parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ProfileViewHolder).binding

        val fileName = datas[position]
        val resId = binding.profileImagesButton.context.resources.getIdentifier(
            fileName, "drawable", binding.profileImagesButton.context.packageName
        )
        binding.profileImagesButton.setTag(fileName)
        binding.profileImagesButton.setImageResource(resId)

        myData.orderByValue().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                curProfileImage = dataSnapshot.child("profileImage").value as String

                if (fileName == curProfileImage) {
                    binding.profileImagesButton.foreground =
                        ContextCompat.getDrawable(context, R.drawable.item_selected)
                } else {
                    binding.profileImagesButton.foreground =
                        ContextCompat.getDrawable(context, R.drawable.image_border_line_transparent)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG_DB", "onCancelled", databaseError.toException())
            }
        })

        if(fileName == curProfileImage) {
            binding.profileImagesButton.foreground =
                ContextCompat.getDrawable(context, R.drawable.item_selected)
        }
        else {
            binding.profileImagesButton.foreground =
                ContextCompat.getDrawable(context, R.drawable.image_border_line_transparent)
        }


        binding.profileImagesButton.setOnClickListener {
            val thisButtonName = binding.profileImagesButton.getTag() as String
            myData.child("profileImage").setValue("$thisButtonName")
            notifyItemChanged(position)
        }
    }

    fun updateData(newProfileImageList: List<String>) {
        datas = newProfileImageList
        notifyDataSetChanged()
    }
}