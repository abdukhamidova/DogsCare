package com.example.dogscare

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityAddBinding
import com.google.firebase.auth.FirebaseAuth

class ActivityAdd : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        val buttonArchive = findViewById<ImageButton>(R.id.imageButtonArchive)
        buttonArchive.visibility = View.GONE
        //endregion

        val dogId = intent.getStringExtra("fireId")
        //region Replace Fragment openCommand
        val openFragment = intent.getStringExtra("openCommand")
        when(openFragment){
            "AddDog" -> replaceFragment(AddDogFragment())
            /*"AdoptDog" -> {
                val formAdoptFragment = FormAdoptFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formAdoptFragment.arguments = bundle
                replaceFragment(formAdoptFragment)
            }*/
            else -> {}
        }
        //endregion

        //region Replace Fragment openArchive
        val archiveTypeString = intent.getStringExtra("archiveType")
        val archiveType = archiveTypeString?.let { ArchiveType.valueOf(it) }
        when (archiveType) {
            ArchiveType.ADOPTION -> {
                val formAdoptFragment = FormAdoptFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formAdoptFragment.arguments = bundle
                replaceFragment(formAdoptFragment)}
            ArchiveType.DEATH -> {
                val formConfirmDeathFragment = FormConfirmDeathFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formConfirmDeathFragment.arguments = bundle
                replaceFragment(formConfirmDeathFragment)
            }
            ArchiveType.TRANSFER -> {
                val formTransferFragment = FormTransferFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                formTransferFragment.arguments = bundle
                replaceFragment(formTransferFragment)
            }
            ArchiveType.OTHER -> {
                val FormOtherFragment = FormOtherFragment()
                val bundle = Bundle()
                bundle.putString("fireId", dogId)
                FormOtherFragment.arguments = bundle
                replaceFragment(FormOtherFragment)
            }
            else -> {}
        }
        //endregion
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