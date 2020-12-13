package com.middleton.scott.cmboxing.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.R


class RemoteDataSource {

    val auth = Firebase.auth
    val db = Firebase.firestore

    interface CallbackWithError<S, E> {
        fun onSuccess(model: S)
        fun onError(error: E)
    }

    fun createUserAccount(
        email: String,
        password: String,
        callback: CallbackWithError<Boolean, Int?>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
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

    fun addUser(email: String?, first: String?, last: String?, uid: String?, callback: CallbackWithError<Boolean, Int?>) {
        val user = hashMapOf(
            "email" to email,
            "first" to first,
            "last" to last,
            "uid" to uid
        )

        if (email != null && uid != null) {
            db.collection("users").document(email)
                .set(user)
            .addOnSuccessListener { callback.onSuccess(true) }
            .addOnFailureListener {  }
        }
    }

}