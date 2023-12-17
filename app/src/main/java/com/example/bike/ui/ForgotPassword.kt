package com.example.bike.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast.*
import com.example.bike.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ForgotPassword : AppCompatActivity() {

    // почта
    private var email: EditText? = null;

    // база данных от Firebase
    private var mDatabase: FirebaseDatabase? = null

    // сервис для аутентификации
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password);

        // инициализация почты
        initEmail();

        // инициализация кнопки отправки сообщения на почту по id
        val btn = findViewById<Button>(R.id.send_reset_password);

        // инициализация события для кнопки по ее клику
        btn.setOnClickListener {
            val em = email?.text?.trim().toString();

            // метод отправки сообщения
            mAuth!!.sendPasswordResetEmail(em)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        // журналирование события - Ошибка передачи сообщения
                        Log.w("ForgotPasswordActivity", task.exception!!.message.toString())
                        makeText(this, "No user found with this email.", LENGTH_SHORT).show()
                    }
                    else {
                        // журналирование события - На почту отправлено
                        Log.d("ForgotPaswwordActivity", "Email sent");
                        // вывод текста на экран
                        makeText(this, "Email sent", LENGTH_SHORT).show();

                        // метод перехода из текущей страницу на страницу с авторизацией
                        updateUI();
                    }
                }
        }
    }

    private fun initEmail() {
        email = findViewById(R.id.email_forgot);

        mAuth = FirebaseAuth.getInstance();
    }

    // метод перехода из текущей страницу на страницу с авторизацией
    private fun updateUI() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}