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

    //Firebase references
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    //UI elements
    private var firstName: TextView? = null
    private var lastName: TextView? = null
    private var email: TextView? = null

    private lateinit var btnMeet: ImageButton;
    private lateinit var btnSearch: ImageButton;
    private lateinit var btnStart: ImageButton;
    private lateinit var btnChat: ImageButton;
    private lateinit var btnProfile: ImageButton;

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate((layoutInflater))

        setContentView(binding.root)


        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentProfile.id, ProfileFragment())
            .commit()

        binding.bottomNav.selectedItemId = R.id.profile

        val fragmentMap: Map<Int, Fragment> = HashMap<Int, Fragment>().also {
            it.put(R.id.profile, ProfileFragment())
            it.put(R.id.chats, ChatFragment())
            it.put(R.id.gps, LocationFragment())
        }

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
            override fun onDataChange(snapshot: DataSnapshot) {
//                firstName!!.text = snapshot.child("firstName").value as String;
//                lastName!!.text = snapshot.child("secondName").value as String;
//
//                mUser.email.also { email!!.text = it };
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity",
                    error.message);
            }
        }
        mUserReference.addValueEventListener(listener);
    }

    fun initButton() {
//        btnSearch = findViewById(R.id.search);
//        btnMeet = findViewById(R.id.meet);
//        btnStart = findViewById(R.id.start);
//        btnChat = findViewById(R.id.chat);
//          btnProfile = findViewById(R.id.bottom_nav);
//
//        btnMeet.setOnClickListener { selectColor(it, "#FFFFE500"); }
//        btnSearch.setOnClickListener { selectColor(it, "#FFFFE500"); }
//        btnStart.setOnClickListener { selectColor(it, "#FFFFE500"); }
//        btnChat.setOnClickListener { selectColor(it, "#FFFFE500"); }

//        btnProfile.setOnClickListener {
//            selectColor(it, "#FFFFE500");
//
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_profile, ProfileFragment.newInstance())
//                .commit();
//
//        }
    }

//    private fun selectColor(btn: View?, color: String) {
//        clearBackgroundColorButton();
//
//        btn!!.setBackgroundColor( Color.parseColor(color));
//    }
//
//    private fun clearBackgroundColorButton() {
//        btnMeet.setBackgroundColor(Color.parseColor("#003B1D49"))
//        btnSearch.setBackgroundColor(Color.parseColor("#003B1D49"))
//        btnStart.setBackgroundColor(Color.parseColor("#003B1D49"))
//        btnChat.setBackgroundColor(Color.parseColor("#003B1D49"))
//        btnProfile.setBackgroundColor(Color.parseColor("#003B1D49"))
//    }

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
