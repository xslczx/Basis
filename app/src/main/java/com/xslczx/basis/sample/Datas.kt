package com.xslczx.basis.sample

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.palette.graphics.Palette
import com.squareup.picasso.Picasso
import java.util.concurrent.CompletableFuture
import kotlin.math.floor

data class Music(
    val url: String,
    val title: String,
    val author: String,
    val pic: String,
    val sort: String,
    var dominantSwatch: Palette.Swatch? = null
) {

    fun preloadColor() {
        CompletableFuture.supplyAsync({
            Picasso.get().load(pic).get()
        }, AppExecutors.io).thenApply {
            Palette.from(it).generate()
        }.whenCompleteAsync({ palette, u ->
            this.dominantSwatch = palette.dominantSwatch
        }, AppExecutors.main)
    }

    fun applyColor(activity: Activity?) {
        dominantSwatch?.let {
            applyColor(activity, dominantSwatch)
        } ?: let {
            CompletableFuture.supplyAsync({
                Picasso.get().load(pic).get()
            }, AppExecutors.io).thenApply {
                Palette.from(it).generate()
            }.whenCompleteAsync({ palette, u ->
                this.dominantSwatch = palette.dominantSwatch
                applyColor(activity, palette.dominantSwatch)
            }, AppExecutors.main)
        }
    }

    private fun applyColor(activity: Activity?, dominantSwatch: Palette.Swatch?) {
        val mActivity = activity as? AppCompatActivity ?: return
        dominantSwatch?.let {
            val colorDark = colorBurn(dominantSwatch.rgb)
            activity.window.statusBarColor = colorDark
            activity.window.navigationBarColor = colorDark
            val toolbar = activity.findViewById<View>(R.id.toolbar) as? Toolbar
            toolbar?.apply {
                toolbar.setBackgroundColor(colorDark)
                toolbar.setTitleTextColor(dominantSwatch.titleTextColor)
                ToolbarColorizeHelper.colorizeToolbar(toolbar, dominantSwatch.titleTextColor, activity)
            }
        } ?: let {
            val colorDark = Color.BLACK
            val titleTextColor = Color.WHITE
            activity.window.statusBarColor = colorDark
            activity.window.navigationBarColor = colorDark
            val toolbar = activity.findViewById<View>(R.id.toolbar) as? Toolbar
            toolbar?.apply {
                toolbar.setBackgroundColor(colorDark)
                toolbar.setTitleTextColor(titleTextColor)
                ToolbarColorizeHelper.colorizeToolbar(toolbar, titleTextColor, activity)
            }
        }
    }

    private fun colorBurn(RGBValues: Int): Int {
        val alpha = RGBValues shr 24
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = floor(red * (1 - 0.1)).toInt()
        green = floor(green * (1 - 0.1)).toInt()
        blue = floor(blue * (1 - 0.1)).toInt()
        return Color.rgb(red, green, blue)
    }
}