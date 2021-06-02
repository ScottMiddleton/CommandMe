package com.middleton.scott.cmboxing.datasource.remote

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.datasource.local.model.User
import java.io.File


class RemoteDataSource {
    private val db = Firebase.firestore
    var storage = FirebaseStorage.getInstance()

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

    fun downloadWorkout(callback: CallbackWithError<Boolean, String?>) {
        db.collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.data
                }
            }
            .addOnFailureListener { exception ->

            }

        val storageRef = storage.reference
        val audio = storageRef.child("workouts/Changer.mp3")

        val localFile = File.createTempFile("audio", "mp3")

        audio.getFile(localFile).addOnSuccessListener {
            // Local temp file has been created
            it
        }.addOnFailureListener {
            // Handle any errors
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