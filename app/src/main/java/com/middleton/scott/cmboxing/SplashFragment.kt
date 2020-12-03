package com.middleton.scott.cmboxing

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.middleton.scott.cmboxing.ui.workouts.WorkoutsScreenDirections

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        object : CountDownTimer(2000, 1000){
            override fun onFinish() {
                val action = SplashFragmentDirections.actionSplashFragmentToWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }.start()
    }

}