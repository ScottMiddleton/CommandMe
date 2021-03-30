package com.middleton.scott.cmboxing.datasource.remote

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.datasource.local.model.User

class RemoteDataSource {
    private val db = Firebase.firestore

    interface CallbackWithError<S, E> {
        fun onSuccess(model: S)
        fun onError(error: E)
    }

    fun createUserFirebaseAccount(
        email: String,
        password: String,
        callback: CallbackWithError<Boolean, String?>
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess(true)
                }
            }.addOnFailureListener {
                callback.onError(it.localizedMessage)
            }
    }

    fun authSignInWithEmailPassword(
        email: String,
        password: String,
        callback: CallbackWithError<Boolean, String?>
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess(true)
                }
            }.addOnFailureListener {
                callback.onError(it.localizedMessage)
            }
    }

//    fun addUserToFirestore(user: User, callback: CallbackWithError<Boolean, String?>) {
//        val userHashMap = hashMapOf(
//            "email" to user.email,
//            "first" to user.first,
//            "last" to user.last,
//            "hasPurchasedUnlimitedCommands" to user.hasPurchasedUnlimitedCommands
//        )
//        db.collection("users").document(user.email)
//            .set(userHashMap)
//            .addOnSuccessListener { callback.onSuccess(true) }
//            .addOnFailureListener {
//                callback.onError(it.localizedMessage)
//            }
//    }

    fun updateUserPurchasedUnlimitedCommands(user: User) {
        val userHashMap = hashMapOf(
            "email" to user.email,
            "first" to user.first,
            "last" to user.last,
            "hasPurchasedUnlimitedCommands" to user.hasPurchasedUnlimitedCommands
        )
        db.collection("users").document(user.email)
            .set(userHashMap)
    }

//    fun getUserByEmail(email: String, callback: CallbackWithError<User, String?>) {
//        val docRef = db.collection("users").document(email)
//
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document.data != null) {
//                    callback.onSuccess(
//                        User(
//                            document.data?.get("email") as String,
//                            document.data?.get("first") as String,
//                            document.data?.get("last") as String
//                        )
//                    )
//                }
//            }
//            .addOnFailureListener {
//                callback.onError(it.message)
//            }
//    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.instance, gso)

        mGoogleSignInClient.signOut().addOnCompleteListener(
            MainActivity.instance
        ) { }
    }
}