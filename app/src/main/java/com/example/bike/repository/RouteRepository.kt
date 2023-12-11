package com.example.bike.repository

import com.example.bike.datasources.Route
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database

object RouteRepository : IRepository<Route> {

    private val db = Firebase.database;

    override fun addItem(item: Route) {
        try {
            db.getReference("routes").child(item.id).setValue(item)
        }
        catch (ex: FirebaseException) {
            throw ex;
        }
    }

    public fun getByUserId(id: String) : ArrayList<String> {
        val arrayList : ArrayList<String> = arrayListOf()

        db.reference.child("routes").orderByChild("userId").equalTo(id)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val r = snapshot.value as Route
                    arrayList.add(r.distance.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        return arrayList
    }
}