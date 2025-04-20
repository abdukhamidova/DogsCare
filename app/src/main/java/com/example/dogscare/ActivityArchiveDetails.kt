package com.example.dogscare

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityArchiveDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityArchiveDetails : AppCompatActivity(){
    private lateinit var binding: ActivityArchiveDetailsBinding
    val database = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArchiveDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationViewForm)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        header.text = "Dokumentacja"
        val buttonArchive = findViewById<ImageButton>(R.id.imageButtonArchive)
        val buttonSave = findViewById<ImageButton>(R.id.imageButtonSave)
        buttonArchive.visibility = View.GONE
        buttonSave.visibility = View.GONE
        //endregion

        val dogId = intent.getStringExtra("fireId")

        //region deafult fragment
        val formFragment = FormBasicFragment()
        val bun = Bundle()
        bun.putString("fireId", dogId)
        formFragment.arguments = bun
        replaceFragment(formFragment)
        //endregion

        //region fragment replace BOTTOM NAV
        binding.bottomNavigationViewForm.setOnItemSelectedListener {
            when(it.itemId){
                R.id.formDog -> {
                    buttonSave.visibility = View.GONE
                    val formInfoFragment = FormBasicFragment()
                    val bundle = Bundle()
                    bundle.putString("fireId", dogId)
                    formInfoFragment.arguments = bundle
                    replaceFragment(formInfoFragment)
                }
                R.id.medicalBook -> {
                    buttonSave.visibility = View.GONE
                    val formMedicalFragment = FormMedicalFragment()
                    val bundle = Bundle()
                    bundle.putString("fireId", dogId)
                    formMedicalFragment.arguments = bundle
                    replaceFragment(formMedicalFragment)
                }
                R.id.archiveInfo -> {
                    if (dogId != null) {
                        fetchArchiveType(dogId){ archiveType ->
                            val formArchive = when (archiveType) {
                                "ADOPTION" -> FormAdoptFragment()
                                "DEATH" -> FormConfirmDeathFragment()
                                "TRANSFER" -> FormTransferFragment()
                                "OTHER" -> FormOtherFragment()
                                else -> null
                            }
                            if(formArchive != null){
                                val bundle = Bundle()
                                bundle.putString("fireId", dogId)
                                formArchive.arguments = bundle
                                replaceFragment(formArchive)
                            }else Toast.makeText(this, "Błąd", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                R.id.dogCalendar -> {
                    buttonSave.visibility = View.GONE
                    val formCalFragment = CalendarDogFragment()
                    val bundle = Bundle()
                    bundle.putString("fireId", dogId)
                    formCalFragment.arguments = bundle
                    replaceFragment(formCalFragment)
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

    private fun fetchArchiveType(fireId: String, onResult: (String) -> Unit) {
        if (user == null) {
            Toast.makeText(this, "Błąd: użytkownik niezalogowany!", Toast.LENGTH_SHORT).show()
            onResult("Imię zwierzęcia")
            return
        }
        val userId = user.uid

        database.collection("users").document(userId)
            .collection("dogs").document(fireId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val archiveType = document.getString("archive") ?: "ARCHIVED"
                    onResult(archiveType)
                } else {
                    Toast.makeText(this, "Dokument nie istnieje", Toast.LENGTH_SHORT).show()
                    onResult("ARCHIVED")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd pobierania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult("ARCHIVED")
            }
    }
}