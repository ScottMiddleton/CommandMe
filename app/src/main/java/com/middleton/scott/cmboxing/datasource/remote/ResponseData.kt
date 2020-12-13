package com.middleton.scott.cmboxing.datasource.remote

import android.content.Context

data class ResponseData(
    val success: Boolean = false,
    val errorResource: Int? = null
) {

    fun getErrorString(context: Context): String? {
        return if (errorResource != null) {
            context.getString(errorResource)
        } else {
            null
        }
    }

}

