package com.example.trackinglocationhelper.view

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.auth.di.DaggerAuthComponent
import com.example.auth.view.SignInFragment
import com.example.auth.view.SignUpFragment
import com.example.core.MyApplication
import com.example.core.viewModel.MyViewModel
import com.example.core.viewModel.ViewModelFactory
import com.example.trackinglocationhelper.databinding.ActivityMainBinding
import com.example.trackinglocationhelper.di.DaggerTrackingComponent
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(this, factory = viewModelFactory)[MyViewModel::class.java]
    }

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val appComponent = (applicationContext as MyApplication).appComponent
        val authComponent = DaggerAuthComponent.factory().create(appComponent)
        DaggerTrackingComponent.factory().create(appComponent, authComponent).injectMainActivity(this)

        viewModel.getTransitionLiveData().observe(this, {
            when(it) {
                MyViewModel.maps -> replaceMapsFragment()
                MyViewModel.signIn -> replaceSignInFragment()
                MyViewModel.signUp -> replaceSignUpFragment()
                MyViewModel.fromMapsToSignIn -> returnFromMapsFragment()
            }
        })

        mAuth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            if (mAuth.currentUser != null) {
                for(i : Int in 0..supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStack()
                }
                replaceMapsFragment()
            } else {
               replaceSignInFragment()
            }
        }
    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

    private fun replaceSignInFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(viewBinding.fragmentContainerView.id, SignInFragment())
            .commit()
    }

    private fun returnFromMapsFragment() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
        replaceSignInFragment()
    }

    private fun replaceMapsFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(viewBinding.fragmentContainerView.id, MapsFragment())
            .commit()
    }

    private fun replaceSignUpFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(viewBinding.fragmentContainerView.id, SignUpFragment())
            .commit()
    }
}