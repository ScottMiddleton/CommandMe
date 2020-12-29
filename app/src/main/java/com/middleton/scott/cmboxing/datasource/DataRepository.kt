package com.middleton.scott.cmboxing.datasource

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.User
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import com.middleton.scott.cmboxing.ui.login.CreateAccountViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    fun getLocalDataSource(): LocalDataSource {
        return localDataSource
    }

    fun createUserFirebaseAuthAccount(
        userVMModel: CreateAccountViewModel.UserVMModel,
        responseLD: MutableLiveData<ResponseData>
    ) {
        remoteDataSource.createUserFirebaseAccount(
            userVMModel.email,
            userVMModel.password,
            object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
                override fun onSuccess(model: Boolean) {
                    localDataSource.userIsLoggedIn = true
                    responseLD.postValue(ResponseData(success = true))
                    GlobalScope.launch {
                        addUserToFireStore(
                            User(
                                userVMModel.email,
                                userVMModel.first,
                                userVMModel.last
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

    fun addUserToFireStore(user: User) {
        remoteDataSource.addUser(user, object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
            override fun onSuccess(model: Boolean) {
                GlobalScope.launch {
                    localDataSource.insertCurrentUser(user)
                }
            }

            override fun onError(error: Int?) {
                // TODO
            }
        })
    }

    fun signOut() {
        remoteDataSource.signOut()
        localDataSource.userIsLoggedIn = false
    }

    fun signIn(email: String, password: String, responseLD: MutableLiveData<ResponseData>) {
        remoteDataSource.signIn(email,
            password,
            object : RemoteDataSource.CallbackWithError<Boolean, Int?> {
                override fun onSuccess(model: Boolean) {
                    localDataSource.userIsLoggedIn = true

                    GlobalScope.launch {
                        remoteDataSource.getUserByEmail(
                            email,
                            object : RemoteDataSource.CallbackWithError<User, Int?> {
                                override fun onSuccess(model: User) {
                                    GlobalScope.launch {
                                        localDataSource.insertCurrentUser(model)
                                        responseLD.postValue(ResponseData(success = true))
                                    }
                                }
                                override fun onError(error: Int?) {
                                    // TODO
                                }
                            })
                    }
                }

                override fun onError(error: Int?) {
                    responseLD.postValue(ResponseData(errorResource = error))
                }
            })
    }
}