package com.example.core

import android.app.Application
import com.example.core.di.AppComponent
import com.example.core.di.AppModule
import com.example.core.di.DaggerAppComponent

class MyApplication: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(context = this)).build()
    }

//    fun initActivityComponent(activity: MainActivity?): ActivityComponent? {
//        if (activityComponent == null) {
//            activityComponent = appComponent.activityComponent(ActivityModule(activity))
//        }
//        return activityComponent
//    }
}