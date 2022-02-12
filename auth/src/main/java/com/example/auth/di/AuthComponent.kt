package com.example.auth.di

import com.example.auth.view.SignInFragment
import com.example.auth.view.SignUpFragment
import com.example.core.di.AppComponent
import dagger.Component

@AuthScope
@Component(dependencies = [AppComponent::class])
interface AuthComponent {

    @Component.Factory
    interface Factory{
        // Takes an instance of AppComponent when creating
        // an instance of AuthComponent
        fun create(appComponent: AppComponent): AuthComponent
    }

    fun injectFragmentSignIn(fragment: SignInFragment)
    fun injectFragmentSignUp(fragment: SignUpFragment)
}
