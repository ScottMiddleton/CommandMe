package com.middleton.scott.cmboxing.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.middleton.scott.cmboxing.R
import kotlinx.android.synthetic.main.fragment_create_account_screen.*
import org.koin.android.ext.android.inject

class CreateAccountScreen : Fragment() {
    private val viewModel: CreateAccountViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
        subscribeUI()
    }

    private fun setClickListeners() {
        email_et.doAfterTextChanged {
            viewModel.user.email = it.toString()
            viewModel.validateCredentials()
        }

        password_et.doAfterTextChanged {
            viewModel.user.password = it.toString()
            viewModel.validateCredentials()
        }

        first_name_et.doAfterTextChanged {
            viewModel.user.first = it.toString()
        }

        last_name_et.doAfterTextChanged {
            viewModel.user.last = it.toString()
        }

        create_account_btn.setOnClickListener {
            viewModel.createUserAccount()
        }
    }

    private fun subscribeUI() {
        viewModel.createAccountResponseLD.observe(viewLifecycleOwner, {
            if(it.success){
                val action = CreateAccountScreenDirections.actionCreateAccountScreenToWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                Toast.makeText(requireContext(), it.getErrorString(requireContext()), LENGTH_LONG).show()
            }
        })

        viewModel.emailValidLD.observe(viewLifecycleOwner, {
            if(it){
                email_til.error = null
            } else {
                email_til.error = getString(R.string.email_validation_error)
            }
        })

        viewModel.passwordValidLD.observe(viewLifecycleOwner, {
            if(it){
                password_til.error = null
            } else {
                password_til.error = getString(R.string.password_validation_error)
            }
        })
    }

}