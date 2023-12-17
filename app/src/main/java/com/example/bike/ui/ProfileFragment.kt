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

    // использование биндинга
    private lateinit var profileFragmentBinding: FragmentProfileBinding

    // путь к файлу картинки
    private lateinit var filePath: Uri

    // создания фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileFragmentBinding = FragmentProfileBinding.inflate(inflater, container, false)

        // метод заполнения данными пользователя из базы данных
        loadUserInfo()

        // событие - установка изображения
        profileFragmentBinding.imageProfile.setOnClickListener {
            selectImage()
        }

        // событие - выход из аккаунта
        profileFragmentBinding.btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            signOutClick()
        }

        // установить историю активностей прошедших поездок
        setListView()

        return profileFragmentBinding.root
    }

    // выход из аккаунта
    private fun signOutClick() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    // Задать список из пройденных маршрутов
    private fun setListView() {
        val arrayList : ArrayList<Route> = arrayListOf()

        // установка асинхронности потока
        Firebase.database.reference.child("routes").orderByChild("userId").equalTo(
            FirebaseAuth.getInstance().currentUser!!.uid).keepSynced(true)

        // выгрузка данных о маршрутах текущего пользователя
        Firebase.database.reference.child("routes").orderByChild("userId").equalTo(
            FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.children!!

                    // перебор данных
                    // перевод из HashMap в класс Route
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

                    // инициализация адаптера
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

    // загрузка персональных данных о пользователе
    private fun loadUserInfo() {
        val db = Firebase.database

        // инициализация прослушивателя ValueEventListener
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

        // получени пользователя FirebaseUser
        val mUser = mAuth.currentUser;

        db.getReference("users").child(mUser!!.uid).addListenerForSingleValueEvent(listener)
    }

    // метод выбора фотографии со стороннего приложения установка его пути
    private var pickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.data != null) {
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

    // выбор фотографии
    private fun selectImage() {
        var intent: Intent = Intent()

        // установка формата данных, которые принимает intent
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        pickImageActivityResultLauncher.launch(intent)
    }

    // обновление фотографии
    private fun uploadImage() {
        if (filePath != null) {
            // получение ключа пользователя
            val uid: String = FirebaseAuth.getInstance().currentUser!!.uid

            // сохранение файла для пользователя с uid в FirebaseStorage
            FirebaseStorage.getInstance().reference.child("images/${uid}")
                .putFile(filePath)
                // если событие прошло успешно
                .addOnSuccessListener {
                    // отображение сообщения на экране
                    Toast.makeText(context, "Photo upload complete", Toast.LENGTH_SHORT).show();

                    // установка данного пути к файлу в поле profileImage в базе данных текущему пользователю
                    FirebaseStorage.getInstance().reference.child("images/${uid}").downloadUrl
                        .addOnSuccessListener {
                            Firebase.database.getReference("users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("profileImage").setValue(filePath.toString())
                        }
                }
        }
    }

    // метод удаления фрагмента, вслучае выхода из него
    override fun onDestroy() {
        super.onDestroy()
    }
}