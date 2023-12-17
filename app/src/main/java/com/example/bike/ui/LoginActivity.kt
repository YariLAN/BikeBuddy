package com.example.bike.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast.*
import com.example.bike.R
import com.google.firebase.auth.FirebaseAuth

// класс, логика которая работает с авторизацией пользователя
class LoginActivity : AppCompatActivity() {

    // электронная почта
    private lateinit var email: EditText;

    // пароль
    private lateinit var pass: EditText

    // диалоговое окошко для отображения состояния каки-либо операций
    private lateinit var progressBar: ProgressDialog;

    // объект FirebaseAuth - сервиса, для регистрации, авторизации пользователей
    private lateinit var auth: FirebaseAuth;

    // метод, вызвающийся при первом открытие страницы
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init();

        // вызов функции записи данных в базу при регистрации по клику на кнопку
        findViewById<Button>(R.id.button).setOnClickListener {
            val e = email.text.toString().trim();
            val p = pass.text.toString().trim();

            // проверка, что заполнены все данные
            if (TextUtils.isEmpty(e) || TextUtils.isEmpty(p)) {
                makeText(this, "Enter the all data", LENGTH_SHORT).show();
            }
            else {
                // установка значения на диалоговое окошко
                progressBar.setTitle("Authentication...");
                progressBar.show();

                // функция проверки введенных данных с теми, что есть в базе
                auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(this) {task ->

                        // скрытие диалогового окна
                        progressBar.hide();

                        // если проверка прошла успешно, переход на главную страницу
                        if (task.isSuccessful) {
                            Log.d("LoginActivity", "loginUser:success");

                            // переход на главную страницу
                            openAppForUser();
                        }
                        else {
                            // если проверку данные не прошли - вывод сообщени на экран и журналирование
                            Log.e("LoginActivity", "signInWithEmail:failure", task.exception);
                            makeText(this, "Authentication is failed.", LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // переход на страницу с регистрацией по клику на кнопку
        findViewById<Button>(R.id.register).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent);
        }

        // переход на страницу для сброса пароля по клику на кнопку
        findViewById<Button>(R.id.resetPassword).setOnClickListener {
            val intent =  Intent(this, ForgotPassword::class.java);
            startActivity(intent);
        }
    }

    // инициализация кнопок
    fun init() {
        email = findViewById(R.id.email_login);
        pass = findViewById(R.id.password);

        auth = FirebaseAuth.getInstance();
        progressBar = ProgressDialog(this);
    }

    // фнукция перехода на главную страницу после авторизации
    private fun openAppForUser() {
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}