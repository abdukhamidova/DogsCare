package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dogscare.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class ActivitySignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_sign_in)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonLogIn.setOnClickListener {
            val email = binding.textEmailSignIn.text.toString()
            val pass = binding.textPasswordSignIn.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, ActivityMain::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Nie udało się zalogować", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Uzupełnij wszystkie pola.", Toast.LENGTH_SHORT).show()

            }
        }
        binding.textNotSignUp.setOnClickListener {
            val intent = Intent(this, ActivitySignUp::class.java)
            startActivity(intent)
        }
    }
        override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser!=null){
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
            finish()
        }
    }
}