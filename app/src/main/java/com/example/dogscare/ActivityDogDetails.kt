package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityDogDetailsBinding

class ActivityDogDetails :AppCompatActivity() {
    private lateinit var binding: ActivityDogDetailsBinding
    private lateinit var header: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDogDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationViewForm)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        val buttonArchive = findViewById<ImageButton>(R.id.imageButtonArchive)
        //endregion

        val dogId = intent.getStringExtra("fireId")
        //region fragment replace ARCHIVE
        buttonArchive.setOnClickListener{
            ArchiveDialog(this) { archiveType ->
                val intent = Intent(this, ActivityAdd::class.java)
                intent.putExtra("archiveType", archiveType.name)
                intent.putExtra("fireId", dogId)
                startActivity(intent)
                finish()
            }.show()
        }
        //endregion

        //region fragment replace openCommand
        val openFragment = intent.getStringExtra("openCommand")

        when(openFragment){
            "DogInfo" -> {
                val formInfoFragment = FormBasicFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formInfoFragment.arguments = bundle
                replaceFragment(formInfoFragment)
            }
            "AdoptDog" -> {
                val formAdoptFragment = FormAdoptFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formAdoptFragment.arguments = bundle
                replaceFragment(formAdoptFragment)
            }
            else -> {}
        }
        //endregion

        //region fragment replace BOTTOM NAV
        binding.bottomNavigationViewForm.setOnItemSelectedListener {
            when(it.itemId){
                R.id.formDog -> {
                    val formInfoFragment = FormBasicFragment()
                    val bundle = Bundle()
                    bundle.putString("fireId", dogId)
                    formInfoFragment.arguments = bundle
                    replaceFragment(formInfoFragment)
                }
                R.id.medicalBook -> {
                    val formMedicalFragment = FormMedicalFragment()
                    val bundle = Bundle()
                    bundle.putString("fireId", dogId)
                    formMedicalFragment.arguments = bundle
                    replaceFragment(formMedicalFragment)
                }
                else -> {}
            }
            true
        }
        //endregion
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