package com.example.dogscare

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityDogFormBinding

class FormDogActivity :AppCompatActivity() {
    private lateinit var binding: ActivityDogFormBinding
    private lateinit var header: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDogFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationViewForm)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        //endregion

        replaceFragment(FormInfoFragment())
        binding.bottomNavigationViewForm.setOnItemSelectedListener {
            when(it.itemId){
                R.id.formDog -> replaceFragment(FormInfoFragment())
                R.id.medicalBook -> replaceFragment(FormMedicalFragment())

                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutFragmentsForm,fragment)
        fragmentTransaction.commit()
    }

    fun setToolbarTitle(title: String){
        header.text = title
    }
}