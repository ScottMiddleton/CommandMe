package com.middleton.scott.cmboxing.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.other.Constants
import com.middleton.scott.cmboxing.utils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_create_account_screen.*
import kotlinx.android.synthetic.main.fragment_create_account_screen.create_account_btn
import kotlinx.android.synthetic.main.fragment_create_account_screen.email_et
import kotlinx.android.synthetic.main.fragment_create_account_screen.email_til
import kotlinx.android.synthetic.main.fragment_create_account_screen.google_login_btn
import kotlinx.android.synthetic.main.fragment_create_account_screen.password_et
import kotlinx.android.synthetic.main.fragment_create_account_screen.password_til
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
        back_btn.setOnClickListener {
            findNavController().popBackStack()
        }

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
            create_account_progress_bar.visibility = View.VISIBLE
            create_account_btn.text = ""
            viewModel.createUserAccount()
        }

        google_login_btn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, Constants.GOOGLE_SIGN_IN)
            google_login_progress_bar.visibility = VISIBLE
            google_login_btn.text = ""
        }
    }

    private fun subscribeUI() {
        viewModel.createAccountResponseLD.observe(viewLifecycleOwner, {
            if(it.success){
                hideKeyboard()
                val action = CreateAccountScreenDirections.actionCreateAccountScreenToWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                create_account_progress_bar.visibility = View.GONE
                create_account_btn.text = getString(R.string.create_account)
                Toast.makeText(requireContext(), it.errorString, LENGTH_LONG).show()
            }
        })

        viewModel.addUserResponseLD.observe(viewLifecycleOwner, {
            if (it.success) {
                hideKeyboard()
                val action =
                    LoginScreenDirections.actionLoginScreenToMyWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                Toast.makeText(
                    requireContext(), it.errorString,
                    Toast.LENGTH_LONG
                ).show()
            }
            google_login_progress_bar.visibility = GONE
            google_login_btn.text = getString(R.string.sign_in)
        })

        viewModel.emailValidLD.observe(viewLifecycleOwner, {
            if(it){
                email_til.error = null
            } else {
                create_account_progress_bar.visibility = View.GONE
                create_account_btn.text = getString(R.string.create_account)
                email_til.error = getString(R.string.email_validation_error)
            }
        })

        viewModel.passwordValidLD.observe(viewLifecycleOwner, {
            if(it){
                password_til.error = null
            } else {
                create_account_progress_bar.visibility = View.GONE
                create_account_btn.text = getString(R.string.create_account)
                password_til.error = getString(R.string.password_validation_error)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == Constants.GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            viewModel.addUser(account?.givenName?: "", account?.familyName?: "", account?.email?: "")
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(
                requireContext(), e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}