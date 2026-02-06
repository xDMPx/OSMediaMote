package com.xdmpx.osmediamote.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.xdmpx.osmediamote.OSMediaMote
import com.xdmpx.osmediamote.R

object MediaControlRequester {

    fun pingServer(
        ip: String, context: Context, onResult: (success: Boolean) -> Unit
    ) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/ping"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            onResult(true)
        }, { err ->
            Log.e("VolleyError:", "$url -> $err")
            onResult(false)
        })

        volleyQueue.add(stringRequest)
    }

    fun fetchTitle(ip: String, context: Context, osMediaMoteViewModel: OSMediaMote) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/title"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            if (osMediaMoteViewModel.osMediaMoteState.value.title != response.toString()) {
                osMediaMoteViewModel.incrementArtHash()
                osMediaMoteViewModel.setDrawFallbackIcon(false)
            }
            osMediaMoteViewModel.setTitle(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun fetchDuration(ip: String, context: Context, osMediaMoteViewModel: OSMediaMote) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/duration"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setDuration(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun fetchPosition(ip: String, context: Context, osMediaMoteViewModel: OSMediaMote) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/position"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setPosition(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun fetchIsPlaying(ip: String, context: Context, osMediaMoteViewModel: OSMediaMote) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/is_playing"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setIsPlaying(response.toString() == "true")
            if (osMediaMoteViewModel.osMediaMoteState.value.isPlaying) {
                fetchPosition(ip, context, osMediaMoteViewModel)
            }
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun requestPause(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun requestPlay(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun requestPlayPause(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play_pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun requestPlayNext(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play_next"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    fun requestPlayPrev(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play_prev"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }
}