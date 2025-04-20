package com.example.dogscare

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth



class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationView)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.email.toString()

        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        val buttonSave = findViewById<ImageButton>(R.id.imageButtonSave)
        val buttonArchive = findViewById<ImageButton>(R.id.imageButtonArchive)
        buttonSave.visibility = View.GONE
        buttonArchive.visibility = View.GONE
        //endregion


        replaceFragment(ListDogFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.listOfDogs -> {
                    replaceFragment(ListDogFragment())
                    buttonSave.visibility = View.GONE
                }
                R.id.mainCalendar -> {
                    replaceFragment(CalendarShelterFragment())
                    buttonSave.visibility = View.VISIBLE
                    buttonSave.setImageResource(R.drawable.icon_add_30)
                }
                R.id.dogsArchive -> {
                    replaceFragment(ListArchiveFragment())
                    buttonSave.visibility = View.GONE
                }

                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutFragments,fragment)
        fragmentTransaction.commit()
    }
    fun setToolbarTitle(title: String){
        header.text = title
    }
}