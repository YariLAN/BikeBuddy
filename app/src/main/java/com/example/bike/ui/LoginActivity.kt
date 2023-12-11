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

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText;
    private lateinit var pass: EditText

    private lateinit var progressBar: ProgressDialog;

    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init();

        findViewById<Button>(R.id.button).setOnClickListener {
            val e = email.text.toString().trim();
            val p = pass.text.toString().trim();

            if (TextUtils.isEmpty(e) || TextUtils.isEmpty(p)) {
                makeText(this, "Enter the all data", LENGTH_SHORT).show();
            }
            else {
                progressBar.setTitle("Authentication...");
                progressBar.show();

                auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(this) {task ->

                        progressBar.hide();

                        if (task.isSuccessful) {
                            Log.d("LoginActivity", "loginUser:success");

                            openAppForUser();
                        }
                        else {
                            Log.e("LoginActivity", "signInWithEmail:failure", task.exception);

                            makeText(this, "Authentication is failed.", LENGTH_SHORT).show()
                        }
                    }
            }
        }

        findViewById<Button>(R.id.register).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent);
        }

        findViewById<Button>(R.id.resetPassword).setOnClickListener {
            val intent =  Intent(this, ForgotPassword::class.java);
            startActivity(intent);
        }
    }

    fun init() {
        email = findViewById(R.id.email_login);
        pass = findViewById(R.id.password);

        auth = FirebaseAuth.getInstance();

        progressBar = ProgressDialog(this);
    }

    private fun openAppForUser() {
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}