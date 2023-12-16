package com.example.bike.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.example.bike.R
import com.example.bike.databinding.ActivityMainBinding
import com.example.bike.databinding.FragmentProfileBinding

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.HashMap
import android.app.Fragment as Fragment1

class MainActivity : AppCompatActivity() {

    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate((layoutInflater))

        setContentView(binding.root)

        // настраиваем стартовый фрагмент при загрузке MainActivity
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentProfile.id, ProfileFragment())
            .commit()

        binding.bottomNav.selectedItemId = R.id.profile

        // Настройка подключений (соотносим ключи к фрагментам программы)
        val fragmentMap: Map<Int, Fragment> = HashMap<Int, Fragment>().also {
            it.put(R.id.profile, ProfileFragment())
            it.put(R.id.chats, ChatFragment())
            it.put(R.id.gps, LocationFragment())
            it.put(R.id.search, SearchMeetingFragment())
            it.put(R.id.bicycle_fr, MeetingFragment())
        }

        // Настраиваем навигационное меню в низу программы
        binding.bottomNav.setOnItemSelectedListener {
            val fragment = fragmentMap[it.itemId]

            supportFragmentManager
                .beginTransaction()
                .replace(binding.fragmentProfile.id, fragment!!, "${fragment.context}")
                .commit()

            return@setOnItemSelectedListener true
        };

        init();
    }

    fun init() {
        mDatabase = Firebase.database;
        mAuth = FirebaseAuth.getInstance();

        val mUser = mAuth!!.currentUser;
        val databaseReference = mDatabase!!.getReference("users");
        val mUserReference = databaseReference.child(mUser!!.uid);

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity",
                    error.message);
            }
        }
        mUserReference.addValueEventListener(listener);
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }
}
