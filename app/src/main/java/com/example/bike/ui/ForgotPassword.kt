package com.example.bike.ui

import android.annotation.SuppressLint
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

    private var email: EditText? = null;

    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password);

        initEmail();

        val btn = findViewById<Button>(R.id.send_reset_password);

        btn.setOnClickListener {
            val em = email?.text?.trim().toString();

            mAuth!!.sendPasswordResetEmail(em)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        Log.w("ForgotPasswordActivity", task.exception!!.message.toString())
                        makeText(this, "No user found with this email.", LENGTH_SHORT).show()
                    }
                    else {
                        Log.d("ForgotPaswwordActivity", "Email sent");
                        makeText(this, "Email sent", LENGTH_SHORT).show();

                        updateUI();
                    }
                }
        }
    }

    private fun initEmail() {
        email = findViewById(R.id.email_forgot);

        mAuth = FirebaseAuth.getInstance();
    }

    private fun updateUI() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}