package com.xslczx.basis.sample

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

object ToolbarColorizeHelper {
    /**
     * Use this method to colorize toolbar icons to the desired target color
     *
     * @param toolbarView       toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity          reference to activity needed to register observers
     */
    fun colorizeToolbar(toolbarView: Toolbar, toolbarIconsColor: Int, activity: Activity) {
        val colorFilter = PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_IN)
        val drawable = ContextCompat.getDrawable(activity, R.drawable.abc_ic_ab_back_material)
        drawable?.colorFilter = colorFilter
        val mActivity = activity as? AppCompatActivity
        mActivity?.supportActionBar?.setHomeAsUpIndicator(drawable)
        for (i in 0 until toolbarView.childCount) {
            val v = toolbarView.getChildAt(i)
            if (v is ImageButton) {
                v.drawable.colorFilter = colorFilter
            }
            if (v is ActionMenuView) {
                for (j in 0 until v.childCount) {
                    val innerView = v.getChildAt(j)
                    if (innerView is ActionMenuItemView) {
                        for (k in innerView.compoundDrawables.indices) {
                            if (innerView.compoundDrawables[k] != null) {
                                innerView.post { innerView.compoundDrawables[k].colorFilter = colorFilter }
                            }
                        }
                    }
                }
            }
        }
        toolbarView.setTitleTextColor(toolbarIconsColor)
        toolbarView.setSubtitleTextColor(toolbarIconsColor)
        setOverflowButtonColor(activity, colorFilter)
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     *
     * @param activity    context object
     * @param colorFilter 颜色过滤器
     */
    private fun setOverflowButtonColor(activity: Activity, colorFilter: PorterDuffColorFilter) {
        val overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description)
        val decorView = activity.window.decorView as ViewGroup
        val viewTreeObserver = decorView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val outViews = ArrayList<View>()
                decorView.findViewsWithText(
                    outViews, overflowDescription,
                    View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
                )
                if (outViews.isEmpty()) {
                    return
                }
                val overflowViewParent = outViews[0].parent as ActionMenuView
                overflowViewParent.overflowIcon!!.colorFilter = colorFilter
                removeOnGlobalLayoutListener(decorView, this)
            }
        })
    }

    private fun removeOnGlobalLayoutListener(v: View, listener: OnGlobalLayoutListener) {
        v.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}