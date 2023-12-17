package com.example.bike.repository

import com.example.bike.datasources.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.database.*

// класс для добавления пользователя в базу данных,
// реализует интерфейс с методами добалвения данных в базу
object UserRepository: IRepository<User> {

    // переменная, отвечающая за базу данных Firebase
    private val db = Firebase.database;

    // метод добавления данных в базу
    override fun addItem(item: User) {
        try {
            // утсановления значения для клоюча item.id (начальный ключ пользователя)
            db.getReference("users").child(item.id).setValue(item)
        }
        // если не удасться записать - вывод исключения
        catch (ex: FirebaseException) {
            throw ex;
        }
    }
}