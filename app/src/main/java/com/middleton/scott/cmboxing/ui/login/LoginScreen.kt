package com.middleton.scott.cmboxing.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.middleton.scott.cmboxing.R
import kotlinx.android.synthetic.main.fragment_login_screen.*
import kotlinx.android.synthetic.main.fragment_login_screen.create_account_btn
import kotlinx.android.synthetic.main.fragment_login_screen.email_til
import org.koin.android.ext.android.inject

class LoginScreen : Fragment() {
    private val viewModel: LoginViewModel by inject()

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
        subscribeUI()
    }

    private fun setClickListeners() {
        login_btn.setOnClickListener {
            login_progress_bar.visibility = VISIBLE
            login_btn.text = ""
            viewModel.login()
        }

        create_account_btn.setOnClickListener {
            val action = LoginScreenDirections.actionLoginScreenToCreateAccountScreen()
            findNavController().navigate(
                action
            )
        }

        email_et.doAfterTextChanged {
            viewModel.email = it.toString()
            viewModel.validate()
        }

        password_et.doAfterTextChanged {
            viewModel.password = it.toString()
            viewModel.validate()
        }
    }

    private fun subscribeUI() {
        viewModel.signInResponseLD.observe(viewLifecycleOwner, {
            if (it.success) {
                val action =
                    LoginScreenDirections.actionLoginScreenToMyWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
                Toast.makeText(
                    requireContext(), it.getErrorString(requireContext()),
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        viewModel.emailValidLD.observe(viewLifecycleOwner, {
            if (it) {
                email_til.error = null
            } else {
                email_til.error = getString(R.string.email_validation_error)
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
            }
        })

        viewModel.passwordValidLD.observe(viewLifecycleOwner, {
            if (it) {
                password_til.error = null
            } else {
                password_til.error = getString(R.string.password_validation_error)
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
            }
        })
    }
}