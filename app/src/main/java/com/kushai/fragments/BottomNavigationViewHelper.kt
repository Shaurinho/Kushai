package com.kushai.fragments

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


object BottomNavigationViewHelper {
    @SuppressLint("RestrictedApi")
    fun disableShiftMode(view: BottomNavigationView) {
        view.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShifting(false)
                item.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData!!.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            Log.e("BNVHelper", "Unable to get shift mode field", e)
        } catch (e: IllegalAccessException) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e)
        }
    }
}