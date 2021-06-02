package com.middleton.scott.cmboxing.datasource

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
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
                    }
                    responseLD.postValue(ResponseData(success = true))
                }

                override fun onError(error: String?) {
                    responseLD.postValue(ResponseData(errorString = error))
                }
            }
        )
    }

    suspend fun insertCurrentUser(user: User) {
        localDataSource.insertCurrentUser(user)
    }

    suspend fun updateUserPurchaseUnlimitedCommands() {
        val user = localDataSource.getCurrentUser()
        user?.let {
            it.hasPurchasedUnlimitedCommands = true
            localDataSource.insertCurrentUser(it)
            remoteDataSource.updateUserPurchasedUnlimitedCommands(it)
        }
    }

    fun userHasPurchasedUnlimitedCommands(onPurchasedResponse: ((hasPurchased: Boolean) -> Unit)) {
        var hasPurchased = false
        startConnectionPurchaseHistory { purchaseHistoryList ->
            purchaseHistoryList.forEach {
                if (it.sku == PRODUCT_UNLIMITED_COMMANDS) {
                    hasPurchased = true
                }
            }
            // Setting as true to remove billing for now. set as hasPurchased for billing
            onPurchasedResponse(true)
        }
    }

    fun signOut() {
        remoteDataSource.signOut()
    }

    fun authSignInWithEmailPassword(
        email: String,
        password: String,
        responseLD: MutableLiveData<ResponseData>
    ) {
        remoteDataSource.authSignInWithEmailPassword(email,
            password,
            object : RemoteDataSource.CallbackWithError<Boolean, String?> {
                override fun onSuccess(model: Boolean) {
                    userHasPurchasedUnlimitedCommands {
                        val authUser = FirebaseAuth.getInstance().currentUser
                        if (authUser != null) {
                            val user = authUser.email?.let { email ->
                                User(
                                    email = email,
                                    hasPurchasedUnlimitedCommands = it
                                )
                            }

                            GlobalScope.launch {
                                user?.let { user -> insertCurrentUser(user) }
                            }
                        }
                    }
                }

                override fun onError(error: String?) {
                    responseLD.postValue(ResponseData(errorString = error))
                }
            })
    }

    suspend fun deleteCommand(command: Command) {
        localDataSource.deleteCommand(command)
        localDataSource.deleteStructuredCommandCrossRefByCommandId(command.id)
        localDataSource.deleteSelectedCommandCrossRefByCommandId(command.id)
    }

    fun userCanAddMoreCommands(): Boolean {
        return if (localDataSource.userHasPurchasedUnlimitedCommands()) {
            true
        } else {
            localDataSource.getCommands().size < NON_PREMIUM_USER_MAXIMUM_NUMBER_OF_COMMANDS
        }
    }

    fun downloadWorkout() {
        return remoteDataSource.downloadWorkout(object : RemoteDataSource.CallbackWithError<Boolean, String?> {
            override fun onSuccess(model: Boolean) {

            }

            override fun onError(error: String?) {
            }
        })
    }
}