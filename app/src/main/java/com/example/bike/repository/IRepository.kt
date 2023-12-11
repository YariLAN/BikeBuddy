package com.example.bike.repository

public interface IRepository<T> {

    fun addItem(item: T);
}