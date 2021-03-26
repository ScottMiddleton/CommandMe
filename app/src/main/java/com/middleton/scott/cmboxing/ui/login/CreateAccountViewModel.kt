package com.middleton.scott.cmboxing.ui.login

import androidx.core.util.PatternsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.User
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import com.middleton.scott.cmboxing.other.Constants.MIN_PASSWORD_LENGTH
import kotlinx.coroutines.launch

class CreateAccountViewModel(private val dataRepository: DataRepository) : ViewModel() {

    var user = UserVMModel("", "", "", "", false)

    val createAccountResponseLD = MutableLiveData<ResponseData>()
    val addUserResponseLD = MutableLiveData<ResponseData>()

    val passwordValidLD = MutableLiveData<Boolean>()
    val emailValidLD = MutableLiveData<Boolean>()

    var createAccountAttempted = false

    var credentialsValid = false

    fun createUserAccount() {
        if (credentialsValid) {
            dataRepository.createUserFirebaseAuthAccount(user, createAccountResponseLD)
        } else {
            createAccountAttempted = true
            validateCredentials()
        }
    }

    fun validateCredentials() {
        val emailValid = PatternsCompat.EMAIL_ADDRESS.matcher(user.email).matches()
        val passwordValid = user.password.length >= MIN_PASSWORD_LENGTH

        credentialsValid = emailValid && passwordValid

        if (createAccountAttempted) {
            emailValidLD.value = emailValid
            passwordValidLD.value = passwordValid
        }
    }

    fun insertCurrentUser(firstName: String, lastName: String, email: String) {
        dataRepository.userHasPurchasedUnlimitedCommands {
            val hasPurchasedUnlimitedCommands = it
            val user = User(email, firstName, lastName, hasPurchasedUnlimitedCommands)
            viewModelScope.launch {
                dataRepository.insertCurrentUser(user)
            }
            dataRepository.addUserToFirestore(user)
        }
    }

    data class UserVMModel(
        var email: String,
        var first: String,
        var last: String,
        var password: String,
        var hasPurchasedUnlimitedCommands: Boolean
    )

}