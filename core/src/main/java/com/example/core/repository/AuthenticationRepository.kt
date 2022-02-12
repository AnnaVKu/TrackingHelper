package com.example.core.repository

import android.util.Log
import com.example.core.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class AuthenticationRepository @Inject constructor() {
    @Inject
    lateinit var mAuth: FirebaseAuth

    fun signIn(email: String, password: String): Completable = Completable.create{ emitter ->
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(!emitter.isDisposed) {
                    if(it.isSuccessful) {
                        emitter.onComplete()
                        Log.i("AAA", " emmiter.onComplete()")
                    } else {
                        emitter.onError(it.exception)
                        Log.i("AAA", " emmiter.onError " + it.exception.toString())
                    }
                }
            }
    }

    fun sendThePasswordOnTheEmail(email: String): Completable = Completable.create{ emitter ->
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
            if(!emitter.isDisposed) {
                if (it.isSuccessful) {
                    emitter.onComplete()
                } else emitter.onError(it.exception)
            }
        }
    }

    fun createNewUser(email: String, password: String): Completable = Completable.create{ emitter ->
        FirebaseAuth
            .getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!emitter.isDisposed) {
                    if(it.isSuccessful) {
                        val database: FirebaseDatabase = FirebaseDatabase.getInstance(Constants.PATH_TO_REALTIME_DATABASE)
                        val reference = database.reference
                        (mAuth.currentUser)?.let {
                            reference
                                .child(Constants.DB_CHILD_USERS)
                                .child(Constants.DB_CHILD_USER_ID)
                                .child(it.uid)
                                .setValue(it.uid)
                        }
                        emitter.onComplete()
                        Log.i("AAA", " emmiter.onComplete() create new user")
                    } else emitter.onError(it.exception)
                }
            }
    }

    fun signOut(): Completable = Completable.create { emitter ->
        if(!emitter.isDisposed) {
            mAuth.signOut()
            emitter.onComplete()
        }
    }
}