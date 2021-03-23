package com.middleton.scott.cmboxing.ui.login

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.User
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import kotlinx.coroutines.launch

class LoginViewModel(private val dataRepository: DataRepository) : ViewModel() {

    var loginAttempted = false

    var email: String = ""
    var password: String = ""

    val signInResponseLD = MutableLiveData<ResponseData>()
    val addUserResponseLD = MutableLiveData<ResponseData>()

    val passwordValidLD = MutableLiveData<Boolean>()
    val emailValidLD = MutableLiveData<Boolean>()

    fun login() {
        loginAttempted = true
        validate()
        if (emailValidLD.value == true && passwordValidLD.value == true) {
            viewModelScope.launch {
                dataRepository.signIn(email, password, signInResponseLD)
            }
        }
    }

    fun validate() {
        if (loginAttempted) {
            emailValidLD.value =
                (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches())

            passwordValidLD.value = password.count() >= 7
        }
    }

    fun addUser(firstName: String, lastName: String, email: String) {
        dataRepository.userHasPurchasedUnlimitedCommands {
            val hasPurchasedUnlimitedCommands = it
            viewModelScope.launch {
                dataRepository.addUser(
                    User(
                        email,
                        firstName,
                        lastName,
                        hasPurchasedUnlimitedCommands
                    ), addUserResponseLD
                )
            }
        }

    }
}