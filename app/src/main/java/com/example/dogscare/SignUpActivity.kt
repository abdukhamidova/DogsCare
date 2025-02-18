package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dogscare.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity: AppCompatActivity(){
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_sign_in)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSignUp.setOnClickListener{
            val email = binding.textEmailSignUp.text.toString()
            val pass = binding.textPasswordSignUp.text.toString()
            val confirmPass = binding.textRepeatPasswordSignUp.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
                if(pass.equals(confirmPass)){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this, "Nie udało się założyć konta", Toast.LENGTH_SHORT).show()
                        }
                    }

                }else{
                    Toast.makeText(this, "Różne hasła.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Uzupełnij wszystkie pola.", Toast.LENGTH_SHORT).show()

            }
        }

        binding.textSignedUp.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }
}