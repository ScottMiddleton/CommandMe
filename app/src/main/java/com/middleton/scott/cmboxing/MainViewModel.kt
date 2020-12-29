package com.middleton.scott.cmboxing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.User

class MainViewModel(val dataRepository: DataRepository): ViewModel() {

    fun getUserLD(): LiveData<User> {
        return dataRepository.getLocalDataSource().getCurrentUser()
    }

    fun logout() {
        dataRepository.signOut()
    }
}