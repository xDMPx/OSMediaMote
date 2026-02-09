package com.xdmpx.osmediamote.utils

object Utils {
    fun secsToHMS(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds - h * 3600) / 60
        val s = seconds - h * 3600 - m * 60

        return String.format(null, "%02d:%02d:%02d", h, m, s)
    }
}