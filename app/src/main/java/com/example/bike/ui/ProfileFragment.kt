package com.example.bike.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.bike.R
import com.example.bike.databinding.FragmentProfileBinding
import com.example.bike.datasources.Route
import com.example.bike.repository.RouteRepository
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.jetbrains.annotations.NotNull
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
class ProfileFragment : Fragment() {

    private lateinit var profileFragmentBinding: FragmentProfileBinding
    private lateinit var filePath: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileFragmentBinding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserInfo()

        profileFragmentBinding.imageProfile.setOnClickListener {
            selectImage()
        }

        profileFragmentBinding.btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            signOutClick()
        }

        setListView()

        return profileFragmentBinding.root
    }

    private fun signOutClick() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    // Задать список из пройденных маршрутов
    private fun setListView() {
        val arrayList : ArrayList<Route> = arrayListOf()

        Firebase.database.reference.child("routes").orderByChild("userId").equalTo(
            FirebaseAuth.getInstance().currentUser!!.uid).keepSynced(true)

        Firebase.database.reference.child("routes").orderByChild("userId").equalTo(
            FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.children!!

                    result.forEach { it ->
                        val res = it.children!!
                        val arrayOfData = arrayListOf<String>()

                        res.forEach { date ->
                            arrayOfData.add(date.value.toString())
                        }

                        val route = Route(
                            arrayOfData[0],
                            arrayOfData[1],
                            arrayOfData[2],
                            arrayOfData[3],
                            arrayOfData[4],
                            arrayOfData[5],
                            arrayOfData[6],
                            arrayOfData[7],
                            arrayOfData[8],
                        )

                        arrayList.add(route)
                    }

                    val adapter = RouteAdapter(requireContext(), arrayList)
                    profileFragmentBinding.listHistoryItems.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    private fun loadUserInfo() {
        val db = Firebase.database

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName: String = snapshot.child("firstName").value.toString()
                val secondName: String = snapshot.child("secondName").value.toString()
                val email: String = snapshot.child("email").value.toString()
                val profileImage: String = snapshot.child("profileImage").value.toString()

                profileFragmentBinding.usernameTv.text = userName
                profileFragmentBinding.textView5.text = secondName
                profileFragmentBinding.textView4.text = email

                if (profileImage.isNotEmpty()) {
                    context?.let { Glide.with(it).load(profileImage).into(profileFragmentBinding.imageProfile) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        val mAuth = FirebaseAuth.getInstance();

        val mUser = mAuth.currentUser;

        db.getReference("users").child(mUser!!.uid).addListenerForSingleValueEvent(listener)
    }

    private var pickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.data != null) {
            val date: Intent? = result.data

            filePath = result.data!!.data!!

            try {
                val bitmap: Bitmap = MediaStore.Images.Media
                    .getBitmap(
                        requireContext().contentResolver,
                        filePath
                    );

                profileFragmentBinding.imageProfile.setImageBitmap(bitmap)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }

            uploadImage()
        }
    }

    private fun selectImage() {
        var intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        pickImageActivityResultLauncher.launch(intent)
    }

    private fun uploadImage() {
        if (filePath != null) {
            val uid: String = FirebaseAuth.getInstance().currentUser!!.uid

            FirebaseStorage.getInstance().reference.child("images/${uid}")
                .putFile(filePath)
                .addOnSuccessListener {
                    Toast.makeText(context, "Photo upload complete", Toast.LENGTH_SHORT).show();

                    FirebaseStorage.getInstance().reference.child("images/${uid}").downloadUrl
                        .addOnSuccessListener {
                            Firebase.database.getReference("users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("profileImage").setValue(filePath.toString())
                        }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}