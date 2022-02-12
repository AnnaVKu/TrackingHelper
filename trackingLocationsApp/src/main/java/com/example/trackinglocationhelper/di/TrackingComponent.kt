package com.example.trackinglocationhelper.di

import com.example.auth.di.AuthComponent
import com.example.core.di.AppComponent
import com.example.trackinglocationhelper.view.MainActivity
import com.example.trackinglocationhelper.view.MapsFragment
import dagger.Component

@TrackingScope
@Component(dependencies = [AppComponent::class, AuthComponent::class])
interface TrackingComponent{

    @Component.Factory
    interface Factory1 {
        fun create(appComponent: AppComponent, authComponent: AuthComponent): TrackingComponent
    }
    fun injectMainActivity(activity: MainActivity)
    fun injectFragmentMaps(fragment: MapsFragment)
}