package com.example.core.di

import android.content.Context
import com.example.core.repository.AuthenticationRepository
import com.example.core.repository.GetDataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, FirebaseModule::class])
interface AppComponent {
    fun injectAppContext(): Context
    fun injectFirebaseAuth(): FirebaseAuth
    fun injectFirebaseDatabase(): FirebaseDatabase
    fun injectDatabaseReference(): DatabaseReference

}