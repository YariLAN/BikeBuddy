package com.example.bike.repository

import com.example.bike.datasources.Route
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.database.database

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
}