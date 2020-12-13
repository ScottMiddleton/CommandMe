package com.middleton.scott.cmboxing.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.middleton.scott.cmboxing.R
import kotlinx.android.synthetic.main.fragment_login_screen.*

class LoginScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
    }

    private fun setClickListeners(){
        create_account_btn.setOnClickListener {
            val action = LoginScreenDirections.actionLoginScreenToCreateAccountScreen()
            findNavController().navigate(
                action
            )
        }
    }
}