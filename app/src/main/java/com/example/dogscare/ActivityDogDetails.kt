package com.example.dogscare

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        val buttonSave = findViewById<ImageButton>(R.id.imageButtonSave)
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

        //region deafult fragment
        val formInfoFragment = FormBasicFragment()
        val bundle = Bundle()
        bundle.putString("fireId", dogId)
        formInfoFragment.arguments = bundle
        replaceFragment(formInfoFragment)
        //endregion

        //region fragment replace BOTTOM NAV
        binding.bottomNavigationViewForm.setOnItemSelectedListener {
            when(it.itemId){
                R.id.formDog -> {
                    buttonSave.setImageResource(R.drawable.icon_save_35_2)
                    buttonArchive.visibility = View.VISIBLE
                    val formFragment = FormBasicFragment()
                    val bun = Bundle()
                    bun.putString("fireId", dogId)
                    formFragment.arguments = bun
                    replaceFragment(formFragment)
                }
                R.id.medicalBook -> {
                    buttonSave.setImageResource(R.drawable.icon_save_35_2)
                    buttonArchive.visibility = View.GONE
                    val formFragment = FormMedicalFragment()
                    val bun = Bundle()
                    bun.putString("fireId", dogId)
                    formFragment.arguments = bun
                    replaceFragment(formFragment)
                }
                R.id.dogCalendar -> {
                    buttonSave.setImageResource(R.drawable.icon_add_30)
                    buttonArchive.visibility = View.GONE
                    val formFragment = CalendarDogFragment()
                    val bun = Bundle()
                    bun.putString("fireId", dogId)
                    formFragment.arguments = bun
                    replaceFragment(formFragment)
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