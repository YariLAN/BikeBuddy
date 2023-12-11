package com.example.bike.repository

import com.example.bike.datasources.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.database.*

object UserRepository: IRepository<User> {

    private val db = Firebase.database;

    override fun addItem(item: User) {
        try {
            db.getReference("users").child(item.id).setValue(item)
        }
        catch (ex: FirebaseException) {
            throw ex;
        }
    }

    fun getUserById(id: String): Task<DataSnapshot> {

        return db.getReference("users").child(id).get();
    }
}