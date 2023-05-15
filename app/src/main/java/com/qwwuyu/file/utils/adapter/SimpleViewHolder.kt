package com.qwwuyu.file.utils.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.recyclerview.widget.RecyclerView

/**
 * 简单实现的ViewHolder.
 */
class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val views: SparseArray<View> = SparseArray()

    fun <V : View> getView(@IdRes id: Int): V {
        var childView = views.get(id)
        if (childView == null) {
            childView = itemView.findViewById(id)
            views.put(id, childView)
        }
        return childView as V
    }

    fun setText(@IdRes viewId: Int, value: CharSequence?): SimpleViewHolder {
        getView<TextView>(viewId).text = value
        return this
    }

    fun setText(@IdRes viewId: Int, @StringRes strId: Int): SimpleViewHolder? {
        getView<TextView>(viewId).setText(strId)
        return this
    }

    fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): SimpleViewHolder {
        getView<TextView>(viewId).setTextColor(color)
        return this
    }

    fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): SimpleViewHolder {
        getView<TextView>(viewId).setTextColor(itemView.resources.getColor(colorRes))
        return this
    }

    fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): SimpleViewHolder {
        getView<ImageView>(viewId).setImageResource(imageResId)
        return this
    }

    fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): SimpleViewHolder {
        getView<ImageView>(viewId).setImageDrawable(drawable)
        return this
    }

    fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): SimpleViewHolder {
        getView<ImageView>(viewId).setImageBitmap(bitmap)
        return this
    }

    fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): SimpleViewHolder {
        getView<View>(viewId).setBackgroundColor(color)
        return this
    }

    fun setBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): SimpleViewHolder {
        getView<View>(viewId).setBackgroundResource(backgroundRes)
        return this
    }

    fun setVisible(@IdRes viewId: Int, isVisible: Boolean): SimpleViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        return this
    }

    fun setGone(@IdRes viewId: Int, isGone: Boolean): SimpleViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (isGone) View.GONE else View.VISIBLE
        return this
    }

    fun setEnabled(@IdRes viewId: Int, isEnabled: Boolean): SimpleViewHolder {
        getView<View>(viewId).isEnabled = isEnabled
        return this
    }
}