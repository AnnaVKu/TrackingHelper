package com.example.auth.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.auth.R
import com.example.auth.databinding.FragmentSignInBinding
import com.example.auth.di.DaggerAuthComponent
import com.example.core.MyApplication
import com.example.core.ResponseWrapper
import com.example.core.viewModel.MyViewModel
import com.example.core.viewModel.ViewModelFactory
import javax.inject.Inject

class SignInFragment : Fragment() {
    private lateinit var viewBinding: FragmentSignInBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[MyViewModel::class.java]
    }

    private var email: String = ""
    private var password: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.i("Result", " SignInFragment onCreateView()")

        viewBinding = FragmentSignInBinding.inflate(inflater, container, false)

        val appComponent = (requireActivity().applicationContext as MyApplication).appComponent
        DaggerAuthComponent.factory().create(appComponent).injectFragmentSignIn(this)

        // SIGN_IN button
        viewBinding.buttonSingIn.setOnClickListener {
            email = viewBinding.textInputLayoutEmail.editText?.text.toString().trim()
            password = viewBinding.textInputLayoutPassword.editText?.text.toString().trim()
            viewModel.signInWasClicked(email, password)
        }

        viewModel.getLiveDataIsLogin().observe(requireActivity(), { result ->
            if (result.status == ResponseWrapper.Status.SUCCESS) {
                viewModel.replaceMapsFragment()
                viewModel.clearDataForSignInFragment()
            }
        })

        viewModel.getLiveDataIsNotValidEmail().observe(requireActivity(), { result ->
            if (result) {
                viewBinding.textInputLayoutEmail.error =
                    resources.getString(R.string.email_not_valid)
                viewBinding.textInputLayoutEmail.requestFocus()
            }
        })

        viewModel.getLiveDataIsNotValidPassword().observe(requireActivity(), { result ->
            if (result) {
                viewBinding.textInputLayoutPassword.error = getString(R.string.password_not_valid)
                viewBinding.textInputLayoutPassword.requestFocus()
            }
        })

        //click on SIGN_UP
        viewBinding.textViewSingUp.setOnClickListener {
            viewModel.replaceSignUpFragment()
        }

        // forgot the password click
        viewBinding.textViewPasswordForgot.setOnClickListener {
            email = viewBinding.textInputLayoutEmail.editText?.text.toString().trim()
            viewModel.passwordForgotClicked(email)
        }

        viewModel.getLiveDataIsPasswordSend().observe(requireActivity(), { result ->
            if (result.status == ResponseWrapper.Status.SUCCESS) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_was_send),
                    Toast.LENGTH_SHORT
                ).show()
            } else if(result.status == ResponseWrapper.Status.ERROR){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_text_valid_email),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        return viewBinding.root
    }

}