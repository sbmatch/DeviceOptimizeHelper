package com.sbmatch.deviceopt.utils;

import android.app.ActivityThread
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

object VectorDrawableUtils {

    private fun getContext(): Context{
        return ActivityThread.currentApplication()
    }

    @JvmStatic
    fun getDrawable( drawableResId: Int): Drawable? {
        return VectorDrawableCompat.create(getContext().resources, drawableResId, getContext().theme)
    }

    fun getDrawable(drawableResId: Int, colorFilter: Int): Drawable {
        val drawable = getDrawable(drawableResId)
        drawable!!.colorFilter = PorterDuffColorFilter(colorFilter, PorterDuff.Mode.SRC_IN)
        return drawable
    }

    fun getBitmap(drawableId: Int): Bitmap {
        val drawable = getDrawable(drawableId)

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}