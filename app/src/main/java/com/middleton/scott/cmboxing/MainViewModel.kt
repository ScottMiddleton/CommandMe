package com.middleton.scott.cmboxing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.User
import kotlinx.coroutines.launch

class MainViewModel(val dataRepository: DataRepository): ViewModel() {

    fun getUserLD(): LiveData<User> {
        return dataRepository.getLocalDataSource().getCurrentUserLD()
    }

    fun logout() {
        dataRepository.signOut()
    }

    fun updateUserPurchasedUnlimitedCommands(){
        viewModelScope.launch {
            dataRepository.updateUserPurchaseUnlimitedCommands()
        }
    }
}