package com.example.app2.di

import com.example.app2.view.MainActivity
import com.example.app2.view.MapsFragment
import com.example.auth.di.AuthComponent
import com.example.core.di.AppComponent
import dagger.Component
import javax.inject.Singleton

@ShowScope
@Component(dependencies = [AppComponent::class, AuthComponent::class])
interface ShowComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent, authComponent: AuthComponent): ShowComponent
    }

    fun injectMainActivity(activity: MainActivity)
    fun injectFragmentMaps(fragment: MapsFragment)
}