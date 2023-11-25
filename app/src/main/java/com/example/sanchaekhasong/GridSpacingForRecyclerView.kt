package com.example.sanchaekhasong

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class GridSpacingForRecyclerView(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position: Int = parent.getChildAdapterPosition(view)
        val column = position % spanCount + 1

        if (position < spanCount){
            outRect.top = spacing/2
        }

        if (column == spanCount){
            outRect.right = spacing
        }
        outRect.left = spacing
        outRect.bottom = spacing
    }
}
