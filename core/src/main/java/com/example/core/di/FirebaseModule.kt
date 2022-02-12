package com.example.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import com.example.core.Constants
import javax.inject.Singleton

@Module
class FirebaseModule {
    private lateinit var db: FirebaseDatabase

    @Singleton
    @Provides
    fun providesFireBaseInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun providesFireBaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance(Constants.PATH_TO_REALTIME_DATABASE)
    }

    @Singleton
    @Provides
    fun providesFireBaseReference(): DatabaseReference {
        db = FirebaseDatabase.getInstance(Constants.PATH_TO_REALTIME_DATABASE)
        return db.reference
    }
}