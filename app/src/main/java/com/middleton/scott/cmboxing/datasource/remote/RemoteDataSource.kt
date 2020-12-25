package com.middleton.scott.cmboxing.datasource.remote

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.User

class RemoteDataSource {

    val auth = Firebase.auth
    val db = Firebase.firestore

    interface CallbackWithError<S, E> {
        fun onSuccess(model: S)
        fun onError(error: E)
    }

    fun createUserFirebaseAccount(
        email: String,
        password: String,
        callback: CallbackWithError<Boolean, Int?>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess(true)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        callback.onError(R.string.weak_password_error)
                    } catch (e: FirebaseAuthInvalidCredentialsException) {

                    } catch (e: FirebaseAuthUserCollisionException) {
                        callback.onError(R.string.email_collision_error)
                    } catch (e: Exception) {
                        callback.onError(R.string.create_account_error)
                    }
                }

            }
    }

    fun addUser(user: User, callback: CallbackWithError<Boolean, Int?>) {
        val userHashMap = hashMapOf(
            "email" to user.email,
            "first" to user.first,
            "last" to user.last
        )
        db.collection("users").document(user.email)
            .set(userHashMap)
            .addOnSuccessListener { callback.onSuccess(true) }
            .addOnFailureListener { }
    }
}