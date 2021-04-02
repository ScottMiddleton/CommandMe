package com.middleton.scott.cmboxing.ui.splash

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.middleton.scott.cmboxing.R


class SplashScreen : Fragment() {

    private lateinit var auth: FirebaseAuth

    private val countDownTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            val currentUser = auth.currentUser
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())

            if (currentUser != null || account != null) {
                val action = SplashScreenDirections.actionSplashFragmentToWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                val action = SplashScreenDirections.actionSplashFragmentToLoginScreen()
                findNavController().navigate(
                    action
                )
            }
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onResume() {
        super.onResume()
        auth = Firebase.auth
        countDownTimer.start()
    }

    override fun onPause() {
        super.onPause()
        countDownTimer.cancel()
    }

}