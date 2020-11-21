package com.middleton.scott.cmboxing.utils

import androidx.lifecycle.MutableLiveData


class CustomMutableLiveData<T : MutableLiveData<T>?> : MutableLiveData<T?>() {

    fun <T> MutableLiveData<T>.forceRefresh() {
        this.value = this.value
    }
}