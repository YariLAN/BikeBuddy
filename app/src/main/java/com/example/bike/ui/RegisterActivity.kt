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

    private lateinit var first: EditText;
    private lateinit var last: EditText;
    private lateinit var email: EditText;
    private lateinit var password: EditText;

    private lateinit var mProgressBar: ProgressDialog;

    private lateinit var mAuth: FirebaseAuth;

    private fun init() {
        first = findViewById(R.id.firstName);
        last = findViewById(R.id.lastName);

        email = findViewById(R.id.email_register);
        password = findViewById(R.id.password);

        mProgressBar = ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        init();

        val registerBtn = findViewById<Button>(R.id.button);

        registerBtn.setOnClickListener {

            val f = first.text.toString().trim();
            val l = last.text.toString().trim();
            val e = email.text.toString().trim();
            val p = password.text.toString().trim();

            if(TextUtils.isEmpty(f) || TextUtils.isEmpty(l) ||
               TextUtils.isEmpty(e) || TextUtils.isEmpty(p)) {

                makeText(this,
                    "Enter all details",
                    LENGTH_SHORT
                ).show();
            }
            else if (p.length < 6) {
                makeText(this,
                    "Password must be at least 6 characters",
                    LENGTH_SHORT
                ).show();
            }
            else {
                mProgressBar.setMessage("Registering User...")
                mProgressBar.show();

                mAuth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(this) { task ->
                        mProgressBar.hide();

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

                            val userId = mAuth.currentUser!!.uid;

                            verifyEmail();

                            UserRepository.addItem(User(userId, f, l, e));

                            updateUserInfoAndUI();
                        }
                    }
            }
        }

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

    private fun updateUserInfoAndUI() {
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}