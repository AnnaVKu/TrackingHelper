package com.example.auth.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.auth.R
import com.example.auth.databinding.FragmentSignUpBinding
import com.example.auth.di.DaggerAuthComponent
import com.example.core.MyApplication
import com.example.core.ResponseWrapper
import com.example.core.viewModel.MyViewModel
import com.example.core.viewModel.ViewModelFactory
import javax.inject.Inject

class SignUpFragment : Fragment() {
    private lateinit var viewBinding: FragmentSignUpBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[MyViewModel::class.java]
    }

    private var name: String = ""
    private var email: String = ""
    private var password: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSignUpBinding.inflate(inflater, container, false)

        val appComponent = (requireActivity().applicationContext as MyApplication).appComponent
        DaggerAuthComponent.factory().create(appComponent).injectFragmentSignUp(this)

        // SIGN_UP button
        viewBinding.buttonSingUp.setOnClickListener {
            name = viewBinding.textInputLayoutUserName.editText?.text.toString().trim()
            email = viewBinding.textInputLayoutUserEmail.editText?.text.toString().trim()
            password = viewBinding.textInputLayoutPassword.editText?.text.toString().trim()
            viewModel.buttonSignUpWasClicked(name, email, password)
        }

        viewModel.getLiveDataIsCreateNewUser().observe(requireActivity(), { result ->
            if (result.status == ResponseWrapper.Status.SUCCESS) {
                viewModel.replaceMapsFragment()
                viewModel.clearDataForSignUpFragment()
            }
        })

        viewModel.getLiveDataIsNotValidName().observe(requireActivity(), { result ->
            if (result) {
                viewBinding.textInputLayoutUserName.error =
                    resources.getString(R.string.user_name_is_require)
                viewBinding.textInputLayoutUserName.requestFocus()
            }
        })

        viewModel.getLiveDataIsNotValidEmail().observe(requireActivity(), { result ->
            if (result) {
                viewBinding.textInputLayoutUserEmail.error =
                    resources.getString(R.string.please_provide_valid_email)
                viewBinding.textInputLayoutUserEmail.requestFocus()
            }
        })

        viewModel.getLiveDataIsNotValidPassword().observe(requireActivity(), { result ->
            if (result) {
                viewBinding.textInputLayoutPassword.error =
                    getString(R.string.password_length_should_be_more_5_characters)
                viewBinding.textInputLayoutPassword.requestFocus()
            }
        })

        //click on I already have an account
        viewBinding.textViewTitleHaveAccount.setOnClickListener {
            viewModel.backToSignInFragment()
        }

        return viewBinding.root
    }
}