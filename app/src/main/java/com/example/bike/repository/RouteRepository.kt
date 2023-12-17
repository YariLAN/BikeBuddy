package com.example.bike.repository

import com.example.bike.datasources.Route
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.database.database

// Класс для добавления данных маршрута в базу
object RouteRepository : IRepository<Route> {

    // База данных
    private val db = Firebase.database;

    // метод добавления
    override fun addItem(item: Route) {
        try {
            // подключение к таблице route и задание нового ключа
            db.getReference("routes").child(item.id).setValue(item)
        }
        // если добавить данные не вышло
        // вызывается исключеник
        catch (ex: FirebaseException) {
            throw ex;
        }
    }
}