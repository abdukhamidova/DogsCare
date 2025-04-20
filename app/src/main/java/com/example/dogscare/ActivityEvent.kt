package com.example.dogscare

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dogscare.databinding.ActivityEventBinding


class ActivityEvent : AppCompatActivity(){
    private lateinit var binding:ActivityEventBinding
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region TOOLBAR
        header = findViewById(R.id.toolbarHeader)
        val buttonDelete = findViewById<ImageButton>(R.id.imageButtonArchive)
        buttonDelete.setImageResource(R.drawable.icon_delete)
        //endregion

        val eventId = intent.getStringExtra("fireId")
        val dogId = intent.getStringExtra("fireDogId")

        //region Replace Fragment openCommand
        val openFragment = intent.getStringExtra("openCommand")
        when(openFragment){
            "ShelterEvent" -> {
                val openEventFragment = EventShelterFragment()
                val bundle = Bundle()
                bundle.putString("fireId", eventId)
                openEventFragment.arguments = bundle
                replaceFragment(openEventFragment)
            }
            "DogEvent" -> {
                //sprawdzenie czy archiwum
                val isArchive = intent.getBooleanExtra("isArchive", false)
                val openEventFragment = EventDogFragment()
                val bundle = Bundle()
                bundle.putString("fireEventId", eventId)
                bundle.putString("fireDogId", dogId)
                bundle.putBoolean("isArchive", isArchive)
                openEventFragment.arguments = bundle
                replaceFragment(openEventFragment)
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