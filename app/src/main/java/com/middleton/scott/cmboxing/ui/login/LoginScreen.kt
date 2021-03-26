package com.middleton.scott.cmboxing.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.other.Constants.GOOGLE_SIGN_IN
import com.middleton.scott.cmboxing.utils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_login_screen.*
import kotlinx.android.synthetic.main.fragment_login_screen.create_account_btn
import kotlinx.android.synthetic.main.fragment_login_screen.email_et
import kotlinx.android.synthetic.main.fragment_login_screen.email_til
import kotlinx.android.synthetic.main.fragment_login_screen.google_login_btn
import kotlinx.android.synthetic.main.fragment_login_screen.google_login_progress_bar
import kotlinx.android.synthetic.main.fragment_login_screen.password_et
import kotlinx.android.synthetic.main.fragment_login_screen.password_til
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
            hideKeyboard()
            login_progress_bar.visibility = VISIBLE
            login_btn.text = ""
            viewModel.signInWithEmailPassword()
        }

        google_login_btn.setOnClickListener {
            hideKeyboard()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
            google_login_progress_bar.visibility = VISIBLE
            google_login_btn.text = ""
        }

        create_account_btn.setOnClickListener {
            hideKeyboard()
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
                hideKeyboard()
                val action =
                    LoginScreenDirections.actionLoginScreenToMyWorkoutsScreen()
                findNavController().navigate(
                    action
                )
            } else {
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
                Toast.makeText(
                    requireContext(), it.errorString,
                    Toast.LENGTH_LONG
                ).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
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
                requireContext(), getString(R.string.google_sign_in_error),
                Toast.LENGTH_LONG
            ).show()
            google_login_progress_bar.visibility = GONE
            google_login_btn.text = getString(R.string.sign_in)
        }
    }
}