package com.middleton.scott.cmboxing.datasource

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import com.middleton.scott.cmboxing.ui.login.CreateAccountScreenViewModel

class DataRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    fun getLocalDataSource(): LocalDataSource {
        return localDataSource
    }

    fun createUserAccount(
        user: CreateAccountScreenViewModel.User,
        responseLD: MutableLiveData<ResponseData>
    ) {
        remoteDataSource.createUserAccount(
            user.email,
            user.password,
            object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
                override fun onSuccess(model: Boolean) {
                    responseLD.postValue(ResponseData(success = true))
                    val authUser = Firebase.auth.currentUser
                    addUser(authUser?.email, user.first, user.last, authUser?.uid)
                }

                override fun onError(error: Int?) {
                    localDataSource.userIsLoggedIn = false
                    responseLD.postValue(ResponseData(errorResource = error))
                }
            }

        )
    }

    private fun addUser(email: String?, first: String?, last: String?, uid: String?){
        remoteDataSource.addUser(email, first, last, uid, object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
            override fun onSuccess(model: Boolean) {

            }

            override fun onError(error: Int?) {

            }
        })
    }
}