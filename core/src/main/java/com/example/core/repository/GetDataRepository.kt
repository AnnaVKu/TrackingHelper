package com.example.core.repository

import com.example.core.Constants
import com.example.core.db.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetDataRepository @Inject constructor() {

    @Inject
    lateinit var mAuth: FirebaseAuth

    private lateinit var reference: DatabaseReference
    private lateinit var listener: ValueEventListener


    fun getDataFromFirestore(): Observable<MutableList<Response>> = Observable.create{ subscriber ->
        reference = FirebaseDatabase.getInstance().reference.child(Constants.DB_CHILD_USERS)
            .child(Constants.DB_CHILD_USER_ID).child(
                mAuth.currentUser!!.uid
            )

        listener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result: MutableList<Response> = ArrayList()
                for (ds in snapshot.children) {
                    ds.getValue(Response::class.java)?.let { result.add(it) }
                }
                // Log.i("Result", "snapshot: ${result.size}")
                subscriber.onNext(result)
            }

            override fun onCancelled(error: DatabaseError) {
                subscriber.onError(error.toException())
            }

        })
    }

    fun removeEventListener() {
        reference?.let { it.removeEventListener(listener) }
    }
}