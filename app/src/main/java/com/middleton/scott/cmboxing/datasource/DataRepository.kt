package com.middleton.scott.cmboxing.datasource

import androidx.lifecycle.MutableLiveData
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.User
import com.middleton.scott.cmboxing.datasource.remote.RemoteDataSource
import com.middleton.scott.cmboxing.datasource.remote.ResponseData
import com.middleton.scott.cmboxing.other.Constants.NON_PREMIUM_USER_MAXIMUM_NUMBER_OF_COMMANDS
import com.middleton.scott.cmboxing.other.Constants.PRODUCT_UNLIMITED_COMMANDS
import com.middleton.scott.cmboxing.ui.login.CreateAccountViewModel
import com.middleton.scott.cmboxing.utils.startConnectionPurchaseHistory
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
            object : RemoteDataSource.CallbackWithError<Boolean, String?> {
                override fun onSuccess(model: Boolean) {
                    localDataSource.userIsLoggedIn = true

                    userHasPurchasedUnlimitedCommands {
                        val hasPurchasedUnlimitedCommands = it
                        val user = User(
                            userVMModel.email,
                            userVMModel.first,
                            userVMModel.last,
                            hasPurchasedUnlimitedCommands
                        )

                        GlobalScope.launch {
                            insertCurrentUser(user)
                        }

                        addUserToFirestore(user)
                    }
                    responseLD.postValue(ResponseData(success = true))
                }

                override fun onError(error: String?) {
                    localDataSource.userIsLoggedIn = false
                    responseLD.postValue(ResponseData(errorString = error))
                }
            }
        )
    }

    suspend fun insertCurrentUser(user: User) {
        localDataSource.insertCurrentUser(user)
    }

    fun addUserToFirestore(user: User) {
        remoteDataSource.addUserToFirestore(
            user,
            object : RemoteDataSource.CallbackWithError<Boolean, String?> {
                override fun onSuccess(model: Boolean) {
                }

                override fun onError(error: String?) {
                }
            })
    }

    suspend fun updateUserPurchaseUnlimitedCommands() {
        val user = localDataSource.getCurrentUser()
        user.hasPurchasedUnlimitedCommands = true
        localDataSource.insertCurrentUser(user)
        remoteDataSource.updateUserPurchasedUnlimitedCommands(user)
    }

    fun userHasPurchasedUnlimitedCommands(onPurchasedResponse: ((hasPurchased: Boolean) -> Unit)) {
        var hasPurchased = false
        startConnectionPurchaseHistory { purchaseHistoryList ->
            purchaseHistoryList.forEach {
                if (it.sku == PRODUCT_UNLIMITED_COMMANDS) {
                    hasPurchased = true
                }
            }
            onPurchasedResponse(hasPurchased)
        }
    }

    fun signOut() {
        remoteDataSource.signOut()
        localDataSource.userIsLoggedIn = false
    }

    fun signInWithEmailPassword(email: String, password: String, responseLD: MutableLiveData<ResponseData>) {
        remoteDataSource.signInWithEmailPassword(email,
            password,
            object : RemoteDataSource.CallbackWithError<Boolean, String?> {
                override fun onSuccess(model: Boolean) {
                    GlobalScope.launch {
                        remoteDataSource.getUserByEmail(
                            email,
                            object : RemoteDataSource.CallbackWithError<User, String?> {
                                override fun onSuccess(model: User) {
                                    userHasPurchasedUnlimitedCommands {
                                        val hasPurchasedUnlimitedCommands = it
                                        model.hasPurchasedUnlimitedCommands =
                                            hasPurchasedUnlimitedCommands
                                        GlobalScope.launch {
                                            insertCurrentUser(model)
                                        }
                                        addUserToFirestore(model)
                                    }
                                    responseLD.postValue(ResponseData(success = true))
                                }

                                override fun onError(error: String?) {
                                    responseLD.postValue(ResponseData(errorString = error))
                                }
                            })
                    }
                }

                override fun onError(error: String?) {
                    localDataSource.userIsLoggedIn = false
                    responseLD.postValue(ResponseData(errorString = error))
                }
            })
    }

    suspend fun deleteCommand(command: Command) {
        localDataSource.deleteCommand(command)
        localDataSource.deleteStructuredCommandCrossRefById(command.id)
        localDataSource.deleteSelectedCommandCrossRefByCommandId(command.id)
    }

    fun userCanAddMoreCommands(): Boolean {
        return if (localDataSource.userHasPurchasedUnlimitedCommands()) {
            true
        } else {
            localDataSource.getCommands().size < NON_PREMIUM_USER_MAXIMUM_NUMBER_OF_COMMANDS
        }
    }
}