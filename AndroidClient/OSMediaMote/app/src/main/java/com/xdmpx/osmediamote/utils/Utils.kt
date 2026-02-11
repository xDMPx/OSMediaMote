package com.xdmpx.osmediamote.utils

import android.view.Window
import android.view.WindowManager

object Utils {
    fun secsToHMS(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds - h * 3600) / 60
        val s = seconds - h * 3600 - m * 60

        return String.format(null, "%02d:%02d:%02d", h, m, s)
    }

    fun setKeepScreenOnFlag(window: Window, keepScreenOn: Boolean) {
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}