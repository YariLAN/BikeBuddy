package com.example.bike.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import com.example.bike.R
import com.example.bike.datasources.User
import com.example.bike.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    // имя
    private lateinit var first: EditText;

    // фамилия
    private lateinit var last: EditText;

    // почта
    private lateinit var email: EditText;

    // пароль
    private lateinit var password: EditText;

    // диалоговое окна для вывода состояния операций
    private lateinit var mProgressBar: ProgressDialog;

    // объект класса FirebaseAuth
    private lateinit var mAuth: FirebaseAuth;

    // инициализация всех свойств
    private fun init() {
        first = findViewById(R.id.firstName);
        last = findViewById(R.id.lastName);

        email = findViewById(R.id.email_register);
        password = findViewById(R.id.password);

        mProgressBar = ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
    }

    // метод создания компонента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        init();

        // инициализация кнопки регистрации
        val registerBtn = findViewById<Button>(R.id.button);

        // инициализация события нажатия на кнопку регистрации
        registerBtn.setOnClickListener {

            // перевод в строковые переменные
            val f = first.text.toString().trim();
            val l = last.text.toString().trim();
            val e = email.text.toString().trim();
            val p = password.text.toString().trim();

            // если не все заполнено
            if(TextUtils.isEmpty(f) || TextUtils.isEmpty(l) ||
               TextUtils.isEmpty(e) || TextUtils.isEmpty(p)) {

                makeText(this,
                    "Enter all details",
                    LENGTH_SHORT
                ).show();
            }
            // если пароль из меньше 6 символов
            else if (p.length < 6) {
                makeText(this,
                    "Password must be at least 6 characters",
                    LENGTH_SHORT
                ).show();
            }
            // если валидация прошла успешно
            else {
                mProgressBar.setMessage("Registering User...")
                mProgressBar.show();

                // метод регистрации от FirebaseAuth
                mAuth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(this) { task ->
                        mProgressBar.hide();

                        // регистрация прошла успешно?
                        if (!(task.isSuccessful)) {
                            Log.w("RegisterActivity",
                                "registerUser:failure",
                                task.exception);

                            makeText(this,
                                "Register is failed",
                                LENGTH_SHORT
                            ).show();
                        }
                        else {
                            Log.d("RegisterActivity", "registerUser:success");

                            // генерация ключа для пользователя
                            val userId = mAuth.currentUser!!.uid;

                            // отправка сообщения на почту о верификации
                            verifyEmail();

                            // загрузка пользователя в базу
                            UserRepository.addItem(User(userId, f, l, e));

                            // переход на MainActivity (компонент главной страницы)
                            updateUserInfoAndUI();
                        }
                    }
            }
        }

        // инициализация кнопки на переход на страницу авторизации
        val log = findViewById<Button>(R.id.login);

        log.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent);
            finish();
        }
    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;

        // метод отправки сообщения на потчу от FirebaseAuth
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (!(task.isSuccessful)) {
                    Log.e("RegisterActivity",
                        "sendEmailVerification",
                        task.exception
                    );

                    makeText(this,
                        "Failed to send verification email",
                        LENGTH_SHORT
                    ).show();
                }
                else {
                    makeText(this,
                        "Verification email sent to ${mUser.email}",
                        LENGTH_SHORT
                    ).show();
                }
            }
    }

    // переход на MainActivity (компонент главной страницы)
    private fun updateUserInfoAndUI() {
        // объект класса для передачи данных в другие компоненты
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // очистка стека, чтобы нельзя было свайпами экрана вернуться на этот компонент
        finish();
    }
}