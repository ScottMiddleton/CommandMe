package com.middleton.scott.cmboxing.datasource

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.User
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import com.middleton.scott.cmboxing.ui.login.CreateAccountScreenViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        remoteDataSource.createUserFirebaseAccount(
            user.email,
            user.password,
            object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
                override fun onSuccess(model: Boolean) {
                    responseLD.postValue(ResponseData(success = true))
                    val authUser = Firebase.auth.currentUser
                    GlobalScope.launch {
                        addUser(User(
                                user.email,
                                user.first,
                                user.last
                            )
                        )
                    }
                }

                override fun onError(error: Int?) {
                    localDataSource.userIsLoggedIn = false
                    responseLD.postValue(ResponseData(errorResource = error))
                }
            }

        )
    }

    private suspend fun addUser(user: User){
        remoteDataSource.addUser(user, object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
            override fun onSuccess(model: Boolean) {
                GlobalScope.launch {
                localDataSource.insertUser(user)
                }
            }

            override fun onError(error: Int?) {

            }
        })
    }
}