package com.qwwuyu.file.utils.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class SimpleAdapter<T> @JvmOverloads constructor(
    protected val itemLayoutId: Int,
    private val list: MutableList<T> = mutableListOf(),
    protected val adapterListener: AdapterListener<T>? = null
) : RecyclerView.Adapter<SimpleViewHolder>() {

    protected lateinit var inflater: LayoutInflater

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        inflater = LayoutInflater.from(recyclerView.context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(inflater.inflate(itemLayoutId, parent, false))
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        val t = list[position]
        onBind(position, holder, t)
        if (adapterListener != null) {
            holder.itemView.setOnClickListener { v -> adapterListener.onItemClick(position, v, t) }
        }
    }

    abstract fun onBind(position: Int, holder: SimpleViewHolder, data: T)

    fun setData(data: List<T>?) {
        list.clear()
        if (data != null) list.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: List<T>) {
        list.addAll(data)
        notifyDataSetChanged()
    }

    fun dataSize(): Int {
        return list.size
    }

    fun moveItem(formPosition: Int, toPosition: Int) {
        val diff = if (formPosition < toPosition) 1 else -1
        var index = formPosition
        while (index != toPosition) {
            Collections.swap(list, index, index + diff)
            index += diff
        }
        notifyItemMoved(formPosition, toPosition)
    }

    fun addData(t: T) {
        list.add(t)
        notifyDataSetChanged()
    }

    fun addData(t: T, position: Int) {
        list.add(position, t)
        notifyDataSetChanged()
    }

    fun addDataWithAnim(t: T, position: Int) {
        list.add(position, t)
        notifyItemInserted(position)
    }

    fun removeData(t: T) {
        list.remove(t)
        notifyDataSetChanged()
    }

    fun removeData(position: Int) {
        list.removeAt(position)
        notifyDataSetChanged()
    }

    fun removeDataWithAnim(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    interface AdapterListener<T> {
        fun onItemClick(position: Int, v: View, data: T)
    }
}