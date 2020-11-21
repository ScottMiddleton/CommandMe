package com.middleton.scott.cmboxing.utils

fun getStringFromSeconds(secs: Int): String {
        val hr: Int =  secs / 3600
        val rem: Int = secs % 3600
        val mn = rem / 60
        val sec = rem % 60
        val hrStr = (if (hr < 10) "0" else "") + hr
        val mnStr = (if (mn < 10) "0" else "") + mn
        val secStr = (if (sec < 10) "0" else "") + sec

        return "$mnStr : $secStr"
    }