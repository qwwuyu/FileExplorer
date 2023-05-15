package com.qwwuyu.file.utils.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by qiwei on 2018/8/3 21:00.
 * Description RecyclerView间隔线.
 */
class VerticalItemDecoration(
    private val spaceHeight: Int,
    private var color: Int = 0,
    private var showEnd: Boolean = false,
    private var marginLeft: Int = 0,
    private var marginRight: Int = 0
) : RecyclerView.ItemDecoration() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (Color.alpha(color) == 0) return
        val adapter = parent.adapter ?: return
        val itemCount = adapter.itemCount
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)!!
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val itemPosition = layoutParams.viewLayoutPosition
            if (showEnd || itemPosition != itemCount - 1) {
                val left: Int = child.left + marginLeft
                val right: Int = child.right - marginRight
                val top: Int = child.bottom
                val bottom: Int = top + spaceHeight
                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (showEnd || itemPosition != parent.adapter!!.itemCount - 1) {
            outRect.bottom = spaceHeight
        }
    }
}